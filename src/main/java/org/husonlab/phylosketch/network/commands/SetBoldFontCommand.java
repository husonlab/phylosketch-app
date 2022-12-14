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

import jloda.fx.undo.UndoableRedoableCommand;
import org.husonlab.phylosketch.network.Document;

import java.util.ArrayList;

/**
 * set bold font
 * Daniel Huson, 12.2022
 */
public class SetBoldFontCommand extends UndoableRedoableCommand {
	private Runnable undo;
	private Runnable redo;

	/**
	 * constructor
	 */
	public SetBoldFontCommand(Document document, boolean bold) {
		super("bold");

		var networkView = document.getNetworkView();

		final var tree = networkView.getTree();

		var nodeData = new ArrayList<Integer>();
		for (var v : document.getSelectedOrAllNodes()) {
			var label = networkView.getView(v).label();
			if (label != null && label.getText().length() > 0 && label.isBold() != bold) {
				nodeData.add(v.getId());
			}
		}

		var edgeData = new ArrayList<Integer>();
		for (var e : document.getSelectedOrAllEdges()) {
			var label = networkView.getView(e).label();
			if (label != null && label.getText().length() > 0 && label.isBold() != bold) {
				edgeData.add(e.getId());
			}
		}

		if (nodeData.size() > 0 || edgeData.size() > 0) {
			undo = () -> {
				nodeData.forEach(id -> {
					var v = tree.findNodeById(id);
					var label = networkView.getView(v).label();
					label.setBold(!bold);
					document.getModel().getTree().setLabel(v, label.getText());
				});
				edgeData.forEach(id -> {
					var e = tree.findEdgeById(id);
					var label = networkView.getView(e).label();
					label.setBold(!bold);
					document.getModel().getTree().setLabel(e, label.getText());
				});
			};

			redo = () -> {
				nodeData.forEach(id -> {
					var v = tree.findNodeById(id);
					var label = networkView.getView(v).label();
					label.setBold(bold);
					document.getModel().getTree().setLabel(v, label.getText());
				});
				edgeData.forEach(id -> {
					var e = tree.findEdgeById(id);
					var label = networkView.getView(e).label();
					label.setBold(bold);
					document.getModel().getTree().setLabel(e, label.getText());
				});
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
