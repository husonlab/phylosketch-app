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
import javafx.scene.layout.VBox;
import javafx.util.converter.DoubleStringConverter;
import jloda.fx.control.RichTextLabel;
import jloda.util.Single;

import java.util.Objects;

public class PrimaryController {

	@FXML
	private AnchorPane anchorPane;

	@FXML
	private Menu arrowMenu;

	@FXML
	private RadioMenuItem arrowNoneRadioMenuItem;

	@FXML
	private RadioMenuItem arrowRightRadioMenuItem;

	@FXML
	private ColorPicker lineColorPicker;

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
	private ToggleButton boldToggleButton;

	@FXML
	private ToggleButton italicToggleButton;

	@FXML
	private ToggleButton underlineToggleButton;

	@FXML
	private ComboBox<String> fontComboBox;

	@FXML
	private TextField fontSizeTextField;


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
	private Button undoButton;

	@FXML
	private Slider widthSlider;

	@FXML
	private Button increaseFontSizeButton;

	@FXML
	private Button decreaseFontSizeButton;

	@FXML
	private SplitPane splitPane;

	@FXML
	private TextArea newickTextArea;

	@FXML
	private VBox vBox;

	@FXML
	private ToolBar fontToolBar;

	@FXML
	private Button closeFontButton;

	@FXML
	private CheckMenuItem showFontsCheckMenuItem;

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

		arrowTypeToggleGroup.getToggles().addAll(arrowNoneRadioMenuItem, arrowRightRadioMenuItem);

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


		var dividerPos = new Single<>(0.1);
		primary.heightProperty().addListener((v, o, n) -> {
			if (o.doubleValue() > 0) {
				dividerPos.set(dividerPos.get() / o.doubleValue() * n.doubleValue());
				splitPane.setDividerPositions(newickTextArea.isVisible() ? dividerPos.get() : 0);
			}
		});

		showNewickToggleButton.setSelected(false);

		newickTextArea.setVisible(false);
		splitPane.setDividerPositions(0.0);

		showNewickToggleButton.selectedProperty().addListener((v, o, n) -> {
			splitPane.setDividerPositions(n ? dividerPos.get() : 0);
			newickTextArea.setMinHeight(n ? 60 : 0);
			newickTextArea.setVisible(n);
			importButton.setVisible(n);
		});
		importButton.setVisible(false);

		fontSizeTextField.setTextFormatter(new TextFormatter<>(new DoubleStringConverter()));
		fontSizeTextField.setText(String.valueOf(RichTextLabel.DEFAULT_FONT.getSize()));

		vBox.getChildren().remove(fontToolBar);

		showFontsCheckMenuItem.selectedProperty().addListener((v, o, n) -> {
			if (n) {
				if (!vBox.getChildren().contains(fontToolBar))
					vBox.getChildren().add(1, fontToolBar);
			} else
				vBox.getChildren().remove(fontToolBar);
		});

		closeFontButton.setOnAction(a -> showFontsCheckMenuItem.setSelected(false));

		newickTextArea.setStyle("-fx-font-family: 'Courier New';");

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

	public TextArea getNewickTextArea() {
		return newickTextArea;
	}

	public ToggleGroup getEdgeShapeToggleGroup() {
		return edgeShapeToggleGroup;
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

	public ToggleGroup getArrowTypeToggleGroup() {
		return arrowTypeToggleGroup;
	}

	public Menu getArrowMenu() {
		return arrowMenu;
	}

	public ColorPicker getLineColorPicker() {
		return lineColorPicker;
	}

	public Menu getEdgeShapeMenu() {
		return edgeShapeMenu;
	}

	public ColorPicker getFontColorPicker() {
		return fontColorPicker;
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

	public ToggleButton getBoldToggleButton() {
		return boldToggleButton;
	}

	public ToggleButton getItalicToggleButton() {
		return italicToggleButton;
	}

	public ToggleButton getUnderlineToggleButton() {
		return underlineToggleButton;
	}

	public ComboBox<String> getFontComboBox() {
		return fontComboBox;
	}

	public TextField getFontSizeTextField() {
		return fontSizeTextField;
	}
}
