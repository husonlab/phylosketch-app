/*
 * LogController.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.views.log;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.FloatingActionButton;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import org.husonlab.phylosketch.Main;

public class LogController {

	@FXML
	private View view;

	@FXML
	private FlowPane flowPane;

	@FXML
	private TextArea logTextArea;

	@FXML
	private ScrollPane scrollPane;

	private final Button clearButton = MaterialDesignIcon.CLEAR.button(a -> logTextArea.clear());

	@FXML
	private void initialize() {
		view.setShowTransitionFactory(BounceInRightTransition::new);

		logTextArea.setStyle("-fx-font-size: 10;");

		var fab = new FloatingActionButton(MaterialDesignIcon.INFO.text, e -> {
			if (view.getCenter() == logTextArea) {
				logTextArea.prefWidthProperty().unbind();
				logTextArea.prefHeightProperty().unbind();
				scrollPane.prefWidthProperty().bind(view.widthProperty());
				scrollPane.prefHeightProperty().bind(view.heightProperty());
				view.setBottom(null);
				view.setCenter(scrollPane);
			} else {
				scrollPane.prefWidthProperty().unbind();
				scrollPane.prefHeightProperty().unbind();
				logTextArea.prefWidthProperty().bind(view.widthProperty());
				logTextArea.prefHeightProperty().bind(view.heightProperty());
				view.setBottom(null);
				view.setCenter(logTextArea);
				Platform.runLater(() -> {
					logTextArea.positionCaret(logTextArea.getText().length());
					logTextArea.setScrollTop(Double.MAX_VALUE);
				});
			}
		});
		fab.getOnAction().handle(null);

		if (Main.isDesktop())
			fab.showOn(view);

		view.showingProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue) {
				AppBar appBar = AppManager.getInstance().getAppBar();
				appBar.setNavIcon(MaterialDesignIcon.MENU.button(e ->
						AppManager.getInstance().getDrawer().open()));
				appBar.setTitleText("Log");
				appBar.getActionItems().addAll(clearButton);
			}
		});

		Platform.runLater(() -> {
			logTextArea.positionCaret(logTextArea.getText().length());
			logTextArea.setScrollTop(Double.MAX_VALUE);
		});

	}

	public View getView() {
		return view;
	}

	public FlowPane getFlowPane() {
		return flowPane;
	}

	public TextArea getLogTextArea() {
		return logTextArea;
	}
}
