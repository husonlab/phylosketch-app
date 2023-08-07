/*
 * ImageFileUtils.java Copyright (C) 2023 Daniel H. Huson
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

package org.husonlab.phylosketch.utils;

import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Services;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import jloda.thirdparty.PngEncoderFX;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * image file utilities
 * Daniel Huson, 4.2023
 */
public class ImageFileUtils {
	/**
	 * saves an image to a public file and returns the file
	 * Source: https://stackoverflow.com/questions/47569927/sharing-multiple-files-with-gluon-shareservice-image-and-txt
	 *
	 * @param image the image
	 * @return the file
	 */
	public static File saveToPublicPNGFile(Image image) {
		if (image == null) {
			return null;
		}

		var bytes = new PngEncoderFX(image, true).pngEncode();
		var dir = Services.get(StorageService.class)
				.flatMap(storage -> storage.getPublicStorage("Pictures"))
				.orElse(null);
		if (dir != null) {
			var file = new File(dir, "Image-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuuMMdd-HHmmss")) + ".png");
			try (var outs = new FileOutputStream(file)) {
				outs.write(bytes);
				return file;
			} catch (IOException ex) {
				System.err.println("Error: " + ex);
			}
		}
		return null;
	}
}
