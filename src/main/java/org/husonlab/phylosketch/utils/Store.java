/*
 * Store.java Copyright (C) 2023 University of Tuebingen
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

import java.io.File;

import static jloda.util.ProgramProperties.isMacOS;

/**
 * use this to store app properties
 */
public class Store {
	public static File access(String name) {
		File file = null;
		if (com.gluonhq.attach.util.Platform.isDesktop()) {
			if (isMacOS())
				file = new File(System.getProperty("user.home") + "/Library/Preferences/" + name);
			else
				file = new File(System.getProperty("user.home") + File.separator + "." + name);

		} else {
			var optionalDir = Services.get(StorageService.class).flatMap(StorageService::getPrivateStorage);
			if (optionalDir.isPresent()) {
				file = new File(optionalDir.get(), name);
				if (false)
					System.err.println("File: " + file);
			}
		}
		return file;
	}
}
