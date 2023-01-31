/*
 * SelectionEffect.java Copyright (C) 2022 Daniel H. Huson
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

import com.gluonhq.charm.glisten.visual.SwatchElement;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import org.husonlab.phylosketch.Main;

public class SelectionEffect extends DropShadow {
	private static SelectionEffect instance;

	public static SelectionEffect getInstance() {
		if (instance == null) {
			instance = new SelectionEffect();
		}

		return instance;
	}

	private SelectionEffect() {
		super(BlurType.THREE_PASS_BOX, Main.DEFAULT_SWATCH.getColor(SwatchElement.PRIMARY_200), 3.0D, 2.0D, 0.0D, 0.0D);
	}

	public static DropShadow create(Color color) {
		DropShadow dropShadow = new DropShadow();
		dropShadow.setColor(color);
		dropShadow.setRadius(3.0D);
		dropShadow.setSpread(2.0D);
		return dropShadow;
	}
}