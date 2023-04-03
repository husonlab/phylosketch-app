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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;
import jloda.fx.util.ProgramProperties;
import jloda.fx.window.SplashScreen;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import org.husonlab.phylosketch.views.log.LogView;
import org.husonlab.phylosketch.views.primary.PrimaryView;
import org.husonlab.phylosketch.views.secondary.SecondaryView;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static com.gluonhq.charm.glisten.application.AppManager.HOME_VIEW;

public class Main extends Application {
	public static final String PRIMARY_VIEW = HOME_VIEW;
	public static final String SECONDARY = "Settings View";
	public static final String LOG_VIEW = "Log View";

	public static final Swatch DEFAULT_SWATCH = Swatch.TEAL;

	private final AppManager appManager = AppManager.initialize(this::postInit);

	private final ObjectProperty<PrimaryView> primaryView = new SimpleObjectProperty<>(this, "primaryView", null);

	@Override
	public void init() {
		DefaultOptions.load();

		if (isDesktop()) {
			SplashScreen.setLabelAnchor(new Point2D(160, 10));
			ProgramProperties.setProgramVersion(Version.VERSION);
			SplashScreen.setVersionString(ProgramProperties.getProgramVersion());
			try (var iconStream = Main.class.getResourceAsStream("splash.png")) {
				if (iconStream != null)
					SplashScreen.setImage(new Image(iconStream));
			} catch (IOException ignored) {
			}
		}

		PhyloTree.SUPPORT_RICH_NEWICK = true;
		System.setProperty(com.gluonhq.attach.util.Constants.ATTACH_DEBUG, "true");
		appManager.addViewFactory(PRIMARY_VIEW, () -> (new PrimaryView(primaryView)).getView());
		appManager.addViewFactory(SECONDARY, () -> (new SecondaryView(primaryView.get())).getView());
		appManager.addViewFactory(LOG_VIEW, () -> (new LogView()).getView());
		DrawerInitialization.apply(appManager.getDrawer());

		System.err.println("""

				***********
				PhyloSketch-App by Daniel H. Huson, Copyright (C) 2023.
				Uses network embedding code written by Celine Scornavacca.
				This program comes with ABSOLUTELY NO WARRANTY.
				This is free software, licensed under the terms of the GNU General Public License, Version 3.
				Sources available at: https://github.com/husonlab/phylosketch-app
				***********
				""");
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		appManager.start(primaryStage);
		if (isDesktop()) {
			primaryStage.setX(100);
			primaryStage.setY(100);
			primaryStage.setWidth(600);
			primaryStage.setHeight(800);
			// SplashScreen.showSplash(Duration.ofSeconds(5));
			primaryStage.setOnCloseRequest(e -> System.exit(0));
		}
	}

	private void postInit(Scene scene) {
		try {
			Swatch.valueOf(ProgramProperties.get("Swatch", Swatch.TEAL.name())).assignTo(scene);
		} catch (Exception ex) {
			Swatch.TEAL.assignTo(scene);
		}

		var stylesURL = Main.class.getResource("styles.css");
		assert stylesURL != null;
		scene.getStylesheets().add(stylesURL.toExternalForm());

		for (var fileName : List.of("PhyloSketch-16x16.png", "PhyloSketch-32x32.png", "PhyloSketch-48x48.png",
				"PhyloSketch-64x64.png", "PhyloSketch-128x128.png")) {
			try (var iconStream = Main.class.getResourceAsStream(fileName)) {
				assert iconStream != null;
				((Stage) scene.getWindow()).getIcons().add(new Image(iconStream));
				ProgramProperties.getProgramIconsFX().setAll(new Image(iconStream));
			} catch (IOException ex) {
				Basic.caught(ex);
			}
		}
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		DefaultOptions.store();
	}

	public static void main(String[] args) {
		Basic.startCollectionStdErr();

		launch(args);
	}

	public static boolean isDesktop() {
		return com.gluonhq.attach.util.Platform.isDesktop();
	}

	public static void setSwatch(Swatch swatch) {
		ProgramProperties.put("Swatch", swatch.name());
		for (var window : Window.getWindows()) {
			if (window.getScene() != null)
				swatch.assignTo(window.getScene());
		}
	}
}
