/*
 * LabelEditing.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.network.interaction;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Duration;
import jloda.fx.selection.SelectionModel;
import jloda.fx.undo.UndoManager;
import jloda.fx.util.DraggableUtils;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.Pair;
import org.husonlab.phylosketch.network.DefaultOptions;
import org.husonlab.phylosketch.network.NetworkView;
import org.husonlab.phylosketch.network.NodeView;
import org.husonlab.phylosketch.network.commands.NodeLabelCommand;
import org.husonlab.phylosketch.views.labeleditor.LabelEditor;

import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * label editing manager
 * Daniel Huson, 11.22
 */
public class LabelEditingManager {
	private final NetworkView networkView;
	private final SelectionModel<Node> nodeSelectionModel;

	private final UndoManager undoManager;
	private String originalText = "";

	private final LabelEditor editor;

	private final ObjectProperty<Node> leftNeighbor = new SimpleObjectProperty<>();
	private final ObjectProperty<Node> rightNeighbor = new SimpleObjectProperty<>();
	private final ObjectProperty<Node> upNeighbor = new SimpleObjectProperty<>();
	private final ObjectProperty<Node> downNeighbor = new SimpleObjectProperty<>();

	private Node currentNode;
	private NodeView currentNodeView;

	/**
	 * constructor
	 *
	 * @param networkView
	 * @param nodeSelectionModel
	 * @param undoManager
	 */
	public LabelEditingManager(NetworkView networkView, SelectionModel<Node> nodeSelectionModel, UndoManager undoManager) {
		this.networkView = networkView;
		this.nodeSelectionModel = nodeSelectionModel;
		this.undoManager = undoManager;

		editor = new LabelEditor(this);

		DraggableUtils.setupDragMouseLayout(editor.getRoot());

		DefaultOptions.textAreaFontSizeProperty().addListener((v, o, n) -> {
			if (n.intValue() >= 12 && n.intValue() <= 40)
				editor.getTextField().setStyle("-fx-font-size: %d;".formatted(n.intValue()));
		});
		editor.getTextField().setStyle("-fx-font-size: %d;".formatted(DefaultOptions.getTextAreaFontSize()));
	}

	public void startEditing(Node v) {
		saveCurrent();
		setCurrent(v);
	}

	public void finishEditing() {
		saveCurrent();
		if (currentNodeView != null) {
			editor.getController().getTextField().textProperty().unbindBidirectional(currentNodeView.label().textProperty());
			currentNodeView = null;
		}

		currentNode = null;

		leftNeighbor.set(null);
		rightNeighbor.set(null);
		upNeighbor.set(null);
		downNeighbor.set(null);

		networkView.getWorld().getChildren().remove(editor.getController().getRootPane());
	}

	private void setCurrent(Node v) {
		if (v != null) {
			var editorPane = editor.getController().getRootPane();
			var textField = editor.getController().getTextField();

			if (currentNode != null) {
				saveCurrent();
			}
			if (currentNodeView != null) {
				textField.textProperty().unbindBidirectional(currentNodeView.label().textProperty());
				currentNodeView = null;
			}
			currentNode = v;
			currentNodeView = networkView.getView(currentNode);

			nodeSelectionModel.clearSelection();
			nodeSelectionModel.select(currentNode);

			originalText = currentNodeView.label().getText();
			textField.textProperty().bindBidirectional(currentNodeView.label().textProperty());

			var box = currentNodeView.shape().getBoundsInLocal();
			if (!networkView.getWorld().getChildren().contains(editorPane)) {
				editorPane.translateXProperty().bind(currentNodeView.shape().translateXProperty());
				editorPane.translateYProperty().bind(currentNodeView.shape().translateYProperty().add(box.getHeight()));
				editorPane.setLayoutX(0);
				editorPane.setLayoutY(0);
				networkView.getWorld().getChildren().add(editorPane);
			} else {
				editorPane.translateXProperty().unbind();
				editorPane.translateYProperty().unbind();
				var translate = new TranslateTransition(Duration.millis(500), editorPane);
				translate.setToX(currentNodeView.shape().getTranslateX());
				translate.setToY(currentNodeView.shape().getTranslateY() + box.getHeight());
				translate.setOnFinished(a -> {
					editorPane.translateXProperty().bind(currentNodeView.shape().translateXProperty());
					editorPane.translateYProperty().bind(currentNodeView.shape().translateYProperty().add(box.getHeight()));
				});
				translate.play();
			}

			Platform.runLater(() -> {
				textField.positionCaret(textField.getText().length());
				textField.requestFocus();
			});

			leftNeighbor.set(findNode(networkView, currentNode, true, Direction.Left));
			rightNeighbor.set(findNode(networkView, currentNode, true, Direction.Right));
			upNeighbor.set(findNode(networkView, currentNode, true, Direction.Up));
			downNeighbor.set(findNode(networkView, currentNode, true, Direction.Down));
		}
	}

	private void saveCurrent() {
		if (currentNode != null && currentNode.getOwner() != null) {
			var newText = editor.getController().getTextField().getText();
			if (!newText.equals(originalText)) {
				undoManager.doAndAdd(new NodeLabelCommand(currentNode, networkView, originalText, newText));
			}
		}
	}

	public void continueEditing(Direction direction) {
		switch (direction) {
			case Left -> setCurrent(leftNeighbor.get());
			case Right -> setCurrent(rightNeighbor.get());
			case Down -> setCurrent(downNeighbor.get());
			case Up -> setCurrent(upNeighbor.get());
		}
	}

	public enum Direction {Left, Right, Up, Down}

	public static Node findNode(NetworkView view, Node v, boolean strict, Direction direction) {
		var tree = (PhyloTree) v.getOwner();

		if (tree.getNumberOfNodes() > 1) {
			var vx = view.getView(v).shape().getTranslateX();
			var vy = view.getView(v).shape().getTranslateY();

			var set = tree.nodeStream()
					.map(u -> {
						var ux = view.getView(u).shape().getTranslateX();
						var uy = view.getView(u).shape().getTranslateY();
						var dx = Math.abs(ux - vx);
						var dy = Math.abs(uy - vy);
						return switch (direction) {
							case Up -> uy < vy && (!strict || dx < dy) ? new Pair<>(vy - uy, u) : null;
							case Down -> uy > vy && (!strict || dx < dy) ? new Pair<>(uy - vy, u) : null;
							case Left -> ux < vx && (!strict || dx > dy) ? new Pair<>(vx - ux, u) : null;
							case Right -> ux > vx && (!strict || dx > dy) ? new Pair<>(ux - vx, u) : null;
						};
					})
					.filter(Objects::nonNull)
					.collect(Collectors.toCollection(TreeSet::new));
			var result = set.size() > 0 ? set.first().getSecond() : null;
			if (result == null && strict) {
				result = findNode(view, v, false, direction);
			}
			return result;
		} else
			return null;
	}

	public ReadOnlyObjectProperty<Node> leftNeighborProperty() {
		return leftNeighbor;
	}

	public ReadOnlyObjectProperty<Node> rightNeighborProperty() {
		return rightNeighbor;
	}

	public ReadOnlyObjectProperty<Node> upNeighborProperty() {
		return upNeighbor;
	}

	public ReadOnlyObjectProperty<Node> downNeighborProperty() {
		return downNeighbor;
	}
}
