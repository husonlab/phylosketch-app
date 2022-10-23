/*
 * InstallNodeInteraction.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import jloda.fx.selection.SelectionModel;
import jloda.fx.undo.UndoManager;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.util.Single;
import org.husonlab.phylosketch.network.commands.MoveSelectedNodesCommand;
import org.husonlab.phylosketch.network.commands.NewEdgeAndNodeCommand;
import org.husonlab.phylosketch.views.primary.PrimaryPresenter;

import java.util.HashMap;
import java.util.Map;

/**
 * install node interaction
 */
public class InstallNodeInteraction {
	public static void apply(boolean useTouch, Pane pane, UndoManager undoManager, NetworkView networkView, SelectionModel<Node> nodeSelection,
							 SelectionModel<Edge> edgeSelection, Node v, ObjectProperty<PrimaryPresenter.Tool> tool) {
		var shape = networkView.getView(v).shape();
		shape.setCursor(Cursor.CROSSHAIR);

		final var mouseDownPosition = new double[2];
		final var previousMousePosition = new double[2];
		final var oldControlPointLocations = new HashMap<Integer, double[]>();
		final var newControlPointLocations = new HashMap<Integer, double[]>();

		final var touchCount = new Single<>(0);
		final var moved = new Single<>(false);
		final var currentTool = new Single<PrimaryPresenter.Tool>(null);
		final var target = new SimpleObjectProperty<Node>(null);
		final var line = new Line();

		target.addListener((var,o,n)->{
			if(o!=null) {
				var aShape=networkView.getView(o).shape();
				aShape.setScaleX(1.0);
				aShape.setScaleY(1.0);
			}
			if(n!=null) {
				var aShape = networkView.getView(n).shape();
				aShape.setScaleX(3);
				aShape.setScaleY(3);
			}
		});

		if (!useTouch) {
			shape.setOnMousePressed(c -> {
				currentTool.set(tool.get());
				if (currentTool.get() == PrimaryPresenter.Tool.MoveNodes || currentTool.get() == PrimaryPresenter.Tool.AddNodesAndEdges) {
					var x = c.getSceneX();
					var y = c.getSceneY();
					handlePressed(currentTool.get(), x, y, mouseDownPosition, previousMousePosition, oldControlPointLocations, newControlPointLocations, line, moved, shape, pane, networkView.getWorld(), target);
				}
				c.consume();
			});

			shape.setOnMouseDragged(c -> {
				if (currentTool.get() == PrimaryPresenter.Tool.MoveNodes || currentTool.get() == PrimaryPresenter.Tool.AddNodesAndEdges) {
					var xScreen = c.getScreenX();
					var yScreen = c.getScreenY();
					var xScene = c.getSceneX();
					var yScene = c.getSceneY();
					handleDragged(currentTool.get(), xScreen, yScreen, xScene, yScene, previousMousePosition,
							oldControlPointLocations, newControlPointLocations, nodeSelection, v, networkView, line, moved, shape, pane, target);
				}
				c.consume();
			});
			shape.setOnMouseReleased(c -> {
				shape.setEffect(null);
				if (currentTool.get() == PrimaryPresenter.Tool.MoveNodes || currentTool.get() == PrimaryPresenter.Tool.AddNodesAndEdges) {
					handleReleased(currentTool.get(), !c.isShiftDown(), mouseDownPosition, previousMousePosition,
							oldControlPointLocations, newControlPointLocations, nodeSelection, edgeSelection, v,
							networkView, c.isShiftDown(), c.isPopupTrigger(), line, moved, shape, pane, target, undoManager);
				}
				c.consume();
			});

		} else {
			final var pressStartTime = new Single<>(0L);
			shape.setOnTouchPressed(c -> {
				pressStartTime.set(System.currentTimeMillis());
				touchCount.set(c.getTouchCount());
				if (touchCount.get() == 1) {
					currentTool.set(tool.get());
					if (currentTool.get() == PrimaryPresenter.Tool.MoveNodes || currentTool.get() == PrimaryPresenter.Tool.AddNodesAndEdges) {
						System.err.println("shape touched");

						var x = c.getTouchPoint().getSceneX();
						var y = c.getTouchPoint().getSceneY();
						handlePressed(currentTool.get(), x, y, mouseDownPosition, previousMousePosition, oldControlPointLocations, newControlPointLocations, line, moved, shape, pane, networkView.getWorld(), target);
					}
					c.consume();
				}
			});

			shape.setOnTouchMoved(c -> {
				if (touchCount.get() == 1) {
					if (currentTool.get() == PrimaryPresenter.Tool.MoveNodes || currentTool.get() == PrimaryPresenter.Tool.AddNodesAndEdges) {
						var xScreen = c.getTouchPoint().getScreenX();
						var yScreen = c.getTouchPoint().getScreenY();
						var xScene = c.getTouchPoint().getSceneX();
						var yScene = c.getTouchPoint().getSceneY();
						handleDragged(currentTool.get(), xScreen, yScreen, xScene, yScene, previousMousePosition, oldControlPointLocations, newControlPointLocations,
								nodeSelection, v, networkView, line, moved, shape, pane, target);
					}
					c.consume();
				}
			});

			shape.setOnTouchReleased(c -> {
				if (touchCount.get() == 1) {
					if (currentTool.get() == PrimaryPresenter.Tool.MoveNodes || currentTool.get() == PrimaryPresenter.Tool.AddNodesAndEdges) {
						handleReleased(currentTool.get(), System.currentTimeMillis() - pressStartTime.get() > 1500, mouseDownPosition, previousMousePosition, oldControlPointLocations, newControlPointLocations, nodeSelection, edgeSelection, v, networkView, false, false, line, moved,
								shape, pane, target, undoManager);
					}
					c.consume();
				}
			});
		}
	}

