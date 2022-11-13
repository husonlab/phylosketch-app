/*
 * NetworkView.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.collections.ListChangeListener;
import javafx.geometry.BoundingBox;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import jloda.fx.control.RichTextLabel;
import jloda.fx.shapes.CircleShape;
import jloda.fx.util.BasicFX;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * network view
 * Daniel Huson, 10.2022
 */
public class NetworkView {
	private final Group edgeBelowWaterGroup = new Group();
	private final Group nodeBelowWaterGroup = new Group();
	private final Group labelBelowWaterGroup = new Group();
	private final Group edgePathGroup = new Group();
	private final Group nodeShapeGroup = new Group();
	private final Group edgeLabelGroup = new Group();
	private final Group nodeLabelGroup = new Group();

	private final Group world = new Group(edgeBelowWaterGroup, nodeBelowWaterGroup, labelBelowWaterGroup, edgePathGroup, nodeShapeGroup, edgeLabelGroup, nodeLabelGroup);

	private final Document document;
	private final PhyloTree tree;
	private final Map<Node, NodeView> nodeViewMap;
	private final Map<Edge, EdgeView> edgeViewMap;

	private double xScale = 1.0;
	private double yScale = 1.0;

	private Consumer<Node> nodeViewAddedCallback = a -> {
	};
	private Consumer<Node> nodeViewRemoveCallback = a -> {
	};

	private Consumer<Edge> edgeViewAddedCallback = a -> {
	};
	private Consumer<Edge> edgeViewRemoveCallback = a -> {
	};

