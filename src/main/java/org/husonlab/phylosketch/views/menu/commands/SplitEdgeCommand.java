/*
 * SplitEdgeCommand.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.paint.Paint;
import jloda.fx.undo.CompositeCommand;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.graph.Edge;
import jloda.graph.Node;
import org.husonlab.phylosketch.network.Document;

import java.util.Collection;

/**
 * split edge command
 * Daniel Huson, 2.2020
 */
public class SplitEdgeCommand extends UndoableRedoableCommand {
	private final Runnable undo;
	private final Runnable redo;

	private int newNodeId = 0;

	/**
	 * constructor
	 */
	public SplitEdgeCommand(Document document, Edge e, Point2D location) {
		super("insert di-node");

		var tree = document.getModel().getTree();
		var networkView = document.getNetworkView();

		final int oldEdgeId = e.getId();
		final double[] oldEdgeCoordinates = networkView.getView(e).getControlCoordinates();
		final double oldEdgeWidth = networkView.getView(e).getStrokeWidth();
		final Paint oldEdgePaint = networkView.getView(e).getStroke();

		final int sourceId = e.getSource().getId();
		final int targetId = e.getTarget().getId();

		undo = () -> {
			if (newNodeId > 0) {
				final Node v = tree.findNodeById(newNodeId);
				networkView.removeView(v);
				tree.deleteNode(v);

				final var oldEdge = tree.newEdge(tree.findNodeById(sourceId), tree.findNodeById(targetId), null, oldEdgeId);
				networkView.createEdgeView(oldEdge);
				networkView.getView(oldEdge).setControlCoordinates(oldEdgeCoordinates);
				networkView.getView(oldEdge).setStrokeWidth(oldEdgeWidth);
				networkView.getView(oldEdge).setStroke(oldEdgePaint);
			}
		};

		redo = () -> {
			var oldEdge = tree.findEdgeById(oldEdgeId);
			networkView.removeView(oldEdge);
			tree.deleteEdge(oldEdge);

			Node newNode;
			if (newNodeId == 0) {
				newNode = tree.newNode();
				newNodeId = newNode.getId();
				Platform.runLater(() -> document.getNodeSelection().select(newNode));
			} else
				newNode = tree.newNode(null, newNodeId);
			networkView.createShapeAndLabel(newNode, location.getX(), location.getY(), null, 0.0, 0.0);

			var e1 = tree.newEdge(tree.findNodeById(sourceId), newNode);
			var ev1 = networkView.createEdgeView(e1);
			ev1.setStrokeWidth(oldEdgeWidth);
			ev1.setStroke(oldEdgePaint);

			var e2 = tree.newEdge(newNode, tree.findNodeById(targetId));
			var ev2 = networkView.createEdgeView(e2);
			ev2.setStrokeWidth(oldEdgeWidth);
			ev2.setStroke(oldEdgePaint);
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

	public static CompositeCommand createAddDiNodesCommand(Document document, Collection<Edge> edges) {
		final CompositeCommand command = new CompositeCommand("insert di-nodes");
		var networkView = document.getNetworkView();
		for (Edge e : edges) {
			final Point2D location = new Point2D(0.5 * (networkView.getView(e.getSource()).shape().getTranslateX() + networkView.getView(e.getTarget()).shape().getTranslateX()),
					0.5 * (networkView.getView(e.getSource()).shape().getTranslateY() + networkView.getView(e.getTarget()).shape().getTranslateY()));
			command.add(new SplitEdgeCommand(document, e, location));
		}
		return command;
	}
}
