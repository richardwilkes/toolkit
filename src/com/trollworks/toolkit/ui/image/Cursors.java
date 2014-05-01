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

package com.trollworks.toolkit.ui.image;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

@SuppressWarnings("nls")
public class Cursors {
	static {
		Images.addLocation(ToolkitImage.class.getResource("images/"));
	}

	public static final Cursor	HORIZONTAL_RESIZE	= create("HorizontalResizeCursor");
	public static final Cursor	MOVE				= create("MoveCursor");
	public static final Cursor	VERTICAL_RESIZE		= create("VerticalResizeCursor");

	private static Cursor create(String name) {
		BufferedImage img = Images.get(name);
		if (img == null) {
			return Cursor.getDefaultCursor();
		}
		return Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(img.getWidth() / 2, img.getHeight() / 2), name);
	}
}
