/*
 * NewEdgeAndNodeCommand.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import jloda.fx.selection.SelectionModel;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.graph.Edge;
import jloda.graph.Node;
import org.husonlab.phylosketch.network.DefaultOptions;
import org.husonlab.phylosketch.network.NetworkPresenter;
import org.husonlab.phylosketch.network.NetworkView;

public class NewEdgeAndNodeCommand extends UndoableRedoableCommand {
	private boolean firstTime = true;
	final private Runnable undo;
	final private Runnable redo;

	private int edgeId;
	private int wId;

	/**
	 * construct
	 */
	public NewEdgeAndNodeCommand(NetworkView networkView, SelectionModel<Node> nodeSelection, Node a, final Node b, double translateX, double translateY) {
		super("Add Edge");
		final var tree = networkView.getTree();

		final int aId = a.getId();
		final int bId = (b != null ? b.getId() : 0);

		undo = () -> {
			if (wId > 0) {
				final var v = tree.findNodeById(aId);
				var w = tree.findNodeById(wId);
				var translateTransition = new TranslateTransition(Duration.millis(100), networkView.getView(w).shape());
				translateTransition.setToX(networkView.getView(v).shape().getTranslateX());
				translateTransition.setToY(networkView.getView(v).shape().getTranslateY());
				translateTransition.setOnFinished(z -> {
					if (edgeId > 0) {
						var e = tree.findEdgeById(edgeId);
						networkView.removeView(e);
						tree.deleteEdge(e);
					}
					networkView.removeView(w);
					tree.deleteNode(w);
				});
				translateTransition.play();
			} else if (edgeId > 0) {
				var e = tree.findEdgeById(edgeId);
				var w = e.getTarget();
				networkView.removeView(e);
				tree.deleteEdge(e);
				if (w.getInDegree() == 1 && networkView.getView(w.getFirstInEdge()).getStroke().equals(DefaultOptions.getReticulateColor())) {
					networkView.getView(w.getFirstInEdge()).setStroke(DefaultOptions.getEdgeColor());
				}
			}
		};

		redo = () -> {
			final var v = tree.findNodeById(aId);
			Node w;
			if (bId == 0) {
				if (wId == 0) {
					w = tree.newNode();
					wId = w.getId();
				} else
					w = tree.newNode(null, wId);
				if (firstTime) {
					networkView.createShapeAndLabel(w, translateX, translateY, "", 10, -0.5 * NetworkPresenter.DEFAULT_FONT_SIZE.get());
					firstTime = false;
				} else {
					networkView.createShapeAndLabel(w, networkView.getView(v).shape().getTranslateX(), networkView.getView(v).shape().getTranslateY(), "", 10, -0.5 * NetworkPresenter.DEFAULT_FONT_SIZE.get());
					var translateTransition = new TranslateTransition(Duration.millis(100), networkView.getView(w).shape());
					translateTransition.setToX(translateX);
					translateTransition.setToY(translateY);
					translateTransition.play();
				}
			} else
				w = tree.findNodeById(bId);

			if (v.getCommonEdge(w) == null && v != w) {
				if (w.getInDegree() == 1) {
					for (var f : w.inEdges()) {
						if (networkView.getView(f).getStroke().equals(DefaultOptions.getEdgeColor()))
							networkView.getView(f).setStroke(DefaultOptions.getReticulateColor());
					}
				}
				Edge e;
				if (edgeId == 0) {
					e = tree.newEdge(v, w);
					edgeId = e.getId();
				} else {
					e = tree.newEdge(v, w, null, edgeId);
				}
				networkView.createEdgeView(e);
				if (e.getTarget().getInDegree() > 1)
					networkView.getView(e).setStroke(DefaultOptions.getReticulateColor());
			}
			if (wId > 0) Platform.runLater(() -> {
				nodeSelection.clearSelection();
				nodeSelection.select(tree.findNodeById(wId));
			});
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
