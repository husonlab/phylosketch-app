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
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import jloda.util.Single;
import org.husonlab.phylosketch.views.other.AwesomeIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PrimaryController {

	@FXML
	private AnchorPane anchorPane;

	@FXML
	private CheckMenuItem boldCheckMenuItem;

	@FXML
	private CheckMenuItem italicCheckMenuItem;

	@FXML
	private CheckMenuItem underlineCheckMenuItem;


	@FXML
	private RadioMenuItem arrowNoneRadioMenuItem;

	@FXML
	private RadioMenuItem arrowRightRadioMenuItem;

	@FXML
	private ColorPicker lineColorPicker;

	@FXML
	private RadioMenuItem editLabelMenuItem;

	@FXML
	private RadioMenuItem editMenuItem;

	@FXML
	private RadioMenuItem eraseMenuItem;

	@FXML
	private ColorPicker fontColorPicker;

	@FXML
	private ComboBox<String> fontComboBox;

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
	private RadioMenuItem roundEdgesRadioMenuItem;

	@FXML
	private ScrollPane scrollPane;

	@FXML
	private StackPane stackPane;

	@FXML
	private RadioMenuItem straightEdgesRadioMenuItem;

	@FXML
	private Button importButton;


	@FXML
	private Slider widthSlider;

	@FXML
	private Button increaseFontSizeButton;

	@FXML
	private Button decreaseFontSizeButton;

	@FXML
	private HBox newickHBox;

	@FXML
	private TextArea newickTextArea;

	@FXML
	private ToggleButton showNewickToggleButton;

	@FXML
	private ToggleButton showWeightsToggleButton;

	@FXML
	private ToggleButton showHTMLToggleButton;

	@FXML
	private VBox vBox;

	@FXML
	private Pane dragPane;

	@FXML
	private Button horizontalZoomInButton;

	@FXML
	private Button horizontalZoomOutButton;

	@FXML
	private Button verticalZoomInButton;

	@FXML
	private Button verticalZoomOutButton;

	@FXML
	private GridPane zoomButtonPane;

	@FXML
	private MenuButton treesMenu;

	private final ToggleGroup modeToggleGroup = new ToggleGroup();
	private final ToggleGroup edgeShapeToggleGroup = new ToggleGroup();
	private final ToggleGroup arrowTypeToggleGroup = new ToggleGroup();

	private final Button undoButton = MaterialDesignIcon.UNDO.button(a -> getUndoButton().fire());
	private final Button redoButton = MaterialDesignIcon.REDO.button(a -> getRedoButton().fire());

	@FXML
	private void initialize() {
		eraseMenuItem.setGraphic(AwesomeIcon.ERASER.graphic());

		primary.getStylesheets().add(Objects.requireNonNull(PrimaryController.class.getResource("primary.css")).toExternalForm());

		{
			var newButton = MaterialDesignIcon.ZOOM_IN.button();
			var pane = (Pane) increaseFontSizeButton.getParent();
			pane.getChildren().set(pane.getChildren().indexOf(increaseFontSizeButton), newButton);
			increaseFontSizeButton = newButton;
		}
		{
			var newButton = MaterialDesignIcon.ZOOM_OUT.button();
			var pane = (Pane) decreaseFontSizeButton.getParent();
			pane.getChildren().set(pane.getChildren().indexOf(decreaseFontSizeButton), newButton);
			decreaseFontSizeButton = newButton;
		}

		primary.showingProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue) {
				var appBar = AppManager.getInstance().getAppBar();
				appBar.setNavIcon(MaterialDesignIcon.MENU.button(e ->
						AppManager.getInstance().getDrawer().open()));
				appBar.setTitleText("PhyloSketch");
				appBar.getActionItems().addAll(undoButton, redoButton);
			}
		});

		scrollPane.viewportBoundsProperty().addListener((v, o, n) -> {
			stackPane.setMinSize(n.getWidth(), n.getHeight());
		});

		for (var item : modeMenuButton.getItems()) {
			if (item instanceof RadioMenuItem) {
				var radioMenuItem = (RadioMenuItem) item;
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

		showNewickToggleButton.setSelected(false);

		newickHBox.visibleProperty().bind(showNewickToggleButton.selectedProperty());

		var mouseY = new Single<>(0.0);
		dragPane.setOnMousePressed(e -> {
			mouseY.set(e.getScreenY());
		});
		dragPane.setOnMouseDragged(e -> {
			var deltaY = e.getScreenY() - mouseY.get();
			var newHeight = vBox.getHeight() + deltaY;
			if (newHeight > 60 && newHeight < 500)
				vBox.setPrefHeight(newHeight);
			mouseY.set(e.getScreenY());
		});

		zoomButtonPane.cursorProperty().bind(scrollPane.cursorProperty());

		for (var button : List.of(verticalZoomInButton, verticalZoomOutButton, horizontalZoomInButton, horizontalZoomOutButton)) {
			button.setVisible(false);
		}
	}

	public void showZoomButtons(boolean show) {
		var transitions = new ArrayList<Transition>();
		var to = (show ? 1.0 : 0.0);
		for (var button : List.of(verticalZoomInButton, verticalZoomOutButton, horizontalZoomInButton, horizontalZoomOutButton)) {
			button.setVisible(true);
			var fade = new FadeTransition(Duration.seconds(0.75), button);
			fade.setToValue(to);
			transitions.add(fade);
			fade.setOnFinished(a -> {
				button.setVisible(show);
			});
		}
		(new ParallelTransition(transitions.toArray(new Transition[0]))).play();
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

	public ColorPicker getLineColorPicker() {
		return lineColorPicker;
	}

	public ColorPicker getFontColorPicker() {
		return fontColorPicker;
	}

	public Slider getWidthSlider() {
		return widthSlider;
	}

	public Button getImportButton() {
		return importButton;
	}

	public Button getIncreaseFontSizeButton() {
		return increaseFontSizeButton;
	}

	public Button getDecreaseFontSizeButton() {
		return decreaseFontSizeButton;
	}

	public ComboBox<String> getFontComboBox() {
		return fontComboBox;
	}

	public CheckMenuItem getBoldCheckMenuItem() {
		return boldCheckMenuItem;
	}

	public CheckMenuItem getItalicCheckMenuItem() {
		return italicCheckMenuItem;
	}

	public CheckMenuItem getUnderlineCheckMenuItem() {
		return underlineCheckMenuItem;
	}

	public ToggleButton getShowWeightsToggleButton() {
		return showWeightsToggleButton;
	}

	public ToggleButton getShowHTMLToggleButton() {
		return showHTMLToggleButton;
	}

	public Button getHorizontalZoomInButton() {
		return horizontalZoomInButton;
	}

	public Button getHorizontalZoomOutButton() {
		return horizontalZoomOutButton;
	}

	public Button getVerticalZoomInButton() {
		return verticalZoomInButton;
	}

	public Button getVerticalZoomOutButton() {
		return verticalZoomOutButton;
	}

	public GridPane getZoomButtonPane() {
		return zoomButtonPane;
	}

	public MenuButton getTreesMenu() {
		return treesMenu;
	}
}
