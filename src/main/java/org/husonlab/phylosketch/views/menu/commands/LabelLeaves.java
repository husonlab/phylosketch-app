/*
 * LabelLeaves.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.stage.Stage;
import jloda.fx.label.EditLabelDialog;
import jloda.graph.Node;
import jloda.util.Pair;
import org.husonlab.phylosketch.network.Document;

import java.util.*;
import java.util.stream.Collectors;

/**
 * label all leaves
 * Daniel Huson, 1.2020
 */
public class LabelLeaves {

	public static List<ChangeNodeLabelsCommand.Data> labelLeavesABC(Document document) {
		var graph = document.getModel().getTree();
		var networkView = document.getNetworkView();
		var seen = new HashSet<String>();
		graph.nodeStream().filter(v -> graph.getLabel(v) != null).forEach(v -> seen.add(graph.getLabel(v)));

		return sortLeaves(document).stream().filter(v -> networkView.getView(v).label().getRawText().length() == 0).map(v -> new ChangeNodeLabelsCommand.Data(v.getId(), document.getNetworkView().getView(v).label().getText(), getNextLabelABC(seen))).collect(Collectors.toList());
	}

	public static List<ChangeNodeLabelsCommand.Data> labelInternalABC(Document document) {
		var graph = document.getModel().getTree();
		var networkView = document.getNetworkView();
		var seen = new HashSet<String>();
		graph.nodeStream().filter(v -> graph.getLabel(v) != null).forEach(v -> seen.add(graph.getLabel(v)));
		return sortInternal(document).stream().filter(v -> networkView.getView(v).label().getRawText().length() == 0)
				.map(v -> new ChangeNodeLabelsCommand.Data(v.getId(), networkView.getView(v).label().getText(), getNextLabelABC(seen))).collect(Collectors.toList());
	}

	public static List<ChangeNodeLabelsCommand.Data> labelLeaves123(Document document) {
		var graph = document.getModel().getTree();
		var networkView = document.getNetworkView();
		var seen = new HashSet<String>();
		graph.nodeStream().filter(v -> graph.getLabel(v) != null).forEach(v -> seen.add(graph.getLabel(v)));
		return sortLeaves(document).stream().filter(v -> networkView.getView(v).label().getRawText().length() == 0)
				.map(v -> new ChangeNodeLabelsCommand.Data(v.getId(), networkView.getView(v).label().getText(), getNextLabel123(seen))).collect(Collectors.toList());
	}

	public static List<ChangeNodeLabelsCommand.Data> labelInternal123(Document document) {
		var graph = document.getModel().getTree();
		var networkView = document.getNetworkView();
		var seen = new HashSet<String>();
		graph.nodeStream().filter(v -> graph.getLabel(v) != null).forEach(v -> seen.add(graph.getLabel(v)));
		return sortInternal(document).stream().filter(v -> networkView.getView(v).label().getRawText().length() == 0)
				.map(v -> new ChangeNodeLabelsCommand.Data(v.getId(), networkView.getView(v).label().getText(), getNextLabel123(seen))).collect(Collectors.toList());
	}

	public static void labelLeaves(Stage owner, Document document) {
		var leaves = sortLeaves(document);

		for (var v : leaves) {
			document.getNodeSelection().clearSelection();
			document.getNodeSelection().select(v);
			if (!showNodeLabelDialog(owner, document, v))
				break;
		}
	}

	private static List<Node> sortLeaves(Document document) {
		var graph = document.getModel().getTree();
		var networkView = document.getNetworkView();

		final List<Pair<Node, Double>> list;
		if (true) // root is left
			list = graph.nodeStream().filter(v -> v.getOutDegree() == 0).map(v -> new Pair<>(v, networkView.getView(v).shape().getTranslateY())).collect(Collectors.toList());
		else
			list = graph.nodeStream().filter(v -> v.getOutDegree() == 0).map(v -> new Pair<>(v, networkView.getView(v).shape().getTranslateX())).collect(Collectors.toList());

		return list.stream().sorted(Comparator.comparingDouble(Pair::getSecond)).map(Pair::getFirst).collect(Collectors.toList());
	}

	private static List<Node> sortInternal(Document document) {
		var graph = document.getModel().getTree();
		var networkView = document.getNetworkView();

		final List<Pair<Node, Double>> list;
		if (true) // root is left
			list = graph.nodeStream().filter(v -> v.getOutDegree() > 0).map(v -> new Pair<>(v, networkView.getView(v).shape().getTranslateY())).collect(Collectors.toList());
		else
			list = graph.nodeStream().filter(v -> v.getOutDegree() > 0).map(v -> new Pair<>(v, networkView.getView(v).shape().getTranslateX())).collect(Collectors.toList());

		return list.stream().sorted(Comparator.comparingDouble(Pair::getSecond)).map(Pair::getFirst).collect(Collectors.toList());
	}

	public static String getNextLabelABC(Set<String> seen) {
		int id = 0;
		String label = "A";
		while (seen.contains(label)) {
			id++;
			int letter = ('A' + (id % 26));
			int number = id / 26;
			label = (char) letter + (number > 0 ? "_" + number : "");
		}
		seen.add(label);
		return label;
	}

	public static String getNextLabel123(Set<String> seen) {
		int id = 1;
		String label = "" + id;
		while (seen.contains(label)) {
			id++;
			label = "" + id;

		}
		seen.add(label);
		return label;
	}

	public static boolean showNodeLabelDialog(Stage owner, Document document, Node v) {
		var networkView = document.getNetworkView();
		var editLabelDialog = new EditLabelDialog(owner, networkView.getView(v).label());
		var result = editLabelDialog.showAndWait();
		if (result.isPresent()) {
			var id = v.getId();
			var oldLabel = networkView.getView(v).label().getText();
			var newLabel = result.get();
			document.getUndoManager().doAndAdd(new ChangeNodeLabelsCommand(document, Collections.singletonList(new ChangeNodeLabelsCommand.Data(id, oldLabel, newLabel))));
			return true;
		} else
			return false;
	}

}
