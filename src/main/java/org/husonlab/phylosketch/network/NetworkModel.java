/*
 * NetworkModel.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import jloda.graph.*;
import jloda.phylo.PhyloTree;
import jloda.util.CanceledException;
import jloda.util.Counter;
import jloda.util.progress.ProgressSilent;
import org.husonlab.phylosketch.algorithms.embedding.EmbeddingOptimizer;
import org.husonlab.phylosketch.algorithms.embedding.HeightAndAngles;
import org.husonlab.phylosketch.algorithms.embedding.LSATree;
import org.husonlab.phylosketch.algorithms.embedding.LayoutTreeRectangular;

/**
 * model of a network with some node and edge attributes
 * Daniel Huson, 10.2022
 */
public class NetworkModel {
	public enum NodeGlyph {Square, Circle}

	public enum EdgeGlyph {StraightLine, RectangleLine, CubicCurve}

	private final PhyloTree tree;
	private final NodeArray<NodeAttributes> nodeAttributesNodeMap;
	private final EdgeArray<EdgeAttributes> edgeAttributesMap;

	public NetworkModel() {
		this.tree = new PhyloTree();
		nodeAttributesNodeMap = tree.newNodeArray();
		edgeAttributesMap = tree.newEdgeArray();
	}

	/**
	 * computes a simple left-to-right embedding
	 *
	 * @param fitWidth
	 * @param fitHeight
	 */
	public void computeEmbedding(boolean toScale, double fitWidth, double fitHeight) {
		clear();

		if (true || tree.hasReticulateEdges()) {
			if (true) {

				try {
					LSATree.computeNodeLSAChildrenMap(tree);
					EmbeddingOptimizer.apply(tree, new ProgressSilent());
				} catch (CanceledException ignored) {
				}
				try (NodeArray<Point2D> nodePointMap = LayoutTreeRectangular.apply(tree, false, HeightAndAngles.Averaging.LeafAverage)) {

					fit(fitWidth, fitHeight, nodePointMap);

					for (var v : tree.nodes()) {
						var point = nodePointMap.get(v);
						var vx = (point != null ? point.getX() : 0);
						var vy = (point != null ? point.getY() : 0);
						var text = tree.getLabel(v);
						var label = new Label(10, -0.5 * NetworkPresenter.DEFAULT_FONT_SIZE.get(), 0, text != null ? text : "");
						setAttributes(v, new NodeAttributes(vx, vy, NodeGlyph.Circle, 8, 8, Color.BLACK, Color.WHITE, label));
					}
					for (var e : tree.edges()) {
						edgeAttributesMap.put(e, new EdgeAttributes(EdgeGlyph.StraightLine, 1.0, Color.BLACK, null));
					}
				}
			} else {
				try (NodeArray<Node> nodeCopyNodeMap = tree.newNodeArray()) {
					var copyTree = new PhyloTree();
					copyTree.copy(tree, nodeCopyNodeMap, null);

					try {
						EmbeddingOptimizer.apply(tree, new ProgressSilent());
					} catch (CanceledException ignored) {
					}
					try (NodeArray<Point2D> copyNodePointMap = LayoutTreeRectangular.apply(copyTree, false, HeightAndAngles.Averaging.LeafAverage);
						 NodeArray<Point2D> nodePointMap = tree.newNodeArray()) {
						for (var v : tree.nodes()) {
							nodePointMap.put(v, copyNodePointMap.get(nodeCopyNodeMap.get(v)));
						}

						fit(fitWidth, fitHeight, nodePointMap);

						for (var v : tree.nodes()) {
							var point = nodePointMap.get(v);
							var vx = (point != null ? point.getX() : 0);
							var vy = (point != null ? point.getY() : 0);
							var text = tree.getLabel(v);
							var label = new Label(10, -0.5 * NetworkPresenter.DEFAULT_FONT_SIZE.get(), 0, text != null ? text : "");
							setAttributes(v, new NodeAttributes(vx, vy, NodeGlyph.Circle, 8, 8, Color.BLACK, Color.WHITE, label));
						}
						for (var e : tree.edges()) {
							edgeAttributesMap.put(e, new EdgeAttributes(EdgeGlyph.StraightLine, 1.0, Color.BLACK, null));
						}
					}
				}
			}
		} else {
			try (var x = tree.newNodeDoubleArray(); var y = tree.newNodeDoubleArray()) {
				if (toScale) {
					var root = tree.getRoot();
					x.put(root, 0.0);
					y.put(root, 0.0);
					tree.preorderTraversal(v -> {
						if (v.getInDegree() == 1) {
							var e = v.getFirstInEdge();
							x.put(v, x.get(e.getSource()) + tree.getWeight(e));
						}
					});
				} else {
					tree.postorderTraversal(v -> {
						if (v.isLeaf())
							x.put(v, 0.0);
						else {
							var min = v.childrenStream().mapToDouble(c -> x.get(v)).min().orElse(0);
							x.put(v, min - 1.0);
						}
					});
				}

				var count = new Counter();
				tree.postorderTraversal(v -> {
					if (v.isLeaf())
						y.put(v, (double) count.incrementAndGet());
					else {
						y.put(v, v.childrenStream().mapToDouble(y::get).average().orElse(0.0));
					}
				});

				fit(fitWidth, fitHeight, x, y);

				for (var v : tree.nodes()) {
					var vx = x.get(v);
					var vy = y.get(v);
					var text = tree.getLabel(v);
					var label = new Label(10, -0.5 * NetworkPresenter.DEFAULT_FONT_SIZE.get(), 0, text != null ? text : "");
					setAttributes(v, new NodeAttributes(vx, vy, NodeGlyph.Circle, 8, 8, Color.BLACK, Color.WHITE, label));
				}
				for (var e : tree.edges()) {
					edgeAttributesMap.put(e, new EdgeAttributes(EdgeGlyph.StraightLine, 1.0, Color.BLACK, null));
				}
			}
		}

		tree.addGraphUpdateListener(new GraphUpdateAdapter() {
			@Override
			public void newEdge(Edge e) {
				if (e.getTarget().getInDegree() == 2) {
					for (var f : e.getTarget().inEdges()) {
						tree.setReticulate(f, true);
					}
				}
			}

			@Override
			public void deleteEdge(Edge e) {
				tree.setReticulate(e, false);

				if (e.getTarget().getInDegree() == 2) {
					for (var f : e.getTarget().inEdges()) {
						tree.setReticulate(f, false);
					}
				}
			}
		});
	}

