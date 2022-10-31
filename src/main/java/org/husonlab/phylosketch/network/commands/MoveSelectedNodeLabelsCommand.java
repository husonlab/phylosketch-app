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
import jloda.graph.Node;
import org.husonlab.phylosketch.network.NetworkView;

import java.util.List;
import java.util.stream.Collectors;

/**
 * move all selected nodes
 * Daniel Huson, 1.2020
 */
public class MoveSelectedNodeLabelsCommand extends UndoableRedoableCommand {
	private final Runnable undo;
	private final Runnable redo;

	/**
	 * constructor
	 */
	public MoveSelectedNodeLabelsCommand(double dx, double dy, NetworkView networkView, ObservableSet<Node> selectedItems) {
		super("Move");

		final var tree = networkView.getTree();

		final List<Integer> nodeData = selectedItems.stream().map(Node::getId).collect(Collectors.toList());


		undo = () -> {
			nodeData.forEach(id -> networkView.moveLabel(tree.findNodeById(id), -dx, -dy));
		};

		redo = () -> {
			nodeData.forEach(id -> networkView.moveLabel(tree.findNodeById(id), dx, dy));
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
