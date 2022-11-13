/*
 * MouseLabelEditing.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.event.Event;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import jloda.fx.control.RichTextLabel;
import jloda.fx.selection.SelectionModel;
import jloda.fx.undo.UndoManager;
import jloda.fx.util.BasicFX;
import jloda.fx.util.DraggableUtils;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.Pair;
import jloda.util.Single;
import org.husonlab.phylosketch.network.NetworkView;
import org.husonlab.phylosketch.network.commands.NodeLabelCommand;
import org.husonlab.phylosketch.views.primary.InteractionMode;

import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Deprecated
public class MouseLabelEditing {

	public static void apply(Event clickedEvent, ReadOnlyObjectProperty<InteractionMode> tool, NetworkView networkView, UndoManager undoManager,
							 SelectionModel<Node> nodeSelection, Node v, RichTextLabel textLabel) {
		var textField = new TextField();
		textField.setId("text-field");
		textField.setPromptText("New node label");
		textField.textProperty().bindBidirectional(textLabel.textProperty());
		textField.focusedProperty().addListener((a, o, n) -> {
			if (n) {
				textField.positionCaret(textField.getText().length());
			}
		});

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

		DraggableUtils.setupDragMouseTranslate(textField);

		textField.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
			System.err.println(e);
			switch (e.getCode()) {
				case ENTER -> {
					if (!e.isShiftDown())
						finish.get().run();
					else
						startEditingOtherLabel(networkView, nodeSelection, v, clickedEvent, findNode(networkView, v, true, Direction.Down));
					e.consume();
				}
				case DOWN -> {
					startEditingOtherLabel(networkView, nodeSelection, v, clickedEvent, findNode(networkView, v, true, Direction.Down));
					e.consume();
				}
				case UP -> {
					startEditingOtherLabel(networkView, nodeSelection, v, clickedEvent, findNode(networkView, v, true, Direction.Up));
					e.consume();
				}
				case LEFT -> {
					if (e.isShiftDown()) {
						startEditingOtherLabel(networkView, nodeSelection, v, clickedEvent, findNode(networkView, v, true, Direction.Left));
						e.consume();
					}
				}
				case RIGHT -> {
					if (e.isShiftDown()) {
						startEditingOtherLabel(networkView, nodeSelection, v, clickedEvent, findNode(networkView, v, true, Direction.Right));
						e.consume();
					}
				}
			}
		});

		Platform.runLater(textField::requestFocus);
	}

	public enum Direction {Left, Right, Up, Down}

	public static Node findNode(NetworkView view, Node v, boolean strict, Direction direction) {
		var tree = (PhyloTree) v.getOwner();

		if (tree.getNumberOfNodes() > 1) {
			var vx = view.getView(v).shape().getTranslateX();
			var vy = view.getView(v).shape().getTranslateY();

			var set = tree.nodeStream()
					.map(u -> {
						var ux = view.getView(u).shape().getTranslateX();
						var uy = view.getView(u).shape().getTranslateY();
						var dx = Math.abs(ux - vx);
						var dy = Math.abs(uy - vy);
						return switch (direction) {
							case Up -> uy < vy && (!strict || dx < dy) ? new Pair<>(vy - uy, u) : null;
							case Down -> uy > vy && (!strict || dx < dy) ? new Pair<>(uy - vy, u) : null;
							case Left -> ux < vx && (!strict || dx > dy) ? new Pair<>(vx - ux, u) : null;
							case Right -> ux > vx && (!strict || dx > dy) ? new Pair<>(ux - vx, u) : null;
						};
					})
					.filter(Objects::nonNull)
					.collect(Collectors.toCollection(TreeSet::new));
			var result = set.size() > 0 ? set.first().getSecond() : null;
			if (result == null && strict) {
				result = findNode(view, v, false, direction);
			}
			return result;
		} else
			return null;
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
}
