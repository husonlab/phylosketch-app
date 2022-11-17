/*
 * NodeView.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import jloda.fx.control.RichTextLabel;

/**
 * node view
 * Daniel Huson, 10.2022
 */
public final record NodeView(Shape shape, Shape shapeBelow, RichTextLabel label, Shape labelShapeBelow) {
	/**
	 * constructs a node view, with shapes below
	 *
	 * @param shape the shape representing the node
	 * @param label the label associated with the node
	 */
	public NodeView(Shape shape, RichTextLabel label) {
		this(shape, createShapeBelow(shape), label, createShapeBelow(label));
		shape.setUserData(this);
		shapeBelow.setUserData(this);
		label.setUserData(this);
		labelShapeBelow.setUserData(this);
	}

	public static Shape createShapeBelow(Shape shape) {
		var belowWater = new Rectangle();
		belowWater.setUserData(shape.getUserData());
		belowWater.setStroke(Color.TRANSPARENT);
		belowWater.setFill(Color.WHITE); // todo: make this the current background color
		InvalidationListener invalidationListener = e -> {
			var bounds = shape.getBoundsInLocal();
			belowWater.setWidth(Math.max(20, bounds.getWidth()));
			belowWater.setHeight(Math.max(20, bounds.getHeight()));
			belowWater.setLayoutX(-bounds.getWidth());
			belowWater.setLayoutY(-bounds.getHeight());
		};
		shape.boundsInLocalProperty().addListener(invalidationListener);
		invalidationListener.invalidated(null);
		belowWater.translateXProperty().bind(shape.translateXProperty());
		belowWater.translateYProperty().bind(shape.translateYProperty());
		return belowWater;
	}

	public static Shape createShapeBelow(RichTextLabel label) {
		var belowWater = new Rectangle(Math.max(20, label.getWidth()), Math.max(20, label.getHeight()));
		belowWater.setUserData(label.getUserData());
		belowWater.setStroke(Color.TRANSPARENT);
		belowWater.setFill(Color.WHITE); // todo: make this the current background color
		belowWater.widthProperty().bind(Bindings.createDoubleBinding(() -> Math.max(20, label.getWidth()), label.widthProperty()));
		belowWater.heightProperty().bind(Bindings.createDoubleBinding(() -> Math.max(20, label.getHeight()), label.heightProperty()));
		belowWater.translateXProperty().bind(label.translateXProperty().add(label.layoutXProperty()).subtract(5));
		belowWater.translateYProperty().bind(label.translateYProperty().add(label.layoutYProperty()).subtract(5));
		belowWater.visibleProperty().bind(label.textProperty().isNotEmpty());
		return belowWater;
	}
}
