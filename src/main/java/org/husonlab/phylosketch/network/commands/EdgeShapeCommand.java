/*
 * EdgeShapeCommand.java Copyright (C) 2022 Daniel H. Huson
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
 */

package org.husonlab.phylosketch.network.commands;

import javafx.geometry.Point2D;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.graph.Edge;
import jloda.util.Pair;
import org.husonlab.phylosketch.network.NetworkView;

/**
 * change edge shape command
 * Daniel Huson, 1.2020
 */
public class EdgeShapeCommand extends UndoableRedoableCommand {
	private final Runnable undo;
	private final Runnable redo;

	public EdgeShapeCommand(NetworkView networkView, Pair<Edge, Integer> edgeAndControlId, Point2D delta) {
		super("Edge Shape");
		final var id = edgeAndControlId.getFirst().getId();
		final var controlId = edgeAndControlId.getSecond();

		var ev = networkView.getView(networkView.getTree().findEdgeById(id));
		var oldCoordinates = ev.getControlCoordinates();
		var newCoordinates = ev.getControlCoordinates();
		if (controlId == 1) {
			oldCoordinates[0] -= delta.getX();
			oldCoordinates[1] -= delta.getY();
			newCoordinates[0] += delta.getX();
			newCoordinates[1] += delta.getY();
		} else {
			oldCoordinates[2] -= delta.getX();
			oldCoordinates[3] -= delta.getY();
			newCoordinates[2] += delta.getX();
			newCoordinates[3] += delta.getY();
		}
		var oldNormalized = ev.computeNormalizedControlCoordinates(oldCoordinates);
		var newNormalized = ev.computeNormalizedControlCoordinates(newCoordinates);

		undo = () -> {
			networkView.getView(networkView.getTree().findEdgeById(id)).setControlCoordinatesFromNormalized(oldNormalized);
		};
		redo = () -> {
			networkView.getView(networkView.getTree().findEdgeById(id)).setControlCoordinatesFromNormalized(newNormalized);
		};
	}

	@Override
	public void undo() {
		undo.run();
	}

	@Override
	public void redo() {
		redo.run();

	}
}
