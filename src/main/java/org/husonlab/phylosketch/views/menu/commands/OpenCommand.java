/*
 * OpenCommand.java Copyright (C) 2023 Daniel H. Huson
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

package org.husonlab.phylosketch.views.menu.commands;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jloda.fx.util.ProgramProperties;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.util.TextFileFilter;
import jloda.fx.window.NotificationManager;
import jloda.util.FileLineIterator;
import jloda.util.StringUtils;
import org.husonlab.phylosketch.network.commands.ReplaceNetworkCommand;
import org.husonlab.phylosketch.views.primary.PrimaryView;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class OpenCommand {
	/**
	 * open a file
	 */
	public static void apply(final Stage owner) {
		final File previousDir = new File(ProgramProperties.get("OpenDir", ""));
		final FileChooser fileChooser = new FileChooser();
		if (previousDir.isDirectory())
			fileChooser.setInitialDirectory(previousDir);
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Extended Newick", "*.newick", "*.new", "*.tree", "*.tre"),
				TextFileFilter.getInstance());
		fileChooser.setTitle("Open File");

		final File selectedFile = fileChooser.showOpenDialog(owner);
		if (selectedFile != null) {
			createFileOpener().accept(selectedFile.getPath());
			ProgramProperties.put("OpenDir", selectedFile.getParent());
		}
	}

	public static Consumer<String> createFileOpener() {
		return fileName -> {
			var primaryView = PrimaryView.lastFocusedProperty().get();
			if (primaryView != null) {
				var document = primaryView.getDocument();
				try (var reader = new FileLineIterator(fileName)) {
					var newick = StringUtils.toString(reader.stream().collect(Collectors.toList()), "");
					document.getUndoManager().doAndAdd(new ReplaceNetworkCommand(document, newick));
					RecentFilesManager.getInstance().insertRecentFile(fileName);
					document.setFileName(fileName);
				} catch (IOException e) {
					NotificationManager.showError("Open file failed: " + e.getMessage());
				}
			}
		};
	}
}
