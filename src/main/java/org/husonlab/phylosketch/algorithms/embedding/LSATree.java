/*
 * LSATree.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.algorithms.embedding;


import jloda.graph.*;
import jloda.phylo.PhyloTree;

import java.util.*;
import java.util.stream.Collectors;

/**
 * some methods for LSA trees
 * Daniel Huson, 7.2007
 */
public class LSATree {
	/**
	 * computes the node-to-guide-tree children map
	 */
	public static void computeNodeLSAChildrenMap(PhyloTree tree) {
		try (NodeArray<Node> reticulation2LSA = tree.newNodeArray()) {
			computeNodeLSAChildrenMap(tree, reticulation2LSA);
		}
	}

	/**
	 * given a reticulate network, returns a mapping of each node to a list of its children in the LSA tree
	 *
	 * @param reticulation2LSA is returned here
	 */
	public static void computeNodeLSAChildrenMap(PhyloTree tree, NodeArray<Node> reticulation2LSA) {
		tree.getLSAChildrenMap().clear();

		if (tree.getRoot() != null) {
			// first we compute the reticulate node to lsa node mapping:
			computeReticulation2LSA(tree, reticulation2LSA);

			for (Node v : tree.nodes()) {
				var children = v.outEdgesStream(false).filter(e -> !tree.isReticulateEdge(e))
						.map(Edge::getTarget).collect(Collectors.toList());
				tree.getLSAChildrenMap().put(v, children);
			}
			for (var v : tree.nodes()) {
				var lsa = reticulation2LSA.get(v);
				if (lsa != null)
					tree.getLSAChildrenMap().get(lsa).add(v);
			}
		}
	}

	/**
	 * recursively determine the number of edges between a reticulation and its lsa
	 *
	 * @return number of edges between a reticulation and its lsa
	 */
	private static NodeIntArray computeReticulationSize(PhyloTree tree, NodeArray<Node> reticulation2LSA) {
		NodeIntArray rSize = new NodeIntArray(tree);

		for (var r : tree.nodes()) {
			var lsa = reticulation2LSA.get(r);
			if (lsa != null) {
				System.err.println("lsa: " + lsa + " r: " + r);
				EdgeSet visited = new EdgeSet(tree);
				computeReticulationSizeRec(r, lsa, visited);
				rSize.set(r, visited.size());
			}
		}

		return rSize;
	}

	/**
	 * recursively count edges from r upto lsa
	 */
	private static void computeReticulationSizeRec(Node r, Node lsa, EdgeSet visited) {
		for (var e : r.inEdges()) {
			if (!visited.contains(e) && e.getSource() != lsa) {
				visited.add(e);
				computeReticulationSizeRec(e.getSource(), lsa, visited);
			}
		}
	}

	/**
	 * compute the reticulate node to lsa node mapping
	 */
	public static void computeReticulation2LSA(PhyloTree tree, NodeArray<Node> reticulation2LSA) {
		reticulation2LSA.clear();
		try (NodeArray<BitSet> ret2PathSet = tree.newNodeArray();
			 NodeArray<EdgeArray<BitSet>> ret2Edge2PathSet = tree.newNodeArray();
			 NodeArray<Set<Node>> node2below = tree.newNodeArray()) {
			computeReticulation2LSARec(tree, tree.getRoot(), reticulation2LSA, ret2PathSet, ret2Edge2PathSet, node2below);
		}
	}

	/**
	 * recursively compute the mapping of reticulate nodes to their lsa nodes
	 */
	private static void computeReticulation2LSARec(PhyloTree tree, Node v, NodeArray<Node> reticulation2LSA, NodeArray<BitSet> ret2PathSet, NodeArray<EdgeArray<BitSet>> ret2Edge2PathSet, NodeArray<Set<Node>> node2below) {
		if (v.getInDegree() > 1) // this is a reticulate node, add paths to node and incoming edges
		{
			// create new paths for this node:
			EdgeArray<BitSet> edge2PathSet = new EdgeArray<>(tree);
			ret2Edge2PathSet.put(v, edge2PathSet);
			BitSet pathsForR = new BitSet();
			ret2PathSet.put(v, pathsForR);
			//  assign a different path number to each in-edge:
			int pathNum = 0;
			for (Edge e = v.getFirstInEdge(); e != null; e = v.getNextInEdge(e)) {
				pathNum++;
				pathsForR.set(pathNum);
				BitSet pathsForEdge = new BitSet();
				pathsForEdge.set(pathNum);
				edge2PathSet.put(e, pathsForEdge);
			}
		}

		Set<Node> reticulationsBelow = new HashSet<>(); // set of all reticulate nodes below v
		node2below.put(v, reticulationsBelow);

		// visit all children and determine all reticulations below this node
		for (Edge f = v.getFirstOutEdge(); f != null; f = v.getNextOutEdge(f)) {
			Node w = f.getTarget();
			if (node2below.get(w) == null) // if haven't processed child yet, do it:
				computeReticulation2LSARec(tree, w, reticulation2LSA, ret2PathSet, ret2Edge2PathSet,node2below);
			reticulationsBelow.addAll(node2below.get(w));
			if (w.getInDegree() > 1)
				reticulationsBelow.add(w);
		}

		// check whether this is the lsa for any of the reticulations below v
		// look at all reticulations below v:
		List<Node> toDelete = new LinkedList<>();
		for (Node r : reticulationsBelow) {
			// determine which paths from the reticulation lead to this node
			var edge2PathSet = ret2Edge2PathSet.get(r);
			var paths = new BitSet();
			for (var f = v.getFirstOutEdge(); f != null; f = v.getNextOutEdge(f)) {
				BitSet eSet = edge2PathSet.get(f);
				if (eSet != null)
					paths.or(eSet);

			}
			BitSet alive = ret2PathSet.get(r);
			if (paths.equals(alive)) // if the set of paths equals all alive paths, v is lsa of r
			{
				reticulation2LSA.put(r, v);
				toDelete.add(r); // don't need to consider this reticulation any more
			}
		}
		// don't need to consider reticulations for which lsa has been found:
		for (Node u : toDelete)
			reticulationsBelow.remove(u);

		// all paths are pulled up the first in-edge"
		if (v.getInDegree() >= 1) {
			for (Node r : reticulationsBelow) {
				// determine which paths from the reticulation lead to this node
				EdgeArray<BitSet> edge2PathSet = ret2Edge2PathSet.get(r);

				BitSet newSet = new BitSet();

				for (Edge e = v.getFirstOutEdge(); e != null; e = v.getNextOutEdge(e)) {
					BitSet pathSet = edge2PathSet.get(e);
					if (pathSet != null)
						newSet.or(pathSet);
				}
				edge2PathSet.put(v.getFirstInEdge(), newSet);
			}
		}
		// open new paths on all additional in-edges:
		if (v.getInDegree() >= 2) {
			for (Node r : reticulationsBelow) {
				BitSet existingPathsForR = ret2PathSet.get(r);

				EdgeArray<BitSet> edge2PathSet = ret2Edge2PathSet.get(r);
				// start with the second in edge:
				for (Edge e = v.getNextInEdge(v.getFirstInEdge()); e != null; e = v.getNextInEdge(e)) {
					BitSet pathsForEdge = new BitSet();
					int pathNum = existingPathsForR.nextClearBit(1);
					existingPathsForR.set(pathNum);
					pathsForEdge.set(pathNum);
					edge2PathSet.put(e, pathsForEdge);
				}
			}
		}
	}
}