	public NetworkView(Document document) {
		this.document = document;
		this.tree = document.getModel().getTree();

		nodeViewMap = new HashMap<>();
		edgeViewMap = new HashMap<>();

		edgePathGroup.getChildren().addListener((ListChangeListener<? super javafx.scene.Node>) c -> {
			while (c.next()) {
				for (var node : c.getRemoved()) {
					if (node instanceof CubicCurve cubicCurve && cubicCurve.getUserData() instanceof CubicCurve belowWater) {
						belowWater.startXProperty().unbind();
						belowWater.startYProperty().unbind();
						belowWater.controlX1Property().unbind();
						belowWater.controlY1Property().unbind();
						belowWater.controlX2Property().unbind();
						belowWater.controlY2Property().unbind();
						belowWater.endXProperty().unbind();
						belowWater.endYProperty().unbind();
						belowWater.translateXProperty().unbind();
						belowWater.translateYProperty().unbind();
						edgeBelowWaterGroup.getChildren().remove(belowWater);
					}
				}
				for (var node : c.getAddedSubList()) {
					if (node instanceof CubicCurve shape) {
						var belowWater = new CubicCurve();
						belowWater.setPickOnBounds(false);
						shape.setUserData(belowWater);
						belowWater.setStrokeWidth(25);
						belowWater.setFill(Color.TRANSPARENT);
						belowWater.setStroke(Color.WHITE); // todo: make this the current background color

						belowWater.startXProperty().bind(shape.startXProperty());
						belowWater.startYProperty().bind(shape.startYProperty());
						belowWater.controlX1Property().bind(shape.controlX1Property());
						belowWater.controlY1Property().bind(shape.controlY1Property());
						belowWater.controlX2Property().bind(shape.controlX2Property());
						belowWater.controlY2Property().bind(shape.controlY2Property());
						belowWater.endXProperty().bind(shape.endXProperty());
						belowWater.endYProperty().bind(shape.endYProperty());
						belowWater.translateXProperty().bind(shape.translateXProperty());
						belowWater.translateYProperty().bind(shape.translateYProperty());

						belowWater.setOnTouchPressed(a -> {
							if (shape.getOnTouchPressed() != null) {
								//System.err.println("Transferring to: "+a.getEventType()+" to: "+shape);
								var copy = a.copyFor(null, shape);
								a.consume();
								shape.getOnTouchPressed().handle(copy);
								copy.consume();
							}
						});
						belowWater.setOnTouchMoved(a -> {
							if (shape.getOnTouchMoved() != null) {
								//System.err.println("Transferring to: "+a.getEventType()+" to: "+shape);
								var copy = a.copyFor(null, shape);
								a.consume();
								shape.getOnTouchMoved().handle(copy);
								copy.consume();
							}
						});
						belowWater.setOnTouchReleased(a -> {
							if (shape.getOnTouchReleased() != null) {
								//System.err.println("Transferring to: "+a.getEventType()+" to: "+shape);
								var copy = a.copyFor(null, shape);
								a.consume();
								shape.getOnTouchReleased().handle(copy);
								copy.consume();
							}
						});

						belowWater.setOnMouseClicked(a -> {
							if (shape.getOnMouseClicked() != null) {
								//System.err.println("Transferring to: "+a.getEventType()+" to: "+shape);
								var copy = a.copyFor(null, shape);
								a.consume();
								shape.getOnMouseClicked().handle(copy);
								copy.consume();
							}
						});
						belowWater.setOnMousePressed(a -> {
							if (shape.getOnMousePressed() != null) {
								//System.err.println("Transferring to: "+a.getEventType()+" to: "+shape);
								var copy = a.copyFor(null, shape);
								a.consume();
								shape.getOnMousePressed().handle(copy);
								copy.consume();
							}
						});
						belowWater.setOnMouseDragged(a -> {
							if (shape.getOnMouseDragged() != null) {
								//System.err.println("Transferring to: "+a.getEventType()+" to: "+shape);
								var copy = a.copyFor(null, shape);
								a.consume();
								shape.getOnMouseDragged().handle(copy);
								copy.consume();
							}
						});
						belowWater.setOnMouseReleased(a -> {
							if (shape.getOnMouseReleased() != null) {
								//System.err.println("Transferring to: "+a.getEventType()+" to: "+shape);
								var copy = a.copyFor(null, shape);
								a.consume();
								shape.getOnMouseReleased().handle(copy);
								copy.consume();
							}
						});

						edgeBelowWaterGroup.getChildren().add(belowWater);
					}
				}
			}
		});

		nodeShapeGroup.getChildren().addListener((ListChangeListener<? super javafx.scene.Node>) c -> {
			while (c.next()) {
				for (var node : c.getRemoved()) {
					if (node instanceof Shape shape && shape.getUserData() instanceof Shape belowWater) {
						belowWater.translateXProperty().unbind();
						belowWater.translateYProperty().unbind();
						nodeBelowWaterGroup.getChildren().remove(belowWater);
					}
				}
				for (var node : c.getAddedSubList()) {
					if (node instanceof Shape shape) {
						var belowWater = new Circle(15);
						shape.setUserData(belowWater);
						belowWater.setStroke(Color.TRANSPARENT);
						belowWater.setFill(Color.WHITE); // todo: make this the current background color
						belowWater.translateXProperty().bind(shape.translateXProperty());
						belowWater.translateYProperty().bind(shape.translateYProperty());

						belowWater.setOnTouchPressed(a -> {
							if (shape.getOnTouchPressed() != null) {
								shape.getOnTouchPressed().handle(a.copyFor(null, shape));
								a.consume();
							}
						});
						belowWater.setOnTouchMoved(a -> {
							if (shape.getOnTouchMoved() != null) {
								shape.getOnTouchMoved().handle(a.copyFor(null, shape));
								a.consume();
							}
						});
						belowWater.setOnTouchReleased(a -> {
							if (shape.getOnTouchReleased() != null) {
								shape.getOnTouchReleased().handle(a.copyFor(null, shape));
								a.consume();
							}
						});

						belowWater.setOnMouseClicked(a -> {
							if (shape.getOnMouseClicked() != null) {
								shape.getOnMouseClicked().handle(a.copyFor(null, shape));
								a.consume();
							}
						});
						belowWater.setOnMousePressed(a -> {
							if (shape.getOnMousePressed() != null) {
								shape.getOnMousePressed().handle(a.copyFor(null, shape));
								a.consume();
							}
						});
						belowWater.setOnMouseDragged(a -> {
							if (shape.getOnMouseDragged() != null) {
								shape.getOnMouseDragged().handle(a.copyFor(null, shape));
								a.consume();
							}
						});
						belowWater.setOnMouseReleased(a -> {
							if (shape.getOnMouseReleased() != null) {
								shape.getOnMouseReleased().handle(a.copyFor(null, shape));
								a.consume();
							}
						});
						nodeBelowWaterGroup.getChildren().add(belowWater);
					}
				}
			}
		});

		nodeLabelGroup.getChildren().addListener((ListChangeListener<? super javafx.scene.Node>) c -> {
			while (c.next()) {
				for (var node : c.getRemoved()) {
					if (node instanceof RichTextLabel label && label.getUserData() instanceof Rectangle belowWater) {
						belowWater.translateXProperty().unbind();
						belowWater.translateYProperty().unbind();
						belowWater.widthProperty().unbind();
						belowWater.heightProperty().unbind();
						labelBelowWaterGroup.getChildren().remove(belowWater);
					}
				}
				for (var node : c.getAddedSubList()) {
					if (node instanceof RichTextLabel label) {
						var belowWater = new Rectangle(20, 20);
						label.setUserData(belowWater);
						belowWater.setStroke(Color.TRANSPARENT);
						belowWater.setFill(Color.WHITE); // todo: make this the current background color
						belowWater.widthProperty().bind(label.widthProperty().add(10));
						belowWater.heightProperty().bind(label.heightProperty().add(10));
						belowWater.translateXProperty().bind(label.translateXProperty().add(label.layoutXProperty()).subtract(5));
						belowWater.translateYProperty().bind(label.translateYProperty().add(label.layoutYProperty()).subtract(5));

						belowWater.setOnTouchPressed(a -> {
							if (label.getOnTouchPressed() != null) {
								label.getOnTouchPressed().handle(a.copyFor(null, label));
								a.consume();
							}
						});
						belowWater.setOnTouchMoved(a -> {
							if (label.getOnTouchMoved() != null) {
								label.getOnTouchMoved().handle(a.copyFor(null, label));
								a.consume();
							}
						});
						belowWater.setOnTouchReleased(a -> {
							if (label.getOnTouchReleased() != null) {
								label.getOnTouchReleased().handle(a.copyFor(null, label));
								a.consume();
							}
						});

						belowWater.setOnMouseClicked(a -> {
							if (label.getOnMouseClicked() != null) {
								label.getOnMouseClicked().handle(a.copyFor(null, label));
								a.consume();
							}
						});
						belowWater.setOnMousePressed(a -> {
							if (label.getOnMousePressed() != null) {
								label.getOnMousePressed().handle(a.copyFor(null, label));
								a.consume();
							}
						});
						belowWater.setOnMouseDragged(a -> {
							if (label.getOnMouseDragged() != null) {
								label.getOnMouseDragged().handle(a.copyFor(null, label));
								a.consume();
							}
						});
						belowWater.setOnMouseReleased(a -> {
							if (label.getOnMouseReleased() != null) {
								label.getOnMouseReleased().handle(a.copyFor(null, label));
								a.consume();
							}
						});
						labelBelowWaterGroup.getChildren().add(belowWater);
					}
				}
			}
		});
	}

