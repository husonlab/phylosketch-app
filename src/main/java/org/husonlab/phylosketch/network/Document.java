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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import jloda.fx.control.RichTextLabel;
import jloda.fx.graph.GraphFX;
import jloda.fx.selection.SelectionModel;
import jloda.fx.selection.SetSelectionModel;
import jloda.fx.undo.UndoManager;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.phylo.algorithms.RootedNetworkProperties;
import jloda.util.NumberUtils;

import java.io.IOException;


public class Document {
	private final UndoManager undoManager = new UndoManager();

	private final SelectionModel<Node> nodeSelection = new SetSelectionModel<>();
	private final SelectionModel<Edge> edgeSelection = new SetSelectionModel<>();

	private final NetworkModel model;
	private final NetworkView networkView;

	private final GraphFX<PhyloTree> graphFX;

	private final StringProperty info = new SimpleStringProperty(this, "info", "");

	private final BooleanProperty toScale = new SimpleBooleanProperty(this, "toScale", false);
	private final BooleanProperty showHTML = new SimpleBooleanProperty(this, "showHTML", false);

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

		graphFX.lastUpdateProperty().addListener(e -> {
			var tree = getModel().getTree();
			info.set(RootedNetworkProperties.computeInfoString(tree));

			var newick = getNewickString();
			if (!DefaultOptions.getTrees().contains(newick))
				DefaultOptions.getTrees().add(0, newick);
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

	public String getInfo() {
		return info.get();
	}

	public StringProperty infoProperty() {
		return info;
	}

	public boolean setNewickString(String newick) {
		try {
			model.getTree().parseBracketNotation(newick, true);
			updateModelAndView();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public void updateModelAndView() {
		model.computeEmbedding(isToScale(), 200, 200);
		NetworkPresenter.model2view(model, getNetworkView());
	}

	public String getNewickString() {
		return getNewickString(isToScale(), isShowHTML());
	}

	public String getNewickString(boolean toScale, boolean showHTML) {
		if (toScale && !model.getTree().hasEdgeWeights()) {
			for (var e : model.getTree().edges()) {
				if (!model.getTree().isReticulateEdge(e)) {
					var weight = Math.max(1.0, Math.round(Math.abs(networkView.getView(e.getSource()).shape().getTranslateX() - (networkView.getView(e.getTarget()).shape().getTranslateX()))));
					model.getTree().setWeight(e, weight);
				}
			}
		}

		if (!showHTML) {
			var tmpTree = new PhyloTree(model.getTree());
			for (var v : tmpTree.nodes()) {
				var label = tmpTree.getLabel(v);
				if (label != null && !label.isBlank()) {
					tmpTree.setLabel(v, RichTextLabel.getRawText(label));
				}
			}
			return tmpTree.toBracketString(toScale) + ";";
		} else
			return model.getTree().toBracketString(toScale) + ";";
	}

	public Iterable<Node> getSelectedOrAllNodes() {
		if (nodeSelection.size() > 0)
			return nodeSelection.getSelectedItems();
		else
			return getModel().getTree().nodes();
	}

	public Iterable<Edge> getSelectedOrAllEdges() {
		if (edgeSelection.size() > 0)
			return edgeSelection.getSelectedItems();
		else
			return getModel().getTree().edges();
	}

	public boolean isToScale() {
		return toScale.get();
	}

	public BooleanProperty toScaleProperty() {
		return toScale;
	}

	public void setToScale(boolean toScale) {
		this.toScale.set(toScale);
	}

	public boolean isShowHTML() {
		return showHTML.get();
	}

	public BooleanProperty showHTMLProperty() {
		return showHTML;
	}

	public void setShowHTML(boolean showHTML) {
		this.showHTML.set(showHTML);
	}
}
