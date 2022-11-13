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

package org.husonlab.phylosketch.deprecated;

import javafx.geometry.BoundingBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;
import jloda.fx.util.BasicFX;
import jloda.util.CollectionUtils;
import jloda.util.Single;

import java.util.function.Function;

/**
 * some utilities for touch events
 * Daniel Huson, 10.2022
 */
@Deprecated
public class TouchUtils {
	/**
	 * redirect touch event to a shape contained in the pane
	 *
	 * @param pane      the pane
	 * @param tolerance max x and y distance from shape borders
	 */
	public static void redirectTouchEventsToClosestShape(Pane pane, Function<Shape, Boolean> allow, double tolerance) {
		var touchId = new Single<>(-1);
		var active = new Single<Shape>();

		pane.setOnTouchPressed(a -> {
			active.set(null);
			touchId.set(-1);
			if (a.getTarget() == pane && a.getTouchCount() == 1) {

				System.out.println("pane touched");
				System.out.println("count: " + a.getTouchCount());
				System.out.println("source: " + a.getSource());
				System.out.println("target: " + a.getTarget());

				touchId.set(a.getTouchPoint().getId());

				for (var shape : CollectionUtils.reverse(BasicFX.getAllRecursively(pane, Shape.class))) {
					if (allow.apply(shape) && hit(a.getTouchPoint().getScreenX(), a.getTouchPoint().getScreenY(), shape, tolerance)) {
						active.set(shape);
						break;
					}
				}
				if (active.isNotNull()) {
					System.out.println("transferring to: " + active.get());
					var copy = a.copyFor(null, active.get());
					a.consume();
					try {
						active.get().fireEvent(copy);
					} finally {
						copy.consume();
					}
				}
				a.consume();
			}
		});
		pane.setOnTouchMoved(a -> {
			if (a.getTouchPoint().getId() == touchId.get() && active.isNotNull()) {
				var copy = a.copyFor(null, active.get());
				a.consume();
				try {
					System.out.println("firing: " + copy);
					active.get().fireEvent(copy);
				} finally {
					copy.consume();
				}
			}
		});

		pane.setOnTouchReleased(a -> {
			if (a.getTouchPoint().getId() == touchId.get() && active.isNotNull()) {
				var copy = a.copyFor(null, active.get());
				a.consume();
				try {
					System.out.println("firing: " + copy);
					active.get().fireEvent(copy);
				} finally {
					copy.consume();
				}
			}
		});
	}

	public static boolean hit(double xScreen, double yScreen, Shape shape, double tolerance) {
		var bounds = shape.screenToLocal(new BoundingBox(xScreen - 0.5 * tolerance, yScreen - 0.5 * tolerance, tolerance, tolerance));
		return shape.intersects(bounds);
	}
}
