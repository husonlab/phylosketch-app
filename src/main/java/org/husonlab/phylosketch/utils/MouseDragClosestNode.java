/*
 * MouseDragClosestNode.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.utils;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MouseDragClosestNode {
	private double mouseDownX = 0.0D;
	private double mouseDownY = 0.0D;
	private double mouseX = 0.0D;
	private double mouseY = 0.0D;
	private static boolean moved;
	private Node target;

	private boolean target1AlreadyPresent = false;
	private boolean target2AlreadyPresent = false;

	public static void setup(Node node, Node reference1, Node target1, Node reference2, Node target2, Group world, BiConsumer<Node, Point2D> totalTranslation,
							 Consumer<MouseEvent> onMousePressed, Consumer<MouseEvent> onMouseFirstMoved, Consumer<MouseEvent> oneMouseReleased, Supplier<Boolean> allow) {
		new MouseDragClosestNode(node, onMousePressed, onMouseFirstMoved, oneMouseReleased, reference1, target1, reference2, target2, totalTranslation, world, allow);
	}

	private MouseDragClosestNode(Node node, Consumer<MouseEvent> onMousePressed, Consumer<MouseEvent> onMouseFirstMoved, Consumer<MouseEvent> oneMouseReleased, Node reference1, Node target1, Node reference2, Node target2, BiConsumer<Node, Point2D> totalTranslation2,
								 Group world, Supplier<Boolean> allow) {
		node.setOnMousePressed(a -> {
			onMousePressed.accept(a);
			if (allow.get()) {
				this.mouseDownX = this.mouseX = a.getScreenX();
				this.mouseDownY = this.mouseY = a.getScreenY();
				moved = false;
				Bounds screenBounds1 = reference1.localToScreen(reference1.getBoundsInLocal());
				double distance1 = (new Point2D(screenBounds1.getCenterX(), screenBounds1.getCenterY())).distance(this.mouseX, this.mouseY);
				Bounds screenBounds2 = reference2.localToScreen(reference2.getBoundsInLocal());
				double distance2 = (new Point2D(screenBounds2.getCenterX(), screenBounds2.getCenterY())).distance(this.mouseX, this.mouseY);
				if (distance1 <= distance2) {
					this.target = target1;
				} else {
					this.target = target2;
				}

				target1AlreadyPresent = world.getChildren().contains(target1);
				if (!target1AlreadyPresent)
					world.getChildren().add(target1);
				target2AlreadyPresent = world.getChildren().contains(target2);
				if (!target2AlreadyPresent)
					world.getChildren().add(target2);
			}

			a.consume();
		});
		node.setOnMouseDragged(a -> {
			if (!moved)
				onMouseFirstMoved.accept(a);

			if (allow.get()) {
				try {
					this.target.setTranslateX(this.target.getTranslateX() + (a.getScreenX() - this.mouseX));
					this.target.setTranslateY(this.target.getTranslateY() + (a.getScreenY() - this.mouseY));
				} catch (NullPointerException ignored) {
				}
				this.mouseX = a.getScreenX();
				this.mouseY = a.getScreenY();
				moved = true;
			}

			a.consume();
		});
		node.setOnMouseReleased(a -> {
			oneMouseReleased.accept(a);
			if (allow.get()) {
				if (moved) {
					double dx = a.getScreenX() - this.mouseDownX;
					double dy = a.getScreenY() - this.mouseDownY;
					if (dx != 0.0D && dy != 0.0D) {
						totalTranslation2.accept(this.target, new Point2D(dx, dy));
					}
				}
				if (!target1AlreadyPresent)
					world.getChildren().remove(target1);
				if (!target2AlreadyPresent)
					world.getChildren().remove(target2);
			}

			a.consume();
		});
	}

	public static boolean wasMoved() {
		boolean result = moved;
		moved = false;
		return result;
	}
}
