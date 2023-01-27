/*
 * MenuView.java Copyright (C) 2023 Daniel H. Huson
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

package org.husonlab.phylosketch.views.menu;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import org.husonlab.phylosketch.views.primary.PrimaryView;

import java.io.IOException;
import java.util.Objects;

/**
 * the menu view
 * Daniel Huson, 1.2023
 */
public class MenuView {
	private final MenuViewController controller;
	private final MenuViewPresenter presenter;
	private HBox infoBar;

	public MenuView(PrimaryView primaryView) {
		var fxmlLoader = new FXMLLoader();
		try (var ins = Objects.requireNonNull(MenuViewController.class.getResource("menu.fxml")).openStream()) {
			fxmlLoader.load(ins);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		controller = fxmlLoader.getController();

		presenter = new MenuViewPresenter(primaryView, controller);

		var textfield = new TextField();
		textfield.setEditable(false);
		textfield.setStyle("-fx-background-color: transparent;");
		textfield.textProperty().bind(primaryView.getDocument().infoProperty());
		textfield.setFocusTraversable(false);
		infoBar = new HBox(textfield);
		textfield.prefWidthProperty().bind(infoBar.widthProperty());
		infoBar.setStyle("-fx-border-width: 1 0 0 0;-fx-padding: 2 16 2 16; -fx-background-color: -primary-swatch-500;-fx-text-fill: white;");
		infoBar.setSpacing(10);
	}

	public MenuBar getMenuBar() {
		return controller.getMenuBar();
	}

	public Pane getInfoBar() {
		return infoBar;
	}
}