	private static void handlePressed(PrimaryPresenter.Tool what, double xScene, double yScene, double[] mouseDownPosition, double[] previousMousePosition,
									  Map<Integer, double[]> oldControlPointLocations,
									  Map<Integer, double[]> newControlPointLocations,
									  Line line, Single<Boolean> moved,
									  Shape shape, Pane pane, Group world, ObjectProperty<Node> target) {
		mouseDownPosition[0] = previousMousePosition[0] = xScene;
		mouseDownPosition[1] = previousMousePosition[1] = yScene;
		moved.set(false);

		oldControlPointLocations.clear();
		newControlPointLocations.clear();

		if (what == PrimaryPresenter.Tool.AddNodesAndEdges) {
			line.setStartX(shape.getTranslateX());
			line.setStartY(shape.getTranslateY());
			shape.setCursor(Cursor.CLOSED_HAND);

			shape.setScaleX(2);
			shape.setScaleY(2);

			final Point2D location = pane.sceneToLocal(previousMousePosition[0], previousMousePosition[1]);

			line.setEndX(location.getX());
			line.setEndY(location.getY());
			world.getChildren().add(line);
			target.set(null);
		}
	}

	private static void handleDragged(PrimaryPresenter.Tool what, double screenX, double screenY, double sceneX, double sceneY, double[] previousMousePosition,
									  Map<Integer, double[]> oldControlPointLocations,
									  Map<Integer, double[]> newControlPointLocations,
									  SelectionModel<Node> nodeSelection, Node v, NetworkView networkView,
									  Line line, Single<Boolean> moved,
									  Shape shape, Pane pane, ObjectProperty<Node> target) {

		shape.setScaleX(1);
		shape.setScaleY(1);

		if (what == PrimaryPresenter.Tool.MoveNodes) {
			//nodeSelection.clearSelection();
			nodeSelection.select(v);
			final double deltaX = (sceneX - previousMousePosition[0]);
			final double deltaY = (sceneY - previousMousePosition[1]);

			for (var u : nodeSelection.getSelectedItems()) {
				var uShape = networkView.getView(u).shape();
				{
						final double deltaXReshapeEdge = (sceneX - previousMousePosition[0]);
						final double deltaYReshapeEdge = (sceneY - previousMousePosition[1]);

					for (var e : u.outEdges()) {
						var edgeView = networkView.getView(e);

						if (!oldControlPointLocations.containsKey(e.getId())) {
							oldControlPointLocations.put(e.getId(), edgeView.getControlCoordinates());
						}
						edgeView.startMoved(deltaXReshapeEdge, deltaYReshapeEdge);
						newControlPointLocations.put(e.getId(), edgeView.getControlCoordinates());
					}
					for (var e : u.inEdges()) {
						var edgeView = networkView.getView(e);
						if (!oldControlPointLocations.containsKey(e.getId())) {
							oldControlPointLocations.put(e.getId(), edgeView.getControlCoordinates());
						}
						edgeView.endMoved(deltaXReshapeEdge, deltaYReshapeEdge);
						newControlPointLocations.put(e.getId(), edgeView.getControlCoordinates());
					}
				}

				uShape.setTranslateX(uShape.getTranslateX() + deltaX);
				uShape.setTranslateY(uShape.getTranslateY() + deltaY);
			}
		} else if (what == PrimaryPresenter.Tool.AddNodesAndEdges) {
			nodeSelection.clearSelection();
			nodeSelection.select(v);

			final var location = pane.sceneToLocal(sceneX, sceneY);
			line.setEndX(location.getX());
			line.setEndY(location.getY());

			final var w = networkView.findNodeIfHit(screenX, screenY);
			if ((w == null || w == v || w != target.get()) && target.get() != null) {
				target.set(null);
			}
			if (w != null && w != v && w != target.get()) {
				target.set(w);
			}
		}
		moved.set(true);
		previousMousePosition[0] = sceneX;
		previousMousePosition[1] = sceneY;
	}

