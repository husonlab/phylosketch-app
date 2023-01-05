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
import jloda.util.progress.ProgressSilent;
import org.husonlab.phylosketch.algorithms.embedding.EmbeddingOptimizer;
import org.husonlab.phylosketch.algorithms.embedding.HeightAndAngles;
import org.husonlab.phylosketch.algorithms.embedding.LSATree;
import org.husonlab.phylosketch.algorithms.embedding.LayoutTreeRectangular;

import java.util.Objects;

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

		try {
			LSATree.computeNodeLSAChildrenMap(tree);
			EmbeddingOptimizer.apply(tree, new ProgressSilent());
		} catch (CanceledException ignored) {
		}
		try (NodeArray<Point2D> nodePointMap = LayoutTreeRectangular.apply(tree, toScale, HeightAndAngles.Averaging.LeafAverage)) {

			fit(fitWidth, fitHeight, nodePointMap);

			for (var v : tree.nodes()) {
				var point = nodePointMap.get(v);
				var vx = (point != null ? point.getX() : 0);
				var vy = (point != null ? point.getY() : 0);
				var text = tree.getLabel(v);
				var label = new Label(10, -0.5 * NetworkPresenter.DEFAULT_FONT_SIZE.get(), 0, text != null ? text : "");
				setAttributes(v, new NodeAttributes(vx, vy, NodeGlyph.Circle, null, null, null, null, label));
			}

			for (var e : tree.edges()) {
				edgeAttributesMap.put(e, new EdgeAttributes(EdgeGlyph.StraightLine, null, null, null));
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

	public static final class NodeAttributes {
		private final double x;
		private final double y;
		private final NodeGlyph glyph;
		private final Double width;
		private final Double height;
		private final Paint stroke;
		private final Paint fill;
		private final Label label;

		public NodeAttributes(double x, double y, NodeGlyph glyph, Double width, Double height, Paint stroke,
							  Paint fill, Label label) {
			this.x = x;
			this.y = y;
			this.glyph = glyph;
			this.width = width;
			this.height = height;
			this.stroke = stroke;
			this.fill = fill;
			this.label = label;
		}

		public double x() {
			return x;
		}

		public double y() {
			return y;
		}

		public NodeGlyph glyph() {
			return glyph;
		}

		public Double width() {
			return width;
		}

		public Double height() {
			return height;
		}

		public Paint stroke() {
			return stroke;
		}

		public Paint fill() {
			return fill;
		}

		public Label label() {
			return label;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (obj == null || obj.getClass() != this.getClass()) return false;
			var that = (NodeAttributes) obj;
			return Double.doubleToLongBits(this.x) == Double.doubleToLongBits(that.x) &&
				   Double.doubleToLongBits(this.y) == Double.doubleToLongBits(that.y) &&
				   Objects.equals(this.glyph, that.glyph) &&
				   Objects.equals(this.width, that.width) &&
				   Objects.equals(this.height, that.height) &&
				   Objects.equals(this.stroke, that.stroke) &&
				   Objects.equals(this.fill, that.fill) &&
				   Objects.equals(this.label, that.label);
		}

		@Override
		public int hashCode() {
			return Objects.hash(x, y, glyph, width, height, stroke, fill, label);
		}

		@Override
		public String toString() {
			return "NodeAttributes[" +
				   "x=" + x + ", " +
				   "y=" + y + ", " +
				   "glyph=" + glyph + ", " +
				   "width=" + width + ", " +
				   "height=" + height + ", " +
				   "stroke=" + stroke + ", " +
				   "fill=" + fill + ", " +
				   "label=" + label + ']';
		}

	}

	public static final class Label {
		private final double dx;
		private final double dy;
		private final double angle;
		private final String text;

		public Label(double dx, double dy, double angle, String text) {
			this.dx = dx;
			this.dy = dy;
			this.angle = angle;
			this.text = text;
		}

		public double dx() {
			return dx;
		}

		public double dy() {
			return dy;
		}

		public double angle() {
			return angle;
		}

		public String text() {
			return text;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (obj == null || obj.getClass() != this.getClass()) return false;
			var that = (Label) obj;
			return Double.doubleToLongBits(this.dx) == Double.doubleToLongBits(that.dx) &&
				   Double.doubleToLongBits(this.dy) == Double.doubleToLongBits(that.dy) &&
				   Double.doubleToLongBits(this.angle) == Double.doubleToLongBits(that.angle) &&
				   Objects.equals(this.text, that.text);
		}

		@Override
		public int hashCode() {
			return Objects.hash(dx, dy, angle, text);
		}

		@Override
		public String toString() {
			return "Label[" +
				   "dx=" + dx + ", " +
				   "dy=" + dy + ", " +
				   "angle=" + angle + ", " +
				   "text=" + text + ']';
		}

	}

	public static final class EdgeAttributes {
		private final EdgeGlyph glyph;
		private final Double strokeWidth;
		private final Paint stroke;
		private final Label label;

		public EdgeAttributes(EdgeGlyph glyph, Double strokeWidth, Paint stroke, Label label) {
			this.glyph = glyph;
			this.strokeWidth = strokeWidth;
			this.stroke = stroke;
			this.label = label;
		}

		public EdgeGlyph glyph() {
			return glyph;
		}

		public Double strokeWidth() {
			return strokeWidth;
		}

		public Paint stroke() {
			return stroke;
		}

		public Label label() {
			return label;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (obj == null || obj.getClass() != this.getClass()) return false;
			var that = (EdgeAttributes) obj;
			return Objects.equals(this.glyph, that.glyph) &&
				   Objects.equals(this.strokeWidth, that.strokeWidth) &&
				   Objects.equals(this.stroke, that.stroke) &&
				   Objects.equals(this.label, that.label);
		}

		@Override
		public int hashCode() {
			return Objects.hash(glyph, strokeWidth, stroke, label);
		}

		@Override
		public String toString() {
			return "EdgeAttributes[" +
				   "glyph=" + glyph + ", " +
				   "strokeWidth=" + strokeWidth + ", " +
				   "stroke=" + stroke + ", " +
				   "label=" + label + ']';
		}

	}
}
