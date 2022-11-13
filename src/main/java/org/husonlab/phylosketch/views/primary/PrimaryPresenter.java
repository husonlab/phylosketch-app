/*
 * PrimaryPresenter.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.views.primary;

import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.util.Duration;
import jloda.fx.util.RunAfterAWhile;
import jloda.phylo.algorithms.RootedNetworkProperties;
import org.husonlab.phylosketch.Main;
import org.husonlab.phylosketch.network.Document;

public class PrimaryPresenter {

	private final ObjectProperty<InteractionMode> interactionMode = new SimpleObjectProperty<>(this, "tool", InteractionMode.Pan);

	public PrimaryPresenter(Document document, PrimaryView view, PrimaryController controller) {
		var scrollPaneTouchPressedHandler = controller.getScrollPane().getOnTouchPressed();

		interactionMode.addListener((v, o, n) -> {
			var allowPanning = interactionMode.get() == InteractionMode.Pan || interactionMode.get() == InteractionMode.EditLabels;

			if (allowPanning) {
				controller.getScrollPane().setOnTouchPressed(scrollPaneTouchPressedHandler);
				controller.getScrollPane().setPannable(true);
			} else {
				controller.getScrollPane().setOnTouchPressed(Event::consume);
				controller.getScrollPane().setPannable(false);
			}

			controller.getModeLabel().setText(n == null ? "" : n.getDescription());
			controller.getModeLabel().setOpacity(1.0);
			var fadeTransition = new FadeTransition(Duration.seconds(1), controller.getModeLabel());
			fadeTransition.setToValue(0.0);
			var transition = new SequentialTransition(new PauseTransition(Duration.seconds(5)), fadeTransition);
			transition.play();
		});

		controller.getModeLabel().setText("");

		controller.getUndoButton().setOnAction(e -> document.getUndoManager().undo());
		controller.getUndoButton().disableProperty().bind(document.getUndoManager().undoableProperty().not());
		controller.getRedoButton().setOnAction(e -> document.getUndoManager().redo());
		controller.getRedoButton().disableProperty().bind(document.getUndoManager().redoableProperty().not());

		if (com.gluonhq.attach.util.Platform.isDesktop()) {
			controller.getStackPane().setOnScroll(e -> {
				var factor = (e.getDeltaY() > 0 ? 1.1 : 1 / 1.1);
				var box = view.getDocument().getNetworkView().getBoundingBox();
				if (!(factor < 1 && Math.min(box.getWidth(), box.getHeight()) < 200 || factor > 1 && Math.max(box.getWidth(), box.getHeight()) > 2000)) {
					view.getDocument().getNetworkView().scale(factor, factor);
				}
				e.consume();
			});
		}
		if (true) {
			controller.getStackPane().setOnZoom(e -> {
				var factor = e.getZoomFactor();
				var box = view.getDocument().getNetworkView().getBoundingBox();
				if (!(factor < 1 && Math.min(box.getWidth(), box.getHeight()) < 200 || factor > 1 && Math.max(box.getWidth(), box.getHeight()) > 2000)) {
					view.getDocument().getNetworkView().scale(factor, factor);
				}
				e.consume();
			});
		}

		controller.getResetButton().setOnAction(e -> view.getDocument().getNetworkView().resetScale());

		if (Main.isDesktop()) {
			controller.getScrollPane().setOnMouseReleased(c -> {
				if (c.isStillSincePress() && !c.isShiftDown()) {
					view.getDocument().getNodeSelection().clearSelection();
					view.getDocument().getEdgeSelection().clearSelection();
				}
			});
		} else {
			controller.getScrollPane().setOnTouchReleased(c -> {
				view.getDocument().getNodeSelection().clearSelection();
				view.getDocument().getEdgeSelection().clearSelection();
			});
		}

		controller.getShowNewickButton().setOnAction(a -> {
			var newick = document.getModel().getTree().toBracketString(false);
			controller.getNewickTextField().setText(newick + ";");
		});

		InvalidationListener invalidationListener = c -> {
			RunAfterAWhile.applyInFXThread(this,
					() -> {
						controller.getNewickTextField().clear();
						var tree = document.getModel().getTree();
						String heading;
						if (tree.getNumberOfNodes() > 0 && tree.isConnected()) {
							if (tree.hasReticulateEdges())
								heading = "Network: ";
							else
								heading = "Tree: ";
						} else
							heading = "";
						controller.getPropertiesTextField().setText(heading + RootedNetworkProperties.computeInfoString(tree).replace(", network", ""));
					});
		};
		document.getGraphFX().getNodeList().addListener(invalidationListener);
		document.getGraphFX().getEdgeList().addListener(invalidationListener);


		controller.getToggles().selectedToggleProperty().addListener((v, o, n) -> {
			Node graphic = null;
			if (n instanceof MenuItem menuItem) {
				if (menuItem == controller.getPanMenuItem()) {
					interactionMode.set(InteractionMode.Pan);
					graphic = MaterialDesignIcon.PAN_TOOL.graphic();
				} else if (menuItem == controller.getEditMenuItem()) {
					interactionMode.set(InteractionMode.CreateNewEdges);
					graphic = MaterialDesignIcon.EDIT.graphic();
				}
				if (menuItem == controller.getMoveMenuItem()) {
					interactionMode.set(InteractionMode.Move);
					graphic = MaterialDesignIcon.SWAP_VERT.graphic();
				}
				if (menuItem == controller.getEraseMenuItem()) {
					interactionMode.set(InteractionMode.Erase);
					graphic = MaterialDesignIcon.REMOVE_CIRCLE.graphic();
				}
				if (menuItem == controller.getEditLabelMenuItem()) {
					interactionMode.set(InteractionMode.EditLabels);
					graphic = MaterialDesignIcon.LABEL_OUTLINE.graphic();
				}
			}
			if (graphic == null)
				graphic = MaterialDesignIcon.HELP_OUTLINE.graphic();
			;
			graphic.setStyle("-fx-text-fill: white;");
			controller.getMenuButton().setGraphic(graphic);
		});
		controller.getPanMenuItem().setSelected(true);
	}

	public ObjectProperty<InteractionMode> interactionModeProperty() {
		return interactionMode;
	}
}
