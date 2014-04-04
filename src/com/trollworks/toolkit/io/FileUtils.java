/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.trollworks.toolkit.io;

import java.io.File;

/** File utilities. */
public class FileUtils {
	/**
	 * @param file The file or base directory to delete.
	 */
	public static final void deleteRecursively(File file) {
		if (file.isDirectory()) {
			for (File one : file.listFiles()) {
				deleteRecursively(one);
			}
		}
		if (file.exists()) {
			file.delete();
		}
	}
}
