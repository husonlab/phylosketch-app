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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.input.InputEvent;
import javafx.util.Duration;
import jloda.fx.selection.rubberband.RubberBandSelection;
import jloda.fx.selection.rubberband.RubberBandSelectionHandler;
import jloda.phylo.algorithms.RootedNetworkProperties;
import org.husonlab.phylosketch.Main;
import org.husonlab.phylosketch.network.Document;

public class PrimaryPresenter {
	public enum EdgeShape {Straight, Rectangular, Round}

	public enum ArrowType {ArrowNone, ArrowRight, ArrowLeft, ArrowBoth}

	private final ObjectProperty<InteractionMode> interactionMode = new SimpleObjectProperty<>(this, "interactionMode", InteractionMode.Pan);

	private final ObjectProperty<EdgeShape> edgeShape = new SimpleObjectProperty<>(this, "edgeShape");

	private final ObjectProperty<ArrowType> arrowType = new SimpleObjectProperty<>(this, "ArrowType");

	private final RubberBandSelection rubberBandSelection;

	public PrimaryPresenter(Document document, PrimaryView view, PrimaryController controller) {

		if (!Main.isDesktop()) {
			controller.getScrollPane().addEventFilter(InputEvent.ANY, a -> {
				if (interactionModeProperty().get() != InteractionMode.Pan) {
					var name = a.getEventType().getName();
					if ((name.startsWith("SCROLL")) && (document.getNodeSelection().size() > 0 || document.getEdgeSelection().size() > 0)) {
						a.consume();
					}
				}
			});
		}

		var selectionHandler = RubberBandSelectionHandler.create(document.getModel().getTree(), document.getNodeSelection(),
				document.getEdgeSelection(), a -> document.getNetworkView().getView(a).shape());
		rubberBandSelection = new RubberBandSelection(controller.getStackPane(), controller.getScrollPane(), document.getNetworkView().getWorld(), selectionHandler);

		interactionMode.addListener((v, o, n) -> {
			controller.getScrollPane().setPannable(n == InteractionMode.Pan);
			controller.getModeLabel().setText(n == null ? "" : n.getDescription());
			controller.getModeLabel().setOpacity(1.0);
			var fadeTransition = new FadeTransition(Duration.seconds(1), controller.getModeLabel());
			fadeTransition.setToValue(0.0);
			var transition = new SequentialTransition(new PauseTransition(Duration.seconds(5)), fadeTransition);
			transition.play();
			if (n != null) {
				switch (n) {
					case Pan -> controller.getScrollPane().setCursor(Cursor.OPEN_HAND);
					case EditLabels -> controller.getScrollPane().setCursor(Cursor.TEXT);
					case Move -> controller.getScrollPane().setCursor(Cursor.HAND);
					case CreateNewEdges -> controller.getScrollPane().setCursor(Cursor.CROSSHAIR);
					default -> controller.getScrollPane().setCursor(Cursor.DEFAULT);
				}
			}
		});

		controller.getModeLabel().setText("");

		edgeShape.addListener((v, o, n) -> {
			System.err.println("Using edge shape: " + n);
		});

		arrowType.addListener((v, o, n) -> {
			System.err.println("Using arrow type: " + n);
		});

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

		controller.getScrollPane().setOnMouseReleased(c -> {
			if (c.isStillSincePress() && !c.isShiftDown()) {
				view.getDocument().getNodeSelection().clearSelection();
				view.getDocument().getEdgeSelection().clearSelection();
			}
		});


		controller.getShowNewickToggleButton().selectedProperty().addListener((v, o, n) -> {
			if (n) {
				controller.getTextField().setText(document.getModel().getTree().toBracketString(false) + ";");
			} else {
				var tree = document.getModel().getTree();
				String heading;
				if (tree.getNumberOfNodes() > 0 && tree.isConnected()) {
					if (tree.hasReticulateEdges())
						heading = "Network: ";
					else
						heading = "Tree: ";
				} else
					heading = "";
				controller.getTextField().setText(heading + RootedNetworkProperties.computeInfoString(tree).replace(", network", ""));
			}
		});
		document.getGraphFX().lastUpdateProperty().addListener(a -> controller.getShowNewickToggleButton().setSelected(false));
		controller.getShowNewickToggleButton().setSelected(true);

		controller.getModeToggleGroup().selectedToggleProperty().addListener((v, o, n) -> {
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
			controller.getModeMenuButton().setGraphic(graphic);
		});
		controller.getPanMenuItem().setSelected(true);

		controller.getEdgeShapeToggleGroup().selectedToggleProperty().addListener((v, o, n) -> {
			if (n == controller.getRectangularEdgesRadioMenuItem())
				edgeShape.set(EdgeShape.Rectangular);
			else if (n == controller.getRoundEdgesRadioMenuItem())
				edgeShape.set(EdgeShape.Round);
			else
				edgeShape.set(EdgeShape.Straight);
		});
		controller.getEdgeShapeToggleGroup().selectToggle(controller.getStraightEdgesRadioMenuItem());

		controller.getArrowTypeToggleGroup().selectedToggleProperty().addListener((v, o, n) -> {
			if (n == controller.getArrowRightRadioMenuItem())
				arrowType.set(ArrowType.ArrowRight);
			else if (n == controller.getArrowLeftRadioMenuItem())
				arrowType.set(ArrowType.ArrowLeft);
			else if (n == controller.getArrowBothRadioMenuItem())
				arrowType.set(ArrowType.ArrowBoth);
			else
				arrowType.set(ArrowType.ArrowNone);

		});
		controller.getArrowTypeToggleGroup().selectToggle(controller.getArrowNoneRadioMenuItem());

		controller.getWidthSlider().setValue(1);
		controller.getSizeSlider().setValue(2);
	}


	public ObjectProperty<InteractionMode> interactionModeProperty() {
		return interactionMode;
	}
}
