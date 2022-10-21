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
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.FloatingActionButton;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;

public class SecondaryController {

	@FXML
	private View secondary;

	@FXML
	private FlowPane flowPane;

	@FXML
	private void initialize() {
		secondary.setShowTransitionFactory(BounceInRightTransition::new);

		FloatingActionButton fab = new FloatingActionButton(MaterialDesignIcon.INFO.text, e -> System.out.println("Info"));

		fab.showOn(secondary);

		secondary.showingProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue) {
				AppBar appBar = AppManager.getInstance().getAppBar();
				appBar.setNavIcon(MaterialDesignIcon.MENU.button(e ->
						AppManager.getInstance().getDrawer().open()));
				appBar.setTitleText("Configure");
				appBar.getActionItems().add(MaterialDesignIcon.FAVORITE.button(e ->
						System.out.println("Favorite")));
			}
		});
	}

	public View getSecondary() {
		return secondary;
	}

	public FlowPane getFlowPane() {
		return flowPane;
	}
}
