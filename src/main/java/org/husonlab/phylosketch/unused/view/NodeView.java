/*
 * NodeView.java Copyright (C) 2022. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.husonlab.phylosketch.unused.view;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import jloda.fx.control.RichTextLabel;
import jloda.fx.shapes.ISized;
import jloda.fx.shapes.NodeShape;

public class NodeView {
    private final RichTextLabel label = new RichTextLabel();
    private final Group shapeGroup = new Group();
    private Shape shape;

    /**
     * constructor
     *
	 */
    public NodeView(Font font, double x, double y) {
        changeShape(NodeShape.Circle);

        label.setFont(font);
        label.setTextFill(Color.BLACK);

        shapeGroup.setTranslateX(x);
        shapeGroup.setTranslateY(y);

        {
            final Circle spacer = new Circle(50);
            spacer.setFill(Color.TRANSPARENT);
            spacer.setStroke(Color.TRANSPARENT);
            spacer.setMouseTransparent(true);
            shapeGroup.getChildren().add(spacer);
        }

        label.setLayoutX(10);
        label.setLayoutY(-7);
        label.translateXProperty().bind(translateXProperty());
        label.translateYProperty().bind(translateYProperty());
    }

    public RichTextLabel getLabel() {
        return label;
    }

    public void setLabelAngle(Double angle) {
        label.setRotate(angle);
    }

    public double getLabelAngle() {
        return label.getRotate();
    }

    public Shape getShape() {
        return shape;
    }

    public NodeShape getNodeShape() {
        return NodeShape.valueOf(shape);
    }

    public void changeShape(NodeShape nodeShape) {
        final Shape newShape = NodeShape.create(nodeShape, 10, 10);
        if (shape == null) {
            newShape.setFill(Color.WHITE);
            newShape.setStroke(Color.BLACK);
            newShape.setStrokeWidth(2);
        } else {
            ((ISized) newShape).setSize(((ISized) shape).getWidth(), ((ISized) shape).getHeight());
            newShape.setFill(shape.getFill());
            newShape.setStroke(shape.getStroke());
            newShape.setStrokeWidth(shape.getStrokeWidth());

        }
        shape = newShape;
        shapeGroup.getChildren().setAll(newShape);
    }

    public double getTranslateX() {
        return shapeGroup.getTranslateX();
    }

    public double getTranslateY() {
        return shapeGroup.getTranslateY();
    }

    public void setTranslateX(double x) {
        shapeGroup.setTranslateX(x);
    }

    public void setTranslateY(double y) {
        shapeGroup.setTranslateY(y);
    }

    public DoubleProperty translateXProperty() {
        return shapeGroup.translateXProperty();
    }

    public DoubleProperty translateYProperty() {
        return shapeGroup.translateYProperty();
    }

    public Group getShapeGroup() {
        return shapeGroup;
    }

    public double getWidth() {
        return ((ISized) shape).getWidth();
    }

    public double getHeight() {
        return ((ISized) shape).getHeight();
    }

    public void setWidth(double width) {
        ((ISized) shape).setSize(width, ((ISized) shape).getHeight());
    }

    public void setHeight(double height) {
        ((ISized) shape).setSize(((ISized) shape).getWidth(), height);
    }
}
