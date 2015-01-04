/*
 * Copyright (c) 1998-2015 by Richard A. Wilkes. All rights reserved.
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

@SuppressWarnings("nls")
public class Cursors {
	static {
		StdImage.addLocation(StdImage.class.getResource("cursors/"));
	}

	public static final Cursor	HORIZONTAL_RESIZE	= create("horizontal_resize_cursor");
	public static final Cursor	MOVE				= create("move_cursor");
	public static final Cursor	VERTICAL_RESIZE		= create("vertical_resize_cursor");

	/**
	 * Creates a new cursor by loading an image with the specified name. If the image cannot be
	 * loaded, then the default cursor is returned instead. The hotspot for the cursor will be
	 * placed at the center of the image.
	 *
	 * @param name The name of the image to load.
	 */
	public static final Cursor create(String name) {
		StdImage img = StdImage.get(name);
		if (img == null) {
			return Cursor.getDefaultCursor();
		}
		return Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(img.getWidth() / 2, img.getHeight() / 2), name);
	}

	/**
	 * Creates a new cursor by loading an image with the specified name. If the image cannot be
	 * loaded, then the default cursor is returned instead.
	 *
	 * @param name The name of the image to load.
	 * @param hotSpotX The horizontal hot spot for the cursor.
	 * @param hotSpotY The vertical hot spot for the cursor.
	 */
	public static final Cursor create(String name, int hotSpotX, int hotSpotY) {
		StdImage img = StdImage.get(name);
		if (img == null) {
			return Cursor.getDefaultCursor();
		}
		return Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(hotSpotX, hotSpotY), name);
	}
}
