/*
 * RunNormalize.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.geometry.Point2D;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.phylo.PhyloTree;
import org.husonlab.phylosketch.algorithms.Normalize;
import org.husonlab.phylosketch.network.Document;
import org.husonlab.phylosketch.network.commands.ReplaceNetworkCommand;


/**
 * run the normalization algorithm
 * Daniel Huson, 1.2022
 */
public class NormalizeCommand extends UndoableRedoableCommand {
	private final Runnable undo;
	private final Runnable redo;

	public NormalizeCommand(Document document) {
		super("normalize");
		var networkView = document.getNetworkView();
		var tree = new PhyloTree();
		Normalize.apply(document.getModel().getTree(), v -> new Point2D(networkView.getView(v).shape().getTranslateX(), networkView.getView(v).shape().getTranslateY()), tree, null);
		tree.edgeStream().filter(e -> e.getTarget().getInDegree() > 1).forEach(e -> tree.setReticulate(e, true));
		var replaceCommand = new ReplaceNetworkCommand(document, tree.toBracketString(false));
		undo = replaceCommand::undo;
		redo = replaceCommand::redo;
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
