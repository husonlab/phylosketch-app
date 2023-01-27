/*
 * Normalize.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.algorithms;

import javafx.geometry.Point2D;
import jloda.fx.window.NotificationManager;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloTree;
import jloda.phylo.algorithms.RootedNetworkProperties;
import jloda.util.IteratorUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * computes the normalization of a rooted phylogenetic network
 * Daniel Huson, 3.2020
 */
public class Normalize {
	static public void apply(PhyloTree inputGraph, Function<Node, Point2D> inputCoordinatesGetter, PhyloTree outputGraph, BiConsumer<Node, Point2D> outputCoordinatesSetter) {
		var start = System.currentTimeMillis();

		var visibleAndLeaves = RootedNetworkProperties.computeAllVisibleNodes(inputGraph, null);
		visibleAndLeaves.addAll(inputGraph.nodeStream().filter(v -> v.getOutDegree() == 0).collect(Collectors.toList()));

		final NodeArray<Node> src2tar = new NodeArray<>(inputGraph);

		var sourceRoot = inputGraph.nodeStream().filter(v -> v.getInDegree() == 0).findAny();
		if (sourceRoot.isEmpty())
			return;

		// setup new nodes:
		for (var s : visibleAndLeaves) {
			var t = outputGraph.newNode();
			src2tar.put(s, t);
			if (sourceRoot.get() == s)
				outputGraph.setRoot(t);
			if (outputCoordinatesSetter != null)
				outputCoordinatesSetter.accept(t, inputCoordinatesGetter.apply(s));
			outputGraph.setLabel(t, inputGraph.getLabel(s));
		}

		final NodeArray<Collection<Node>> visibleAndLeavesBelow = inputGraph.newNodeArray();

		computeAllBelowRec(sourceRoot.get(), visibleAndLeaves, visibleAndLeavesBelow);

		// create full graph:
		for (var vs : visibleAndLeaves) {
			var vt = src2tar.get(vs);
			for (var ws : visibleAndLeavesBelow.get(vs)) {
				var wt = src2tar.get(ws);
				outputGraph.newEdge(vt, wt);
			}
		}

		if (true) {
			NodeArray<Set<Node>> parents = outputGraph.newNodeArray();
			for (var v : outputGraph.nodes()) {
				parents.put(v, IteratorUtils.asSet(v.parents()));
			}

			for (var e : outputGraph.edges()) {
				for (var f : e.getTarget().inEdges()) {
					if (f != e && parents.get(f.getSource()).contains(e.getSource())) {
						outputGraph.deleteEdge(e);
						break;
					}
				}
			}
			//var edgesToDelete = target.edgeStream().filter(isReducible).collect(Collectors.toList());
			//edgesToDelete.forEach(target::deleteEdge);
		} else {
			// transitive reduction:
			var edgesToDelete = new HashSet<Edge>();
			for (var x : outputGraph.nodes()) {
				for (var z : x.children()) {
					for (var y : x.children()) {
						if (y.isChild(z)) {
							edgesToDelete.add(x.getCommonEdge(z));
							break;
						}
					}
				}
			}
			edgesToDelete.forEach(outputGraph::deleteEdge);
		}

		// remove digons:

		var nodesToRemove = outputGraph.nodeStream().filter(v -> v.getInDegree() == 1 && v.getOutDegree() == 1).collect(Collectors.toSet());

		for (var v : nodesToRemove) {
			outputGraph.delDivertex(v);
		}

		var srcNodeLabels = inputGraph.nodeStream().map(inputGraph::getLabel).filter(s -> s != null && s.length() > 0).collect(Collectors.toList());
		var targetNodeLabels = outputGraph.nodeStream().map(outputGraph::getLabel).filter(s -> s != null && s.length() > 0).collect(Collectors.toSet());

		var lost = srcNodeLabels.stream().filter(s -> !targetNodeLabels.contains(s)).count();
		if (lost > 0)
			NotificationManager.showInformation("Number of labeled internal nodes removed: " + lost);

		var finish = System.currentTimeMillis();

		var sourceReticulations = inputGraph.nodeStream().filter(v -> v.getInDegree() > 1).count();
		var targetReticulations = outputGraph.nodeStream().filter(v -> v.getInDegree() > 1).count();

		System.err.printf("Network with %,d nodes, %,d edges and %,d reticulations -> normalization with %,d nodes, %,d edges and %,d reticulations (time: %ds)%n",
				inputGraph.getNumberOfNodes(), inputGraph.getNumberOfEdges(), sourceReticulations, outputGraph.getNumberOfNodes(), outputGraph.getNumberOfEdges(), targetReticulations,
				((finish - start) / 1000));
	}

	/**
	 * collect all visible nodes below a given node
	 *
	 * @param v            the current node
	 * @param visible      the set of all visible or leaf nodes
	 * @param visibleBelow the mapping of v to all below
	 * @return the set of all below v
	 */
	private static Set<Node> computeAllBelowRec(Node v, Set<Node> visible, NodeArray<Collection<Node>> visibleBelow) {
		var set = new HashSet<Node>();

		for (var w : v.children()) {
			set.addAll(computeAllBelowRec(w, visible, visibleBelow));
			if (visible.contains(w))
				set.add(w);
		}
		visibleBelow.put(v, set);
		return set;
	}
}
