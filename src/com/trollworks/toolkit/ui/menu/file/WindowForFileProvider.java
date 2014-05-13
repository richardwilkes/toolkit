/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui.menu.file;

import com.trollworks.toolkit.ui.widget.AppWindow;

import java.io.File;
import java.io.IOException;

/** Provides a window for files of a specific {@link FileType}. */
public interface WindowForFileProvider {
	/**
	 * @param file The {@link File} to load data from.
	 * @return The resulting {@link AppWindow}.
	 */
	AppWindow create(File file) throws IOException;
}
