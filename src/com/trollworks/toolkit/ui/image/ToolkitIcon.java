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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

import javax.swing.Icon;

/** Provides a {@link BufferedImage} that implements Swing's {@link Icon} interface for convenience. */
public class ToolkitIcon extends BufferedImage implements Icon {
	public ToolkitIcon(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied, Hashtable<?, ?> properties) {
		super(cm, raster, isRasterPremultiplied, properties);
		clear();
	}

	@Override
	public Graphics2D getGraphics() {
		Graphics2D gc = (Graphics2D) super.getGraphics();
		gc.setClip(0, 0, getWidth(), getHeight());
		return gc;
	}

	public void clear() {
		fill(new Color(0, getTransparency() != OPAQUE));
	}

	public void fill(Color color) {
		Graphics2D gc = getGraphics();
		gc.setBackground(color);
		gc.clearRect(0, 0, getWidth(), getHeight());
		gc.dispose();
	}

	@Override
	public void paintIcon(Component component, Graphics gc, int x, int y) {
		gc.drawImage(this, x, y, component);
	}

	@Override
	public int getIconWidth() {
		return getWidth();
	}

	@Override
	public int getIconHeight() {
		return getHeight();
	}
}
