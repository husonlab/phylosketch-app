/*
 * NewEdgeAndNodeCommand.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.application.Platform;
import javafx.scene.layout.Pane;
import jloda.fx.selection.SelectionModel;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.graph.Edge;
import jloda.graph.Node;
import org.husonlab.phylosketch.network.NetworkView;

public class NewEdgeAndNodeCommand extends UndoableRedoableCommand {
	final private Runnable undo;
	final private Runnable redo;

	private int edgeId;
	private int wId;

	/**
	 * construct
	 *
	 */
	public NewEdgeAndNodeCommand(Pane pane, NetworkView networkView, SelectionModel<Node> nodeSelection, Node a, final Node b, double x, double y) {
		super("Add Edge");
		final var tree = networkView.getTree();

		final int aId = a.getId();
		final int bId = (b != null ? b.getId() : 0);

		undo = () -> {
			if (edgeId > 0) {
				var e = tree.findEdgeById(edgeId);
				networkView.removeView(e);
				tree.deleteEdge(e);
			}
			if (wId > 0) {
				var w = tree.findNodeById(wId);
				networkView.removeView(w);
				tree.deleteNode(w);
			}
		};

		redo = () -> {
			Node w;
			if (bId == 0) {
				if (wId == 0) {
					w = tree.newNode();
					wId = w.getId();
				} else
					w = tree.newNode(null, wId);
				networkView.createShape(w, x, y);
			} else
				w = tree.findNodeById(bId);

			final var v = tree.findNodeById(aId);
			if (v.getCommonEdge(w) == null && v != w) {
				Edge e;
				if (edgeId == 0) {
					e = tree.newEdge(v, w);
					edgeId = e.getId();
				} else {
					e = tree.newEdge(v, w, null, edgeId);
				}
				networkView.createEdgeView(e);
			}
			if (wId > 0) Platform.runLater(() ->{
				nodeSelection.clearSelection();
				nodeSelection.select(tree.findNodeById(wId));
			});
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