	private static void fit(double fitWidth, double fitHeight, NodeArray<Point2D> map) {
		if (fitWidth > 0) {
			var min = map.values().stream().mapToDouble(Point2D::getX).min().orElse(0);
			var max = map.values().stream().mapToDouble(Point2D::getX).max().orElse(0);
			var diff = max - min > 0 ? max - min : 1.0;
			map.replaceAll(((key, value) -> new Point2D((value.getX() - min) / diff * fitWidth, value.getY())));
		}
		if (fitHeight > 0) {
			var min = map.values().stream().mapToDouble(Point2D::getY).min().orElse(0);
			var max = map.values().stream().mapToDouble(Point2D::getY).max().orElse(0);
			var diff = max - min > 0 ? max - min : 1.0;
			map.replaceAll(((v, value) -> new Point2D(value.getX(), (value.getY() - min) / diff * fitHeight)));
		}

	}

	private static void fit(double fitWidth, double fitHeight, NodeDoubleArray x, NodeDoubleArray y) {
		if (fitWidth > 0) {
			var min = x.values().stream().mapToDouble(a -> a).min().orElse(0);
			var max = x.values().stream().mapToDouble(a -> a).max().orElse(0);
			var diff = max - min > 0 ? max - min : 1.0;
			for (var v : x.keySet()) {
				x.computeIfPresent(v, (k, o) -> (o - min) / diff * fitWidth);
			}
		}
		if (fitHeight > 0) {
			var min = y.values().stream().mapToDouble(a -> a).min().orElse(0);
			var max = y.values().stream().mapToDouble(a -> a).max().orElse(0);
			var diff = max - min > 0 ? max - min : 1.0;
			for (var v : y.keySet()) {
				y.computeIfPresent(v, (k, o) -> (o - min) / diff * fitHeight);
			}
		}

	}

	public void clear() {
		nodeAttributesNodeMap.clear();
		edgeAttributesMap.clear();
	}

	public PhyloTree getTree() {
		return tree;
	}

	public NodeAttributes getAttributes(Node v) {
		return nodeAttributesNodeMap.get(v);
	}

	public void setAttributes(Node v, NodeAttributes attributes) {
		nodeAttributesNodeMap.put(v, attributes);
	}

	public EdgeAttributes getAttributes(Edge e) {
		return edgeAttributesMap.get(e);
	}

	public void setAttributes(Edge e, EdgeAttributes attributes) {
		edgeAttributesMap.put(e, attributes);
	}

	public static record NodeAttributes(double x, double y, NodeGlyph glyph, double width, double height, Paint stroke,
										Paint fill, Label label) {
	}

	public static record Label(double dx, double dy, double angle, String text) {
	}

	public static record EdgeAttributes(EdgeGlyph glyph, double strokeWidth, Paint stroke, Label label) {
	}
}
