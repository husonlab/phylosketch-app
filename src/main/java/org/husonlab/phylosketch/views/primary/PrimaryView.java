/*
 * PrimaryView.java Copyright (C) 2022 Daniel H. Huson
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

import com.gluonhq.charm.glisten.mvc.View;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.husonlab.phylosketch.DefaultOptions;
import org.husonlab.phylosketch.Main;
import org.husonlab.phylosketch.network.Document;
import org.husonlab.phylosketch.network.NetworkPresenter;
import org.husonlab.phylosketch.views.menu.MenuView;

import java.io.IOException;
import java.util.Objects;

public class PrimaryView {
	private final static ObjectProperty<PrimaryView> lastFocused = new SimpleObjectProperty<>(null);
	private final Document document;
	private final PrimaryController controller;
	private final PrimaryPresenter presenter;

	public PrimaryView(ObjectProperty<PrimaryView> primaryView) {
		primaryView.set(this);
		this.document = new Document();

		var fxmlLoader = new FXMLLoader();
		try (var ins = Objects.requireNonNull(PrimaryController.class.getResource("primary.fxml")).openStream()) {
			fxmlLoader.load(ins);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		controller = fxmlLoader.getController();

		presenter = new PrimaryPresenter(getDocument(), this, controller);

		NetworkPresenter.setupView(getView().focusedProperty(), controller.getStackPane(), getDocument(), getPresenter().interactionModeProperty());

		if (DefaultOptions.getTrees().size() > 0) {
			try {
				getDocument().setNewickString(DefaultOptions.getTrees().get(0).getNewick());
			} catch (Exception ignored) {
				DefaultOptions.getTrees().remove(0);
				getDocument().setNewickString("((a,b),(c,d));");
			}
		} else
			getDocument().setNewickString("((a,b),(c,d));");

			if (Main.isDesktop()) {
				Platform.runLater(() -> {
					var scene = controller.getPrimary().getScene();
					var root = scene.getRoot();
					var menuView = new MenuView(this);
					var pane = new BorderPane();
					pane.prefHeightProperty().bind(scene.heightProperty());
					pane.setMinHeight(Pane.USE_PREF_SIZE);
					pane.setMaxHeight(Pane.USE_PREF_SIZE);
					pane.prefWidthProperty().bind(scene.widthProperty());
					pane.setMinWidth(Pane.USE_PREF_SIZE);
					pane.setMaxWidth(Pane.USE_PREF_SIZE);
					scene.setRoot(pane);
					pane.setTop(menuView.getMenuBar());
					pane.setCenter(root);
					pane.setBottom(menuView.getInfoBar());

					lastFocused.set(this);
					controller.getPrimary().getScene().getWindow().focusedProperty().addListener((v, o, n) -> {
						if (n)
							lastFocused.set(this);
					});
				});
			}
	}

	public PrimaryController getController() {
		return controller;
	}

	public PrimaryPresenter getPresenter() {
		return presenter;
	}

	public View getView() {
		return controller.getPrimary();
	}

	public Document getDocument() {
		return document;
	}

	public static ReadOnlyObjectProperty<PrimaryView> lastFocusedProperty() {
		return lastFocused;
	}

	public Stage getStage() {
		return (Stage) getView().getScene().getWindow();
	}

}
