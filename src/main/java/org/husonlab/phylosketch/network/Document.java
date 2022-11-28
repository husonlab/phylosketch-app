/*
 * Document.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.network;

import javafx.collections.ListChangeListener;
import jloda.fx.graph.GraphFX;
import jloda.fx.selection.SelectionModel;
import jloda.fx.selection.SetSelectionModel;
import jloda.fx.undo.UndoManager;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;


public class Document {
	private final UndoManager undoManager = new UndoManager();

	private final SelectionModel<Node> nodeSelection = new SetSelectionModel<>();
	private final SelectionModel<Edge> edgeSelection = new SetSelectionModel<>();

	private final NetworkModel model;
	private final NetworkView networkView;

	private final GraphFX<PhyloTree> graphFX;


	public Document() {
		model = new NetworkModel();
		networkView = new NetworkView(this);
		graphFX = new GraphFX<>(model.getTree());

		graphFX.getNodeList().addListener((ListChangeListener<Node>) a -> {
			while (a.next()) {
				if (a.wasRemoved()) {
					nodeSelection.clearSelection(a.getRemoved());
				}
			}
		});
		graphFX.getEdgeList().addListener((ListChangeListener<Edge>) a -> {
			while (a.next()) {
				if (a.wasRemoved()) {
					edgeSelection.clearSelection(a.getRemoved());
				}
			}
		});
	}

	public SelectionModel<Node> getNodeSelection() {
		return nodeSelection;
	}

	public SelectionModel<Edge> getEdgeSelection() {
		return edgeSelection;
	}

	public NetworkModel getModel() {
		return model;
	}

	public NetworkView getNetworkView() {
		return networkView;
	}

	public GraphFX<PhyloTree> getGraphFX() {
		return graphFX;
	}

	public UndoManager getUndoManager() {
		return undoManager;
	}
}
