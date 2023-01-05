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
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.SetChangeListener;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import jloda.fx.control.RichTextLabel;
import jloda.fx.shapes.CircleShape;
import jloda.fx.shapes.SquareShape;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import org.husonlab.phylosketch.network.interaction.EdgeShapeInteraction;
import org.husonlab.phylosketch.network.interaction.LabelEditingManager;
import org.husonlab.phylosketch.network.interaction.NodeLabelInteraction;
import org.husonlab.phylosketch.network.interaction.NodeShapeInteraction;
import org.husonlab.phylosketch.utils.SelectionEffect;
import org.husonlab.phylosketch.views.primary.InteractionMode;


/**
 * computes network view from model and vice versa
 * Daniel Huson, 10.2022
 */
public class NetworkPresenter {
	public static DoubleProperty DEFAULT_FONT_SIZE = new SimpleDoubleProperty(RichTextLabel.getDefaultFont().getSize());

	static {
		DEFAULT_FONT_SIZE.addListener((v, o, n) -> {
			RichTextLabel.setDefaultFont(new Font(RichTextLabel.getDefaultFont().getName(), n.doubleValue()));
		});
	}

	public static void setupView(Pane pane, Document document, ObjectProperty<InteractionMode> modeProperty) {
		var networkView = document.getNetworkView();
		pane.getChildren().setAll(networkView.getWorld());
		var nodeSelection = document.getNodeSelection();
		var edgeSelection = document.getEdgeSelection();

		var labelEditingManager = new LabelEditingManager(networkView, nodeSelection, document.getUndoManager());
		modeProperty.addListener(a -> labelEditingManager.finishEditing());

		networkView.setNodeViewAddedCallback(v -> {
			NodeLabelInteraction.install(labelEditingManager, document, v, modeProperty);
			// todo: need to edit this method
			NodeShapeInteraction.install(labelEditingManager, document, v, modeProperty);
		});

		networkView.setEdgeViewAddedCallback(e -> EdgeShapeInteraction.apply(document, e, modeProperty));

		nodeSelection.getSelectedItems().addListener((SetChangeListener<? super Node>) c -> {
			if (c.wasAdded()) {
				var nv = networkView.getView(c.getElementAdded());
				nv.shape().setEffect(SelectionEffect.getInstance());
				if (nv.label() != null)
					nv.label().setEffect(SelectionEffect.getInstance());
			} else if (c.wasRemoved()) {
				var nv = networkView.getView(c.getElementRemoved());
				if (nv != null) {
					nv.shape().setEffect(null);
					if (nv.label() != null)
						nv.label().setEffect(null);
				}
			}
		});

		edgeSelection.getSelectedItems().addListener((SetChangeListener<? super Edge>) c -> {
			if (c.wasAdded()) {
				var ev = networkView.getView(c.getElementAdded());
				ev.curve().setEffect(SelectionEffect.getInstance());
				if (ev.label() != null)
					ev.label().setEffect(SelectionEffect.getInstance());
			} else if (c.wasRemoved()) {
				var ev = networkView.getView(c.getElementRemoved());
				if (ev != null) {
					ev.curve().setEffect(null);
					if (ev.label() != null)
						ev.label().setEffect(null);
				}
			}
		});
	}

	public static void model2view(NetworkModel model, NetworkView networkView) {
		networkView.clear();
		var tree = model.getTree();

		try (NodeArray<DoubleProperty> x = tree.newNodeArray();
			 NodeArray<DoubleProperty> y = tree.newNodeArray()) {
			for (var v : tree.nodes()) {
				var attributes = model.getAttributes(v);
				var size = attributes.height() != null ? attributes.height() : DefaultOptions.getNodeSize();
				var fill = attributes.fill() != null ? attributes.fill() : DefaultOptions.getNodeColor();
				var stroke = attributes.stroke() != null ? attributes.stroke() : DefaultOptions.getNodeColor();
				Shape shape;
				switch (attributes.glyph()) {
					case Square:
						shape = new SquareShape(size);
						break;
					case Circle:
						shape = new CircleShape(size);
						break;
					default:
						throw new IllegalArgumentException();
				}
				shape.setId("graph-node");
				shape.setStroke(stroke);
				shape.setFill(fill);
				shape.setTranslateX(attributes.x());
				shape.setTranslateY(attributes.y());
				x.put(v, shape.translateXProperty());
				y.put(v, shape.translateYProperty());

				var label = attributes.label();
				var text = label.text();
				var textLabel = new RichTextLabel(text != null ? text : "");
				textLabel.translateXProperty().bind(shape.translateXProperty());
				textLabel.translateYProperty().bind(shape.translateYProperty());
				textLabel.setLayoutX(label.dx());
				textLabel.setLayoutY(label.dy());
				textLabel.setRotate(label.angle()); // todo: not sure about this
				var nv = new NodeView(shape, textLabel);
				networkView.setView(v, nv);
			}

			for (var e : tree.edges()) {
				var attributes = model.getAttributes(e);
				var controlPoints = networkView.computeControlPoints(e, attributes.glyph());
				var ev = networkView.createEdgeView(e);
				ev.setStrokeWidth(attributes.strokeWidth() != null ? attributes.strokeWidth() : DefaultOptions.getEdgeWidth());
				if (attributes.stroke() != null)
					ev.setStroke(attributes.stroke());
				else if (tree.isTreeEdge(e) || tree.isTransferAcceptorEdge(e))
					ev.setStroke(DefaultOptions.getEdgeColor());
				else
					ev.setStroke(DefaultOptions.getReticulateColor());

				ev.getCircle1().setTranslateX(controlPoints[0]);
				ev.getCircle1().setTranslateY(controlPoints[1]);
				ev.getCircle2().setTranslateX(controlPoints[2]);
				ev.getCircle2().setTranslateY(controlPoints[3]);
				var label = attributes.label();
				if (label != null)
					networkView.addLabel(e, label.text(), label.dx(), label.dy());
			}
		}
	}

	public static void view2model(NetworkView view, NetworkModel model) {
		model.clear();

		for (var v : view.getTree().nodes()) {
			var nv = view.getView(v);
			var shape = nv.shape();
			var textLabel = nv.label();
			var label = new NetworkModel.Label(textLabel.getLayoutX(), textLabel.getLayoutY(), textLabel.getRotate(), textLabel.getText());

			var width = shape.getLayoutBounds().getWidth();
			var height = shape.getLayoutBounds().getHeight();

			var attributes = new NetworkModel.NodeAttributes(shape.getTranslateX(), shape.getTranslateY(), shape instanceof Rectangle ? NetworkModel.NodeGlyph.Square : NetworkModel.NodeGlyph.Circle, width, height,
					shape.getStroke(), shape.getFill(), label);
			model.setAttributes(v, attributes);
		}

		for (var e : view.getTree().edges()) {
			var ev = view.getView(e);
			var curve = ev.curve();
			var textLabel = ev.label();
			var label = textLabel != null ? new NetworkModel.Label(textLabel.getLayoutX(), textLabel.getLayoutY(), textLabel.getRotate(), textLabel.getText()) : null;

			var attributes = new NetworkModel.EdgeAttributes(NetworkModel.EdgeGlyph.StraightLine, curve.getStrokeWidth(), curve.getStroke(), label);
			model.setAttributes(e, attributes);
		}
	}
}
