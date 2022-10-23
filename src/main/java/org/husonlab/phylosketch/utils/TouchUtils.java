/*
 * TouchUtils.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.utils;

import javafx.geometry.BoundingBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;
import jloda.fx.util.BasicFX;
import jloda.util.CollectionUtils;
import jloda.util.Single;

/**
 * some utilities for touch events
 * Daniel Huson, 10.2022
 */
public class TouchUtils {
	/**
	 * redirect touch event to a shape contained in the pane
	 *
	 * @param pane      the pane
	 * @param tolerance max x and y distance from shape borders
	 */
	public static void redirectTouchEventsToClosestShape(Pane pane, double tolerance) {
		var touchCount = new Single<>(0);
		var active = new Single<Shape>();

		pane.setOnTouchPressed(c -> {
			active.set(null);
			if (c.getTarget() == pane) {
				/*
				System.err.println("pane touched");
				System.err.println("count: " + c.getTouchCount());
				System.err.println("source: " + c.getSource());
				System.err.println("target: " + c.getTarget());
				 */

				touchCount.set(c.getTouchCount());

				if (touchCount.get() == 1) {
					for (var shape : CollectionUtils.reverse(BasicFX.getAllRecursively(pane, Shape.class))) {
						if (hit(c.getTouchPoint().getScreenX(), c.getTouchPoint().getScreenY(), shape, tolerance)) {
							active.set(shape);
							break;
						}
					}
					if (active.isNotNull()) {
						active.get().fireEvent(c.copyFor(active.get(), null));
						c.consume();
					}
				}
			}
		});
		pane.setOnTouchMoved(c -> {
			if (active.isNotNull()) {
				active.get().fireEvent(c.copyFor(active.get(), null));
				c.consume();
			}
		});
		pane.setOnTouchReleased(c -> {
			if (active.isNotNull()) {
				active.get().fireEvent(c.copyFor(active.get(), null));
				c.consume();
			}
		});
	}

	public static boolean hit(double xScreen, double yScreen, Shape shape, double tolerance) {
		var bounds = shape.screenToLocal(new BoundingBox(xScreen - 0.5 * tolerance, yScreen - 0.5 * tolerance, tolerance, tolerance));
		return shape.intersects(bounds);
	}
}
