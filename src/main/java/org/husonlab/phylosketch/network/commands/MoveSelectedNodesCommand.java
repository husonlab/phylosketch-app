/*
 * MoveSelectedNodesCommand.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.network.commands;

import javafx.collections.ObservableSet;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.graph.Edge;
import jloda.graph.Node;
import org.husonlab.phylosketch.network.NetworkView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * move all selected nodes
 * Daniel Huson, 1.2020
 */
public class MoveSelectedNodesCommand extends UndoableRedoableCommand {
	private final Runnable undo;
	private final Runnable redo;

	/**
	 * constructor
	 */
	public MoveSelectedNodesCommand(double dx, double dy, NetworkView networkView, ObservableSet<Node> selectedItems,
									Map<Integer, double[]> oldEdgeControlCoordinates0, Map<Integer, double[]> newEdgeControlCoordinates0) {
		super("Move");

		final var tree = networkView.getTree();
		final Map<Integer, double[]> oldEdgeControlCoordinates = new HashMap<>(oldEdgeControlCoordinates0);
		final Map<Integer, double[]> newEdgeControlCoordinates = new HashMap<>(newEdgeControlCoordinates0);

		final List<Integer> nodeData = selectedItems.stream().map(Node::getId).collect(Collectors.toList());
		final List<Integer> edgeData = tree.edgeStream().filter(e -> oldEdgeControlCoordinates.containsKey(e.getId()) && newEdgeControlCoordinates.containsKey(e.getId()))
				.map(Edge::getId).collect(Collectors.toList());


		undo = () -> {
			nodeData.forEach(id -> networkView.moveNode(tree.findNodeById(id), -dx, -dy));
			edgeData.forEach(id -> networkView.getView(tree.findEdgeById(id)).setControlCoordinates(oldEdgeControlCoordinates.get(id)));
		};

		redo = () -> {
			nodeData.forEach(id -> networkView.moveNode(tree.findNodeById(id), dx, dy));
			edgeData.forEach(id -> networkView.getView(tree.findEdgeById(id)).setControlCoordinates(newEdgeControlCoordinates.get(id)));
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
