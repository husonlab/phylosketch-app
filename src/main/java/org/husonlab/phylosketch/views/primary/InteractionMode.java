/*
 * InteractionMode.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.views.primary;

public enum InteractionMode {
	Pan("Pan"), //MaterialDesignIcon.PAN_TOOL,
	CreateNewEdges("Press node and move to create new edge"), // MaterialDesignIcon.EDIT,
	Erase("Erase nodes and edges"), // MaterialDesignIcon.REMOVE_CIRCLE,
	Move("Move nodes, edges and labels"), // MaterialDesignIcon.SWAP_VERT,
	EditLabels("Edit node labels"); // MaterialDesignIcon.LABEL_OUTLINE,
	private final String description;

	InteractionMode(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
