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

package org.husonlab.phylosketch.network.interaction;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import jloda.fx.selection.SelectionModel;
import jloda.fx.undo.UndoManager;
import jloda.fx.util.GeometryUtilsFX;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.util.Single;
import org.husonlab.phylosketch.Main;
import org.husonlab.phylosketch.network.NetworkView;
import org.husonlab.phylosketch.network.commands.MoveSelectedNodesCommand;
import org.husonlab.phylosketch.network.commands.NewEdgeAndNodeCommand;
import org.husonlab.phylosketch.utils.GraphUtils;
import org.husonlab.phylosketch.views.primary.InteractionMode;

import java.util.HashMap;

/**
 * apply mouse interaction for node shapes
 * Daniel Huson, 11.2022
 */
public class NodeShapeInteraction {
	public final static SelectOnlyService selectOnlyService = new SelectOnlyService();

	public static void install(LabelEditingManager editingManager, Pane pane, UndoManager undoManager, NetworkView networkView, SelectionModel<Node> nodeSelection,
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
		final var moved = new Single<>(false);

		shape.setOnMousePressed(a -> {
			if (a.getClickCount() == 1 && !moved.get()) {
				if (!a.isShiftDown() && Main.isDesktop()) {
					nodeSelection.clearSelection();
					edgeSelection.clearSelection();
					nodeSelection.select(v);
				} else if (!nodeSelection.isSelected(v))
					nodeSelection.select(v);
			}

			selectOnlyService.restart(nodeSelection, edgeSelection, v);

			currentTool.set(tool.get());

			startScenePosition[0] = previousScenePosition[0] = a.getSceneX();
			startScenePosition[1] = previousScenePosition[1] = a.getSceneY();
			moved.set(false);

			if (currentTool.get() == InteractionMode.Move) {
				oldControlPointLocations.clear();
				newControlPointLocations.clear();
			} else if (currentTool.get() == InteractionMode.CreateNewEdges) {
				shape.setCursor(Cursor.CLOSED_HAND);

				line.setStartX(shape.getTranslateX());
				line.setStartY(shape.getTranslateY());

				final Point2D location = pane.sceneToLocal(previousScenePosition[0], previousScenePosition[1]);

				line.setEndX(location.getX());
				line.setEndY(location.getY());
				networkView.getWorld().getChildren().add(line);
				target.set(null);
			} else if (currentTool.get() == InteractionMode.EditLabels) {// edit labels
				editingManager.startEditing(v);
			}
			a.consume();
		});

		shape.setOnMouseDragged(a -> {
			shape.setScaleX(1);
			shape.setScaleY(1);

			selectOnlyService.cancel();

			if (!moved.get() && !nodeSelection.isSelected(v)) {
				nodeSelection.clearSelection();
				edgeSelection.clearSelection();
				nodeSelection.select(v);
			}

			if (currentTool.get() == InteractionMode.Move) {
				final double deltaX = (a.getSceneX() - previousScenePosition[0]);
				final double deltaY = (a.getSceneY() - previousScenePosition[1]);

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
				final var location = pane.sceneToLocal(a.getSceneX(), a.getSceneY());
				line.setEndX(location.getX());
				line.setEndY(location.getY());

				final var w = networkView.findNodeIfHit(a.getScreenX(), a.getScreenY());
				if ((w == null || w == v || w != target.get()) && target.get() != null) {
					target.set(null);
				}
				if (w != null && w != v && w != target.get()) {
					target.set(w);
				}
			}
			moved.set(true);
			previousScenePosition[0] = a.getSceneX();
			previousScenePosition[1] = a.getSceneY();
			a.consume();
		});

		shape.setOnMouseReleased(a -> {
			selectOnlyService.cancel();

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
				if (GeometryUtilsFX.distance(line.getStartX(), line.getStartY(), x, y) >= 5) {
					var w = networkView.findNodeIfHit(x, y, 8);
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
				shape.setCursor(Cursor.CROSSHAIR);
			}
			moved.set(false);
			target.set(null);
			a.consume();
		});

		shape.setOnMouseEntered(a -> {
			shape.setScaleX(2);
			shape.setScaleY(2);
		});

		shape.setOnMouseExited(a -> {
			shape.setScaleX(1);
			shape.setScaleY(1);
		});
	}

	public static class SelectOnlyService extends Service<Boolean> {
		private SelectionModel<Node> nodeSelectionModel;
		private SelectionModel<Edge> edgeSelectionModel;
		private Node node;

		public SelectOnlyService() {
		}

		public void restart(SelectionModel<Node> nodeSelectionModel, SelectionModel<Edge> edgeSelectionModel, Node node) {
			this.nodeSelectionModel = nodeSelectionModel;
			this.edgeSelectionModel = edgeSelectionModel;
			this.node = node;
			restart();
		}

		@Override
		protected Task<Boolean> createTask() {
			return new Task<>() {
				@Override
				protected Boolean call() throws InterruptedException {
					Thread.sleep(1000);
					nodeSelectionModel.clearSelection();
					edgeSelectionModel.clearSelection();
					nodeSelectionModel.select(node);
					return true;
				}
			};
		}
	}

}
