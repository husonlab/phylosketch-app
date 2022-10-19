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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import jloda.phylo.PhyloTree;

import java.io.IOException;

public class PrimaryController {

	@FXML
	private View primary;


	@FXML
	private Label modeLabel;

	@FXML
	private Label label;

	@FXML
	private ToggleButton eraserToggleButton;

	@FXML
	private ToggleButton labelToggleButton;

	@FXML
	private Pane mainPane;

	@FXML
	private ToggleButton penToggleButton;

	@FXML
	private ToggleButton moveToggleButton;

	@FXML
	private Button redoButton;

	@FXML
	private HBox togglesBox;

	@FXML
	private Button undoButton;

	@FXML
	private ScrollPane scrollPane;

	@FXML
	private StackPane stackPane;

	@FXML
	private Button resetButton;

	@FXML
	void buttonClick() {
		var tree = new PhyloTree();
		try {
			tree.parseBracketNotation("((a,b),(c,d));", true);
			label.setText("Sketch a phylogenetic tree or network! " + tree.toBracketString(false));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final ToggleGroup toggles = new ToggleGroup();


	@FXML
	private void initialize() {
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
		for(var item:togglesBox.getChildren()) {
			if(item instanceof Toggle)
				toggles.getToggles().add((Toggle)item);
		}
		scrollPane.viewportBoundsProperty().addListener((v,o,n)-> {
				stackPane.setMinSize(n.getWidth(), n.getHeight());
		});
	}

	public View getPrimary() {
		return primary;
	}

	public Label getLabel() {
		return label;
	}

	public ToggleButton getEraserToggleButton() {
		return eraserToggleButton;
	}

	public ToggleButton getLabelToggleButton() {
		return labelToggleButton;
	}

	public Pane getMainPane() {
		return mainPane;
	}

	public ToggleButton getPenToggleButton() {
		return penToggleButton;
	}

	public ToggleButton getMoveToggleButton() {
		return moveToggleButton;
	}

	public Button getRedoButton() {
		return redoButton;
	}

	public Button getUndoButton() {
		return undoButton;
	}

	public ToggleGroup getToggles() {
		return toggles;
	}

	public HBox getTogglesBox() {
		return togglesBox;
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
}
