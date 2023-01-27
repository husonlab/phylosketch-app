/*
 * RemoveDiNodesCommand.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.views.menu.commands;

import jloda.fx.undo.UndoableRedoableCommand;
import jloda.graph.Node;
import jloda.util.Triplet;
import org.husonlab.phylosketch.network.Document;
import org.husonlab.phylosketch.network.EdgeView;
import org.husonlab.phylosketch.network.NodeView;

import java.util.Collection;
import java.util.HashMap;

/**
 * remove di-nodes command
 * Daniel Huson, 1.202
 */
public class RemoveDiNodesCommand extends UndoableRedoableCommand {
	private Runnable undo;
	private Runnable redo;

	public RemoveDiNodesCommand(Document document, Collection<Node> nodes) {
		super("Remove Di Nodes");
		var networkView = document.getNetworkView();

		var diNode2Views = new HashMap<Integer, Triplet<EdgeView, NodeView, EdgeView>>();
		nodes.stream().filter(v -> v.getInDegree() == 1).filter(v -> v.getOutDegree() == 1).filter(v -> document.getModel().getTree().getLabel(v) == null)
				.forEach(v -> {
					diNode2Views.put(v.getId(), new Triplet<>(networkView.getView(v.getFirstInEdge()), networkView.getView(v), networkView.getView(v.getFirstOutEdge())));
				});

		if (diNode2Views.size() > 0) {
			var id2EdgeId = new HashMap<Integer, Integer>();

			undo = () -> {
				var tree = document.getModel().getTree();
				for (var id : diNode2Views.keySet()) {
					var e = tree.findEdgeById(id2EdgeId.get(id));
					if (e != null) {
						var views = diNode2Views.get(id);
						var a = e.getSource();
						var b = e.getTarget();
						var v = tree.newNode(null, id);
						networkView.setView(v, views.getSecond());
						networkView.setView(tree.newEdge(a, v), views.getFirst());
						networkView.setView(tree.newEdge(v, b), views.getThird());
						networkView.removeView(e);
						tree.deleteEdge(e);
					}
				}
			};

			redo = () -> {
				id2EdgeId.clear();
				var tree = document.getModel().getTree();
				for (var id : diNode2Views.keySet()) {
					var v = tree.findNodeById(id);
					if (v != null) {
						var a = v.getFirstInEdge().getSource();
						var b = v.getFirstOutEdge().getTarget();
						networkView.removeView(v.getFirstInEdge());
						networkView.removeView(v.getFirstOutEdge());
						networkView.removeView(v);
						tree.deleteNode(v);
						var e = tree.newEdge(a, b);
						networkView.createEdgeView(e);
						id2EdgeId.put(id, e.getId());
					}
				}
			};
		}
	}

	@Override
	public boolean isUndoable() {
		return undo != null;
	}

	@Override
	public boolean isRedoable() {
		return redo != null;
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
