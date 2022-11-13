/*
 * TouchLabelEditing.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.husonlab.phylosketch.deprecated;

import com.gluonhq.charm.glisten.control.TextField;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TouchEvent;
import jloda.fx.control.RichTextLabel;
import jloda.fx.selection.SelectionModel;
import jloda.fx.undo.UndoManager;
import jloda.fx.util.BasicFX;
import jloda.graph.Node;
import jloda.util.Single;
import org.husonlab.phylosketch.network.NetworkView;
import org.husonlab.phylosketch.network.commands.NodeLabelCommand;
import org.husonlab.phylosketch.network.interaction.LabelEditingManager;
import org.husonlab.phylosketch.views.primary.InteractionMode;

import static org.husonlab.phylosketch.network.interaction.LabelEditingManager.findNode;

@Deprecated
public class TouchLabelEditing {
	public static void apply(Event clickedEvent, ReadOnlyObjectProperty<InteractionMode> tool, NetworkView networkView, UndoManager undoManager,
							 SelectionModel<Node> nodeSelection, Node v, RichTextLabel textLabel) {
		var textField = new TextField();
		textField.setId("text-field");
		textField.setPromptText("New node label");
		textField.textProperty().bindBidirectional(textLabel.textProperty());

		var oldText = textLabel.getText();
		final var finish = new Single<Runnable>();

		final InvalidationListener listenerToRunFinish = a -> {
			finish.get().run();
		};

		finish.set(() -> {
			var newText = textLabel.getText();
			if (!newText.equals(oldText)) {
				undoManager.doAndAdd(new NodeLabelCommand(v, textLabel.textProperty(), oldText, newText));
			}
			networkView.getWorld().getChildren().remove(textField);
			tool.removeListener(listenerToRunFinish);
		});

		tool.addListener(listenerToRunFinish);

		undoManager.undoStackSizeProperty().addListener(new WeakInvalidationListener(listenerToRunFinish));

		// close all currently open label text fields
		Platform.runLater(() -> networkView.getWorld().getChildren().removeAll(BasicFX.findRecursively(networkView.getWorld(), a -> a instanceof TextField)));

		Platform.runLater(() -> networkView.getWorld().getChildren().add(textField));

		textField.setTranslateX(textLabel.getTranslateX());
		textField.setTranslateY(textLabel.getTranslateY());

		makeDraggableByTouch(textField, textField.translateXProperty(), textField.translateYProperty());

		textField.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
			System.err.println(e);
			if (e.getCode() == KeyCode.ENTER) {
				if (!e.isShiftDown())
					finish.get().run();
				else
					startEditingOtherLabel(networkView, nodeSelection, v, clickedEvent, findNode(networkView, v, true, LabelEditingManager.Direction.Down));
				e.consume();
			}
		});
		textField.focusedProperty().addListener((a, o, n) -> {
			if (!n)
				finish.get().run();
		});

		//Platform.runLater(textField::requestFocus);
	}

	private static void startEditingOtherLabel(NetworkView networkView, SelectionModel<Node> nodeSelection, Node v, Event event, Node u) {
		if (u != null) {
			Platform.runLater(() -> {
				var other = networkView.getView(u).label();
				nodeSelection.clearSelection();
				nodeSelection.select(u);
				other.fireEvent(event.copyFor(other, other));
				event.consume();
			});
		}
	}

	private static Point2D previous;
	private static boolean moved;
	private static int touchId = -1;


	private static void makeDraggableByTouch(javafx.scene.Node draggable, DoubleProperty x, DoubleProperty y) {

		draggable.addEventFilter(TouchEvent.TOUCH_PRESSED, a -> {
			if (a.getTouchCount() == 1 && touchId == -1) {
				previous = new Point2D(a.getTouchPoint().getScreenX(), a.getTouchPoint().getScreenY());
				touchId = a.getTouchPoint().getId();
			} else
				previous = null;
			moved = false;
		});
		draggable.addEventFilter(TouchEvent.TOUCH_MOVED, a -> {
			if (a.getTouchPoint().getId() == touchId) {
				var current = new Point2D(a.getTouchPoint().getScreenX(), a.getTouchPoint().getScreenY());
				x.set(x.get() + (current.getX() - previous.getX()));
				y.set(y.get() + (current.getY() - previous.getY()));
				previous = current;
				moved = true;
				a.consume();
			}
		});

		draggable.addEventFilter(TouchEvent.TOUCH_RELEASED, a -> {
			if (a.getTouchPoint().getId() == touchId) {
				if (moved)
					a.consume();
				touchId = -1;
			}
		});
	}
}
