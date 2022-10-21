/*
 * InstallEdgeInteraction.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.network;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import jloda.fx.selection.SelectionModel;
import jloda.fx.undo.UndoManager;
import jloda.fx.util.MouseDragClosestNode;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.util.Pair;
import org.husonlab.phylosketch.network.commands.EdgeShapeCommand;
import org.husonlab.phylosketch.views.primary.PrimaryPresenter;

import java.util.function.Function;

public class InstallEdgeInteraction {

	public static void apply(boolean useTouch, Pane pane, UndoManager undoManager, NetworkView networkView, SelectionModel<Node> nodeSelection,
							 SelectionModel<Edge> edgeSelection, Edge e, ObjectProperty<PrimaryPresenter.Tool> tool) {
		var ev = networkView.getView(e);
		var curve = ev.getCurve();

		final var isSelected = new SimpleBooleanProperty(edgeSelection.isSelected(e));
		edgeSelection.getSelectedItems().addListener((InvalidationListener) c -> isSelected.set(edgeSelection.isSelected(e)));

		// reference current translating control
		final Function<Circle, Pair<Edge, Integer>> translatingControl = circle -> {
			if (circle == ev.getCircle1())
				return new Pair<>(e, 1);
			else
				return new Pair<>(e, 2);
		};


		MouseDragClosestNode.setup(useTouch, curve, isSelected, networkView.getView(e.getSource()).shape(), ev.getCircle1(),
				networkView.getView(e.getTarget()).shape(), ev.getCircle2(),
				(circle, delta) -> undoManager.add(new EdgeShapeCommand(networkView, translatingControl.apply((Circle) circle), delta)));

		curve.setOnMouseEntered(c -> curve.setStrokeWidth(4.0 * curve.getStrokeWidth()));
		curve.setOnMouseExited(c -> curve.setStrokeWidth(1.0 / 4.0 * curve.getStrokeWidth()));

		curve.setOnMouseClicked(c -> {
			if (!MouseDragClosestNode.wasMoved()) {
				if (!c.isShiftDown()) {
					nodeSelection.clearSelection();
					edgeSelection.clearSelection();
					edgeSelection.select(e);
				} else
					edgeSelection.toggleSelection(e);
			}
			c.consume();
		});
	}
}
