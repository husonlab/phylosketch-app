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

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.util.Pair;
import org.husonlab.phylosketch.network.Document;

import java.util.ArrayList;
import java.util.Objects;

/**
 * change line color command
 * Daniel Huson, 12.2022
 */
public class ChangeLineColorCommand extends UndoableRedoableCommand {
	private Runnable undo;
	private Runnable redo;

	public ChangeLineColorCommand(Document document, Color color) {
		super("line color");
		var networkView = document.getNetworkView();
		var edgeOldColor = new ArrayList<Pair<Integer, Paint>>();
		for (var e : document.getSelectedOrAllEdges()) {
			if (!Objects.equals(color, networkView.getView(e).getStroke()))
				edgeOldColor.add(new Pair<>(e.getId(), networkView.getView(e).getStroke()));
		}
		if (edgeOldColor.size() > 0) {
			undo = () -> {
				var tree = document.getModel().getTree();
				for (var pair : edgeOldColor) {
					var e = pair.getFirst();
					var value = pair.getSecond();
					networkView.getView(tree.findEdgeById(e)).setStroke(value);
				}
			};
			redo = () -> {
				var tree = document.getModel().getTree();
				for (var pair : edgeOldColor) {
					var e = pair.getFirst();
					networkView.getView(tree.findEdgeById(e)).setStroke(color);
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