	private static void handleReleased(PrimaryPresenter.Tool what, boolean clearSelection, double[] mouseDownPosition, double[] previousMousePosition,
									   Map<Integer, double[]> oldControlPointLocations,
									   Map<Integer, double[]> newControlPointLocations,
									   SelectionModel<Node> nodeSelection, SelectionModel<Edge> edgeSelection,
									   Node v, NetworkView networkView,
									   boolean isShiftDown, boolean isPopupTrigger, Line line, Single<Boolean> moved,
									   Shape shape, Pane pane, ObjectProperty<Node> target, UndoManager undoManager) {
		try {
			if (!moved.get()) {
				if (isPopupTrigger) {
					return;
				} else {
					if (clearSelection) {
						nodeSelection.clearSelection();
						edgeSelection.clearSelection();
					}
					nodeSelection.toggleSelection(v);
				}
			} else {
				if (what == PrimaryPresenter.Tool.MoveNodes) {
					// yes, createShape, not doAndAdd()
					final double dx = previousMousePosition[0] - mouseDownPosition[0];
					final double dy = previousMousePosition[1] - mouseDownPosition[1];
					undoManager.add(new MoveSelectedNodesCommand(dx, dy, networkView,
							nodeSelection.getSelectedItems(), oldControlPointLocations, newControlPointLocations));
				} else if (what == PrimaryPresenter.Tool.AddNodesAndEdges) {
					var x = line.getEndX();
					var y = line.getEndY();

					var w = networkView.findNodeIfHit(x, y);
					if (w == null && target.get() != null)
						w = target.get();
					undoManager.doAndAdd(new NewEdgeAndNodeCommand(pane, networkView, nodeSelection, v, w, x, y));
					shape.setCursor(Cursor.CROSSHAIR);
				}
				moved.set(false);
			}
			networkView.getWorld().getChildren().remove(line);
		}
		finally {
			shape.setScaleX(1);
			shape.setScaleY(1);
			target.set(null);
		}
	}
}