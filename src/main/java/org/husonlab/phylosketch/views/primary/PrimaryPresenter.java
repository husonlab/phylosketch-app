/*
 * PrimaryPresenter.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import jloda.util.StringUtils;

public class PrimaryPresenter {
	public enum Tool {AddNodesAndEdges, EraseNodesAndEdges, MoveNodes, AddLabels}

	private final ObjectProperty<Tool> tool = new SimpleObjectProperty<>(this, "tool");

	public PrimaryPresenter(PrimaryView view, PrimaryController controller) {
		controller.getToggles().selectedToggleProperty().addListener((v, o, n) -> {
			if (n == controller.getMoveToggleButton()) {
				tool.set(PrimaryPresenter.Tool.MoveNodes);
			} else if (n == controller.getPenToggleButton()) {
				tool.set(PrimaryPresenter.Tool.AddNodesAndEdges);
			} else if (n == controller.getEraserToggleButton()) {
				tool.set(PrimaryPresenter.Tool.EraseNodesAndEdges);
			} else if (n == controller.getLabelToggleButton()) {
				tool.set(PrimaryPresenter.Tool.AddLabels);
			} else if (n == null)
				tool.set(null);
			controller.getScrollPane().setPannable(tool.get() == null || tool.get() == Tool.AddLabels);
		});

		controller.getModeLabel().setText("");
		tool.addListener((c, o, n) -> controller.getModeLabel().setText(n == null ? "" : StringUtils.fromCamelCase(n.name())));

		controller.getUndoButton().setOnAction(e -> view.getUndoManager().undo());
		controller.getUndoButton().disableProperty().bind(view.getUndoManager().undoableProperty().not());
		controller.getRedoButton().setOnAction(e -> view.getUndoManager().redo());
		controller.getRedoButton().disableProperty().bind(view.getUndoManager().redoableProperty().not());

		if (com.gluonhq.attach.util.Platform.isDesktop()) {
			controller.getStackPane().setOnScroll(e -> {
				var factor = (e.getDeltaY() > 0 ? 1.1 : 1 / 1.1);
				var box = view.getDocument().getView().getBoundingBox();
				if(factor<1 && Math.min(box.getWidth(), box.getHeight())<50 || factor>1 && Math.max(box.getWidth(), box.getHeight())>2000) {
					controller.getLabel().setText("Zoom capped");
				}
				else {
					controller.getLabel().setText(String.format("Factor: %.1f",factor));
					view.getDocument().getView().scale(factor, factor);
				}
				e.consume();
			});
		}
		else {
			controller.getStackPane().setOnZoom(e->{
				var factor = e.getZoomFactor();

				if(factor!=1.0) {
					var box=view.getDocument().getView().getBoundingBox();
					if (factor < 1 && Math.min(box.getWidth(), box.getHeight()) < 25 || factor > 1 && Math.max(box.getWidth(), box.getHeight()) > 2000) {
						controller.getLabel().setText("Zoom capped");
					} else {
						controller.getLabel().setText(String.format("Factor: %.1f", factor));
						view.getDocument().getView().scale(factor, factor);
						// todo: scroll so that center stays at same place
					}
				}
				e.consume();
			});
		}

		controller.getResetButton().setOnAction(e-> view.getDocument().getView().resetScale());
	}

	public ObjectProperty<Tool> toolProperty() {
		return tool;
	}
}
