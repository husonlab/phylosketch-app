/*
 * TouchEdgeShapes.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.network.interaction;

import javafx.beans.property.ObjectProperty;
import javafx.scene.shape.Circle;
import jloda.graph.Edge;
import jloda.util.Pair;
import org.husonlab.phylosketch.Main;
import org.husonlab.phylosketch.network.Document;
import org.husonlab.phylosketch.network.commands.DeleteSubTreeCommand;
import org.husonlab.phylosketch.network.commands.EdgeShapeCommand;
import org.husonlab.phylosketch.utils.MouseDragClosestNode;
import org.husonlab.phylosketch.views.primary.InteractionMode;

import java.util.function.Function;

import static org.husonlab.phylosketch.network.interaction.NodeShapeInteraction.consumeAllScrollAndTouchEvents;

/**
 * apply mouse interaction for edge shapes
 * Daniel Huson, 11.2022
 */
public class EdgeShapeInteraction {

	public static void apply(Document document, Edge e, ObjectProperty<InteractionMode> mode) {
		var networkView = document.getNetworkView();
		var nodeSelection = document.getNodeSelection();
		var edgeSelection = document.getEdgeSelection();

		var ev = networkView.getView(e);
		var curve = ev.curve();
		var curveBelow = ev.curveBelow();

		curve.setMouseTransparent(true);
		consumeAllScrollAndTouchEvents(curve);
		consumeAllScrollAndTouchEvents(curveBelow);


		// reference current translating control
		final Function<Circle, Pair<Edge, Integer>> translatingControl = circle -> {
			if (circle == ev.getCircle1())
				return new Pair<>(e, 1);
			else
				return new Pair<>(e, 2);
		};

		var sourceShape = networkView.getView(e.getSource()).shape();
		var targetShape = networkView.getView(e.getTarget()).shape();

		MouseDragClosestNode.setup(curveBelow, sourceShape, ev.getCircle1(), targetShape, ev.getCircle2(), networkView.getWorld(),
				(circle, delta) -> document.getUndoManager().add(new EdgeShapeCommand(networkView, translatingControl.apply((Circle) circle), delta)),
				a -> {
					if (Main.isDesktop() && a.isShiftDown()) {
						if (nodeSelection.isSelected(e.getSource()) == edgeSelection.isSelected(e))
							nodeSelection.toggleSelection(e.getSource());
						if (nodeSelection.isSelected(e.getTarget()) == edgeSelection.isSelected(e))
							nodeSelection.toggleSelection(e.getTarget());
						edgeSelection.toggleSelection(e);
					} else {
						edgeSelection.select(e);
						nodeSelection.select(e.getSource());
						nodeSelection.select(e.getTarget());
					}
				},
				a -> {
					if (edgeSelection.isSelected(e)) {
						nodeSelection.clearSelection();
						edgeSelection.clearSelection();
						edgeSelection.select(e);
						nodeSelection.select(e.getSource());
						nodeSelection.select(e.getTarget());
					}
				},
				a -> {
					if (Main.isDesktop() && a.isStillSincePress() && !a.isShiftDown()) {
						nodeSelection.clearSelection();
						edgeSelection.clearSelection();
						edgeSelection.select(e);
						nodeSelection.select(e.getSource());
						nodeSelection.select(e.getTarget());
					}
					if (mode.get() == InteractionMode.Erase)
						document.getUndoManager().doAndAdd(new DeleteSubTreeCommand(document, e.getTarget()));
				},
				() -> mode.get() == InteractionMode.Move);
	}
}
