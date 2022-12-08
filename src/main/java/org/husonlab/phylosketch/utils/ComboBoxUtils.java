/*
 * ComboBoxUtils.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyEvent;
import jloda.util.Single;

public class ComboBoxUtils {

	public static void installTypeToSearch(ComboBox<String> comboBox) {
		var text = new Single<>("");

		comboBox.addEventFilter(KeyEvent.KEY_TYPED, e -> {
			if (e.getCharacter().length() > 0) {
				var ch = e.getCharacter().charAt(0);
				if (Character.isLetterOrDigit(ch) || ch == ' ') {
					text.set(text.get() + ch);
					for (var item : comboBox.getItems()) {
						if (item.toLowerCase().startsWith(text.get())) {
							comboBox.setEditable(true);
							comboBox.getButtonCell().setText(item);
							comboBox.setEditable(false);
							if (false) comboBox.setValue(item);
							break;
						}
					}
				}
			} else
				text.set("");
		});
		comboBox.focusedProperty().addListener(e -> text.set(""));
	}
}
