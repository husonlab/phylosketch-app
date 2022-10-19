/*
 * NetworkPresenter.java Copyright (C) 2022 Daniel H. Huson
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
import javafx.beans.property.ObjectProperty;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import jloda.fx.control.RichTextLabel;
import jloda.fx.shapes.CircleShape;
import jloda.fx.shapes.SquareShape;
import jloda.fx.undo.UndoManager;
import jloda.fx.util.BasicFX;
import jloda.fx.util.MouseDragClosestNode;
import jloda.fx.util.SelectionEffect;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import org.husonlab.phylosketch.views.primary.PrimaryPresenter;


/**
 * computes network view from model and vice versa
 * Daniel Huson, 10.2022
 */
public class NetworkPresenter {

	public static void setupView(Pane pane, Document document, UndoManager undoManager, ObjectProperty<PrimaryPresenter.Tool> toolProperty) {
		var view=document.getView();
		pane.getChildren().setAll(view.getWorld());
		var nodeSelection=document.getNodeSelection();
		var edgeSelection=document.getEdgeSelection();
		view.setNodeViewAddedCallBack((v, nv)-> NodeMouseInteraction.install(pane, undoManager, view,nodeSelection,edgeSelection,v,nv,toolProperty));

		view.setEdgeViewAddedCallBack((e, ev)->{
				ev.path().setOnMouseClicked(c -> {
					if (!MouseDragClosestNode.wasMoved()) {
						if (!c.isShiftDown()) {
							nodeSelection.clearSelection();
							edgeSelection.clearSelection();
							edgeSelection.select(e);
						} else
							edgeSelection.toggleSelection(e);
					}
					c.consume();
				});
		});

		nodeSelection.getSelectedItems().addListener((SetChangeListener<? super Node>) c->{
			if(c.wasAdded()) {
				var nv=view.getView(c.getElementAdded());
				nv.shape().setEffect(SelectionEffect.getInstance());
				if(nv.label()!=null)
					nv.label().setEffect(SelectionEffect.getInstance());
			} else if(c.wasRemoved()) {
				var nv=view.getView(c.getElementRemoved());
				nv.shape().setEffect(null);
				if(nv.label()!=null)
					nv.label().setEffect(null);
			}
		});

		edgeSelection.getSelectedItems().addListener((SetChangeListener<? super Edge>) c->{
			if(c.wasAdded()) {
				var ev=view.getView(c.getElementAdded());
				ev.path().setEffect(SelectionEffect.getInstance());
				if(ev.label()!=null)
					ev.label().setEffect(SelectionEffect.getInstance());
			} else if(c.wasRemoved()) {
				var ev=view.getView(c.getElementRemoved());
				ev.path().setEffect(null);
				if(ev.label()!=null)
					ev.label().setEffect(null);
			}
		});

		// todo: catch close pressed and dispatch to a shape
		pane.setOnTouchPressed(c->{
			System.err.println("pane touched");
				var a=document.getView().findNodeIfHit(c.getTouchPoint().getScreenX(),c.getTouchPoint().getScreenY(),25);
				if(a!=null) {
					System.err.println("shape dispatched");
					var shape=document.getView().getView(a).shape();
					shape.fireEvent(c.copyFor(c.getSource(),shape));
					c.consume();
				}
			});
	}

