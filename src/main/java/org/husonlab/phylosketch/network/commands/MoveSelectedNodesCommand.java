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
import org.husonlab.phylosketch.network.Document;
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
	public MoveSelectedNodesCommand(double dx, double dy, Document document, ObservableSet<Node> selectedItems,
									Map<Integer, double[]> oldEdgeControlCoordinates0, Map<Integer, double[]> newEdgeControlCoordinates0) {
		super("Move");
		final var networkView = document.getNetworkView();
		final var tree = networkView.getTree();

		final Map<Integer, double[]> oldEdgeControlCoordinates = new HashMap<>(oldEdgeControlCoordinates0);
		final Map<Integer, double[]> newEdgeControlCoordinates = new HashMap<>(newEdgeControlCoordinates0);

		final List<Integer> nodeData = selectedItems.stream().map(Node::getId).collect(Collectors.toList());
		final List<Integer> edgeData = tree.edgeStream().filter(e -> oldEdgeControlCoordinates.containsKey(e.getId()) && newEdgeControlCoordinates.containsKey(e.getId()))
				.map(Edge::getId).collect(Collectors.toList());

		final Map<Integer, Double> oldEdgeWeights;
		if (tree.hasEdgeWeights()) {
			oldEdgeWeights = new HashMap<>();
			tree.edgeStream().forEach(e -> oldEdgeWeights.put(e.getId(), tree.getWeight(e)));
		} else {
			oldEdgeWeights = null;
		}


		undo = () -> {
			if (oldEdgeWeights != null) {
				edgeData.forEach(id -> {
					tree.setWeight(tree.findEdgeById(id), oldEdgeWeights.get(id));
				});
			}
			nodeData.forEach(id -> networkView.moveNode(tree.findNodeById(id), -dx, -dy));
			edgeData.forEach(id -> networkView.getView(tree.findEdgeById(id)).setControlCoordinatesFromNormalized(oldEdgeControlCoordinates.get(id)));
			document.getGraphFX().incrementLastUpdate();
		};

		redo = () -> {
			tree.clearEdgeWeights();
			nodeData.forEach(id -> networkView.moveNode(tree.findNodeById(id), dx, dy));
			edgeData.forEach(id -> networkView.getView(tree.findEdgeById(id)).setControlCoordinatesFromNormalized(newEdgeControlCoordinates.get(id)));
			document.getGraphFX().incrementLastUpdate();
		};

		// run this here because we usually don't redo directly
		tree.clearEdgeWeights();
		document.getGraphFX().incrementLastUpdate();
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
