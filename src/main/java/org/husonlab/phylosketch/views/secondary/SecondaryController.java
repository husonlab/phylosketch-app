/*
 * SecondaryController.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.views.secondary;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.control.SettingsPane;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.util.Objects;

public class SecondaryController {

	@FXML
	private SettingsPane settingsPane;

	@FXML
	private View view;

	@FXML
	private TextField propertiesField;

	@FXML
	private void initialize() {
		view.getStylesheets().add(Objects.requireNonNull(SecondaryController.class.getResource("secondary.css")).toExternalForm());

		view.setShowTransitionFactory(BounceInRightTransition::new);

		view.showingProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue) {
				var appBar = AppManager.getInstance().getAppBar();
				appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> AppManager.getInstance().getDrawer().open()));
				appBar.setTitleText("Properties and Settings");
			}
		});

		settingsPane.setSearchBoxVisible(false);
	}

	public View getView() {
		return view;
	}

	public SettingsPane getSettingsPane() {
		return settingsPane;
	}

	public TextField getPropertiesField() {
		return propertiesField;
	}
}
