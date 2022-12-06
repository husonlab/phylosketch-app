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
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public class PrimaryController {

	@FXML
	private AnchorPane anchorPane;

	@FXML
	private RadioMenuItem arrowBothRadioMenuItem;

	@FXML
	private RadioMenuItem arrowLeftRadioMenuItem;

	@FXML
	private Menu arrowMenu;

	@FXML
	private RadioMenuItem arrowNoneRadioMenuItem;

	@FXML
	private RadioMenuItem arrowRightRadioMenuItem;

	@FXML
	private ColorPicker borderColorPicker;

	@FXML
	private Menu edgeShapeMenu;

	@FXML
	private RadioMenuItem editLabelMenuItem;

	@FXML
	private RadioMenuItem editMenuItem;

	@FXML
	private RadioMenuItem eraseMenuItem;

	@FXML
	private ColorPicker fontColorPicker;

	@FXML
	private MenuItem fontMenuItem;

	@FXML
	private Label modeLabel;

	@FXML
	private MenuButton modeMenuButton;

	@FXML
	private RadioMenuItem moveMenuItem;

	@FXML
	private RadioMenuItem panMenuItem;

	@FXML
	private View primary;

	@FXML
	private RadioMenuItem rectangularEdgesRadioMenuItem;

	@FXML
	private Button redoButton;

	@FXML
	private Button resetButton;

	@FXML
	private RadioMenuItem roundEdgesRadioMenuItem;

	@FXML
	private ScrollPane scrollPane;

	@FXML
	private ToggleButton showNewickToggleButton;

	@FXML
	private Slider sizeSlider;

	@FXML
	private StackPane stackPane;

	@FXML
	private RadioMenuItem straightEdgesRadioMenuItem;

	@FXML
	private MenuButton styleMenuButton;

	@FXML
	private Button importButton;

	@FXML
	private TextField infoTextField;

	@FXML
	private TextField newickTextField;
	@FXML
	private Button undoButton;

	@FXML
	private Slider widthSlider;

	@FXML
	private Button increaseFontSizeButton;

	@FXML
	private Button decreaseFontSizeButton;

	private final ToggleGroup modeToggleGroup = new ToggleGroup();
	private final ToggleGroup edgeShapeToggleGroup = new ToggleGroup();
	private final ToggleGroup arrowTypeToggleGroup = new ToggleGroup();


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

		for (var item : modeMenuButton.getItems()) {
			if (item instanceof RadioMenuItem radioMenuItem) {
				modeToggleGroup.getToggles().add(radioMenuItem);
			}
		}

		edgeShapeToggleGroup.getToggles().addAll(straightEdgesRadioMenuItem, rectangularEdgesRadioMenuItem, roundEdgesRadioMenuItem);

		arrowTypeToggleGroup.getToggles().addAll(arrowNoneRadioMenuItem, arrowRightRadioMenuItem, arrowLeftRadioMenuItem, arrowBothRadioMenuItem);

		// don't allow size 0
		widthSlider.valueChangingProperty().addListener((v, o, n) -> {
			if (!n && widthSlider.getValue() == 0)
				Platform.runLater(() -> widthSlider.setValue(1));
		});

		// don't allow size 0
		sizeSlider.valueChangingProperty().addListener((v, o, n) -> {
			if (!n && sizeSlider.getValue() == 0)
				Platform.runLater(() -> sizeSlider.setValue(1));
		});

		//infoTextField.setStyle("-fx-text-fill: white; -fx-background-color: -primary-swatch-500;");


		showNewickToggleButton.setSelected(false);
		newickTextField.setPrefHeight(0);
		newickTextField.setVisible(false);

		showNewickToggleButton.selectedProperty().addListener((v, o, n) -> {
			newickTextField.setPrefHeight(n ? 40 : 0);
			newickTextField.setVisible(n);
			importButton.setVisible(n);
		});
		importButton.setVisible(false);
	}


	public View getPrimary() {
		return primary;
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

	public MenuButton getModeMenuButton() {
		return modeMenuButton;
	}

	public ToggleGroup getModeToggleGroup() {
		return modeToggleGroup;
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

	public ToggleButton getShowNewickToggleButton() {
		return showNewickToggleButton;
	}

	public TextField getNewickTextField() {
		return newickTextField;
	}

	public AnchorPane getAnchorPane() {
		return anchorPane;
	}

	public ToggleGroup getEdgeShapeToggleGroup() {
		return edgeShapeToggleGroup;
	}

	public MenuButton getStyleMenuButton() {
		return styleMenuButton;
	}

	public RadioMenuItem getRectangularEdgesRadioMenuItem() {
		return rectangularEdgesRadioMenuItem;
	}

	public RadioMenuItem getRoundEdgesRadioMenuItem() {
		return roundEdgesRadioMenuItem;
	}

	public RadioMenuItem getStraightEdgesRadioMenuItem() {
		return straightEdgesRadioMenuItem;
	}

	public RadioMenuItem getArrowNoneRadioMenuItem() {
		return arrowNoneRadioMenuItem;
	}

	public RadioMenuItem getArrowRightRadioMenuItem() {
		return arrowRightRadioMenuItem;
	}

	public RadioMenuItem getArrowLeftRadioMenuItem() {
		return arrowLeftRadioMenuItem;
	}

	public RadioMenuItem getArrowBothRadioMenuItem() {
		return arrowBothRadioMenuItem;
	}

	public ToggleGroup getArrowTypeToggleGroup() {
		return arrowTypeToggleGroup;
	}

	public Menu getArrowMenu() {
		return arrowMenu;
	}

	public ColorPicker getBorderColorPicker() {
		return borderColorPicker;
	}

	public Menu getEdgeShapeMenu() {
		return edgeShapeMenu;
	}

	public ColorPicker getFontColorPicker() {
		return fontColorPicker;
	}

	public MenuItem getFontMenuItem() {
		return fontMenuItem;
	}

	public Slider getSizeSlider() {
		return sizeSlider;
	}

	public Slider getWidthSlider() {
		return widthSlider;
	}

	public Button getImportButton() {
		return importButton;
	}

	public TextField getInfoTextField() {
		return infoTextField;
	}

	public Button getIncreaseFontSizeButton() {
		return increaseFontSizeButton;
	}

	public Button getDecreaseFontSizeButton() {
		return decreaseFontSizeButton;
	}
}
