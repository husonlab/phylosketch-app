/*
 * DrawerInitialization.java Copyright (C) 2022 Daniel H. Huson
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

import com.gluonhq.attach.lifecycle.LifecycleService;
import com.gluonhq.attach.util.Platform;
import com.gluonhq.attach.util.Services;
import com.gluonhq.charm.glisten.application.ViewStackPolicy;
import com.gluonhq.charm.glisten.control.NavigationDrawer;
import com.gluonhq.charm.glisten.control.NavigationDrawer.Item;
import com.gluonhq.charm.glisten.control.NavigationDrawer.ViewItem;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

import static org.husonlab.phylosketch.Main.*;

public class DrawerInitialization {

	public static void apply(NavigationDrawer drawer) {

		var iconView = new ImageView(new Image(Objects.requireNonNull(DrawerInitialization.class.getResourceAsStream("phylosketch.png"))));
		iconView.setFitHeight(32);
		iconView.setFitWidth(32);

		var header = new NavigationDrawer.Header(Version.NAME, Version.VERSION, iconView);
		drawer.setHeader(header);

		final var primaryItem = new ViewItem("Main Viewer", MaterialDesignIcon.HOME.graphic(), PRIMARY_VIEW, ViewStackPolicy.SKIP);
		final var secondaryItem = new ViewItem("Properties and Default Settings", MaterialDesignIcon.DASHBOARD.graphic(), SECONDARY);
		final var otherItem = new ViewItem("Log", MaterialDesignIcon.INFO.graphic(), LOG_VIEW);
		drawer.getItems().addAll(primaryItem, secondaryItem, otherItem);

		if (Main.isDesktop()) {
			final var quitItem = new Item("Quit", MaterialDesignIcon.EXIT_TO_APP.graphic());
			quitItem.selectedProperty().addListener((obs, ov, nv) -> {
				if (nv) {
					Services.get(LifecycleService.class).ifPresent(LifecycleService::shutdown);
				}
			});
			drawer.getItems().add(quitItem);
		}
	}
}