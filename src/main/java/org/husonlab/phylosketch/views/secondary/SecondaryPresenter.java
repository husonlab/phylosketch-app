/*
 * SecondaryPresenter.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.views.secondary;

import com.gluonhq.charm.glisten.control.settings.DefaultOption;
import com.gluonhq.charm.glisten.control.settings.Option;
import com.gluonhq.charm.glisten.control.settings.OptionEditor;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.text.Font;
import jloda.fx.control.RichTextLabel;
import jloda.fx.util.BasicFX;
import jloda.fx.util.ProgramExecutorService;
import jloda.fx.util.RunAfterAWhile;
import org.husonlab.phylosketch.network.DefaultOptions;
import org.husonlab.phylosketch.network.Document;
import org.husonlab.phylosketch.network.commands.ReplaceNetworkCommand;
import org.husonlab.phylosketch.views.primary.PrimaryView;

import java.util.Collection;

/**
 * settings presenter
 * Daniel Huson, 12.22
 */
public class SecondaryPresenter {
	private final Document document;

	public SecondaryPresenter(SecondaryController controller, PrimaryView primaryView) {
		this.document = primaryView.getDocument();

		controller.getPropertiesField().textProperty().bind(document.infoProperty());

		// general:

		var treeOption = new DefaultOption<>(MaterialDesignIcon.DATE_RANGE.graphic(),
				"Previous Trees", "Choose tree or network to show", "Input", document.getNewickString(), true,
				NewickTreeEditor::new);
		controller.getSettingsPane().getOptions().addAll(treeOption);
		treeOption.valueProperty().addListener((v, o, n) -> {
			if (n != null && !n.equals(document.getNewickString())) {
				document.getUndoManager().doAndAdd(new ReplaceNetworkCommand(document, n));
			}
		});

		var inputFontSizeOption = new DefaultOption<>(MaterialDesignIcon.FORMAT_SIZE.graphic(),
				"Size", "Set the font size for the input text", "Input", DefaultOptions.getTextAreaFontSize(), true);
		controller.getSettingsPane().getOptions().add(inputFontSizeOption);
		DefaultOptions.bindBidirectional(inputFontSizeOption.valueProperty(), DefaultOptions.textAreaFontSizeProperty());

		// Labels:
		var fontFamilyOption = new DefaultOption<>(MaterialDesignIcon.FONT_DOWNLOAD.graphic(),
				"Font Family", "Set the default font family for labels", "Labels", DefaultOptions.getLabelFontFamily(), true, FontFamilyEditor::new);
		controller.getSettingsPane().getOptions().add(fontFamilyOption);

		fontFamilyOption.valueProperty().bindBidirectional(DefaultOptions.labelFontFamilyProperty());

		var fontSizeOption = new DefaultOption<>(MaterialDesignIcon.FORMAT_SIZE.graphic(),
				"Size", "Set the default font size for labels", "Labels", DefaultOptions.getLabelFontSize(), true);
		controller.getSettingsPane().getOptions().add(fontSizeOption);
		DefaultOptions.bindBidirectional(fontSizeOption.valueProperty(), DefaultOptions.labelFontSizeProperty());

		fontSizeOption.valueProperty().addListener((v, o, n) -> {
			if (n <= 0) {
				Platform.runLater(() -> fontSizeOption.valueProperty().setValue(14.0));
			} else {
				var font = RichTextLabel.getDefaultFont();
				RichTextLabel.setDefaultFont(Font.font(font.getFamily(), BasicFX.getWeight(font), BasicFX.getPosture(font), n));
			}
		});

		// nodes:

		var nodeColorOption = new DefaultOption<>(MaterialDesignIcon.COLOR_LENS.graphic(),
				"Color", "Set the default color for nodes", "Nodes", DefaultOptions.getNodeColor(), true);
		controller.getSettingsPane().getOptions().add(nodeColorOption);
		nodeColorOption.valueProperty().bindBidirectional(DefaultOptions.nodeColorProperty());

		var nodeSizeOption = new DefaultOption<>(MaterialDesignIcon.CROP_FREE.graphic(),
				"Size", "Set the default size for nodes", "Nodes", DefaultOptions.getNodeSize(), true);
		controller.getSettingsPane().getOptions().add(nodeSizeOption);
		DefaultOptions.bindBidirectional(nodeSizeOption.valueProperty(), DefaultOptions.nodeSizeProperty());

		nodeSizeOption.valueProperty().addListener((v, o, n) -> {
			if (n <= 0)
				Platform.runLater(() -> nodeSizeOption.valueProperty().setValue(2.0));
			System.err.println("Default node size: " + n);
		});

		// edges:

		var edgeColorOption = new DefaultOption<>(MaterialDesignIcon.COLOR_LENS.graphic(),
				"Color", "Set the default color for edges", "Edges", DefaultOptions.getEdgeColor(), true);
		controller.getSettingsPane().getOptions().add(edgeColorOption);
		edgeColorOption.valueProperty().bindBidirectional(DefaultOptions.edgeColorProperty());

		var reticulateEdgeOption = new DefaultOption<>(MaterialDesignIcon.COLOR_LENS.graphic(),
				"Reticulate Color", "Set the default color for reticulate edges", "Edges", DefaultOptions.getReticulateColor(), true);
		controller.getSettingsPane().getOptions().add(reticulateEdgeOption);
		reticulateEdgeOption.valueProperty().bindBidirectional(DefaultOptions.reticulateColorProperty());

		var edgeWidthOption = new DefaultOption<>(MaterialDesignIcon.LINE_WEIGHT.graphic(),
				"Width", "Set the default width for edges", "Edges", DefaultOptions.getEdgeWidth(), true);
		controller.getSettingsPane().getOptions().add(edgeWidthOption);
		DefaultOptions.bindBidirectional(edgeWidthOption.valueProperty(), DefaultOptions.edgeWidthProperty());

		edgeWidthOption.valueProperty().addListener((v, o, n) -> {
			if (n <= 0)
				Platform.runLater(() -> edgeWidthOption.valueProperty().setValue(1.0));
			System.err.println("Default line width: " + n);
		});

		// resetAll:
		var resetAll = new SimpleBooleanProperty(this, "resetAll", false);
		var resetAllOption = new DefaultOption<>(MaterialDesignIcon.CLEAR_ALL.graphic(),
				"Reset All", "Reset all default properties", "Reset", false, true);
		controller.getSettingsPane().getOptions().add(resetAllOption);

		resetAllOption.valueProperty().bindBidirectional(resetAll);
		resetAll.addListener((v, o, n) -> {
			if (n) {
				DefaultOptions.resetAll();
				ProgramExecutorService.submit(500, () -> Platform.runLater(() -> resetAll.setValue(false)));
			}
		});
	}

