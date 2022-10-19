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

package org.husonlab.phylosketch.unused.commands;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import jloda.fx.undo.CompositeCommand;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import org.husonlab.phylosketch.unused.view.EdgeView;
import org.husonlab.phylosketch.unused.view.PhyloView;

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
     *
	 */
    public SplitEdgeCommand(Pane pane, PhyloView view, Edge e, Point2D location) {
        super("Split Edge");

        final PhyloTree graph = view.getGraph();

        final int oldEdgeId = e.getId();
        final double[] oldEdgeCoordinates = view.getEdgeView(e).getControlCoordinates();
        final double oldEdgeWidth = view.getEdgeView(e).getCurve().getStrokeWidth();
        final Paint oldEdgePaint = view.getEdgeView(e).getCurve().getStroke();

        final int sourceId = e.getSource().getId();
        final int targetId = e.getTarget().getId();

        undo = () -> {
            if (newNodeId > 0) {
                final Node v = graph.findNodeById(newNodeId);
                view.removeNode(v);
                graph.deleteNode(v);

                final Edge oldEdge = graph.newEdge(graph.findNodeById(sourceId), graph.findNodeById(targetId), null, oldEdgeId);
                final EdgeView ev = view.addEdge(oldEdge);
                ev.setControlCoordinates(oldEdgeCoordinates);
                ev.getCurve().setStrokeWidth(oldEdgeWidth);
                ev.getCurve().setStroke(oldEdgePaint);
            }
        };

        redo = () -> {
            final Edge oldEdge = graph.findEdgeById(oldEdgeId);
            view.removeEdge(oldEdge);
            graph.deleteEdge(oldEdge);

            Node newNode;
            if (newNodeId == 0) {
                newNode = graph.newNode();
                newNodeId = newNode.getId();
            } else
                newNode = graph.newNode(null, newNodeId);
            view.addNode(newNode, pane, location.getX(), location.getY());

            final Edge e1 = graph.newEdge(graph.findNodeById(sourceId), newNode);
            final EdgeView ev1 = view.addEdge(e1);
            ev1.getCurve().setStrokeWidth(oldEdgeWidth);
            ev1.getCurve().setStroke(oldEdgePaint);

            final Edge e2 = graph.newEdge(newNode, graph.findNodeById(targetId));
            final EdgeView ev2 = view.addEdge(e2);
            ev2.getCurve().setStrokeWidth(oldEdgeWidth);
            ev2.getCurve().setStroke(oldEdgePaint);
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

    public static CompositeCommand createAddDiNodesCommand(Pane pane, PhyloView view, Collection<Edge> edges) {
        final CompositeCommand command = new CompositeCommand("Add Di Nodes");
        for (Edge e : edges) {
            final Point2D location = new Point2D(0.5 * (view.getX(e.getSource()) + view.getX(e.getTarget())),
                    0.5 * (view.getY(e.getSource()) + view.getY(e.getTarget())));
            command.add(new SplitEdgeCommand(pane, view, e, location));
        }
        return command;
    }
}
