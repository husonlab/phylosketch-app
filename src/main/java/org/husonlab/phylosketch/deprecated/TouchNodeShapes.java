/*
 * TouchNodeShapes.java Copyright (C) 2022 Daniel H. Huson
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
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import jloda.fx.selection.SelectionModel;
import jloda.fx.undo.UndoManager;
import jloda.fx.util.GeometryUtilsFX;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.util.Single;
import org.husonlab.phylosketch.network.NetworkView;
import org.husonlab.phylosketch.network.commands.MoveSelectedNodesCommand;
import org.husonlab.phylosketch.network.commands.NewEdgeAndNodeCommand;
import org.husonlab.phylosketch.utils.GraphUtils;
import org.husonlab.phylosketch.utils.Utilities;
import org.husonlab.phylosketch.views.primary.InteractionMode;

import java.util.HashMap;

/**
 * apply touch interaction for node shapes
 * Daniel Huson, 11.2022
 */
@Deprecated
public class TouchNodeShapes {
	public static void install(Pane pane, UndoManager undoManager, NetworkView networkView, SelectionModel<Node> nodeSelection,
							   SelectionModel<Edge> edgeSelection, Node v, ObjectProperty<InteractionMode> tool) {
		var shape = networkView.getView(v).shape();
		var label = networkView.getView(v).label();

		// these are required to keep edge shapes when moving nodes
		final var oldControlPointLocations = new HashMap<Integer, double[]>();
		final var newControlPointLocations = new HashMap<Integer, double[]>();

		// these are used when creating new edge and node:
		final var target = new SimpleObjectProperty<Node>(null);
		final var line = new Line();

		target.addListener((a, o, n) -> {
			if (o != null) {
				var other = networkView.getView(o).shape();
				other.setScaleX(1);
				other.setScaleY(1);
			}
			if (n != null) {
				var other = networkView.getView(n).shape();
				other.setScaleX(2);
				other.setScaleY(2);
			}
		});

		final var currentTool = new Single<InteractionMode>(null);

		final var startScenePosition = new double[2];
		final var previousScenePosition = new double[2];
		final var touchId = new Single<>(-1);
		final var moved = new Single<>(false);
		final var pressStartTime = new Single<>(0L);

		// 1. pressing on a node always selects it
		// 2. pressing on a node longer than 1 second with out moving it, deselects all other nodes and edges
		// 3. if current tool is AddNodesAndEdges, start creating edge and node
		// 4. if current tool is MoveNodes, start moving selected nodes
		// 5. if current tool is AddLabels, open label for editing

		shape.setOnTouchPressed(a -> {
			var touchPoint = a.getTouchPoint();
			touchId.set(touchPoint.getId());
			pressStartTime.set(System.currentTimeMillis());

			var time = pressStartTime.get();
			Utilities.waitAndThenRunFX(() -> !moved.get() && time.equals(pressStartTime.get()),
					() -> {
						nodeSelection.clearSelection();
						nodeSelection.select(v);
						edgeSelection.clearSelection();
					});

			currentTool.set(tool.get());
			nodeSelection.select(v);

			startScenePosition[0] = previousScenePosition[0] = touchPoint.getSceneX();
			startScenePosition[1] = previousScenePosition[1] = touchPoint.getSceneY();
			moved.set(false);

			shape.setScaleX(2);
			shape.setScaleY(2);

			if (currentTool.get() == InteractionMode.Move) {
				oldControlPointLocations.clear();
				newControlPointLocations.clear();
			} else if (currentTool.get() == InteractionMode.CreateNewEdges) {
				line.setStartX(shape.getTranslateX());
				line.setStartY(shape.getTranslateY());

				final Point2D location = pane.sceneToLocal(previousScenePosition[0], previousScenePosition[1]);

				line.setEndX(location.getX());
				line.setEndY(location.getY());
				networkView.getWorld().getChildren().add(line);
				target.set(null);
			} else if (currentTool.get() == InteractionMode.EditLabels) {// edit labels
				TouchLabelEditing.apply(a, tool, networkView, undoManager, nodeSelection, v, label);
			}
			a.consume();
		});

		shape.setOnTouchMoved(a -> {
			shape.setScaleX(1);
			shape.setScaleY(1);

			var touchPoint = a.getTouchPoint();
			if (touchPoint.getId() == touchId.get()) {
				if (currentTool.get() == InteractionMode.Move) {
					final double deltaX = (touchPoint.getSceneX() - previousScenePosition[0]);
					final double deltaY = (touchPoint.getSceneY() - previousScenePosition[1]);

					for (var u : nodeSelection.getSelectedItems()) {
						var uShape = networkView.getView(u).shape();
						{
							for (var e : u.outEdges()) {
								var edgeView = networkView.getView(e);

								if (!oldControlPointLocations.containsKey(e.getId())) {
									oldControlPointLocations.put(e.getId(), edgeView.getControlCoordinates());
								}
								edgeView.startMoved(deltaX, deltaY);
								newControlPointLocations.put(e.getId(), edgeView.getControlCoordinates());
							}
							for (var e : u.inEdges()) {
								var edgeView = networkView.getView(e);
								if (!oldControlPointLocations.containsKey(e.getId())) {
									oldControlPointLocations.put(e.getId(), edgeView.getControlCoordinates());
								}
								edgeView.endMoved(deltaX, deltaY);
								newControlPointLocations.put(e.getId(), edgeView.getControlCoordinates());
							}
						}
						uShape.setTranslateX(uShape.getTranslateX() + deltaX);
						uShape.setTranslateY(uShape.getTranslateY() + deltaY);
					}
				} else if (currentTool.get() == InteractionMode.CreateNewEdges) {
					shape.setScaleX(1);
					shape.setScaleY(1);
					final var location = pane.sceneToLocal(touchPoint.getSceneX(), touchPoint.getSceneY());
					line.setEndX(location.getX());
					line.setEndY(location.getY());

					final var w = networkView.findNodeIfHit(touchPoint.getScreenX(), touchPoint.getScreenY(), 8);
					if ((w == null || w == v || w != target.get()) && target.get() != null) {
						target.set(null);
					}
					if (w != null && w != v && w != target.get()) {
						target.set(w);
					}
				}

				moved.set(true);
				previousScenePosition[0] = touchPoint.getSceneX();
				previousScenePosition[1] = touchPoint.getSceneY();
				a.consume();
			}
		});

		shape.setOnTouchReleased(a -> {
			shape.setScaleX(1);
			shape.setScaleY(1);

			var touchPoint = a.getTouchPoint();
			if (touchPoint.getId() == touchId.get()) {
				if (currentTool.get() == InteractionMode.Move) {
					if (moved.get()) {
						final double dx = previousScenePosition[0] - startScenePosition[0];
						final double dy = previousScenePosition[1] - startScenePosition[1];
						undoManager.add(new MoveSelectedNodesCommand(dx, dy, networkView,
								nodeSelection.getSelectedItems(), oldControlPointLocations, newControlPointLocations));
					}
				} else if (currentTool.get() == InteractionMode.CreateNewEdges) {
					networkView.getWorld().getChildren().remove(line);
					var x = line.getEndX();
					var y = line.getEndY();
					if (GeometryUtilsFX.distance(line.getStartX(), line.getStartY(), x, y) >= 8) {
						var w = networkView.findNodeIfHit(x, y);
						if (w == null && target.get() != null)
							w = target.get();

						var isDag = true;
						if (w != null) {
							var tree = networkView.getTree();
							var e = tree.newEdge(v, w);
							try {
								isDag = GraphUtils.isDAG(tree);
							} finally {
								tree.deleteEdge(e);
							}
						}
						if (isDag) {
							undoManager.doAndAdd(new NewEdgeAndNodeCommand(pane, networkView, nodeSelection, v, w, x, y));
						}
					}
				}
			}
			moved.set(false);
			pressStartTime.set(0L);
			a.consume();
		});
	}
}
