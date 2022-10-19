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

package org.husonlab.phylosketch.unused.model;

import javafx.geometry.Point2D;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloTree;
import jloda.util.Counter;

import java.io.IOException;

public class Document {
	private final PhyloTree tree;
	private final NodeArray<Point2D> nodePointMap;

	public Document() {
		tree = new PhyloTree();
		nodePointMap = tree.newNodeArray();
	}

	public PhyloTree getTree() {
		return tree;
	}

	public NodeArray<Point2D> getNodePointMap() {
		return nodePointMap;
	}

	public Point2D getPoint(Node v) {
		return nodePointMap.get(v);
	}

	public void setPoint(Node v, Point2D p) {
		nodePointMap.put(v, p);
	}

	public static Document createSimple() {
		var doc = new Document();
		try {
			doc.getTree().parseBracketNotation("((a,b),(c,d));", true);
		} catch (IOException ignored) {
			// can't happen
		}
		var lefNum = new Counter();
		doc.getTree().postorderTraversal(v -> {
			if (v.getOutDegree() == 0) {
				doc.setPoint(v, new Point2D(100, 50 * (lefNum.getAndIncrement())));
			} else {
				var x = v.childrenStream().mapToDouble(w -> doc.getPoint(w).getX()).min().orElse(0) - 25;
				var y = v.childrenStream().mapToDouble(w -> doc.getPoint(w).getY()).average().orElse(0);
				doc.setPoint(v, new Point2D(x, y));
			}
		});
		return doc;
	}
}
