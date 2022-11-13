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

package org.husonlab.phylosketch.deprecated;

import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import jloda.fx.selection.SelectionModel;
import jloda.fx.undo.UndoManager;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.util.Pair;
import jloda.util.Single;
import org.husonlab.phylosketch.network.NetworkView;
import org.husonlab.phylosketch.network.commands.EdgeShapeCommand;
import org.husonlab.phylosketch.utils.Utilities;
import org.husonlab.phylosketch.views.primary.InteractionMode;

import java.util.function.Function;

/**
 * apply touch interaction for edge shapes
 * Daniel Huson, 11.2022
 */

@Deprecated
public class TouchEdgeShapes {

	public static void install(Pane pane, UndoManager undoManager, NetworkView networkView, SelectionModel<Node> nodeSelection,
							   SelectionModel<Edge> edgeSelection, Edge e, ObjectProperty<InteractionMode> tool) {
		var ev = networkView.getView(e);
		var curve = ev.getCurve();

		// reference current translating control
		final Function<Circle, Pair<Edge, Integer>> translatingControl = circle -> {
			if (circle == ev.getCircle1())
				return new Pair<>(e, 1);
			else
				return new Pair<>(e, 2);
		};

		final var moved = new Single<>(false);
		final var pressStartTime = new Single<>(0L);

		curve.setOnTouchPressed(a -> {
			System.err.println("Pressed: " + e);
			pressStartTime.set(System.currentTimeMillis());

			moved.set(false);
			var time = pressStartTime.get();
			Utilities.waitAndThenRunFX(() -> !moved.get() && time.equals(pressStartTime.get()),
					() -> {
						nodeSelection.clearSelection();
						edgeSelection.clearSelection();
					});
			edgeSelection.select(e);
			a.consume();
		});
		curve.setOnTouchMoved(a -> {
			moved.set(true);
			a.consume();
		});
		curve.setOnTouchReleased(a -> {
			pressStartTime.set(0L);
			a.consume();
		});

		TouchMoveClosestNode.setup(curve, networkView.getView(e.getSource()).shape(), ev.getCircle1(), networkView.getView(e.getTarget()).shape(),
				ev.getCircle2(), (circle, delta) -> undoManager.add(new EdgeShapeCommand(networkView, translatingControl.apply((Circle) circle), delta)), networkView.getWorld(),
				() -> {
					nodeSelection.clearSelection();
					edgeSelection.clearSelection();
					edgeSelection.select(e);
				},
				() -> tool.get() == InteractionMode.Move);
	}
}
