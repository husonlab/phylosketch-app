/*
 * PrimaryPresenter.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.views.primary;

import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Duration;
import jloda.fx.control.RichTextLabel;
import jloda.fx.util.AutoCompleteComboBox;
import jloda.fx.util.RunAfterAWhile;
import jloda.phylo.PhyloTree;
import org.husonlab.phylosketch.Main;
import org.husonlab.phylosketch.network.Document;
import org.husonlab.phylosketch.network.NetworkModel;
import org.husonlab.phylosketch.network.commands.*;

import java.util.Objects;

/**
 * the primary view presenter
 * Daniel Huson, 10.2022
 */
public class PrimaryPresenter {
	public enum ArrowType {ArrowNone, ArrowRight, ArrowLeft, ArrowBoth}

	private final ObjectProperty<InteractionMode> interactionMode = new SimpleObjectProperty<>(this, "interactionMode", InteractionMode.Pan);

	private final ObjectProperty<NetworkModel.EdgeGlyph> edgeGlyph = new SimpleObjectProperty<>(this, "edgeShape");

	private final DoubleProperty textFieldFontSize = new SimpleDoubleProperty(this, "textFieldFontSize", 14);

	public PrimaryPresenter(Document document, PrimaryView view, PrimaryController controller) {

		if (!Main.isDesktop()) {
			controller.getScrollPane().addEventFilter(InputEvent.ANY, a -> {
				if (interactionModeProperty().get() != InteractionMode.Pan) {
					var name = a.getEventType().getName();
					if ((name.startsWith("SCROLL")) && (document.getNodeSelection().size() > 0 || document.getEdgeSelection().size() > 0)) {
						a.consume();
					}
				}
			});
		}

		interactionMode.addListener((v, o, n) -> {
			controller.getScrollPane().setPannable(n == InteractionMode.Pan);
			controller.getModeLabel().setText(n == null ? "" : n.getDescription());
			controller.getModeLabel().setOpacity(1.0);
			var fadeTransition = new FadeTransition(Duration.seconds(1), controller.getModeLabel());
			fadeTransition.setToValue(0.0);
			var transition = new SequentialTransition(new PauseTransition(Duration.seconds(5)), fadeTransition);
			transition.play();
			if (n != null) {
				switch (n) {
					case Pan -> controller.getScrollPane().setCursor(Cursor.OPEN_HAND);
					case EditLabels -> controller.getScrollPane().setCursor(Cursor.TEXT);
					case Move -> controller.getScrollPane().setCursor(Cursor.HAND);
					case CreateNewEdges -> controller.getScrollPane().setCursor(Cursor.CROSSHAIR);
					default -> controller.getScrollPane().setCursor(Cursor.DEFAULT);
				}
			}
		});

		controller.getModeLabel().setText("");

		controller.getUndoButton().setOnAction(e -> document.getUndoManager().undo());
		controller.getUndoButton().disableProperty().bind(document.getUndoManager().undoableProperty().not());
		controller.getRedoButton().setOnAction(e -> document.getUndoManager().redo());
		controller.getRedoButton().disableProperty().bind(document.getUndoManager().redoableProperty().not());

		if (com.gluonhq.attach.util.Platform.isDesktop()) {
			controller.getStackPane().setOnScroll(e -> {
				var factor = (e.getDeltaY() > 0 ? 1.1 : 1 / 1.1);
				var box = view.getDocument().getNetworkView().getBoundingBox();
				if (!(factor < 1 && Math.min(box.getWidth(), box.getHeight()) < 200 || factor > 1 && Math.max(box.getWidth(), box.getHeight()) > 2000)) {
					view.getDocument().getNetworkView().scale(factor, factor);
				}
				e.consume();
			});
		}
		controller.getStackPane().setOnZoom(e -> {
			var factor = e.getZoomFactor();
			var box = view.getDocument().getNetworkView().getBoundingBox();
			if (!(factor < 1 && Math.min(box.getWidth(), box.getHeight()) < 200 || factor > 1 && Math.max(box.getWidth(), box.getHeight()) > 2000)) {
				view.getDocument().getNetworkView().scale(factor, factor);
			}
			e.consume();
		});

		controller.getScrollPane().setOnMouseReleased(c -> {
			if (c.isStillSincePress() && !c.isShiftDown()) {
				view.getDocument().getNodeSelection().clearSelection();
				view.getDocument().getEdgeSelection().clearSelection();
			}
		});

		controller.getModeToggleGroup().selectedToggleProperty().addListener((v, o, n) -> {
			Node graphic = null;
			if (n instanceof MenuItem menuItem) {
				if (menuItem == controller.getPanMenuItem()) {
					interactionMode.set(InteractionMode.Pan);
					graphic = MaterialDesignIcon.PAN_TOOL.graphic();
				} else if (menuItem == controller.getEditMenuItem()) {
					interactionMode.set(InteractionMode.CreateNewEdges);
					graphic = MaterialDesignIcon.EDIT.graphic();
				}
				if (menuItem == controller.getMoveMenuItem()) {
					interactionMode.set(InteractionMode.Move);
					graphic = MaterialDesignIcon.SWAP_VERT.graphic();
				}
				if (menuItem == controller.getEraseMenuItem()) {
					interactionMode.set(InteractionMode.Erase);
					graphic = MaterialDesignIcon.REMOVE_CIRCLE.graphic();
				}
				if (menuItem == controller.getEditLabelMenuItem()) {
					interactionMode.set(InteractionMode.EditLabels);
					graphic = MaterialDesignIcon.LABEL_OUTLINE.graphic();
				}
			}
			if (graphic == null)
				graphic = MaterialDesignIcon.HELP_OUTLINE.graphic();
			;
			graphic.setStyle("-fx-text-fill: white;");
			controller.getModeMenuButton().setGraphic(graphic);
		});
		controller.getPanMenuItem().setSelected(true);

		edgeGlyph.addListener((c, o, n) -> document.getUndoManager().doAndAdd(new ChangeEdgeShapeCommand(document, o, n)));

		controller.getEdgeShapeToggleGroup().selectedToggleProperty().addListener((v, o, n) -> {
			if (n == controller.getRectangularEdgesRadioMenuItem())
				edgeGlyph.set(NetworkModel.EdgeGlyph.RectangleLine);
			else if (n == controller.getRoundEdgesRadioMenuItem())
				edgeGlyph.set(NetworkModel.EdgeGlyph.CubicCurve);
			else
				edgeGlyph.set(NetworkModel.EdgeGlyph.StraightLine);
		});
		controller.getEdgeShapeToggleGroup().selectToggle(controller.getStraightEdgesRadioMenuItem());

		controller.getArrowTypeToggleGroup().selectedToggleProperty().addListener((v, o, n) -> {
			if (n != null) {
				var showArrow = (n == controller.getArrowRightRadioMenuItem());
				document.getUndoManager().doAndAdd(new ShowArrowHeadCommand(document, showArrow));
			}
		});
		controller.getArrowTypeToggleGroup().selectToggle(controller.getArrowNoneRadioMenuItem());

		controller.getWidthSlider().valueProperty().addListener((v, o, n) -> RunAfterAWhile.applyInFXThread(controller.getWidthSlider(), () -> {
			document.getUndoManager().doAndAdd(new ChangeLineWidthCommand(document, n.doubleValue()));
		}));

		controller.getLineColorPicker().setOnAction(a -> {
			document.getUndoManager().doAndAdd(new ChangeLineColorCommand(document, controller.getLineColorPicker().getValue()));
		});

		controller.getFontColorPicker().setOnAction(a -> {
			document.getUndoManager().doAndAdd(new ChangeLabelColorCommand(document, controller.getFontColorPicker().getValue()));
		});

		InvalidationListener edgeSelectionInvalidationListener = a -> {
			var hasArrow = false;
			var hasNoArrow = false;
			var maxLineWidth = 0.0;
			Paint lineColor = null;
			int numLineColors = 0;
			for (var e : document.getSelectedOrAllEdges()) {
				var ev = document.getNetworkView().getView(e);
				if (ev != null) {
					if (ev.isShowArrowHead()) {
						hasArrow = true;
					} else {
						hasNoArrow = true;
					}
					maxLineWidth = Math.max(maxLineWidth, ev.getStrokeWidth());
					if (numLineColors < 2) {
						if (lineColor == null) {
							lineColor = ev.getStroke();
							numLineColors = 1;
						} else if (!lineColor.equals(ev.getStroke())) {
							numLineColors = 2;
						}
					}
				}
			}
			if (hasArrow == hasNoArrow)
				controller.getArrowTypeToggleGroup().selectToggle(null);
			else if (hasArrow)
				controller.getArrowTypeToggleGroup().selectToggle(controller.getArrowRightRadioMenuItem());
			else
				controller.getArrowTypeToggleGroup().selectToggle(controller.getArrowNoneRadioMenuItem());
			controller.getLineColorPicker().setValue(numLineColors == 1 ? (Color) lineColor : null);
		};
		document.getEdgeSelection().getSelectedItems().addListener(edgeSelectionInvalidationListener);
		document.getGraphFX().lastUpdateProperty().addListener(edgeSelectionInvalidationListener);
		edgeSelectionInvalidationListener.invalidated(null);

		InvalidationListener nodeSelectionInvalidationListener = a -> {
			Paint color = null;
			var numColors = 0;
			var hasBold = false;
			var hasNotBold = false;
			var hasItalic = false;
			var hasNotItalic = false;
			var hasUnderline = false;
			var hasNotUnderline = false;
			var fontFamily = "";

			for (var v : document.getSelectedOrAllNodes()) {
				var nv = document.getNetworkView().getView(v);
				if (nv != null) {
					var label = nv.label();
					if (label != null && label.getText().length() > 0) {
						if (numColors < 2) {
							if (color == null) {
								color = label.getTextFill();
								numColors = 1;
							} else if (!color.equals(label.getTextFill())) {
								numColors = 2;
							}
						}
						if (label.isBold())
							hasBold = true;
						else
							hasNotBold = true;
						if (label.isItalic())
							hasItalic = true;
						else
							hasNotItalic = true;
						if (label.isUnderline())
							hasUnderline = true;
						else
							hasNotUnderline = true;
						if (fontFamily != null) {
							if (fontFamily.equals(""))
								fontFamily = label.getFontFamily();
							else if (!fontFamily.equals(label.getFontFamily()))
								fontFamily = null;
						}
					}
				}
			}
			controller.getFontColorPicker().setValue(numColors == 1 ? (Color) color : null);
			controller.getBoldCheckMenuItem().setSelected(hasBold && !hasNotBold);
			controller.getItalicCheckMenuItem().setSelected(hasItalic && !hasNotItalic);
			controller.getUnderlineCheckMenuItem().setSelected(hasUnderline && !hasNotUnderline);
			if (!Objects.equals(fontFamily, ""))
				controller.getFontComboBox().setValue(fontFamily);
		};
		document.getNodeSelection().getSelectedItems().addListener(nodeSelectionInvalidationListener);
		document.getGraphFX().lastUpdateProperty().addListener(nodeSelectionInvalidationListener);
		nodeSelectionInvalidationListener.invalidated(null);

		controller.getWidthSlider().setValue(1);

		controller.infoStringProperty().bind(document.infoProperty());

		var newickText = controller.getNewickTextArea().textProperty();
		var inputChanged = new SimpleBooleanProperty(this, "inputChanged", false);

		InvalidationListener updateNewickString = a -> {
			newickText.set(document.getNewickString());
			inputChanged.set(false);
		};

		document.getGraphFX().lastUpdateProperty().addListener(updateNewickString);

		newickText.set(document.getNewickString());
		newickText.addListener(a -> inputChanged.set(true));

		var isValidNewick = new SimpleBooleanProperty(this, "isValidNewick", false);
		isValidNewick.bind(Bindings.createBooleanBinding(() -> canParse(newickText.get(), true), newickText, document.getGraphFX().lastUpdateProperty()));

		controller.getImportButton().disableProperty().bind((inputChanged.and(isValidNewick)).not());

		controller.getImportButton().setOnAction(a -> {
			if (!newickText.get().endsWith(";"))
				newickText.set(newickText.get() + ";");
			document.getUndoManager().doAndAdd(new ReplaceNetworkCommand(document, newickText.get(), edgeGlyph.get()));
			inputChanged.set(false);
		});

		controller.getShowWeightsToggleButton().selectedProperty().bindBidirectional(document.toScaleProperty());
		controller.getShowWeightsToggleButton().selectedProperty().addListener(updateNewickString);
		controller.getShowWeightsToggleButton().selectedProperty().addListener(e -> inputChanged.set(true));

		controller.getShowHTMLToggleButton().selectedProperty().bindBidirectional(document.showHTMLProperty());
		controller.getShowHTMLToggleButton().selectedProperty().addListener(updateNewickString);
		controller.getShowHTMLToggleButton().selectedProperty().addListener(e -> inputChanged.set(true));

		controller.getShowNewickToggleButton().selectedProperty().addListener((v, o, n) -> {
			if (n)
				controller.getNewickTextArea().setText(document.getNewickString());
			inputChanged.set(false);
		});

		controller.getIncreaseFontSizeButton().setOnAction(a -> document.getUndoManager().doAndAdd("font size",
				() -> {
					document.getNetworkView().setFontScale(1.0 / 1.2 * document.getNetworkView().getFontScale());
					textFieldFontSize.set(1.0 / 1.2 * textFieldFontSize.get());

				}, () -> {
					document.getNetworkView().setFontScale(1.2 * document.getNetworkView().getFontScale());
					textFieldFontSize.set(1.2 * textFieldFontSize.get());

				}));

		controller.getDecreaseFontSizeButton().setOnAction(a -> document.getUndoManager().doAndAdd("font size",
				() -> {
					document.getNetworkView().setFontScale(1.2 * document.getNetworkView().getFontScale());
					textFieldFontSize.set(1.2 * textFieldFontSize.get());

				}, () -> {
					document.getNetworkView().setFontScale(1.0 / 1.2 * document.getNetworkView().getFontScale());
					textFieldFontSize.set(1.0 / 1.2 * textFieldFontSize.get());
				}));


		textFieldFontSize.addListener((c, o, n) -> {
			if (n.doubleValue() >= 10 && n.doubleValue() <= 32) {
				controller.getNewickTextArea().setStyle("-fx-font-family: 'Courier New';-fx-font-size: %.1f;".formatted(n.doubleValue()));
			}
		});


		controller.getItalicCheckMenuItem().setOnAction(a -> document.getUndoManager().doAndAdd(new SetItalicFontCommand(document, controller.getItalicCheckMenuItem().isSelected())));
		controller.getBoldCheckMenuItem().setOnAction(a -> document.getUndoManager().doAndAdd(new SetBoldFontCommand(document, controller.getBoldCheckMenuItem().isSelected())));
		controller.getUnderlineCheckMenuItem().setOnAction(a -> document.getUndoManager().doAndAdd(new SetUnderlineFontCommand(document, controller.getUnderlineCheckMenuItem().isSelected())));

		controller.getFontComboBox().getItems().addAll(Font.getFamilies());
		controller.getFontComboBox().setValue(RichTextLabel.getDefaultFont().getFamily());
		controller.getFontComboBox().valueProperty().addListener((v, o, n) -> {
			if (controller.getFontComboBox().getItems().contains(n))
				document.getUndoManager().doAndAdd(new SetFontFamilyCommand(document, controller.getFontComboBox().getValue()));
		});
		AutoCompleteComboBox.install(controller.getFontComboBox());

		// pressing enter in the newick text area will load the newick string, if it is valid
		controller.getNewickTextArea().addEventFilter(KeyEvent.KEY_PRESSED, e -> {
			if (e.getCode().equals(KeyCode.ENTER)) {
				e.consume();
				if (!controller.getImportButton().isDisabled()) {
					controller.getImportButton().fire();
				}
			}
		});
	}

	private static boolean canParse(String newick, boolean first) {
		newick = newick.trim();
		if (!newick.startsWith("(") && !(newick.endsWith(")") || newick.endsWith(";")))
			return false;
		if (newick.endsWith(";"))
			newick = newick.substring(0, newick.length() - 1).trim();
		try {
			var tree = new PhyloTree();
			tree.parseBracketNotation(newick, true);
			return tree.getNumberOfNodes() > 0 && (!first || !canParse(newick.substring(0, newick.length() - 1), false));
		} catch (Exception ignored) {
			return false;
		}
	}

	public ObjectProperty<InteractionMode> interactionModeProperty() {
		return interactionMode;
	}
}
