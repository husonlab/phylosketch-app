/*
 * DefaultOptions.java Copyright (C) 2023 Daniel H. Huson
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

package org.husonlab.phylosketch;

import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Services;
import com.gluonhq.charm.glisten.visual.Swatch;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import jloda.fx.control.RichTextLabel;
import jloda.fx.util.ProgramProperties;
import jloda.fx.util.RunAfterAWhile;
import org.husonlab.phylosketch.network.Document;
import org.husonlab.phylosketch.network.NetworkModel;

import java.io.File;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * maintains default options for program
 * Daniel Huson, 12.22
 */
public class DefaultOptions {
	private final ObservableList<NamedNewick> trees = FXCollections.observableArrayList();
	private final IntegerProperty textAreaFontSize = new SimpleIntegerProperty(this, "textAreaFontSize");
	private final ObjectProperty<Swatch> swatch = new SimpleObjectProperty<>(this, "swatch");

	private final StringProperty labelFontFamily = new SimpleStringProperty(this, "labelFontFamily");
	private final DoubleProperty labelFontSize = new SimpleDoubleProperty(this, "labelFontSize");

	private final ObjectProperty<Color> nodeFill = new SimpleObjectProperty<>(this, "nodeFill");
	private final ObjectProperty<Color> nodeStroke = new SimpleObjectProperty<>(this, "nodeStroke");
	private final DoubleProperty nodeSize = new SimpleDoubleProperty(this, "nodeSize");

	private final ObjectProperty<NetworkModel.EdgeGlyph> edgeGlyph = new SimpleObjectProperty<>(this, "edgeGlyph");
	private final ObjectProperty<Color> edgeColor = new SimpleObjectProperty<>(this, "edgeColor");
	private final ObjectProperty<Color> reticulateColor = new SimpleObjectProperty<>(this, "reticulateColor");
	private final DoubleProperty edgeWidth = new SimpleDoubleProperty(this, "edgeWidth");

	private final LongProperty update = new SimpleLongProperty(this, "update", 0L);

	static private DefaultOptions instance;

	private static DefaultOptions getInstance() {
		if (instance == null) {
			instance = new DefaultOptions();
			var list = Stream.of(ProgramProperties.get("trees", new String[0])).filter(t -> Document.canParse(t, true)).
					map(NamedNewick::new).filter(t -> !instance.trees.contains(t)).collect(Collectors.toList());
			instance.trees.setAll(list);
			instance.trees.addListener((InvalidationListener) e -> {
				ProgramProperties.put("trees", instance.trees.stream().map(NamedNewick::getNewick).toArray(String[]::new));
			});
			ProgramProperties.track(instance.textAreaFontSize, 20);
			ProgramProperties.track(instance.swatch, Swatch::valueOf, Main.DEFAULT_SWATCH);

			ProgramProperties.track(instance.labelFontFamily, "Arial");
			ProgramProperties.track(instance.labelFontSize, 14.0);
			RichTextLabel.setDefaultFont(Font.font(instance.labelFontFamily.get(), instance.labelFontSize.get()));

			ProgramProperties.track(instance.nodeFill, Color.BLACK);
			ProgramProperties.track(instance.nodeStroke, Color.BLACK);
			ProgramProperties.track(instance.nodeSize, 2.0);
			ProgramProperties.track(instance.edgeGlyph, NetworkModel.EdgeGlyph::valueOfNoFail, NetworkModel.EdgeGlyph.RectangleLine);
			ProgramProperties.track(instance.edgeColor, Color.BLACK);
			ProgramProperties.track(instance.reticulateColor, Color.DARKORANGE);
			ProgramProperties.track(instance.edgeWidth, 1.0);

			// save properties after each change:
			instance.update.bind(Bindings.createLongBinding(System::currentTimeMillis, getTrees(), labelFontFamilyProperty(), labelFontSizeProperty(),
					nodeFillProperty(), nodeStrokeProperty(), nodeSizeProperty(), edgeGlyphProperty(), edgeColorProperty(), edgeWidthProperty(), reticulateColorProperty(),
					swatchProperty()));
			instance.update.addListener((v, o, n) -> RunAfterAWhile.apply(instance, DefaultOptions::store));
		}
		return instance;
	}

