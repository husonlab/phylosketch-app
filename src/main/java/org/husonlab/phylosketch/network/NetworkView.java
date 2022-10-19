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
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;
import jloda.fx.control.RichTextLabel;
import jloda.fx.shapes.CircleShape;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloTree;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * network view
 * Daniel Huson, 10.2022
 */
public class NetworkView {
	private final Group edgePathGroup=new Group();
	private final Group nodeShapeGroup=new Group();
	private final Group edgeLabelGroup=new Group();
	private final Group nodeLabelGroup=new Group();
	private final Group world=new Group(edgePathGroup,nodeShapeGroup,edgeLabelGroup,nodeLabelGroup);

	private final PhyloTree tree;
	private final NodeArray<NodeView> nodeViewMap;
	private final EdgeArray<EdgeView> edgeViewMap;

	private double xScale=1.0;
	private double yScale=1.0;

	private BiConsumer<Node,NodeView> nodeGroupAddedCallBack=(a,g)->{};
	private BiConsumer<Node,NodeView> nodeViewRemovedCallBack =(a, g)->{};

	private BiConsumer<Edge,EdgeView> edgeViewAddedCallBack =(a, g)->{};
	private BiConsumer<Edge,EdgeView> edgeViewRemovedCallBack =(a, g)->{};

	private Function<Node,Shape> createNewNodeShape = v-> {
		var shape=new CircleShape(8);
		shape.setStroke(Color.BLACK);
		shape.setFill(Color.WHITE);
		return shape;
	};

	private Function<Edge,Path> createNewEdgePath = e->{
		var start=new MoveTo();
		start.xProperty().bind(getView(e.getSource()).shape().translateXProperty());
		start.yProperty().bind(getView(e.getSource()).shape().translateYProperty());
		var end=new LineTo();
		end.xProperty().bind(getView(e.getTarget()).shape().translateXProperty());
		end.yProperty().bind(getView(e.getTarget()).shape().translateYProperty());
		return new Path(start,end);
	};

	public NetworkView(PhyloTree tree) {
		this.tree = tree;
		nodeViewMap =tree.newNodeArray();
		edgeViewMap = tree.newEdgeArray();
	}

	public void clear() {
		for(var v:tree.nodes())
			removeView(v);
		for(var e:tree.edges()) {
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
				nodeViewMap.remove(v);
				nodeViewRemovedCallBack.accept(v,oldNodeView);
			}
		if(nodeView!=null) {
			if(nodeView.shape()!=null)
				nodeShapeGroup.getChildren().add(nodeView.shape());
			if(nodeView.label()!=null)
				nodeLabelGroup.getChildren().add(nodeView.label());
			nodeViewMap.put(v,nodeView);
			nodeGroupAddedCallBack.accept(v, nodeView);
		}
	}

	public void createShape(Node v, double x, double y) {
		var shape=createNewNodeShape.apply(v);
		shape.setTranslateX(x);
		shape.setTranslateY(y);
		setView(v,new NodeView(shape,null));
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
			if(oldEdgeView.path()!=null)
				edgePathGroup.getChildren().remove(oldEdgeView.path());
			if(oldEdgeView.label()!=null)
				edgeLabelGroup.getChildren().remove(oldEdgeView.label());
			edgeViewMap.remove(e);
			edgeViewRemovedCallBack.accept(e,oldEdgeView);
		}
		if(edgeView!=null) {
			if(edgeView.path()!=null)
				edgePathGroup.getChildren().add(edgeView.path());
			if(edgeView.label()!=null)
				edgeLabelGroup.getChildren().add(edgeView.label());
			edgeViewMap.put(e, edgeView);
			edgeViewAddedCallBack.accept(e, edgeView);
		}
	}

	public void createPath(Edge e) {
		var path=createNewEdgePath.apply(e);
		setView(e,new EdgeView(path,null));
	}

	public void addLabel(Edge e, String text, double dx, double dy) {
		var edgeView= getView(e);
		if(edgeView.label()!=null) {
			edgeView.label().translateXProperty().unbind();
			edgeView.label().translateYProperty().unbind();
			edgeLabelGroup.getChildren().remove(edgeView.label());
		}
		var path=edgeView.path();
		var textLabel=new RichTextLabel(text);
		textLabel.translateXProperty().bind(path.translateXProperty());
		textLabel.translateYProperty().bind(path.translateYProperty());
		textLabel.setLayoutX(dx);
		textLabel.setLayoutX(dy);
		edgeView.setLabel(textLabel);
		edgeLabelGroup.getChildren().add(textLabel);
	}


	public void removeView(Edge e) {
		setView(e,null);
	}

	public void setNodeViewAddedCallBack(BiConsumer<Node, NodeView> nodeGroupAddedCallBack) {
		this.nodeGroupAddedCallBack = nodeGroupAddedCallBack;
	}

	public void setNodeViewRemovedCallBack(BiConsumer<Node, NodeView> nodeGroupRemovedCallBack) {
		this.nodeViewRemovedCallBack = nodeGroupRemovedCallBack;
	}

	public void setEdgeViewAddedCallBack(BiConsumer<Edge, EdgeView> edgeViewAddedCallBack) {
		this.edgeViewAddedCallBack = edgeViewAddedCallBack;
	}

	public void setEdgeViewRemovedCallBack(BiConsumer<Edge, EdgeView> edgeViewRemovedCallBack) {
		this.edgeViewRemovedCallBack = edgeViewRemovedCallBack;
	}

	public void setCreateNewNodeShape(Function<Node, Shape> createNewNodeShape) {
		this.createNewNodeShape = createNewNodeShape;
	}

	public void setCreateNewEdgePath(Function<Edge, Path> createNewEdgePath) {
		this.createNewEdgePath = createNewEdgePath;
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
		if(xFactor<=0 || yFactor<=0)
			throw new IllegalArgumentException();
		this.xScale *=xFactor;
		this.yScale *=yFactor;
		for(var v:tree.nodes()) {
			var shape=getView(v).shape();
			shape.setTranslateX(shape.getTranslateX()*xFactor);
			shape.setTranslateY(shape.getTranslateY()*yFactor);
		}
	}

	public void resetScale() {
		for(var v:tree.nodes()) {
			var shape=getView(v).shape();
			shape.setTranslateX(shape.getTranslateX()/xScale);
			shape.setTranslateY(shape.getTranslateY()/yScale);
		}
		xScale=1.0;
		yScale=1.0;
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

	public static final class EdgeView {
		private  Path path;
		private  RichTextLabel label;

		public EdgeView(Path path, RichTextLabel label) {
			this.path = path;
			this.label = label;
		}

		public Path path() {
			return path;
		}

		public RichTextLabel label() {
			return label;
		}

		public void setPath(Path path) {
			this.path = path;
		}

		public void setLabel(RichTextLabel label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return "EdgeView[" +
				   "path=" + path + ", " +
				   "label=" + label + ']';
		}
	}
}
