/*
 * ShowArrowHeadCommand.java Copyright (C) 2022 Daniel H. Huson
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

import java.util.HashSet;

/**
 * show arrow head command
 * Daniel Huson, 12.2022
 */
public class ShowArrowHeadCommand extends UndoableRedoableCommand {
	private Runnable undo;
	private Runnable redo;

	public ShowArrowHeadCommand(Document document, boolean showArrow) {
		super("edge style");
		var toChange = new HashSet<Integer>();

		var networkView = document.getNetworkView();
		for (var e : document.getSelectedOrAllEdges()) {
			if (networkView.getView(e).isShowArrowHead() != showArrow)
				toChange.add(e.getId());
		}
		if (toChange.size() > 0) {
			undo = () -> {
				var tree = document.getModel().getTree();
				for (var id : toChange) {
					networkView.getView(tree.findEdgeById(id)).setShowArrowHead(!showArrow);
				}
			};
			redo = () -> {
				var tree = document.getModel().getTree();
				for (var id : toChange) {
					networkView.getView(tree.findEdgeById(id)).setShowArrowHead(showArrow);
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
		if (isUndoable())
			undo.run();
	}

	@Override
	public void redo() {
		if (isRedoable())
			redo.run();
	}
}
