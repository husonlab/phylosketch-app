/*
 * LabelEditorController.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.views.labeleditor;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class LabelEditorController {

	@FXML
	private AnchorPane rootPane;
	@FXML
	private Button doneButton;

	@FXML
	private Button downButton;

	@FXML
	private Button leftButton;

	@FXML
	private Button rightButton;

	@FXML
	private TextField textField;

	@FXML
	private Button upButton;

	public AnchorPane getRootPane() {
		return rootPane;
	}

	public Button getDoneButton() {
		return doneButton;
	}

	public Button getDownButton() {
		return downButton;
	}

	public Button getLeftButton() {
		return leftButton;
	}

	public Button getRightButton() {
		return rightButton;
	}

	public TextField getTextField() {
		return textField;
	}

	public Button getUpButton() {
		return upButton;
	}
}
