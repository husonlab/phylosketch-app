/*
 * DistributeNodesCommand.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.unused.commands;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.util.Duration;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.graph.Node;
import jloda.util.ProgramProperties;
import org.husonlab.phylosketch.unused.view.NodeView;
import org.husonlab.phylosketch.unused.view.PhyloView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

/**
 * distribute nodes command
 * Daniel Huson, 2.2020
 */
public class DistributeNodesCommand extends UndoableRedoableCommand {
    public enum Direction {Horizontally, Vertically}

    private final Runnable undo;
    private final Runnable redo;

    public DistributeNodesCommand(PhyloView view, Collection<Node> nodes, Direction direction) {
        super("Node distribution");

        final ArrayList<Data> dataList = new ArrayList<>();

        if (direction == Direction.Horizontally) {
            final Optional<Double> minX = nodes.stream().map(v -> view.getNodeView(v).getTranslateX()).min(Double::compare);
            final Optional<Double> maxX = nodes.stream().map(v -> view.getNodeView(v).getTranslateX()).max(Double::compare);

            if (minX.isPresent() && maxX.isPresent()) {
                final double delta = (maxX.get() - minX.get()) / (nodes.size() - 1);
                final ArrayList<Node> sorted = new ArrayList<>(nodes);
                sorted.sort(Comparator.comparingDouble(v -> view.getNodeView(v).getTranslateX()));
                for (int i = 0; i < sorted.size(); i++) {
                    final Node v = sorted.get(i);
                    final NodeView nv = view.getNodeView(v);
                    dataList.add(new Data(v.getId(), nv.getTranslateX(), minX.get() + i * delta));
                }
            }
        } else {
            final Optional<Double> minY = nodes.stream().map(v -> view.getNodeView(v).getTranslateY()).min(Double::compare);
            final Optional<Double> maxY = nodes.stream().map(v -> view.getNodeView(v).getTranslateY()).max(Double::compare);
            if (minY.isPresent() && maxY.isPresent()) {
                final double delta = (maxY.get() - minY.get()) / (nodes.size() - 1);
                final ArrayList<Node> sorted = new ArrayList<>(nodes);
                sorted.sort(Comparator.comparingDouble(v -> view.getNodeView(v).getTranslateY()));

                for (int i = 0; i < sorted.size(); i++) {
                    final Node v = sorted.get(i);
                    final NodeView nv = view.getNodeView(v);
                    dataList.add(new Data(v.getId(), nv.getTranslateY(), minY.get() + i * delta));
                }
            }
        }

        undo = () -> animateDistribute(view, dataList, direction, true);

        redo = () -> animateDistribute(view, dataList, direction, false);
    }

    private void animateDistribute(PhyloView view, ArrayList<Data> dataList, Direction direction, boolean back) {
        if (dataList.size() < ProgramProperties.get("AnimationLimit", 5000)) {
            final Animation animation = new Transition() {
                {
                    setCycleDuration(Duration.millis(500));
                }

                @Override
                protected void interpolate(double p) {
                    final double q = 1.0 - p;
                    for (Data data : dataList) {
                        final NodeView nv = view.getNodeView(view.getGraph().findNodeById(data.id));
                        if (direction == Direction.Horizontally)
                            nv.setTranslateX((back ? p : q) * data.oldValue + (back ? q : p) * data.newValue);
                        else
                            nv.setTranslateY((back ? p : q) * data.oldValue + (back ? q : p) * data.newValue);
                    }

                }
            };
            animation.play();
        } else {
            for (Data data : dataList) {
                final NodeView nv = view.getNodeView(view.getGraph().findNodeById(data.id));
                if (direction == Direction.Horizontally)
                    nv.setTranslateX(back ? data.oldValue : data.newValue);
                else
                    nv.setTranslateY(back ? data.oldValue : data.newValue);
            }

        }
    }

    @Override
    public void undo() {
        undo.run();
    }

    @Override
    public void redo() {
        redo.run();
    }

    private static class Data {
        final int id;
        final double oldValue;
        final double newValue;

        public Data(int id, double oldValue, double newValue) {
            this.id = id;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }
}
