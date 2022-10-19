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

import java.io.IOException;
import java.util.Objects;

public class SecondaryView {
	final private SecondaryPresenter presenter;
	final private SecondaryController controller;

	public SecondaryView()  {
		var fxmlLoader = new FXMLLoader();
		try (var ins = Objects.requireNonNull(SecondaryController.class.getResource("secondary.fxml")).openStream()) {
			fxmlLoader.load(ins);
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		controller = fxmlLoader.getController();
		presenter = new SecondaryPresenter(this, controller);
	}

	public View getView() {
		return controller.getSecondary();
	}
}