	public class NewickTreeEditor extends ChoiceBoxEditor {
		public NewickTreeEditor(Option<String> option) {
			super(option, DefaultOptions.getTrees());
			document.getGraphFX().lastUpdateProperty().addListener((v, o, n) -> {
				getEditor().setValue(document.getNewickString());
			});
		}
	}

	public static class FontFamilyEditor extends ChoiceBoxEditor {
		public FontFamilyEditor(Option<String> option) {
			super(option, Font.getFamilies());
		}
	}

	public static class ChoiceBoxEditor implements OptionEditor<String> {
		private final ChoiceBox<String> choiceBox = new ChoiceBox<>();

		public ChoiceBoxEditor(Option<String> option, Collection<String> choices) {
			choiceBox.getItems().addAll(choices);
			choiceBox.valueProperty().bindBidirectional(option.valueProperty());
		}

		public ChoiceBoxEditor(Option<String> option, ObservableList<String> choices) {
			choiceBox.setItems(choices);
			choiceBox.valueProperty().bindBidirectional(option.valueProperty());
		}

		@Override
		public ChoiceBox<String> getEditor() {
			return choiceBox;
		}

		@Override
		public Property<String> valueProperty() {
			return choiceBox.valueProperty();
		}

		@Override
		public String getValue() {
			return choiceBox.getValue();
		}

		@Override
		public void setValue(String s) {
			choiceBox.setValue(s);
		}
	}

	;

}
