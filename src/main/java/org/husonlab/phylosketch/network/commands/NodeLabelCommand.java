/*
 * NodeLabelCommand.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.beans.property.StringProperty;
import jloda.fx.control.RichTextLabel;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;

public class NodeLabelCommand extends UndoableRedoableCommand {
	private final Runnable undo;
	private final Runnable redo;

	public NodeLabelCommand(Node v, StringProperty label, String oldValue, String newValue) {
		super("change label");

		undo = () -> {
			var tree = (PhyloTree) v.getOwner();
			tree.setLabel(v, RichTextLabel.getRawText(oldValue));
			label.set(oldValue);
		};

		redo = () -> {
			var tree = (PhyloTree) v.getOwner();
			tree.setLabel(v, RichTextLabel.getRawText(newValue));
			label.set(newValue);
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