	public static void model2view(NetworkModel model, NetworkView view) {
		view.clear();

		try(NodeArray<DoubleProperty> x=model.getTree().newNodeArray();
			NodeArray<DoubleProperty> y=model.getTree().newNodeArray()) {
			for (var v : model.getTree().nodes()) {
				var attributes=model.getAttributes(v);
				var shape=switch (attributes.glyph()) {
					case Square -> new SquareShape(attributes.height(),(Color)attributes.stroke(),(Color)attributes.fill());
					case Circle -> new CircleShape(attributes.height());
				};
				shape.setStroke(attributes.stroke());
				shape.setFill(attributes.fill());
				shape.setTranslateX(attributes.x());
				shape.setTranslateY(attributes.y());
				x.put(v,shape.translateXProperty());
				y.put(v,shape.translateYProperty());

				var nv=new NetworkView.NodeView(shape,null);

				var label=attributes.label();
				if(label!=null) {
					var textLabel=new RichTextLabel(label.text());
					textLabel.translateXProperty().bind(shape.translateXProperty());
					textLabel.translateYProperty().bind(shape.translateYProperty());
					textLabel.setLayoutX(label.dx());
					textLabel.setLayoutY(label.dy());
					textLabel.setRotate(label.angle()); // todo: not sure about this
					nv.setLabel(textLabel);
				}
				view.setView(v,nv);
			}

			for(var e:model.getTree().edges()) {
				var attributes=model.getAttributes(e);
				var v=e.getSource();
				var w=e.getTarget();

				var label=attributes.label();
				var textLabel=(label!=null?new RichTextLabel((attributes.label().text())):null);
				Path path=null;

				switch (attributes.glyph()) {
					case StraightLine ->  {
						var start=new MoveTo();
						start.xProperty().bind(x.get(v));
						start.yProperty().bind(y.get(v));
						var end=new LineTo();
						end.xProperty().bind(x.get(w));
						end.yProperty().bind(y.get(w));
						path=new Path(start,end);
						if(textLabel!=null) {
							textLabel.translateXProperty().bind((start.xProperty().add(end.xProperty()).divide(2)));
							textLabel.translateYProperty().bind((start.xProperty().add(end.yProperty()).divide(2)));
						}
					}
					case RectangleLine -> {
						var start=new MoveTo();
						start.xProperty().bind(x.get(v));
						start.yProperty().bind(y.get(v));
						var mid=new LineTo();
						mid.xProperty().bind(x.get(v));
						mid.yProperty().bind(y.get(w));
						var end=new LineTo();
						end.xProperty().bind(x.get(w));
						end.yProperty().bind(y.get(w));
						path=new Path(start,mid,end);
						if(textLabel!=null) {
							textLabel.translateXProperty().bind(start.xProperty());
							textLabel.translateYProperty().bind((end.yProperty()));
						}
					}
					case QuadCurveLine -> {
						var start=new MoveTo();
						start.xProperty().bind(x.get(v));
						start.yProperty().bind(y.get(v));
						var end=new QuadCurveTo();
						end.controlXProperty().bind(x.get(v));
						end.controlYProperty().bind(y.get(w));
						end.xProperty().bind(x.get(w));
						end.yProperty().bind(y.get(w));
						path=new Path(start,end);
						if(textLabel!=null) {
							textLabel.translateXProperty().bind(start.xProperty());
							textLabel.translateYProperty().bind((end.yProperty()));
						}
					}
				};

				if(path!=null) {
					path.setStroke(attributes.stroke());
					path.setStrokeWidth(attributes.strokeWidth());
					var group=new Group();
					group.getChildren().add(path);
					if(textLabel!=null) {
						textLabel.setLayoutX(label.dx());
						textLabel.setLayoutY(label.dy());
						textLabel.setRotate(attributes.label().angle());
						group.getChildren().add(textLabel);
					}
					var ev=new NetworkView.EdgeView(path,textLabel);
					view.setView(e,ev);
				}
			}
		}
	}

	public static void view2model(NetworkView view,NetworkModel model){
		model.clear();

		for(var v:view.getTree().nodes()) {
			var nv = view.getView(v);
		var shape=nv.shape();
			var textLabel = nv.label();
			var label = textLabel != null ? new NetworkModel.Label(textLabel.getLayoutX(), textLabel.getLayoutY(),textLabel.getRotate(), textLabel.getText()) : null;

			var width = shape.getLayoutBounds().getWidth();
			var height =  shape.getLayoutBounds().getHeight();

			var attributes = new NetworkModel.NodeAttributes(shape.getTranslateX(), shape.getTranslateY(), shape instanceof Rectangle ? NetworkModel.NodeGlyph.Square : NetworkModel.NodeGlyph.Circle, width, height,
					shape.getStroke(), shape.getFill(), label);
				model.setAttributes(v,attributes);
		}

		for(var e:view.getTree().edges()) {
			var ev=view.getView(e);
			var path=ev.path();
			var textLabel = ev.label();
			var label = textLabel != null ? new NetworkModel.Label(textLabel.getLayoutX(), textLabel.getLayoutY(), textLabel.getRotate(), textLabel.getText()) : null;

			var attributes=new NetworkModel.EdgeAttributes(determineEdgeGlyph(path),path.getStrokeWidth(),path.getStroke(),label);
			model.setAttributes(e,attributes);
		}
	}

	public static NetworkModel.EdgeGlyph determineEdgeGlyph(Path path) {
		var elements=path.getElements();
		if(elements.size()==2 && elements.get(1) instanceof QuadCurveTo)
			return NetworkModel.EdgeGlyph.QuadCurveLine;
		else if(elements.size()==3 && elements.get(1) instanceof LineTo && elements.get(2) instanceof LineTo)
			return NetworkModel.EdgeGlyph.RectangleLine;
		else
			return NetworkModel.EdgeGlyph.StraightLine;
	}
}
