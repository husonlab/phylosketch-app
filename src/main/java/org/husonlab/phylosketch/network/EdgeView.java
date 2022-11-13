/*
 * EdgeView.java Copyright (C) 2022. Daniel H. Huson
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

package org.husonlab.phylosketch.network;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import jloda.fx.control.RichTextLabel;
import jloda.fx.util.GeometryUtilsFX;
import jloda.graph.Edge;

/**
 * edge view
 * Daniel Huson, 10.2022
 */
public class EdgeView {
	final private int id;
	final private CubicCurve curve;
	private RichTextLabel label;
	final private Circle circle1;
	final private Circle circle2;
	final private Shape arrowHead;
	private final ObservableList<Node> children;

	/**
	 * constructor
	 */
	public EdgeView(Edge edge, ReadOnlyDoubleProperty aX, ReadOnlyDoubleProperty aY, ReadOnlyDoubleProperty bX, ReadOnlyDoubleProperty bY) {
		curve = new CubicCurve();
		curve.setId("graph-edge");
		curve.setFill(Color.TRANSPARENT);
		curve.setStroke(Color.BLACK);
		curve.setStrokeWidth(1);
		curve.setPickOnBounds(false);
		curve.setStrokeLineCap(StrokeLineCap.ROUND);

		curve.startXProperty().bind(aX);
		curve.startYProperty().bind(aY);
		curve.endXProperty().bind(bX);
		curve.endYProperty().bind(bY);

		circle1 = new Circle(3);
		circle1.setFill(Color.DARKRED);
		circle1.translateXProperty().bindBidirectional(curve.controlX1Property());
		circle1.translateYProperty().bindBidirectional(curve.controlY1Property());
		circle1.setTranslateX(0.7 * curve.getStartX() + 0.3 * curve.getEndX());
		circle1.setTranslateY(0.7 * curve.getStartY() + 0.3 * curve.getEndY());

		circle2 = new Circle(3);
		circle2.setFill(Color.DARKRED);
		circle2.translateXProperty().bindBidirectional(curve.controlX2Property());
		circle2.translateYProperty().bindBidirectional(curve.controlY2Property());
		circle2.setTranslateX(0.3 * curve.getStartX() + 0.7 * curve.getEndX());
		circle2.setTranslateY(0.3 * curve.getStartY() + 0.7 * curve.getEndY());

		id = edge.getId();

		arrowHead = new Polygon(-3, -3, 5, 0, -3, 3);
		arrowHead.setStrokeWidth(curve.getStrokeWidth());
		arrowHead.setFill(curve.getFill());
		arrowHead.setStroke(curve.getStroke());

		arrowHead.strokeWidthProperty().bind(curve.strokeWidthProperty());
		arrowHead.fillProperty().bind(curve.strokeProperty());
		arrowHead.strokeProperty().bind(curve.strokeProperty());

		//arrowHead.setLayoutY(-arrowHead.getStrokeWidth());
		InvalidationListener invalidationListener = (e) -> {
			final var angle = GeometryUtilsFX.computeAngle(new Point2D(curve.getEndX() - curve.getControlX2(), curve.getEndY() - curve.getControlY2()));
			arrowHead.setLayoutX(-0.5 * arrowHead.getStrokeWidth());
			//arrowHead.setLayoutY(-arrowHead.getStrokeWidth());

			arrowHead.setRotationAxis(new Point3D(0, 0, 1));
			arrowHead.setRotate(angle);
			final var location = GeometryUtilsFX.translateByAngle(new Point2D(curve.getEndX(), curve.getEndY()), angle, -15);
			arrowHead.setTranslateX(location.getX());
			arrowHead.setTranslateY(location.getY());
		};

		invalidationListener.invalidated(null);

		curve.controlX2Property().addListener(invalidationListener);
		curve.controlY2Property().addListener(invalidationListener);
		curve.endXProperty().addListener(invalidationListener);
		curve.endYProperty().addListener(invalidationListener);

		label = null;

		children = FXCollections.observableArrayList();
		children.add(curve);
		children.add(arrowHead);
	}

