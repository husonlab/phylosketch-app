/*
 * SecondaryView.java Copyright (C) 2022 Daniel H. Huson
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

import com.gluonhq.charm.glisten.mvc.View;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import jloda.util.Basic;
import org.husonlab.phylosketch.views.primary.PrimaryView;

import java.io.IOException;
import java.util.Objects;

/**
 * the settings view
 * Daniel Huson, 12.22
 */
public class SecondaryView {
	private final SecondaryController controller;
	private final Parent root;

	public SecondaryView(PrimaryView primaryView) {
		var fxmlLoader = new FXMLLoader();
		try (var ins = Objects.requireNonNull(SecondaryView.class.getResource("secondary.fxml")).openStream()) {
			fxmlLoader.load(ins);
		} catch (IOException ex) {
			Basic.caught(ex);
			throw new RuntimeException(ex);
		}
		controller = fxmlLoader.getController();
		root = fxmlLoader.getRoot();

		new SecondaryPresenter(controller, primaryView);
	}

	public SecondaryController getController() {
		return controller;
	}

	public Node getRoot() {
		return root;
	}

	public View getView() {
		return controller.getView();
	}
}