	public void clear() {
		for (var v : tree.nodes())
			removeView(v);
		for (var e : tree.edges()) {
			removeView(e);
		}
	}

	public PhyloTree getTree() {
		return tree;
	}

	public NodeView getView(Node v) {
		return nodeViewMap.get(v);
	}

	public void setView(Node v, NodeView nodeView) {
		var oldNodeView = nodeViewMap.get(v);
		if (oldNodeView != null) {
			if (oldNodeView.shape() != null)
				nodeShapeGroup.getChildren().remove(oldNodeView.shape());
			if (oldNodeView.label() != null)
				nodeLabelGroup.getChildren().remove(oldNodeView.label());
			nodeViewRemoveCallback.accept(v);
			nodeViewMap.remove(v);
		}
		if (nodeView != null) {
			if (nodeView.shape() != null)
				nodeShapeGroup.getChildren().add(nodeView.shape());
			if (nodeView.label() != null)
				nodeLabelGroup.getChildren().add(nodeView.label());
			nodeViewMap.put(v, nodeView);
			nodeViewAddedCallback.accept(v);
		}
	}

	public void createShapeAndLabel(Node v, double x, double y, String text, double labelDx, double labelDy) {
		var shape = new CircleShape(8);
		shape.setId("graph-node");
		shape.setStroke(Color.BLACK);
		shape.setFill(Color.WHITE);
		shape.setTranslateX(x);
		shape.setTranslateY(y);
		var label = new RichTextLabel(text);
		label.translateXProperty().bind(shape.translateXProperty());
		label.translateYProperty().bind(shape.translateYProperty());
		label.setLayoutX(labelDx);
		label.setLayoutY(labelDy);
		setView(v, new NodeView(shape, label));
	}

