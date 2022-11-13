/*
 * LabelEditorPresenter.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.views.labeleditor;

import javafx.scene.input.KeyEvent;
import org.husonlab.phylosketch.network.interaction.LabelEditingManager;

/**
 * label edit presenter
 * Daniel Huson, 11.22
 */
public class LabelEditorPresenter {

	public LabelEditorPresenter(LabelEditorController controller, LabelEditingManager manager) {
		controller.getDoneButton().setOnAction(a -> manager.finishEditing());

		controller.getLeftButton().setOnAction(a -> manager.continueEditing(LabelEditingManager.Direction.Left));
		controller.getLeftButton().disableProperty().bind(manager.leftNeighborProperty().isNull());

		controller.getRightButton().setOnAction(a -> manager.continueEditing(LabelEditingManager.Direction.Right));
		controller.getRightButton().disableProperty().bind(manager.rightNeighborProperty().isNull());

		controller.getUpButton().setOnAction(a -> manager.continueEditing(LabelEditingManager.Direction.Up));
		controller.getUpButton().disableProperty().bind(manager.upNeighborProperty().isNull());

		controller.getDownButton().setOnAction(a -> manager.continueEditing(LabelEditingManager.Direction.Down));
		controller.getDownButton().disableProperty().bind(manager.downNeighborProperty().isNull());

		controller.getTextField().addEventFilter(KeyEvent.KEY_PRESSED, a -> {
			switch (a.getCode()) {
				case ENTER -> manager.finishEditing();
				case UP -> manager.continueEditing(LabelEditingManager.Direction.Up);
				case DOWN -> manager.continueEditing(LabelEditingManager.Direction.Down);
				case LEFT -> {
					if (a.isShiftDown())
						manager.continueEditing(LabelEditingManager.Direction.Left);
				}
				case RIGHT -> {
					if (a.isShiftDown())
						manager.continueEditing(LabelEditingManager.Direction.Right);
				}
			}
		});

		controller.getTextField().setOnSwipeUp(a -> {
			manager.continueEditing(LabelEditingManager.Direction.Up);
			a.consume();
		});
		controller.getTextField().setOnSwipeDown(a -> {
			manager.continueEditing(LabelEditingManager.Direction.Down);
			a.consume();
		});
		controller.getTextField().setOnSwipeLeft(a -> {
			manager.continueEditing(LabelEditingManager.Direction.Left);
			a.consume();
		});
		controller.getTextField().setOnSwipeRight(a -> {
			manager.continueEditing(LabelEditingManager.Direction.Right);
			a.consume();
		});
	}
}
