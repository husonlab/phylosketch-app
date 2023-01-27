/*
 * SaveCommand.java Copyright (C) 2023 Daniel H. Huson
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
import jloda.fx.util.ProgramProperties;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.util.TextFileFilter;
import jloda.util.FileUtils;
import org.husonlab.phylosketch.views.primary.PrimaryView;

import java.io.File;
import java.io.IOException;

public class SaveCommand {

	public static boolean apply(PrimaryView primaryView) {
		final var fileChooser = new FileChooser();
		fileChooser.setTitle("Save File - " + ProgramProperties.getProgramVersion());

		final var currentFile = new File(primaryView.getDocument().getFileName());

		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Newick", "*.new", "*.tre"),
				TextFileFilter.getInstance());

		if (!currentFile.isDirectory()) {
			fileChooser.setInitialDirectory(currentFile.getParentFile());
			fileChooser.setInitialFileName(currentFile.getName());
		} else {
			final var tmp = new File(ProgramProperties.get("SaveFileDir", ""));
			if (tmp.isDirectory()) {
				fileChooser.setInitialDirectory(tmp);
			}
		}

		final var selectedFile = fileChooser.showSaveDialog(primaryView.getStage());

		if (selectedFile != null) {
			try {
				apply(selectedFile, primaryView);
			} catch (IOException e) {
				return false;
			}
			ProgramProperties.put("SaveFileDir", selectedFile.getParent());
			RecentFilesManager.getInstance().insertRecentFile(selectedFile.getPath());
			return true;
		} else
			return false;
	}

	public static void apply(File file, PrimaryView primaryView) throws IOException {
		try (var w = FileUtils.getOutputWriterPossiblyZIPorGZIP(file.getPath())) {
			w.write(primaryView.getDocument().getNewickString() + "\n");
		}
	}
}
