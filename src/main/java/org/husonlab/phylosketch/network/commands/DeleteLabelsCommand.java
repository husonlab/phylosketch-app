/*
 * DeleteLabelsCommand.java Copyright (C) 2023 Daniel H. Huson
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

import jloda.fx.control.RichTextLabel;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.graph.Node;
import org.husonlab.phylosketch.network.Document;

import java.util.Collection;
import java.util.HashMap;

public class DeleteLabelsCommand extends UndoableRedoableCommand {
	private Runnable undo;
	private Runnable redo;

	public DeleteLabelsCommand(Document document, Collection<Node> nodes) {
		super("delete labels");

		var networkView = document.getNetworkView();

		var idLabelMap = new HashMap<Integer, String>();
		for (var v : nodes) {
			var label = networkView.getView(v).label().getText();
			if (label != null && !label.isBlank()) {
				idLabelMap.put(v.getId(), label);
			}
		}

		if (idLabelMap.size() > 0) {
			undo = () -> {
				var tree = document.getModel().getTree();
				for (var id : idLabelMap.keySet()) {
					var text = idLabelMap.get(id);
					var v = tree.findNodeById(id);
					tree.setLabel(v, RichTextLabel.getRawText(text));
					networkView.getView(v).label().setText(text);
				}
			};
			redo = () -> {
				var tree = document.getModel().getTree();
				for (var id : idLabelMap.keySet()) {
					var v = tree.findNodeById(id);
					tree.setLabel(v, null);
					networkView.getView(v).label().setText(null);
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
