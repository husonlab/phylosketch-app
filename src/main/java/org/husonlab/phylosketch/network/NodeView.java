/*
 * NodeView.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.scene.shape.Shape;
import jloda.fx.control.RichTextLabel;

public final class NodeView {
	private Shape shape;
	private RichTextLabel label;

	public NodeView(Shape shape, RichTextLabel label) {
		this.shape = shape;
		this.label = label;
	}

	public Shape shape() {
		return shape;
	}

	public RichTextLabel label() {
		return label;
	}

	public void setShape(Shape shape) {
		this.shape = shape;
	}

	public void setLabel(RichTextLabel label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "NodeView[" +
			   "shape=" + shape + ", " +
			   "label=" + label + ']';
	}
}
