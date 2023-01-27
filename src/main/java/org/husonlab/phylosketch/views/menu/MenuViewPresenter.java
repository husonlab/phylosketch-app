/*
 * MenuViewPresenter.java Copyright (C) 2023 Daniel H. Huson
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

package org.husonlab.phylosketch.views.menu;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.fx.util.BasicFX;
import jloda.fx.util.FileOpenManager;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.window.SplashScreen;
import jloda.graph.Node;
import jloda.phylo.algorithms.RootedNetworkProperties;
import jloda.util.IteratorUtils;
import jloda.util.StringUtils;
import org.husonlab.phylosketch.network.Document;
import org.husonlab.phylosketch.network.commands.DeleteLabelsCommand;
import org.husonlab.phylosketch.network.commands.DeleteSubTreeCommand;
import org.husonlab.phylosketch.network.commands.ReplaceNetworkCommand;
import org.husonlab.phylosketch.views.menu.commands.*;
import org.husonlab.phylosketch.views.primary.PrimaryView;

import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class MenuViewPresenter {
	public MenuViewPresenter(PrimaryView primaryView, MenuViewController controller) {

		var document = primaryView.getDocument();
		var undoManager = document.getUndoManager();

		primaryView.getStage().setTitle("PhyloSketch-App");

		controller.getNewMenuItem().setDisable(true); // don't know how to implement this
		controller.getOpenMenuItem().setOnAction(e -> OpenCommand.apply((Stage) primaryView.getView().getScene().getWindow()));

		FileOpenManager.setFileOpener(OpenCommand.createFileOpener());
		RecentFilesManager.getInstance().setFileOpener(FileOpenManager.getFileOpener());
		RecentFilesManager.getInstance().setupMenu(controller.getRecentMenu());

		controller.getSaveAsMenuItem().setOnAction(e -> SaveCommand.apply(primaryView));
		controller.getSaveAsMenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());

		// adding the following code breaks transpilation for iOS:
		/*
		controller.getPageSetupMenuItem().setOnAction(e -> Print.showPageLayout(primaryView.getStage()));

		controller.getPrintMenuItem().setOnAction(e -> Print.print(primaryView.getStage(), primaryView.getController().getStackPane()));
		controller.getPrintMenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());
		*/
		controller.getPrintMenuItem().setDisable(true);
		controller.getPageSetupMenuItem().setDisable(true);

		controller.getCloseMenuItem().setOnAction(e -> System.exit(0));
		controller.getQuitMenuItem().setOnAction(e -> System.exit(0));

		controller.getUndoMenuItem().setOnAction(e -> undoManager.undo());
		controller.getUndoMenuItem().disableProperty().bind(undoManager.undoableProperty().not());

		controller.getRedoMenuItem().setOnAction(e -> undoManager.redo());
		controller.getRedoMenuItem().disableProperty().bind(undoManager.redoableProperty().not());

		controller.getCutMenuItem().setOnAction(e -> {
			if (document.getNodeSelection().size() > 0) {
				var deleteLabelsCommand = new DeleteLabelsCommand(document, document.getNodeSelection().getSelectedItems());
				undoManager.doAndAdd(UndoableRedoableCommand.create("cut", deleteLabelsCommand::undo,
						() -> {
							copySelectedNodesToClipboard(document);
							deleteLabelsCommand.redo();
						}));
			}
		});
		controller.getCutMenuItem().disableProperty().bind(Bindings.isEmpty(document.getNodeSelection().getSelectedItems()));

		controller.getCopyMenuItem().setOnAction(e -> {
			if (document.getNodeSelection().size() > 0) {
				copySelectedNodesToClipboard(document);
			} else {
				final var snapshot = primaryView.getController().getStackPane().snapshot(null, null);
				final var clipboardContent = new ClipboardContent();
				clipboardContent.putImage(snapshot);
				Clipboard.getSystemClipboard().setContent(clipboardContent);
			}
		});
		controller.getCopyMenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());

		controller.getCopyNewickMenuItem().setOnAction(e -> {
			final var clipboardContent = new ClipboardContent();
			clipboardContent.putString(document.getNewickString());
			Clipboard.getSystemClipboard().setContent(clipboardContent);
		});
		controller.getCopyNewickMenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());

		controller.getPasteMenuItem().setDisable(true);

		controller.getPasteNewickMenuItem().setOnAction(e -> {
			var cb = Clipboard.getSystemClipboard();
			if (cb.hasString() && Document.canParse(cb.getString(), true)) {
				undoManager.doAndAdd(new ReplaceNetworkCommand(document, cb.getString()));
			}
		});

		controller.getDeleteMenuItem().setOnAction(a -> {
			var nodesToDelete = new HashSet<>(document.getNodeSelection().getSelectedItems());
			for (var v : nodesToDelete) {
				if (v != document.getModel().getTree().getRoot() && IteratorUtils.asSet(document.getModel().getTree().nodes()).contains(v))
					undoManager.doAndAdd(new DeleteSubTreeCommand(document, v));
			}
		});
		controller.getDeleteMenuItem().disableProperty().bind(Bindings.isEmpty(document.getNodeSelection().getSelectedItems()));

		controller.getDeleteLabelsMenuItem().setOnAction(a -> undoManager.doAndAdd(new DeleteLabelsCommand(document, document.getNodeSelection().getSelectedItems())));
		controller.getDeleteLabelsMenuItem().disableProperty().bind(Bindings.isEmpty(document.getNodeSelection().getSelectedItems()));


		// not implemented:
		controller.getFindMenuItem().setDisable(true);
		controller.getFindAgainMenuItem().setDisable(true);
		controller.getReplaceMenuItem().setDisable(true);

		// selection menu items:

		controller.getSelectAllMenuItem().setOnAction(e -> {
			document.getNodeSelection().selectAll(document.getModel().getTree().getNodesAsList());
			document.getEdgeSelection().selectAll(document.getModel().getTree().getEdgesAsList());
		});
		controller.getSelectAllMenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());

		controller.getSelectNoneMenuItem().setOnAction(e -> {
			document.getNodeSelection().clearSelection();
			document.getEdgeSelection().clearSelection();
		});
		controller.getSelectNoneMenuItem().disableProperty().bind(Bindings.isEmpty(document.getNodeSelection().getSelectedItems())
				.and(Bindings.isEmpty(document.getEdgeSelection().getSelectedItems())));

		controller.getSelectInvertMenuItem().setOnAction(a -> {
			document.getModel().getTree().nodeStream().forEach(v -> document.getNodeSelection().toggleSelection(v));
			document.getModel().getTree().edgeStream().forEach(e -> document.getEdgeSelection().toggleSelection(e));
		});
		controller.getSelectInvertMenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());

		controller.getSelectTreeEdgesMenuItem().setOnAction(a -> {
			var tree = document.getModel().getTree();
			tree.edgeStream().filter(tree::isTreeEdge).forEach(e -> document.getEdgeSelection().select(e));
		});
		controller.getSelectTreeEdgesMenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());

		controller.getSelectReticulateEdgesMenuItem().setOnAction(a -> {
			var tree = document.getModel().getTree();
			tree.edgeStream().filter(tree::isReticulateEdge).forEach(e -> document.getEdgeSelection().select(e));
		});
		controller.getSelectReticulateEdgesMenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());

		controller.getSelectRootMenuItem().setOnAction(a -> {
			var root = document.getModel().getTree().getRoot();
			if (root != null)
				document.getNodeSelection().select(root);
		});
		controller.getSelectRootMenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());

		controller.getSelectLeavesMenuItem().setOnAction(a -> {
			document.getModel().getTree().nodeStream().filter(Node::isLeaf).forEach(v -> document.getNodeSelection().select(v));
		});
		controller.getSelectLeavesMenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());

		controller.getSelectTreeNodesMenuItem().setOnAction(a -> {
			document.getModel().getTree().nodeStream().filter(v -> v.getInDegree() < 2).forEach(v -> document.getNodeSelection().select(v));
		});
		controller.getSelectTreeNodesMenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());

		controller.getSelectReticulateNodesMenuItem().setOnAction(a -> {
			document.getModel().getTree().nodeStream().filter(v -> v.getInDegree() >= 2).forEach(v -> document.getNodeSelection().select(v));
		});
		controller.getSelectReticulateNodesMenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());

		controller.getSelectVisibleNodesMenuItem().setOnAction(e -> RootedNetworkProperties.computeAllVisibleNodes(document.getModel().getTree(), null)
				.forEach(v -> document.getNodeSelection().select(v)));
		controller.getSelectVisibleNodesMenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());

		controller.getSelectVisibleReticulationsMenuItem().setOnAction(e -> RootedNetworkProperties.computeAllVisibleNodes(document.getModel().getTree(), null)
				.stream().filter(v -> v.getInDegree() > 1).forEach(v -> document.getNodeSelection().select(v)));
		controller.getSelectVisibleReticulationsMenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());

		controller.getSelectStableNodesMenuItem().setOnAction(e -> RootedNetworkProperties.computeAllCompletelyStableInternal(document.getModel().getTree())
				.forEach(v -> document.getNodeSelection().select(v)));
		controller.getSelectStableNodesMenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());

		controller.getSelectAllAboveMenuItem().setOnAction(c -> {
			final var list = new LinkedList<>(document.getNodeSelection().getSelectedItems());

			while (list.size() > 0) {
				var v = list.remove();
				for (var e : v.inEdges()) {
					var w = e.getSource();
					document.getEdgeSelection().select(e);
					if (!document.getNodeSelection().isSelected(w)) {
						document.getNodeSelection().select(w);
						list.add(w);
					}
				}
			}
		});
		controller.getSelectAllAboveMenuItem().disableProperty().bind(Bindings.isEmpty(document.getNodeSelection().getSelectedItems()));

		controller.getSelectAllBelowMenuItem().setOnAction(c -> {
			final var list = new LinkedList<>(document.getNodeSelection().getSelectedItems());

			while (list.size() > 0) {
				var v = list.remove();
				for (var e : v.outEdges()) {
					var w = e.getTarget();
					document.getEdgeSelection().select(e);
					if (!document.getNodeSelection().isSelected(w)) {
						document.getNodeSelection().select(w);
						list.add(w);
					}
				}
			}
		});
		controller.getSelectAllBelowMenuItem().disableProperty().bind(Bindings.isEmpty(document.getNodeSelection().getSelectedItems()));

		var isLeafLabeledDag = new SimpleBooleanProperty(false);
		document.getGraphFX().lastUpdateProperty().addListener(a -> {
			var tree = document.getModel().getTree();
			isLeafLabeledDag.set(tree.isReticulated() && tree.nodeStream().filter(Node::isLeaf).filter(v -> tree.getLabel(v) == null || tree.getLabel(v).isBlank()).findAny().isEmpty());
		});


		controller.getSelectLowestStableAncestorMenuItem().setOnAction(e -> document.getNodeSelection().selectAll(RootedNetworkProperties.computeAllLowestStableAncestors(document.getModel().getTree(), document.getNodeSelection().getSelectedItems())));
		controller.getSelectLowestStableAncestorMenuItem().disableProperty().bind(Bindings.isEmpty(document.getNodeSelection().getSelectedItems()).or(isLeafLabeledDag.not()));

		// labeling menu items:

		controller.getLabelLeavesABCMenuItem().setOnAction(c -> undoManager.doAndAdd(new ChangeNodeLabelsCommand(document, LabelLeaves.labelLeavesABC(document))));
		controller.getLabelLeavesABCMenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());

		controller.getLabelLeaves123MenuItem().setOnAction(c -> undoManager.doAndAdd(new ChangeNodeLabelsCommand(document, LabelLeaves.labelLeaves123(document))));
		controller.getLabelLeaves123MenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());

		controller.getLabelLeavesMenuItem().setOnAction(c -> LabelLeaves.labelLeaves(primaryView.getStage(), document));
		controller.getLabelLeavesMenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());

		controller.getLabelInternalABCMenuItem().setOnAction(c -> undoManager.doAndAdd(new ChangeNodeLabelsCommand(document, LabelLeaves.labelInternalABC(document))));
		controller.getLabelInternalABCMenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());

		controller.getLabelInternal123MenuItem().setOnAction(c -> undoManager.doAndAdd(new ChangeNodeLabelsCommand(document, LabelLeaves.labelInternal123(document))));
		controller.getLabelInternal123MenuItem().disableProperty().bind(document.getGraphFX().emptyProperty());

		// other options:

		controller.getRemoveDiNodesMenuItem().setOnAction(c -> undoManager.doAndAdd(new RemoveDiNodesCommand(document, document.getNodeSelection().getSelectedItems())));
		controller.getRemoveDiNodesMenuItem().disableProperty().bind(Bindings.isEmpty(document.getNodeSelection().getSelectedItems()));

		controller.getAddDiNodesMenuItem().setOnAction(c -> undoManager.doAndAdd(SplitEdgeCommand.createAddDiNodesCommand(document, document.getEdgeSelection().getSelectedItems())));
		controller.getAddDiNodesMenuItem().disableProperty().bind(Bindings.isEmpty(document.getEdgeSelection().getSelectedItems()));

		controller.getStraightEdgesMenuItem().setOnAction(a -> primaryView.getController().getStraightEdgesRadioMenuItem().setSelected(true));
		controller.getRectangularEdgesMenuItem().setOnAction(a -> primaryView.getController().getRectangularEdgesRadioMenuItem().setSelected(true));
		controller.getCubicEdgesMenuItem().setOnAction(a -> primaryView.getController().getRoundEdgesRadioMenuItem().setSelected(true));

		controller.getNormalizationMenuItem().setOnAction(c -> undoManager.doAndAdd(new NormalizeCommand(document)));
		controller.getNormalizationMenuItem().disableProperty().bind(isLeafLabeledDag.not());

		controller.getUseDarkThemeCheckMenuItem().setDisable(true);

		controller.getIncreaseFontSizeMenuItem().setOnAction(primaryView.getController().getIncreaseFontSizeButton().getOnAction());
		controller.getDecreaseFontSizeMenuItem().setOnAction(primaryView.getController().getDecreaseFontSizeButton().getOnAction());

		controller.getZoomInVerticallyMenuItem().setOnAction(primaryView.getController().getVerticalZoomInButton().getOnAction());
		controller.getZoomOutVerticallyMenuItem().setOnAction(primaryView.getController().getVerticalZoomOutButton().getOnAction());

		controller.getZoomInHorizontallyMenuItem().setOnAction(primaryView.getController().getHorizontalZoomInButton().getOnAction());
		controller.getZoomOutHorizontallyMenuItem().setOnAction(primaryView.getController().getHorizontalZoomOutButton().getOnAction());

		BasicFX.setupFullScreenMenuSupport(primaryView.getStage(), controller.getEnterFullScreenMenuItem());

		controller.getAboutMenuItem().setOnAction(e -> SplashScreen.showSplash(Duration.ofMinutes(2)));

		controller.getCheckForUpdatesMenuItem().setOnAction(e -> CheckForUpdate.apply());
	}

	private static void copySelectedNodesToClipboard(Document document) {
		if (document.getNodeSelection().size() > 0) {
			var tree = document.getModel().getTree();
			var string = StringUtils.toString(document.getNodeSelection().getSelectedItems().stream()
					.map(tree::getLabel).filter(s -> s != null && !s.isBlank()).collect(Collectors.toList()), "\n");
			if (!string.isBlank()) {
				final var clipboardContent = new ClipboardContent();
				clipboardContent.putString(string);
				Clipboard.getSystemClipboard().setContent(clipboardContent);
			}
		}
	}
}
