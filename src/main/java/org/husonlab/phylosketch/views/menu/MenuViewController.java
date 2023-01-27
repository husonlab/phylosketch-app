/*
 * MenuViewController.java Copyright (C) 2023 Daniel H. Huson
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

import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import jloda.fx.util.ProgramProperties;

public class MenuViewController {

	@FXML
	private MenuItem aboutMenuItem;

	@FXML
	private MenuItem addDiNodesMenuItem;

	@FXML
	private MenuItem checkForUpdatesMenuItem;

	@FXML
	private MenuItem closeMenuItem;

	@FXML
	private MenuItem copyMenuItem;

	@FXML
	private MenuItem copyNewickMenuItem;

	@FXML
	private MenuItem cutMenuItem;

	@FXML
	private MenuItem decreaseFontSizeMenuItem;

	@FXML
	private MenuItem deleteLabelsMenuItem;

	@FXML
	private MenuItem deleteMenuItem;

	@FXML
	private Menu editMenu;

	@FXML
	private MenuItem enterFullScreenMenuItem;

	@FXML
	private Menu fileMenu;

	@FXML
	private MenuItem findAgainMenuItem;

	@FXML
	private MenuItem findMenuItem;

	@FXML
	private MenuItem increaseFontSizeMenuItem;

	@FXML
	private MenuItem labelInternal123MenuItem;

	@FXML
	private MenuItem labelInternalABCMenuItem;

	@FXML
	private MenuItem labelLeaves123MenuItem;

	@FXML
	private MenuItem labelLeavesMenuItem;

	@FXML
	private MenuItem labelLeavesABCMenuItem;

	@FXML
	private MenuBar menuBar;

	@FXML
	private MenuItem newMenuItem;

	@FXML
	private MenuItem normalizationMenuItem;

	@FXML
	private MenuItem openMenuItem;

	@FXML
	private MenuItem pageSetupMenuItem;

	@FXML
	private MenuItem pasteMenuItem;

	@FXML
	private MenuItem pasteNewickMenuItem;

	@FXML
	private MenuItem printMenuItem;

	@FXML
	private MenuItem quitMenuItem;

	@FXML
	private Menu recentMenu;

	@FXML
	private MenuItem redoMenuItem;

	@FXML
	private MenuItem removeDiNodesMenuItem;

	@FXML
	private MenuItem replaceMenuItem;

	@FXML
	private MenuItem rectangularEdgesMenuItem;

	@FXML
	private MenuItem cubicEdgesMenuItem;

	@FXML
	private MenuItem saveAsMenuItem;

	@FXML
	private MenuItem selectAllAboveMenuItem;

	@FXML
	private MenuItem selectAllBelowMenuItem;

	@FXML
	private MenuItem selectAllMenuItem;

	@FXML
	private MenuItem selectInvertMenuItem;

	@FXML
	private MenuItem selectLeavesMenuItem;

	@FXML
	private MenuItem selectLowestStableAncestorMenuItem;

	@FXML
	private MenuItem selectNoneMenuItem;

	@FXML
	private MenuItem selectReticulateEdgesMenuItem;

	@FXML
	private MenuItem selectReticulateNodesMenuItem;

	@FXML
	private MenuItem selectRootMenuItem;

	@FXML
	private MenuItem selectStableNodesMenuItem;

	@FXML
	private MenuItem selectTreeEdgesMenuItem;

	@FXML
	private MenuItem selectTreeNodesMenuItem;

	@FXML
	private MenuItem selectVisibleNodesMenuItem;

	@FXML
	private MenuItem selectVisibleReticulationsMenuItem;

	@FXML
	private MenuItem straightEdgesMenuItem;

	@FXML
	private MenuItem undoMenuItem;

	@FXML
	private CheckMenuItem useDarkThemeCheckMenuItem;

	@FXML
	private Menu windowMenu;

	@FXML
	private MenuItem zoomInHorizontallyMenuItem;

	@FXML
	private MenuItem zoomInVerticallyMenuItem;

	@FXML
	private MenuItem zoomOutHorizontallyMenuItem;

	@FXML
	private MenuItem zoomOutVerticallyMenuItem;

	@FXML
	private void initialize() {
		getMenuBar().getStylesheets().setAll("org/husonlab/phylosketch/views/menu/menu.css");

		if (ProgramProperties.isMacOS()) {
			getMenuBar().setUseSystemMenuBar(true);
			getFileMenu().getItems().remove(getQuitMenuItem());
			// windowMenu.getItems().remove(getAboutMenuItem());
			//editMenu.getItems().remove(getPreferencesMenuItem());
		}
	}

	public MenuItem getAboutMenuItem() {
		return aboutMenuItem;
	}

	public MenuItem getAddDiNodesMenuItem() {
		return addDiNodesMenuItem;
	}

	public MenuItem getCheckForUpdatesMenuItem() {
		return checkForUpdatesMenuItem;
	}

	public MenuItem getCloseMenuItem() {
		return closeMenuItem;
	}

	public MenuItem getCopyMenuItem() {
		return copyMenuItem;
	}

	public MenuItem getCopyNewickMenuItem() {
		return copyNewickMenuItem;
	}

	public MenuItem getCutMenuItem() {
		return cutMenuItem;
	}

	public MenuItem getDecreaseFontSizeMenuItem() {
		return decreaseFontSizeMenuItem;
	}

	public MenuItem getDeleteLabelsMenuItem() {
		return deleteLabelsMenuItem;
	}

	public MenuItem getDeleteMenuItem() {
		return deleteMenuItem;
	}

	public Menu getEditMenu() {
		return editMenu;
	}

	public MenuItem getEnterFullScreenMenuItem() {
		return enterFullScreenMenuItem;
	}

	public Menu getFileMenu() {
		return fileMenu;
	}

	public MenuItem getFindAgainMenuItem() {
		return findAgainMenuItem;
	}

	public MenuItem getFindMenuItem() {
		return findMenuItem;
	}

	public MenuItem getIncreaseFontSizeMenuItem() {
		return increaseFontSizeMenuItem;
	}

	public MenuItem getLabelInternal123MenuItem() {
		return labelInternal123MenuItem;
	}

	public MenuItem getLabelInternalABCMenuItem() {
		return labelInternalABCMenuItem;
	}

	public MenuItem getLabelLeaves123MenuItem() {
		return labelLeaves123MenuItem;
	}

	public MenuItem getLabelLeavesMenuItem() {
		return labelLeavesMenuItem;
	}

	public MenuItem getLabelLeavesABCMenuItem() {
		return labelLeavesABCMenuItem;
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	public MenuItem getNewMenuItem() {
		return newMenuItem;
	}

	public MenuItem getNormalizationMenuItem() {
		return normalizationMenuItem;
	}

	public MenuItem getOpenMenuItem() {
		return openMenuItem;
	}

	public MenuItem getPageSetupMenuItem() {
		return pageSetupMenuItem;
	}

	public MenuItem getPasteMenuItem() {
		return pasteMenuItem;
	}

	public MenuItem getPasteNewickMenuItem() {
		return pasteNewickMenuItem;
	}

	public MenuItem getPrintMenuItem() {
		return printMenuItem;
	}

	public MenuItem getQuitMenuItem() {
		return quitMenuItem;
	}

	public Menu getRecentMenu() {
		return recentMenu;
	}

	public MenuItem getRedoMenuItem() {
		return redoMenuItem;
	}

	public MenuItem getRemoveDiNodesMenuItem() {
		return removeDiNodesMenuItem;
	}

	public MenuItem getReplaceMenuItem() {
		return replaceMenuItem;
	}

	public MenuItem getRectangularEdgesMenuItem() {
		return rectangularEdgesMenuItem;
	}

	public MenuItem getCubicEdgesMenuItem() {
		return cubicEdgesMenuItem;
	}

	public MenuItem getSaveAsMenuItem() {
		return saveAsMenuItem;
	}

	public MenuItem getSelectAllAboveMenuItem() {
		return selectAllAboveMenuItem;
	}

	public MenuItem getSelectAllBelowMenuItem() {
		return selectAllBelowMenuItem;
	}

	public MenuItem getSelectAllMenuItem() {
		return selectAllMenuItem;
	}

	public MenuItem getSelectInvertMenuItem() {
		return selectInvertMenuItem;
	}

	public MenuItem getSelectLeavesMenuItem() {
		return selectLeavesMenuItem;
	}

	public MenuItem getSelectLowestStableAncestorMenuItem() {
		return selectLowestStableAncestorMenuItem;
	}

	public MenuItem getSelectNoneMenuItem() {
		return selectNoneMenuItem;
	}

	public MenuItem getSelectReticulateEdgesMenuItem() {
		return selectReticulateEdgesMenuItem;
	}

	public MenuItem getSelectReticulateNodesMenuItem() {
		return selectReticulateNodesMenuItem;
	}

	public MenuItem getSelectRootMenuItem() {
		return selectRootMenuItem;
	}

	public MenuItem getSelectStableNodesMenuItem() {
		return selectStableNodesMenuItem;
	}

	public MenuItem getSelectTreeEdgesMenuItem() {
		return selectTreeEdgesMenuItem;
	}

	public MenuItem getSelectTreeNodesMenuItem() {
		return selectTreeNodesMenuItem;
	}

	public MenuItem getSelectVisibleNodesMenuItem() {
		return selectVisibleNodesMenuItem;
	}

	public MenuItem getSelectVisibleReticulationsMenuItem() {
		return selectVisibleReticulationsMenuItem;
	}

	public MenuItem getStraightEdgesMenuItem() {
		return straightEdgesMenuItem;
	}

	public MenuItem getUndoMenuItem() {
		return undoMenuItem;
	}

	public CheckMenuItem getUseDarkThemeCheckMenuItem() {
		return useDarkThemeCheckMenuItem;
	}

	public Menu getWindowMenu() {
		return windowMenu;
	}

	public MenuItem getZoomInHorizontallyMenuItem() {
		return zoomInHorizontallyMenuItem;
	}

	public MenuItem getZoomInVerticallyMenuItem() {
		return zoomInVerticallyMenuItem;
	}

	public MenuItem getZoomOutHorizontallyMenuItem() {
		return zoomOutHorizontallyMenuItem;
	}

	public MenuItem getZoomOutVerticallyMenuItem() {
		return zoomOutVerticallyMenuItem;
	}
}
