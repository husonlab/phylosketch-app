/*
 * TouchNodeLabels.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.network.interaction;

import javafx.beans.property.ObjectProperty;
import jloda.graph.Node;
import jloda.util.Single;
import org.husonlab.phylosketch.Main;
import org.husonlab.phylosketch.network.Document;
import org.husonlab.phylosketch.network.commands.DeleteSubTreeCommand;
import org.husonlab.phylosketch.network.commands.MoveSelectedNodeLabelsCommand;
import org.husonlab.phylosketch.views.primary.InteractionMode;

import static org.husonlab.phylosketch.network.interaction.NodeShapeInteraction.consumeAllScrollAndTouchEvents;
import static org.husonlab.phylosketch.network.interaction.NodeShapeInteraction.selectOnlyService;

/**
 * apply mouse interaction for node labels
 * Daniel Huson, 11.2022
 */
public class NodeLabelInteraction {

	public static void install(LabelEditingManager editingManager, Document document, Node v, ObjectProperty<InteractionMode> mode) {
		var networkView = document.getNetworkView();
		var nodeSelection = document.getNodeSelection();
		var edgeSelection = document.getEdgeSelection();

		var label = networkView.getView(v).label();
		var labelShapeBelow = networkView.getView(v).labelShapeBelow();

		label.setMouseTransparent(true);
		consumeAllScrollAndTouchEvents(label);
		consumeAllScrollAndTouchEvents(labelShapeBelow);

		final var currentTool = new Single<InteractionMode>(null);

		final var startScenePosition = new double[2];
		final var previousScenePosition = new double[2];
		final var moved = new Single<>(false);

		labelShapeBelow.setOnMousePressed(a -> {
			if (Main.isDesktop() && a.isShiftDown())
				nodeSelection.toggleSelection(v);
			else
				nodeSelection.select(v);

			selectOnlyService.restart(nodeSelection, edgeSelection, v);

			currentTool.set(mode.get());
			if (currentTool.get() == InteractionMode.Move) { // move label
				startScenePosition[0] = previousScenePosition[0] = a.getSceneX();
				startScenePosition[1] = previousScenePosition[1] = a.getSceneY();
				moved.set(false);
			} else if (currentTool.get() == InteractionMode.EditLabels) { // edit labels
				editingManager.startEditing(v);
			}
			a.consume();
		});

		labelShapeBelow.setOnMouseDragged(a -> {
			selectOnlyService.cancel();

			if (!moved.get() && !nodeSelection.isSelected(v)) {
				nodeSelection.clearSelection();
				edgeSelection.clearSelection();
				nodeSelection.select(v);
			}

			if (currentTool.get() == InteractionMode.Move) {
				final double deltaX = (a.getSceneX() - previousScenePosition[0]);
				final double deltaY = (a.getSceneY() - previousScenePosition[1]);

				if (false) {
					label.setLayoutX(label.getLayoutX() + deltaX);
					label.setLayoutY(label.getLayoutY() + deltaY);
				} else {
					for (var u : nodeSelection.getSelectedItems()) {
						var uLabel = networkView.getView(u).label();
						uLabel.setLayoutX(uLabel.getLayoutX() + deltaX);
						uLabel.setLayoutY(uLabel.getLayoutY() + deltaY);
					}
				}
				moved.set(true);
				previousScenePosition[0] = a.getSceneX();
				previousScenePosition[1] = a.getSceneY();
			}
			a.consume();
		});

		labelShapeBelow.setOnMouseReleased(a -> {
			selectOnlyService.cancel();

			if (Main.isDesktop() && !moved.get() && !a.isShiftDown()) {
				nodeSelection.clearSelection();
				edgeSelection.clearSelection();
				nodeSelection.select(v);
			}

			if (currentTool.get() == InteractionMode.Move) {
				document.getUndoManager().add(new MoveSelectedNodeLabelsCommand(a.getSceneX() - startScenePosition[0],
						a.getSceneY() - startScenePosition[1], networkView, nodeSelection.getSelectedItems()));
			} else if (currentTool.get() == InteractionMode.Erase) {
				document.getUndoManager().doAndAdd(new DeleteSubTreeCommand(document, v));
			}
			moved.set(false);
			a.consume();
		});
	}
}