	public ObservableList<Node> getChildren() {
		return children;
	}

	public int getId() {
		return id;
	}

	public CubicCurve getCurve() {
		return curve;
	}

	public RichTextLabel label() {
		return label;
	}

	public void setLabel(RichTextLabel label) {
		this.label = label;
	}

	public Circle getCircle1() {
		return circle1;
	}

	public Circle getCircle2() {
		return circle2;
	}

	public Shape getArrowHead() {
		return arrowHead;
	}

	public void startMoved(double deltaX, double deltaY) {
		final var start = new Point2D(curve.getStartX(), curve.getStartY());
		final var end = new Point2D(curve.getEndX(), curve.getEndY());
		final var newStart = new Point2D(start.getX() + deltaX, start.getY() + deltaY);

		final var oldDistance = start.distance(end);

		if (oldDistance > 0) {
			final var scaleFactor = newStart.distance(end) / oldDistance;
			final var deltaAngle = GeometryUtilsFX.computeObservedAngle(end, start, newStart);


			if (scaleFactor != 0 || deltaAngle != 0) {

				final var oldControl1 = new Point2D(curve.getControlX1(), curve.getControlY1());
				final var newAngle1 = GeometryUtilsFX.computeAngle(oldControl1.subtract(end)) + deltaAngle;

				final var newControl1 = GeometryUtilsFX.translateByAngle(end, newAngle1, scaleFactor * oldControl1.distance(end));
				curve.setControlX1(newControl1.getX());
				curve.setControlY1(newControl1.getY());

				final var oldControl2 = new Point2D(curve.getControlX2(), curve.getControlY2());
				final var newAngle2 = GeometryUtilsFX.computeAngle(oldControl2.subtract(end)) + deltaAngle;

				final var newControl2 = GeometryUtilsFX.translateByAngle(end, newAngle2, scaleFactor * oldControl2.distance(end));
				curve.setControlX2(newControl2.getX());
				curve.setControlY2(newControl2.getY());
			}
		}
	}

	public void endMoved(double deltaX, double deltaY) {
		final var start = new Point2D(curve.getStartX(), curve.getStartY());
		final var end = new Point2D(curve.getEndX(), curve.getEndY());
		final var newEnd = new Point2D(end.getX() + deltaX, end.getY() + deltaY);
		final var oldDistance = end.distance(start);

		if (oldDistance > 0) {
			final var scaleFactor = newEnd.distance(start) / oldDistance;
			final var deltaAngle = GeometryUtilsFX.computeObservedAngle(start, end, newEnd);

			if (scaleFactor != 0 || deltaAngle != 0) {

				final var oldControl1 = new Point2D(curve.getControlX1(), curve.getControlY1());
				final var newAngle1 = GeometryUtilsFX.computeAngle(oldControl1.subtract(start)) + deltaAngle;

				final var newControl1 = GeometryUtilsFX.translateByAngle(start, newAngle1, scaleFactor * oldControl1.distance(start));
				curve.setControlX1(newControl1.getX());
				curve.setControlY1(newControl1.getY());

				final var oldControl2 = new Point2D(curve.getControlX2(), curve.getControlY2());
				final var newAngle2 = GeometryUtilsFX.computeAngle(oldControl2.subtract(start)) + deltaAngle;

				final var newControl2 = GeometryUtilsFX.translateByAngle(start, newAngle2, scaleFactor * oldControl2.distance(start));
				curve.setControlX2(newControl2.getX());
				curve.setControlY2(newControl2.getY());
			}
		}
	}

	public double[] getControlCoordinates() {
		return new double[]{curve.getControlX1(), curve.getControlY1(), curve.getControlX2(), curve.getControlY2()};
	}

	public void setControlCoordinates(double[] coordinates) {
		curve.setControlX1(coordinates[0]);
		curve.setControlY1(coordinates[1]);
		curve.setControlX2(coordinates[2]);
		curve.setControlY2(coordinates[3]);
	}
}