	public static void resetAll() {
		if (instance.trees.size() > 0)
			instance.trees.setAll(instance.trees.get(0));
		instance.addTree("((a,b),(c.d));");
		setTextAreaFontSize(20);
		setSwatch(Main.DEFAULT_SWATCH);

		setLabelFontFamily("Arial");
		setLabelFontSize(14.0);
		setNodeFill(Color.BLACK);
		setNodeStroke(Color.BLACK);
		setNodeSize(2.0);
		setEdgeGlyph(NetworkModel.EdgeGlyph.RectangleLine);
		setEdgeColor(Color.BLACK);
		setReticulateColor(Color.DARKORANGE);
		setEdgeWidth(1.0);
	}

	public void addTree(String newick) {
		var namedNewick = new NamedNewick(newick);
		if (!getTrees().contains(namedNewick))
			getTrees().add(namedNewick);
	}

	public static ObservableList<NamedNewick> getTrees() {
		return getInstance().trees;
	}

	public static Swatch getSwatch() {
		return getInstance().swatch.get();
	}

	public static ObjectProperty<Swatch> swatchProperty() {
		return getInstance().swatch;
	}

	public static void setSwatch(Swatch swatch) {
		getInstance().swatch.set(swatch);
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

	public static Color getNodeFill() {
		return getInstance().nodeFill.get();
	}

	public static ObjectProperty<Color> nodeFillProperty() {
		return getInstance().nodeFill;
	}

	public static void setNodeFill(Color nodeColor) {
		getInstance().nodeFill.set(nodeColor);
	}

	public static Color getNodeStroke() {
		return getInstance().nodeStroke.get();
	}

	public static ObjectProperty<Color> nodeStrokeProperty() {
		return getInstance().nodeStroke;
	}

	public static void setNodeStroke(Color nodeStroke) {
		getInstance().nodeStroke.set(nodeStroke);
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

	public static NetworkModel.EdgeGlyph getEdgeGlyph() {
		return getInstance().edgeGlyph.get();
	}

	public static ObjectProperty<NetworkModel.EdgeGlyph> edgeGlyphProperty() {
		return getInstance().edgeGlyph;
	}

	public static void setEdgeGlyph(NetworkModel.EdgeGlyph edgeGlyph) {
		getInstance().edgeGlyph.set(edgeGlyph);
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
				var optionalDir = Services.get(StorageService.class).flatMap(StorageService::getPrivateStorage);
				if (optionalDir.isPresent()) {
					System.err.println(optionalDir.get());
					propertiesFile = new File(optionalDir.get(), "PhyloSketch-App.def");
				}
				Services.get(StorageService.class).flatMap(StorageService::getPrivateStorage).ifPresent(dir -> propertiesFile = new File(dir, "PhyloSketch-App.def"));
			}
		}
		if (propertiesFile != null) {
			ProgramProperties.load(propertiesFile.getPath());
			System.err.println("Properties file: " + propertiesFile.getPath() + ", already exists: " + propertiesFile.exists());
		} else {
			System.err.println("No properties file");
		}
	}

	public static void store() {
		if (propertiesFile != null) {
			ProgramProperties.store();
		}
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

	public static class NamedNewick {
		private final String name;
		private final String newick;

		public NamedNewick(String newick) {
			this.name = newick.length() < 60 ? newick : newick.substring(0, 57) + "...";
			this.newick = newick;
		}

		public String getName() {
			return name;
		}

		public String getNewick() {
			return newick;
		}

		public String toString() {
			return getName();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof NamedNewick)) return false;
			NamedNewick that = (NamedNewick) o;
			return name.equals(that.name) && newick.equals(that.newick);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, newick);
		}
	}
}
