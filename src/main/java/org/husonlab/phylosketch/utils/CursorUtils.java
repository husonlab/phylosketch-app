/*
 * CursorUtils.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.scene.ImageCursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * cursor utils
 */
public class CursorUtils {
	/**
	 * creates an eraser cursor
	 *
	 * @return eraser cursor
	 */
	public static ImageCursor createEraserCursor() {
		var shape = new Rectangle(10, 10);
		shape.setStrokeWidth(0.5);
		shape.setFill(Color.WHITE.deriveColor(1, 1, 1, 0.5));
		shape.setStroke(Color.BLACK);
		var image = shape.snapshot(null, null);
		return new ImageCursor(image, 5, 5);
	}
}
