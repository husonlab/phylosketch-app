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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ChoiceBox;
import javafx.scene.text.Font;
import jloda.fx.control.RichTextLabel;
import jloda.fx.shapes.ISized;
import jloda.fx.util.BasicFX;
import jloda.fx.util.ProgramExecutorService;
import jloda.fx.util.ProgramProperties;
import org.husonlab.phylosketch.DefaultOptions;
import org.husonlab.phylosketch.network.Document;
import org.husonlab.phylosketch.network.NetworkModel;
import org.husonlab.phylosketch.network.commands.ChangeAllEdgeGlyphCommand;
import org.husonlab.phylosketch.network.commands.ReplaceNetworkCommand;
import org.husonlab.phylosketch.utils.TypeToSearchSupport;
import org.husonlab.phylosketch.views.primary.PrimaryView;

import java.util.Collection;
import java.util.List;

/**
 * settings presenter
 * Daniel Huson, 12.22
 */
public class SecondaryPresenter {
	private final Document document;

	private final BooleanProperty resetAll;

	public SecondaryPresenter(SecondaryController controller, PrimaryView primaryView) {
		this.document = primaryView.getDocument();

		controller.getPropertiesField().textProperty().bind(document.infoProperty());

		// general:

		var treeOption = new DefaultOption<>(MaterialDesignIcon.DATE_RANGE.graphic(),
				"Previous Trees", "Choose tree or network to show", "Input", new DefaultOptions.NamedNewick(document.getNewickString()), true,
				NewickTreeEditor::new);
		controller.getSettingsPane().getOptions().addAll(treeOption);
		treeOption.valueProperty().addListener((v, o, n) -> {
			if (n != null && !n.equals(new DefaultOptions.NamedNewick(document.getNewickString()))) {
				document.getUndoManager().doAndAdd(new ReplaceNetworkCommand(document, n.getNewick()));
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

		fontFamilyOption.valueProperty().addListener((v, o, n) -> {
			var font = RichTextLabel.getDefaultFont();
			if (Font.getFamilies().contains(n))
				Platform.runLater(() -> fontFamilyOption.valueProperty().setValue(RichTextLabel.getDefaultFont().getFamily()));
			else {
				RichTextLabel.setDefaultFont(Font.font(n, BasicFX.getWeight(font), BasicFX.getPosture(font), font.getSize()));
				if (false)
					document.getModel().getTree().nodeStream().forEach(a -> document.getNetworkView().getView(a).label().setFontFamily(n));
			}
		});

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
				if (false)
					document.getModel().getTree().nodeStream().forEach(a -> document.getNetworkView().getView(a).label().setFontSize(n));
			}
		});

		// nodes:

		var nodeFillOption = new DefaultOption<>(MaterialDesignIcon.COLOR_LENS.graphic(),
				"Fill", "Set the default fill color for nodes", "Nodes", DefaultOptions.getNodeFill(), true);
		controller.getSettingsPane().getOptions().add(nodeFillOption);
		nodeFillOption.valueProperty().bindBidirectional(DefaultOptions.nodeFillProperty());
		nodeFillOption.valueProperty().addListener((v, o, n) -> {
			document.getModel().getTree().nodeStream().forEach(a -> document.getNetworkView().getView(a).shape().setFill(n));
		});

		var nodeStrokeOption = new DefaultOption<>(MaterialDesignIcon.COLOR_LENS.graphic(),
				"Stroke", "Set the default stroke color for nodes", "Nodes", DefaultOptions.getNodeStroke(), true);
		controller.getSettingsPane().getOptions().add(nodeStrokeOption);
		nodeStrokeOption.valueProperty().bindBidirectional(DefaultOptions.nodeStrokeProperty());
		nodeStrokeOption.valueProperty().addListener((v, o, n) -> {
			document.getModel().getTree().nodeStream().forEach(a -> document.getNetworkView().getView(a).shape().setStroke(n));
		});

		var nodeSizeOption = new DefaultOption<>(MaterialDesignIcon.CROP_FREE.graphic(),
				"Size", "Set the default size for nodes", "Nodes", DefaultOptions.getNodeSize(), true);
		controller.getSettingsPane().getOptions().add(nodeSizeOption);
		DefaultOptions.bindBidirectional(nodeSizeOption.valueProperty(), DefaultOptions.nodeSizeProperty());

		nodeSizeOption.valueProperty().addListener((v, o, n) -> {
			if (n <= 0)
				Platform.runLater(() -> nodeSizeOption.valueProperty().setValue(2.0));
			else {
				document.getModel().getTree().nodeStream().map(a -> document.getNetworkView().getView(a).shape()).filter(s -> s instanceof ISized)
						.forEach(s -> ((ISized) s).setSize(n, n));
			}
			System.err.println("Default node size: " + n);
		});

		// edges:

		var edgeGlyphOption = new DefaultOption<>(MaterialDesignIcon.FONT_DOWNLOAD.graphic(),
				"Edge type", "Set the default edge type", "Edges", DefaultOptions.getEdgeGlyph(), true, EdgeGlyphEditor::new);
		controller.getSettingsPane().getOptions().add(edgeGlyphOption);
		edgeGlyphOption.valueProperty().bindBidirectional(DefaultOptions.edgeGlyphProperty());

		edgeGlyphOption.valueProperty().addListener((v, o, n) -> {
			ProgramProperties.put("EdgeGylph", n.name());
			(new ChangeAllEdgeGlyphCommand(document, o, n)).redo();
		});


		var edgeColorOption = new DefaultOption<>(MaterialDesignIcon.COLOR_LENS.graphic(),
				"Color", "Set the default color for edges", "Edges", DefaultOptions.getEdgeColor(), true);
		controller.getSettingsPane().getOptions().add(edgeColorOption);
		edgeColorOption.valueProperty().bindBidirectional(DefaultOptions.edgeColorProperty());
		edgeColorOption.valueProperty().addListener((v, o, n) -> {
			document.getModel().getTree().edgeStream().forEach(a -> document.getNetworkView().getView(a).setStroke(n));
		});

		var reticulateEdgeOption = new DefaultOption<>(MaterialDesignIcon.COLOR_LENS.graphic(),
				"Reticulate Color", "Set the default color for reticulate edges", "Edges", DefaultOptions.getReticulateColor(), true);
		controller.getSettingsPane().getOptions().add(reticulateEdgeOption);
		reticulateEdgeOption.valueProperty().bindBidirectional(DefaultOptions.reticulateColorProperty());
		reticulateEdgeOption.valueProperty().addListener((v, o, n) -> {
			var tree = document.getModel().getTree();
			for (var e : tree.edges()) {
				if (tree.isReticulateEdge(e) && !tree.isTransferAcceptorEdge(e))
					document.getNetworkView().getView(e).setStroke(n);
			}
		});

		var edgeWidthOption = new DefaultOption<>(MaterialDesignIcon.LINE_WEIGHT.graphic(),
				"Width", "Set the default width for edges", "Edges", DefaultOptions.getEdgeWidth(), true);
		controller.getSettingsPane().getOptions().add(edgeWidthOption);
		DefaultOptions.bindBidirectional(edgeWidthOption.valueProperty(), DefaultOptions.edgeWidthProperty());

		edgeWidthOption.valueProperty().addListener((v, o, n) -> {
			if (n <= 0) {
				Platform.runLater(() -> edgeWidthOption.valueProperty().setValue(1.0));
			} else {
				document.getModel().getTree().edgeStream().forEach(a -> document.getNetworkView().getView(a).setStrokeWidth(n));

			}
			System.err.println("Default line width: " + n);
		});

		// resetAll:
		resetAll = new SimpleBooleanProperty(this, "resetAll", false);
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

	public class NewickTreeEditor extends ChoiceBoxEditor<DefaultOptions.NamedNewick> {
		public NewickTreeEditor(Option<DefaultOptions.NamedNewick> option) {
			super(option, DefaultOptions.getTrees());
			document.getGraphFX().lastUpdateProperty().addListener((v, o, n) -> {
				getEditor().setValue(new DefaultOptions.NamedNewick(document.getNewickString()));
			});
		}
	}

	public static class FontFamilyEditor extends ChoiceBoxEditor<String> {
		public FontFamilyEditor(Option<String> option) {
			super(option, Font.getFamilies());
		}
	}

	public static class EdgeGlyphEditor extends ChoiceBoxEditor<NetworkModel.EdgeGlyph> {
		public EdgeGlyphEditor(Option<NetworkModel.EdgeGlyph> option) {
			super(option, List.of(NetworkModel.EdgeGlyph.values()));
		}
	}

	public static class ChoiceBoxEditor<T> implements OptionEditor<T> {
		private final ChoiceBox<T> choiceBox = new ChoiceBox<T>();

		public ChoiceBoxEditor(Option<T> option, Collection<T> choices) {
			choiceBox.getItems().addAll(choices);
			choiceBox.valueProperty().bindBidirectional(option.valueProperty());
			TypeToSearchSupport.install(choiceBox);
		}

		@Override
		public ChoiceBox<T> getEditor() {
			return choiceBox;
		}

		@Override
		public Property<T> valueProperty() {
			return choiceBox.valueProperty();
		}

		@Override
		public T getValue() {
			return choiceBox.getValue();
		}

		@Override
		public void setValue(T s) {
			choiceBox.setValue(s);
		}
	}

	;

}
