/*
 * Main.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch;

import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.visual.Swatch;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.husonlab.phylosketch.views.primary.PrimaryView;
import org.husonlab.phylosketch.views.secondary.SecondaryView;

import java.io.IOException;

import static com.gluonhq.charm.glisten.application.AppManager.HOME_VIEW;

public class Main extends Application {

	public static final String PRIMARY_VIEW = HOME_VIEW;
	public static final String SECONDARY_VIEW = "Configuration View";

	private final AppManager appManager = AppManager.initialize(this::postInit);

	@Override
	public void init()  {
		System.setProperty(com.gluonhq.attach.util.Constants.ATTACH_DEBUG,"true");
		appManager.addViewFactory(PRIMARY_VIEW, ()->(new PrimaryView()).getView());
		appManager.addViewFactory(SECONDARY_VIEW, ()->(new SecondaryView()).getView());
		DrawerManager.buildDrawer(appManager);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		appManager.start(primaryStage);

		if (com.gluonhq.attach.util.Platform.isDesktop()) {
			primaryStage.setX(100);
			primaryStage.setY(100);
			primaryStage.setWidth(600);
			primaryStage.setHeight(800);
		}
	}

	private void postInit(Scene scene) {
		Swatch.TEAL.assignTo(scene);

		var stylesURL = Main.class.getResource("styles.css");
		assert stylesURL != null;
		scene.getStylesheets().add(stylesURL.toExternalForm());

		try (var iconStream = Main.class.getResourceAsStream("phylosketch.png")) {
			assert iconStream != null;
			((Stage) scene.getWindow()).getIcons().add(new Image(iconStream));
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
