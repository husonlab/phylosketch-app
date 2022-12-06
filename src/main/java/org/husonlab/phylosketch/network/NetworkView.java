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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.BoundingBox;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
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
	private final Group edgePathGroup = new Group();
	private final Group nodeShapeGroup = new Group();
	private final Group edgeLabelGroup = new Group();
	private final Group nodeLabelGroup = new Group();

	private final Group edgeBelowWaterGroup = new Group();
	private final Group nodeBelowWaterGroup = new Group();
	private final Group labelBelowWaterGroup = new Group();

	private final Group world = new Group(edgeBelowWaterGroup, labelBelowWaterGroup, nodeBelowWaterGroup, edgePathGroup, nodeShapeGroup, edgeLabelGroup, nodeLabelGroup);

	private final PhyloTree tree;
	private final Map<Node, NodeView> nodeViewMap;
	private final Map<Edge, EdgeView> edgeViewMap;

	private double xScale = 1.0;
	private double yScale = 1.0;

	private final DoubleProperty fontScale = new SimpleDoubleProperty(1.0);

	private Consumer<Node> nodeViewAddedCallback = a -> {
	};
	private Consumer<Node> nodeViewRemoveCallback = a -> {
	};

	private Consumer<Edge> edgeViewAddedCallback = a -> {
	};

	private Consumer<Edge> edgeViewRemoveCallback = a -> {
	};

	public NetworkView(Document document) {
		this.tree = document.getModel().getTree();

		nodeViewMap = new HashMap<>();
		edgeViewMap = new HashMap<>();

		edgePathGroup.getChildren().addListener((ListChangeListener<? super javafx.scene.Node>) c -> {
			while (c.next()) {
				for (var node : c.getRemoved()) {
					if (node.getUserData() instanceof EdgeView edgeView) {
						edgeBelowWaterGroup.getChildren().remove(edgeView.curveBelow());
					}
				}
				for (var node : c.getAddedSubList()) {
					if (node.getUserData() instanceof EdgeView edgeView) {
						edgeBelowWaterGroup.getChildren().add(edgeView.curveBelow());
					}
				}
			}
		});

		nodeShapeGroup.getChildren().addListener((ListChangeListener<? super javafx.scene.Node>) c -> {
			while (c.next()) {
				for (var node : c.getRemoved()) {
					if (node.getUserData() instanceof NodeView nodeView) {
						nodeBelowWaterGroup.getChildren().remove(nodeView.shapeBelow());
					}
				}
				for (var node : c.getAddedSubList()) {
					if (node.getUserData() instanceof NodeView nodeView) {
						nodeBelowWaterGroup.getChildren().add(nodeView.shapeBelow());
					}
				}
			}
		});

		nodeLabelGroup.getChildren().addListener((ListChangeListener<? super javafx.scene.Node>) c -> {
			while (c.next()) {
				for (var node : c.getRemoved()) {
					if (node.getUserData() instanceof NodeView nodeView) {
						labelBelowWaterGroup.getChildren().remove(nodeView.labelShapeBelow());
					}
				}
				for (var node : c.getAddedSubList()) {
					if (node instanceof RichTextLabel label) {
						if (node.getUserData() instanceof NodeView nodeView) {
							labelBelowWaterGroup.getChildren().add(nodeView.labelShapeBelow());
						}
					}
				}
			}
		});

		fontScale.addListener((c, o, n) -> {
			for (var v : document.getModel().getTree().nodes()) {
				var label = document.getNetworkView().getView(v).label();
				label.setScale(n.doubleValue());
			}
			for (var e : document.getModel().getTree().edges()) {
				var label = document.getNetworkView().getView(e).label();
				if (label != null)
					label.setScale(n.doubleValue());
			}
		});
	}

	public void clear() {
		nodeViewMap.clear();
		edgeViewMap.clear();
		for (var group : BasicFX.getAllRecursively(world, Group.class)) {
			for (var node : BasicFX.getAllRecursively(group, n -> !(n instanceof Group)))
				group.getChildren().remove(node);
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
			if (nodeView.label() != null) {
				var label = nodeView.label();
				label.scaleProperty().addListener((a, o, n) -> {
					var diff = 0.5 * (n.doubleValue() / o.doubleValue() * label.getHeight() - label.getHeight());
					label.setLayoutY(label.getLayoutY() - diff);
				});
				label.setScale(getFontScale());
			}
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
			if (edgeView.label() != null)
				edgeView.label().setScale(getFontScale());
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
		var path = edgeView.curve();
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
			final var shapeBelow = getView(v).shapeBelow();
			if (shapeBelow.contains(shapeBelow.screenToLocal(xScreen, yScreen)))
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
			final CubicCurve cubicCurve = getView(e).curve();
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
			final CubicCurve cubicCurve = getView(e).curve();
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

	public double getFontScale() {
		return fontScale.get();
	}

	public DoubleProperty fontScaleProperty() {
		return fontScale;
	}

	public void setFontScale(double fontScale) {
		this.fontScale.set(fontScale);
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
