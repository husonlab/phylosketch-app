/*
 * DefaultOptions.java Copyright (C) 2022 Daniel H. Huson
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

package org.husonlab.phylosketch.network;

import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Services;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import jloda.fx.control.RichTextLabel;
import jloda.fx.util.BasicFX;
import jloda.fx.util.ProgramProperties;
import org.husonlab.phylosketch.Main;

import java.io.File;

/**
 * maintains default options for program
 * Daniel Huson, 12.22
 */
public class DefaultOptions {
	private final ObservableList<String> trees = FXCollections.observableArrayList();
	private final IntegerProperty textAreaFontSize = new SimpleIntegerProperty(this, "textAreaFontSize");
	private final StringProperty labelFontFamily = new SimpleStringProperty(this, "labelFontFamily");
	private final DoubleProperty labelFontSize = new SimpleDoubleProperty(this, "labelFontSize");
	private final ObjectProperty<Color> nodeColor = new SimpleObjectProperty<>(this, "nodeColor");
	private final DoubleProperty nodeSize = new SimpleDoubleProperty(this, "nodeSize");
	private final ObjectProperty<Color> edgeColor = new SimpleObjectProperty<>(this, "edgeColor");
	private final ObjectProperty<Color> reticulateColor = new SimpleObjectProperty<>(this, "reticulateColor");
	private final DoubleProperty edgeWidth = new SimpleDoubleProperty(this, "edgeWidth");

	private final LongProperty update = new SimpleLongProperty(this, "update", 0L);

	static private DefaultOptions instance;

	private static DefaultOptions getInstance() {
		if (instance == null) {
			instance = new DefaultOptions();
			instance.trees.setAll(ProgramProperties.get("trees", new String[0]));
			instance.trees.addListener((InvalidationListener) e -> {
				ProgramProperties.put("trees", instance.trees.toArray(new String[0]));
			});
			ProgramProperties.track(instance.labelFontFamily, "Arial");
			ProgramProperties.track(instance.labelFontSize, 14.0);
			RichTextLabel.setDefaultFont(Font.font(instance.labelFontFamily.get(), instance.labelFontSize.get()));

			ProgramProperties.track(instance.textAreaFontSize, 20);
			ProgramProperties.track(instance.nodeColor, Color.BLACK);
			ProgramProperties.track(instance.nodeSize, 2.0);
			ProgramProperties.track(instance.edgeColor, Color.BLACK);
			ProgramProperties.track(instance.reticulateColor, Color.DARKORANGE);
			ProgramProperties.track(instance.edgeWidth, 1.0);

			// save properties after each change:
			instance.update.bind(Bindings.createLongBinding(System::currentTimeMillis, getTrees(), labelFontFamilyProperty(), labelFontSizeProperty(),
					nodeColorProperty(), nodeSizeProperty(), edgeColorProperty(), edgeWidthProperty(), reticulateColorProperty()));
			instance.update.addListener(e -> store());
		}
		return instance;
	}

	public static void resetAll() {
		if (instance.trees.size() > 0)
			instance.trees.setAll(instance.trees.get(0));
		instance.labelFontFamily.set("Arial");
		instance.labelFontSize.set(14.0);
		instance.textAreaFontSize.set(20);
		instance.nodeColor.set(Color.BLACK);
		instance.nodeSize.set(2.0);
		instance.edgeColor.set(Color.BLACK);
		instance.reticulateColor.set(Color.DARKORANGE);
		instance.edgeWidth.set(1.0);
	}

	public static ObservableList<String> getTrees() {
		return getInstance().trees;
	}

	public static String getLabelFontFamily() {
		return getInstance().labelFontFamily.get();
	}

	public static StringProperty labelFontFamilyProperty() {
		return getInstance().labelFontFamily;
	}

	public static void setLabelFontFamily(String labelFontFamily) {
		getInstance().labelFontFamily.set(labelFontFamily);
	}

	public static double getLabelFontSize() {
		return getInstance().labelFontSize.get();
	}

	public static DoubleProperty labelFontSizeProperty() {
		return getInstance().labelFontSize;
	}

	public static void setLabelFontSize(double labelFontSize) {
		getInstance().labelFontSize.set(labelFontSize);
	}

	public static int getTextAreaFontSize() {
		return getInstance().textAreaFontSize.get();
	}

	public static IntegerProperty textAreaFontSizeProperty() {
		return getInstance().textAreaFontSize;
	}

	public static void setTextAreaFontSize(int textAreaFontSize) {
		getInstance().textAreaFontSize.set(textAreaFontSize);
	}

	public static Color getNodeColor() {
		return getInstance().nodeColor.get();
	}

	public static ObjectProperty<Color> nodeColorProperty() {
		return getInstance().nodeColor;
	}

	public static void setNodeColor(Color nodeColor) {
		getInstance().nodeColor.set(nodeColor);
	}

	public static double getNodeSize() {
		return getInstance().nodeSize.get();
	}

	public static DoubleProperty nodeSizeProperty() {
		return getInstance().nodeSize;
	}

	public static void setNodeSize(double nodeSize) {
		getInstance().nodeSize.set(nodeSize);
	}

	public static Color getEdgeColor() {
		return getInstance().edgeColor.get();
	}

	public static ObjectProperty<Color> edgeColorProperty() {
		return getInstance().edgeColor;
	}

	public static void setEdgeColor(Color edgeColor) {
		getInstance().edgeColor.set(edgeColor);
	}

	public static Color getReticulateColor() {
		return getInstance().reticulateColor.get();
	}

	public static ObjectProperty<Color> reticulateColorProperty() {
		return getInstance().reticulateColor;
	}

	public static void setReticulateColor(Color reticulateColor) {
		getInstance().reticulateColor.set(reticulateColor);
	}

	public static double getEdgeWidth() {
		return getInstance().edgeWidth.get();
	}

	public static DoubleProperty edgeWidthProperty() {
		return getInstance().edgeWidth;
	}

	public static void setEdgeWidth(double edgeWidth) {
		getInstance().edgeWidth.set(edgeWidth);
	}

	private static File propertiesFile;

	public static void load() {
		if (propertiesFile == null) {
			if (Main.isDesktop()) {
				if (ProgramProperties.isMacOS())
					propertiesFile = new File(System.getProperty("user.home") + "/Library/Preferences/PhyloSketch-App.def");
				else
					propertiesFile = new File(System.getProperty("user.home") + File.separator + ".PhyloSketch-App.def");

			} else {
				Services.get(StorageService.class).flatMap(StorageService::getPrivateStorage).ifPresent(dir -> propertiesFile = new File(dir, "PhyloSketch-App.def"));
			}
		}
		if (propertiesFile != null) {
			System.err.println("propertiesFile: " + propertiesFile);
			ProgramProperties.load(propertiesFile.getPath());
		}
	}

	public static void store() {
		if (propertiesFile != null)
			ProgramProperties.store(propertiesFile.getPath());
	}

	public static void bindBidirectional(Property<Integer> p, IntegerProperty q) {
		p.addListener((v, o, n) -> q.set(n));
		q.addListener((v, o, n) -> p.setValue(n.intValue()));
	}

	public static void bindBidirectional(Property<Double> p, DoubleProperty q) {
		p.addListener((v, o, n) -> q.set(n));
		q.addListener((v, o, n) -> p.setValue(n.doubleValue()));
	}

	public static void bindBidirectional(Property<String> p, StringProperty q) {
		p.addListener((v, o, n) -> q.set(n));
		q.addListener((v, o, n) -> p.setValue(n));
	}
}
