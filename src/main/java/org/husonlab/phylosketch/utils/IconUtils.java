/*
 * IconUtils.java Copyright (C) 2023 Daniel H. Huson
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

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import jloda.util.FileUtils;

import java.util.Objects;

/**
 * icon utilities
 * Daniel Huson, 4.2023
 */
public class IconUtils {
	/**
	 * get a icon
	 *
	 * @param clazz    the class to use to get the resource
	 * @param fileName the icon file name
	 * @param height   the height
	 * @return image view or label
	 */
	public static Node getIcon(Class<?> clazz, String fileName, double height) {
		try (var ins = Objects.requireNonNull(clazz.getResourceAsStream(fileName))) {
			var image = new Image(ins);
			var imageView = new ImageView(image);
			imageView.setPreserveRatio(true);
			imageView.setFitHeight(height);
			return imageView;
		} catch (Exception ex) {
			var label = new Label(FileUtils.replaceFileSuffix(fileName, ""));
			label.setPrefHeight(height);
			return label;
		}
	}
}
