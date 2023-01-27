/*
 * ChangeAllEdgeGlyphCommand.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.network.commands;

import javafx.animation.ParallelTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.graph.Edge;
import org.husonlab.phylosketch.network.Document;
import org.husonlab.phylosketch.network.NetworkModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * change the shape of the edges
 * Daniel Huson, 12.2022
 */
public class ChangeEdgeGlyphCommand extends UndoableRedoableCommand {
	private Runnable undo;
	private Runnable redo;

	public ChangeEdgeGlyphCommand(Document document, Collection<Edge> edges, NetworkModel.EdgeGlyph newShape) {
		super("change edge type");

		var networkView = document.getNetworkView();
		var id2oldCoordinates = new HashMap<Integer, double[]>();

		for (var e : edges) {
			id2oldCoordinates.put(e.getId(), networkView.getView(e).getControlCoordinatesNormalized());
		}

		undo = () -> {
			for (var id : id2oldCoordinates.keySet()) {
				var e = document.getModel().getTree().findEdgeById(id);
				networkView.getView(e).setControlCoordinatesFromNormalized(id2oldCoordinates.get(id));
			}
		};

		redo = () -> {
			var tree = document.getModel().getTree();
			var theEdges = id2oldCoordinates.keySet().stream().map(tree::findEdgeById).filter(Objects::nonNull).collect(Collectors.toList());
			changeEdgeShape(document, theEdges, newShape);
		};
	}


	@Override
	public boolean isUndoable() {
		return undo != null;
	}

	@Override
	public boolean isRedoable() {
		return redo != null;
	}

	@Override
	public void undo() {
		undo.run();
	}

	@Override
	public void redo() {
		redo.run();
	}

	public static void changeEdgeShape(Document document, Collection<Edge> edges, NetworkModel.EdgeGlyph glyph) {
		var transitions = new ArrayList<Transition>();
		for (var e : edges) {
			var ev = document.getNetworkView().getView(e);
			var coordinates = document.getNetworkView().computeControlPoints(e, glyph);
			{
				var translate = new TranslateTransition(Duration.seconds(0.5));
				translate.setNode(ev.getCircle1());
				translate.setToX(coordinates[0]);
				translate.setToY(coordinates[1]);
				transitions.add(translate);
			}
			{
				var translate = new TranslateTransition(Duration.seconds(0.5));
				translate.setNode(ev.getCircle2());
				translate.setToX(coordinates[2]);
				translate.setToY(coordinates[3]);
				transitions.add(translate);
			}
		}
		var parallel = new ParallelTransition(transitions.toArray(new Transition[0]));
		parallel.setOnFinished(e -> document.setEdgeGlyph(glyph));
		parallel.play();
	}
}
