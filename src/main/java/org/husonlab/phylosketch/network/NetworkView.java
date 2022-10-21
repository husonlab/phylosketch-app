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

import javafx.geometry.BoundingBox;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Shape;
import jloda.fx.control.RichTextLabel;
import jloda.fx.shapes.CircleShape;
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
	private final Group world = new Group(edgePathGroup, nodeShapeGroup, edgeLabelGroup, nodeLabelGroup);

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
			var oldNodeView= nodeViewMap.get(v);
			if(oldNodeView!=null) {
				if(oldNodeView.shape()!=null)
					nodeShapeGroup.getChildren().remove(oldNodeView.shape());
				if(oldNodeView.label()!=null)
					nodeLabelGroup.getChildren().remove(oldNodeView.label());
				nodeViewRemoveCallback.accept(v);
				nodeViewMap.remove(v);
			}
		if(nodeView!=null) {
			if(nodeView.shape()!=null)
				nodeShapeGroup.getChildren().add(nodeView.shape());
			if(nodeView.label()!=null)
				nodeLabelGroup.getChildren().add(nodeView.label());
			nodeViewMap.put(v, nodeView);
			nodeViewAddedCallback.accept(v);
		}
	}

	public void createShape(Node v, double x, double y) {
		var shape = new CircleShape(8);
		shape.setStroke(Color.BLACK);
		shape.setFill(Color.WHITE);
		shape.setTranslateX(x);
		shape.setTranslateY(y);
		setView(v, new NodeView(shape, null));
	}

	public void addLabel(Node v, String text, double dx, double dy) {
		var nodeView= getView(v);
		var oldLabel=nodeView.label();
		if(oldLabel!=null) {
			oldLabel.translateXProperty().unbind();
			oldLabel.translateYProperty().unbind();
			nodeLabelGroup.getChildren().remove(oldLabel);
		}
		var shape=nodeView.shape();
		var newLabel=new RichTextLabel(text);
		newLabel.translateXProperty().bind(shape.translateXProperty());
		newLabel.translateYProperty().bind(shape.translateYProperty());
		newLabel.setLayoutX(dx);
		newLabel.setLayoutX(dy);
		nodeView.setLabel(newLabel);
		nodeLabelGroup.getChildren().add(newLabel);
	}

	public void removeView(Node v) {
		setView(v,null);
	}

	public EdgeView getView(Edge e) {
		return edgeViewMap.get(e);
	}

	public void setView(Edge e, EdgeView edgeView) {
		var oldEdgeView= edgeViewMap.get(e);
		if(oldEdgeView!=null) {
			edgePathGroup.getChildren().removeAll(oldEdgeView.getChildren());
			if(oldEdgeView.label()!=null)
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
		var edgeView = new EdgeView(document, e, source.translateXProperty(), source.translateYProperty(), target.translateXProperty(), target.translateYProperty());
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

	public Node findNodeIfHit(double xScreen, double yScreen,double tolerance) {
		for (var v : tree.nodes()) {
			final var shape = getView(v).shape();
			var bounds=shape.screenToLocal(new BoundingBox(xScreen-0.5*tolerance,yScreen-0.5*tolerance,tolerance,tolerance));
			System.err.println("xScreen: "+xScreen);
			System.err.println("yScreen: "+yScreen);
			System.err.println("bounds: "+bounds);
			System.err.println("shape: "+shape.getLayoutBounds());
			if(shape.intersects(bounds))
				return v;
		}
		return null;
	}

	public void moveNode(Node v, double dx, double dy) {
		var shape=getView(v).shape();
		shape.setTranslateX(shape.getTranslateX()+dx);
		shape.setTranslateY(shape.getTranslateY()+dy);
	}

	public void scale(double xFactor,double yFactor) {
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
		var minX=Double.MAX_VALUE;
		var maxX=Double.MIN_VALUE;
		var minY=Double.MAX_VALUE;
		var maxY=Double.MIN_VALUE;
		for(var v:tree.nodes()) {
			var shape=getView(v).shape();
			if(shape!=null) {
				minX = Math.min(minX, shape.getTranslateX());
				maxX = Math.max(maxX, shape.getTranslateX());
				minY = Math.min(minY, shape.getTranslateY());
				maxY = Math.max(maxY, shape.getTranslateY());
			}
			}
		return new BoundingBox(minX,minY,maxX-minX,maxY-minY);

	}

	public static final class NodeView {
		private  Shape shape;
		private RichTextLabel label;

		public NodeView(Shape shape, RichTextLabel label) {
			this.shape = shape;
			this.label = label;
		}

		public Shape shape() {
			return shape;
		}

		public RichTextLabel label() {
			return label;
		}

		public void setShape(Shape shape) {
			this.shape = shape;
		}

		public void setLabel(RichTextLabel label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return "NodeView[" +
				   "shape=" + shape + ", " +
				   "label=" + label + ']';
		}
	}
}