	public void removeView(Node v) {
		setView(v, null);
	}

	public EdgeView getView(Edge e) {
		return edgeViewMap.get(e);
	}

	public void setView(Edge e, EdgeView edgeView) {
		var oldEdgeView = edgeViewMap.get(e);
		if (oldEdgeView != null) {
			edgePathGroup.getChildren().removeAll(oldEdgeView.getChildren());
			if (oldEdgeView.label() != null)
				edgeLabelGroup.getChildren().remove(oldEdgeView.label());
			edgeViewRemoveCallback.accept(e);
			edgeViewMap.remove(e);
		}
		if (edgeView != null) {
			edgePathGroup.getChildren().addAll(edgeView.getChildren());
			if (edgeView.label() != null)
				edgeLabelGroup.getChildren().add(edgeView.label());
			edgeViewMap.put(e, edgeView);
			edgeViewAddedCallback.accept(e);
		}
	}

	public void createEdgeView(Edge e) {
		var source = getView(e.getSource()).shape();
		var target = getView(e.getTarget()).shape();
		var edgeView = new EdgeView(e, source.translateXProperty(), source.translateYProperty(), target.translateXProperty(), target.translateYProperty());
		setView(e, edgeView);
	}

	public void addLabel(Edge e, String text, double dx, double dy) {
		var edgeView = getView(e);
		if (edgeView.label() != null) {
			edgeView.label().translateXProperty().unbind();
			edgeView.label().translateYProperty().unbind();
			edgeLabelGroup.getChildren().remove(edgeView.label());
		}
		var path = edgeView.getCurve();
		var textLabel = new RichTextLabel(text);
		textLabel.translateXProperty().bind(path.translateXProperty());
		textLabel.translateYProperty().bind(path.translateYProperty());
		textLabel.setLayoutX(dx);
		textLabel.setLayoutX(dy);
		edgeView.setLabel(textLabel);
		edgeLabelGroup.getChildren().add(textLabel);
	}


	public void removeView(Edge e) {
		setView(e, null);
	}

	public void setNodeViewAddedCallback(Consumer<Node> nodeGroupAddedCallBack) {
		this.nodeViewAddedCallback = nodeGroupAddedCallBack;
	}

	public void setNodeViewRemoveCallback(Consumer<Node> nodeGroupRemovedCallBack) {
		this.nodeViewRemoveCallback = nodeGroupRemovedCallBack;
	}

	public void setEdgeViewAddedCallback(Consumer<Edge> edgeViewAddedCallback) {
		this.edgeViewAddedCallback = edgeViewAddedCallback;
	}

