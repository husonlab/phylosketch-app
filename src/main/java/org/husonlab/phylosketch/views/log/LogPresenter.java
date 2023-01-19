/*
 * LogPresenter.java Copyright (C) 2022 Daniel H. Huson
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


import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.husonlab.phylosketch.utils.AwesomeIcon;

public class LogPresenter {
	public LogPresenter(LogView view, LogController controller) {

		if (true) {
			controller.getFlowPane().getChildren().add(new Label("AwesomeIcons            "));
			for (var icon : AwesomeIcon.values()) {
				var label = new Label(icon.name());
				label.setStyle("-fx-font-size: 9;");
				controller.getFlowPane().getChildren().add(new VBox(icon.graphic(), label));
			}
		}

		if (true) {
			controller.getFlowPane().getChildren().add(new Label("MaterialDesignIcons             "));
			for (var icon : MaterialDesignIcon.values()) {
				var label = new Label(icon.name());
				label.setStyle("-fx-font-size: 9;");
				controller.getFlowPane().getChildren().add(new VBox(icon.graphic(), label));
			}
		}
	}
}
