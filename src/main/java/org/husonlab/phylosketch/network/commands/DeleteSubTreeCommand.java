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

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.fx.window.MainWindowManager;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import org.husonlab.phylosketch.network.Document;
import org.husonlab.phylosketch.network.NetworkModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * delete subtree
 * Daniel Huson, 12.2022
 */
public class DeleteSubTreeCommand extends UndoableRedoableCommand {
	private Runnable undo;
	private Runnable redo;

	/**
	 * constructor
	 */
	public DeleteSubTreeCommand(Document document, Node v) {
		super("delete subtree");


		if (document.getModel().getTree().getRoot() != v) {
			var oldTree = new PhyloTree();
			var nodeTranslate = new HashMap<Integer, Point2D>();
			var nodeLabelLayout = new HashMap<Integer, Point2D>();

			var oldToScale = document.isToScale();

			oldTree.copy(document.getModel().getTree());
			for (var w : document.getModel().getTree().nodes()) {
				var view = document.getNetworkView().getView(w);
				if (view != null) {
					var shape = document.getNetworkView().getView(w).shape();
					if (shape != null)
						nodeTranslate.put(w.getId(), new Point2D(shape.getTranslateX(), shape.getTranslateY()));
					var label = document.getNetworkView().getView(w).label();
					if (label != null)
						nodeLabelLayout.put(w.getId(), new Point2D(label.getLayoutX(), label.getLayoutY()));
				}
			}
			undo = () -> {
				document.getModel().getTree().copy(oldTree);
				document.setToScale(oldToScale);
				document.updateModelAndView();
				for (var w : document.getModel().getTree().nodes()) {
					if (nodeTranslate.containsKey(w.getId())) {
						var location = nodeTranslate.get(w.getId());
						var shape = document.getNetworkView().getView(w).shape();
						shape.setTranslateX(location.getX());
						shape.setTranslateY(location.getY());
						var offset = nodeLabelLayout.get(w.getId());
						var label = document.getNetworkView().getView(w).label();
						label.setLayoutX(offset.getX());
						label.setLayoutY(offset.getY());
					}
				}
				ChangeEdgeShapeCommand.changeEdgeShape(document, document.getEdgeGlyph());
			};

			redo = () -> {
				var tree = document.getModel().getTree();
				try (var allBelow = tree.newNodeSet()) {
					tree.preorderTraversal(v, allBelow::add);
					allBelow.forEach(tree::deleteNode);
				}
				document.updateModelAndView();
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
