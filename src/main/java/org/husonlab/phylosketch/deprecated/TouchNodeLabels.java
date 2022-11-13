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

package org.husonlab.phylosketch.deprecated;

import javafx.beans.property.ObjectProperty;
import jloda.fx.selection.SelectionModel;
import jloda.fx.undo.UndoManager;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.util.Single;
import org.husonlab.phylosketch.network.NetworkView;
import org.husonlab.phylosketch.network.commands.MoveSelectedNodeLabelsCommand;
import org.husonlab.phylosketch.views.primary.InteractionMode;

/**
 * apply touch interaction for node labels
 * Daniel Huson, 11.2022
 */
@Deprecated
public class TouchNodeLabels {
	public static void install(UndoManager undoManager, NetworkView networkView, SelectionModel<Node> nodeSelection,
							   SelectionModel<Edge> edgeSelection, Node v, ObjectProperty<InteractionMode> tool) {
		var label = networkView.getView(v).label();

		final var currentTool = new Single<InteractionMode>(null);

		final var startScenePosition = new double[2];
		final var previousScenePosition = new double[2];
		final var touchId = new Single<>(-1);
		final var moved = new Single<>(false);
		final var pressStartTime = new Single<>(0L);

		label.setOnTouchPressed(a -> {
			var touchPoint = a.getTouchPoint();
			touchId.set(touchPoint.getId());
			pressStartTime.set(System.currentTimeMillis());

			nodeSelection.select(v);

			currentTool.set(tool.get());
			if (currentTool.get() == InteractionMode.Move) {// move label
				startScenePosition[0] = previousScenePosition[0] = touchPoint.getSceneX();
				startScenePosition[1] = previousScenePosition[1] = touchPoint.getSceneY();
				moved.set(false);
			} else if (currentTool.get() == InteractionMode.EditLabels) {// edit labels
				TouchLabelEditing.apply(a, tool, networkView, undoManager, nodeSelection, v, label);
			}
			a.consume();
		});

		label.setOnTouchMoved(a -> {
			var touchPoint = a.getTouchPoint();
			if (touchPoint.getId() == touchId.get()) {
				if (currentTool.get() == InteractionMode.Move) {
					final double deltaX = (touchPoint.getSceneX() - previousScenePosition[0]);
					final double deltaY = (touchPoint.getSceneY() - previousScenePosition[1]);
					label.setLayoutX(label.getLayoutX() + deltaX);
					label.setLayoutY(label.getLayoutY() + deltaY);
					moved.set(true);
					previousScenePosition[0] = touchPoint.getSceneX();
					previousScenePosition[1] = touchPoint.getSceneY();
				}
				a.consume();
			}
		});

		label.setOnTouchReleased(a -> {
			var touchPoint = a.getTouchPoint();
			if (touchPoint.getId() == touchId.get()) {
				if (!moved.get() && System.currentTimeMillis() - pressStartTime.get() > 500) {
					edgeSelection.clearSelection();
					nodeSelection.clearSelection();
				}
				nodeSelection.select(v);

				if (currentTool.get() == InteractionMode.Move) {
					undoManager.add(new MoveSelectedNodeLabelsCommand(touchPoint.getSceneX() - startScenePosition[0],
							touchPoint.getSceneY() - startScenePosition[1], networkView, nodeSelection.getSelectedItems()));
				}
			}
		});
	}
}
