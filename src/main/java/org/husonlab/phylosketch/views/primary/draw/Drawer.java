/*
 * Drawer.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.views.primary.draw;

import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import jloda.fx.control.RichTextLabel;
import jloda.graph.NodeArray;
import org.husonlab.phylosketch.unused.model.Document;

public class Drawer {
	private final Document document;
	private final NodeArray<Shape> nodeShapeMap;

	private double xScale =1.0;
	private double yScale =1.0;

	public Drawer (Document document) {
		this.document =document;
		this.nodeShapeMap=document.getTree().newNodeArray();
	}

	public void apply(Pane pane) {
		nodeShapeMap.clear();
		var nodes=new Group();
		var edges=new Group();
		var labels=new Group();
		pane.getChildren().setAll(edges,nodes,labels);
		var tree= document.getTree();
			tree.preorderTraversal(v -> {
				var vShape= craeteNodeShape(document.getPoint(v));
				nodes.getChildren().add(vShape);
				nodeShapeMap.put(v,vShape);
				var p=v.getParent();
				if(p!=null) {
					var pShape=nodeShapeMap.get(p);
					var edgeShape=createEdgeShape(pShape,vShape);
					edges.getChildren().add(edgeShape);
				}
				var nodeLabel=tree.getLabel(v);
				if(nodeLabel!=null && !nodeLabel.isBlank()) {
					var label=new RichTextLabel("<b>"+nodeLabel);
					label.translateXProperty().bind(vShape.translateXProperty().add(10));
					label.translateYProperty().bind(vShape.translateYProperty().subtract(10));
					labels.getChildren().add(label);
				}
			});
	}

	public void scale(double xFactor,double yFactor) {
		if(xFactor<=0 || yFactor<=0)
			throw new IllegalArgumentException();
		this.xScale *=xFactor;
		this.yScale *=yFactor;
		for(var shape:nodeShapeMap.values()) {
			shape.setTranslateX(shape.getTranslateX()*xFactor);
			shape.setTranslateY(shape.getTranslateY()*yFactor);
		}
	}

	public void resetScale() {
		for(var shape:nodeShapeMap.values()) {
			shape.setTranslateX(shape.getTranslateX()/xScale);
			shape.setTranslateY(shape.getTranslateY()/yScale);
		}
		xScale=1.0;
		yScale=1.0;
	}

	private static Shape createEdgeShape(Shape pShape, Shape vShape) {
		var line=new Line();
		line.startXProperty().bind(pShape.translateXProperty());
		line.startYProperty().bind(pShape.translateYProperty());
		line.endXProperty().bind(vShape.translateXProperty());
		line.endYProperty().bind(vShape.translateYProperty());
		return line;
	}

	public static Shape craeteNodeShape(Point2D location) {
		var circle=new Circle(0,0,2);
		circle.setTranslateX(location.getX());
		circle.setTranslateY(location.getY());
		return circle;
	}

	public BoundingBox getBoundingBox() {
		var minX=Double.MAX_VALUE;
		var maxX=Double.MIN_VALUE;
		var minY=Double.MAX_VALUE;
		var maxY=Double.MIN_VALUE;
		for(var shape:nodeShapeMap.values()) {
			minX=Math.min(minX,shape.getTranslateX());
			maxX=Math.max(maxX,shape.getTranslateX());
			minY=Math.min(minY,shape.getTranslateY());
			maxY=Math.max(maxY,shape.getTranslateY());
		}
		return new BoundingBox(minX,minY,maxX-minX,maxY-minY);

	}
}
