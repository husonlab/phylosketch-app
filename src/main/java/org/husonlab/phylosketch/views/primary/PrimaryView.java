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
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXMLLoader;
import org.husonlab.phylosketch.network.Document;
import org.husonlab.phylosketch.network.NetworkPresenter;

import java.io.IOException;
import java.util.Objects;

public class PrimaryView {
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

		NetworkPresenter.setupView(controller.getStackPane(), getDocument(), getPresenter().interactionModeProperty());

		getDocument().setNewickString("((a,b),(c,d));");
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
}