	public void setEdgeViewRemoveCallback(Consumer<Edge> edgeViewRemoveCallback) {
		this.edgeViewRemoveCallback = edgeViewRemoveCallback;
	}


	public Node findNodeIfHit(double xScreen, double yScreen) {
		for (var v : tree.nodes()) {
			final var shape = getView(v).shape();
			if (shape.contains(shape.screenToLocal(xScreen, yScreen)))
				return v;
		}
		return null;
	}

	public Node findNodeIfHit(double xScreen, double yScreen, double tolerance) {
		for (var v : tree.nodes()) {
			final var shape = getView(v).shape();
			var bounds = shape.screenToLocal(new BoundingBox(xScreen - 0.5 * tolerance, yScreen - 0.5 * tolerance, tolerance, tolerance));
			if (shape.intersects(bounds))
				return v;
		}
		return null;
	}

	public void moveNode(Node v, double dx, double dy) {
		var shape = getView(v).shape();
		shape.setTranslateX(shape.getTranslateX() + dx);
		shape.setTranslateY(shape.getTranslateY() + dy);
	}

	public void moveLabel(Node v, double dx, double dy) {
		var label = getView(v).label();
		if (label != null) {
			label.setLayoutX(label.getLayoutX() + dx);
			label.setLayoutY(label.getLayoutY() + dy);
		}
	}

	public void scale(double xFactor, double yFactor) {
		if (xFactor <= 0 || yFactor <= 0)
			throw new IllegalArgumentException();
		this.xScale *= xFactor;
		this.yScale *= yFactor;
		for (var v : tree.nodes()) {
			var shape = getView(v).shape();
			shape.setTranslateX(shape.getTranslateX() * xFactor);
			shape.setTranslateY(shape.getTranslateY() * yFactor);
		}
		for (var e : tree.edges()) {
			final CubicCurve cubicCurve = getView(e).getCurve();
			cubicCurve.setControlX1(xFactor * cubicCurve.getControlX1());
			cubicCurve.setControlY1(yFactor * cubicCurve.getControlY1());
			cubicCurve.setControlX2(xFactor * cubicCurve.getControlX2());
			cubicCurve.setControlY2(yFactor * cubicCurve.getControlY2());
		}
		for (var textField : BasicFX.getAllRecursively(world, v -> "text-field".equals(v.getId()))) {
			textField.setTranslateX(textField.getTranslateX() * xFactor);
			textField.setTranslateY(textField.getTranslateY() * yFactor);

		}
	}

	public void resetScale() {
		for (var v : tree.nodes()) {
			var shape = getView(v).shape();
			shape.setTranslateX(shape.getTranslateX() / xScale);
			shape.setTranslateY(shape.getTranslateY() / yScale);
		}
		for (var e : tree.edges()) {
			final CubicCurve cubicCurve = getView(e).getCurve();
			cubicCurve.setControlX1(cubicCurve.getControlX1() / xScale);
			cubicCurve.setControlY1(cubicCurve.getControlY1() / yScale);
			cubicCurve.setControlX2(cubicCurve.getControlX2() / xScale);
			cubicCurve.setControlY2(cubicCurve.getControlY2() / yScale);
		}
		xScale = 1.0;
		yScale = 1.0;
	}


	public Group getWorld() {
		return world;
	}

	public BoundingBox getBoundingBox() {
		var minX = Double.MAX_VALUE;
		var maxX = Double.MIN_VALUE;
		var minY = Double.MAX_VALUE;
		var maxY = Double.MIN_VALUE;
		for (var v : tree.nodes()) {
			var shape = getView(v).shape();
			if (shape != null) {
				minX = Math.min(minX, shape.getTranslateX());
				maxX = Math.max(maxX, shape.getTranslateX());
				minY = Math.min(minY, shape.getTranslateY());
				maxY = Math.max(maxY, shape.getTranslateY());
			}
		}
		return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
	}
}
