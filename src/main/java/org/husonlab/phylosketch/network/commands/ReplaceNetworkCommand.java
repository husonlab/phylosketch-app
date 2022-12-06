/*
 * ReplaceNetworkCommand.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.geometry.Point2D;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.phylo.PhyloTree;
import org.husonlab.phylosketch.network.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * replace the current tree by the new tree provided as a newick string
 * Daniel Huson, 12.22
 */
public class ReplaceNetworkCommand extends UndoableRedoableCommand {
	private final Document document;
	private final PhyloTree oldTree = new PhyloTree();
	private final Map<Integer, Point2D> nodeTranslate = new HashMap<>();
	private final Map<Integer, Point2D> nodeLabelLayout = new HashMap<>();

	private final PhyloTree newTree = new PhyloTree();
	private boolean canUndoRedo = true;

	public ReplaceNetworkCommand(Document document, String newNewick) {
		super("Replace Network");
		this.document = document;
		oldTree.copy(document.getModel().getTree());
		for (var v : document.getModel().getTree().nodes()) {
			var shape = document.getNetworkView().getView(v).shape();
			nodeTranslate.put(v.getId(), new Point2D(shape.getTranslateX(), shape.getTranslateY()));
			var label = document.getNetworkView().getView(v).label();
			nodeLabelLayout.put(v.getId(), new Point2D(label.getLayoutX(), label.getLayoutY()));
		}
		try {
			newTree.parseBracketNotation(newNewick, true);
		} catch (IOException ignored) {
			canUndoRedo = false;
		}
	}

	@Override
	public boolean isUndoable() {
		return canUndoRedo;
	}

	@Override
	public boolean isRedoable() {
		return canUndoRedo;
	}

	@Override
	public void undo() {
		document.getModel().getTree().copy(oldTree);
		document.updateModelAndView();
		for (var v : document.getModel().getTree().nodes()) {
			if (nodeTranslate.containsKey(v.getId())) {
				var location = nodeTranslate.get(v.getId());
				var shape = document.getNetworkView().getView(v).shape();
				shape.setTranslateX(location.getX());
				shape.setTranslateY(location.getY());
				var offset = nodeLabelLayout.get(v.getId());
				var label = document.getNetworkView().getView(v).label();
				label.setLayoutX(offset.getX());
				label.setLayoutY(offset.getY());
			}
		}
	}

	@Override
	public void redo() {
		document.getModel().getTree().copy(newTree);
		document.updateModelAndView();
		if (document.getNetworkView().getFontScale() != 1.0) {
			for (var v : document.getModel().getTree().nodes()) {
				var label = document.getNetworkView().getView(v).label();
				if (label != null) {
					var diff = 0.5 * (document.getNetworkView().getFontScale() - 1) * label.getFontSize();
					label.setLayoutY(label.getLayoutY() - diff);
				}
			}
		}
	}
}
