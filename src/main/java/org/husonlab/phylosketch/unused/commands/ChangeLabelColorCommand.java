/*
 * ChangeLabelColorCommand.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.scene.paint.Color;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.graph.Node;
import org.husonlab.phylosketch.unused.view.PhyloView;

import java.util.ArrayList;
import java.util.Collection;

/**
 * undoable
 * Daniel Huson, 2.2020
 */
public class ChangeLabelColorCommand extends UndoableRedoableCommand {
    private final Runnable undo;
    private final Runnable redo;
    private final ArrayList<Data> dataList = new ArrayList<>();

    public ChangeLabelColorCommand(PhyloView editor, Collection<Node> nodes, Color value) {
        super("Label Color");

        for (Node v : nodes) {
            dataList.add(new Data(v.getId(), (Color) editor.getLabel(v).getTextFill(), value));
        }

        undo = () -> {
            for (Data data : dataList) {
                editor.getLabel(editor.getGraph().findNodeById(data.id)).setTextFill(data.oldValue);
            }
        };

        redo = () -> {
            for (Data data : dataList) {
                editor.getLabel(editor.getGraph().findNodeById(data.id)).setTextFill(data.newValue);
            }
        };
    }

    @Override
    public void undo() {
        undo.run();

    }

    @Override
    public void redo() {
        redo.run();
    }

    static class Data {
        final int id;
        final Color oldValue;
        final Color newValue;

        public Data(int id, Color oldValue, Color newValue) {
            this.id = id;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }
}
