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

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.fx.window.MainWindowManager;
import org.husonlab.phylosketch.network.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * change label color
 * Daniel Huson, 12.2022
 */
public class ChangeLabelColorCommand extends UndoableRedoableCommand {
	private Runnable undo;
	private Runnable redo;

	/**
	 * constructor
	 */
	public ChangeLabelColorCommand(Document document, Color color0) {
		super("label color");

		Color color;
		if (MainWindowManager.isUseDarkTheme() && color0.equals(Color.WHITE) || !MainWindowManager.isUseDarkTheme() && color0.equals(Color.BLACK))
			color = null;
		else
			color = color0;

		var networkView = document.getNetworkView();

		final var tree = networkView.getTree();

		var nodeData = new ArrayList<Integer>();
		final Map<Integer, Paint> oldNodeColor = new HashMap<>();
		for (var v : document.getSelectedOrAllNodes()) {
			var label = networkView.getView(v).label();
			if (label != null && label.getText().length() > 0 && !Objects.equals(label.getTextFill(), color)) {
				oldNodeColor.put(v.getId(), label.getTextFill());
				nodeData.add(v.getId());
			}
		}

		var edgeData = new ArrayList<Integer>();
		final Map<Integer, Paint> oldEdgeColor = new HashMap<>();
		for (var e : document.getSelectedOrAllEdges()) {
			var label = networkView.getView(e).label();
			if (label != null && label.getText().length() > 0) {
				oldEdgeColor.put(e.getId(), label.getTextFill());
				edgeData.add(e.getId());
			}
		}

		if (nodeData.size() > 0 || edgeData.size() > 0) {
			undo = () -> {
				nodeData.forEach(id -> {
					var v = tree.findNodeById(id);
					var label = networkView.getView(v).label();
					label.setTextFill(oldNodeColor.get(id));
					document.getModel().getTree().setLabel(v, label.getText());
				});
				edgeData.forEach(id -> {
					var e = tree.findEdgeById(id);
					var label = networkView.getView(e).label();
					label.setTextFill(oldEdgeColor.get(id));
					document.getModel().getTree().setLabel(e, label.getText());
				});
			};

			redo = () -> {
				nodeData.forEach(id -> {
					var v = tree.findNodeById(id);
					var label = networkView.getView(v).label();
					label.setTextFill(color);
					document.getModel().getTree().setLabel(v, label.getText());
				});
				edgeData.forEach(id -> {
					var e = tree.findEdgeById(id);
					var label = networkView.getView(e).label();
					label.setTextFill(color);
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
