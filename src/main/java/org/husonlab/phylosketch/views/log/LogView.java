/*
 * LogView.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.views.log;

import com.gluonhq.charm.glisten.mvc.View;
import javafx.fxml.FXMLLoader;
import jloda.fx.message.EchoPrintStreamForTextArea;
import jloda.util.Basic;
import jloda.util.StringUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

public class LogView {
	final private LogPresenter presenter;
	final private LogController controller;

	public LogView() {
		var fxmlLoader = new FXMLLoader();
		try (var ins = Objects.requireNonNull(LogController.class.getResource("other.fxml")).openStream()) {
			fxmlLoader.load(ins);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		controller = fxmlLoader.getController();
		presenter = new LogPresenter(this, controller);

		var collected = Basic.stopCollectingStdErr();
		Basic.restoreSystemErr();

		Function<String, String> filter = s -> {
			if (s.isBlank() || s.contains("xception") || s.startsWith("\t") || !s.contains("gluonhq"))
				return s;
			else
				return null;
		};
		System.setErr(new EchoPrintStreamForTextArea(System.err, filter, controller.getLogTextArea()));
		collected.lines().forEach(System.err::println);
	}

	public View getView() {
		return controller.getView();
	}
}
