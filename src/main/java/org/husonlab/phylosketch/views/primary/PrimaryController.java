/*
 * PrimaryController.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.views.primary;

import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public class PrimaryController {

	@FXML
	private View primary;


	@FXML
	private Label modeLabel;

	@FXML
	private TextField newickTextField;


	@FXML
	private TextField propertiesTextField;

	@FXML
	private Button showNewickButton;

	@FXML
	private Pane mainPane;


	@FXML
	private Button redoButton;

	@FXML
	private Button undoButton;

	@FXML
	private ScrollPane scrollPane;

	@FXML
	private StackPane stackPane;

	@FXML
	private Button resetButton;

	@FXML
	private MenuButton menuButton;

	private final ToggleGroup toggles = new ToggleGroup();

	@FXML
	private RadioMenuItem editLabelMenuItem;

	@FXML
	private RadioMenuItem editMenuItem;

	@FXML
	private RadioMenuItem eraseMenuItem;

	@FXML
	private RadioMenuItem moveMenuItem;

	@FXML
	private RadioMenuItem panMenuItem;

	@FXML
	private void initialize() {
		primary.getStylesheets().add(Objects.requireNonNull(PrimaryController.class.getResource("primary.css")).toExternalForm());

		primary.showingProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue) {
				var appBar = AppManager.getInstance().getAppBar();
				appBar.setNavIcon(MaterialDesignIcon.MENU.button(e ->
						AppManager.getInstance().getDrawer().open()));
				appBar.setTitleText("PhyloSketch");
				appBar.getActionItems().add(MaterialDesignIcon.SEARCH.button(e ->
						System.err.println("Search")));
			}
		});
		scrollPane.viewportBoundsProperty().addListener((v, o, n) -> {
			stackPane.setMinSize(n.getWidth(), n.getHeight());
		});

		for (var item : menuButton.getItems()) {
			if (item instanceof RadioMenuItem radioMenuItem) {
				toggles.getToggles().add(radioMenuItem);
			}
		}
		toggles.selectedToggleProperty().addListener((v, o, n) -> {

		});
	}

	public View getPrimary() {
		return primary;
	}

	public TextField getNewickTextField() {
		return newickTextField;
	}

	public Button getShowNewickButton() {
		return showNewickButton;
	}

	public Pane getMainPane() {
		return mainPane;
	}

	public Button getRedoButton() {
		return redoButton;
	}

	public Button getUndoButton() {
		return undoButton;
	}

	public ScrollPane getScrollPane() {
		return scrollPane;
	}

	public StackPane getStackPane() {
		return stackPane;
	}

	public Button getResetButton() {
		return resetButton;
	}

	public Label getModeLabel() {
		return modeLabel;
	}

	public TextField getPropertiesTextField() {
		return propertiesTextField;
	}

	public MenuButton getMenuButton() {
		return menuButton;
	}

	public ToggleGroup getToggles() {
		return toggles;
	}

	public RadioMenuItem getEditLabelMenuItem() {
		return editLabelMenuItem;
	}

	public RadioMenuItem getEditMenuItem() {
		return editMenuItem;
	}

	public RadioMenuItem getEraseMenuItem() {
		return eraseMenuItem;
	}

	public RadioMenuItem getMoveMenuItem() {
		return moveMenuItem;
	}

	public RadioMenuItem getPanMenuItem() {
		return panMenuItem;
	}
}
