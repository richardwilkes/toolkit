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

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

/** Colorizes an image by blending any solid pixels with the specified color. */
public class ColorFilter extends RGBImageFilter {
	private static final int	OPAQUE	= 0xFF000000;
	private int					mRed;
	private int					mGreen;
	private int					mBlue;
	private boolean				mIncludeAlpha;

	/**
	 * Constructs an image filter that colorizes an image by blending any solid pixels with the
	 * specified color.
	 *
	 * @param color The color to blend with.
	 */
	public ColorFilter(Color color) {
		this(color, false);
	}

	/**
	 * Constructs an image filter that colorizes an image by blending its pixels with the specified
	 * color.
	 *
	 * @param color The color to blend with.
	 * @param includeAlpha Whether or not to include pixels with an alpha channel in the
	 *            colorization.
	 */
	public ColorFilter(Color color, boolean includeAlpha) {
		mRed = color.getRed();
		mGreen = color.getGreen();
		mBlue = color.getBlue();
		mIncludeAlpha = includeAlpha;
		canFilterIndexColorModel = true;
	}

	/**
	 * Creates a colorized image.
	 *
	 * @param image The image to colorize.
	 * @param color The color to apply.
	 * @return The colorized image.
	 */
	public static StdImage createColorizedImage(Image image, Color color) {
		return createColorizedImage(image, color, false);
	}

	/**
	 * Creates a colorized image.
	 *
	 * @param image The image to colorize.
	 * @param color The color to apply.
	 * @param includeAlpha Whether or not to include pixels with an alpha channel in the
	 *            colorization.
	 * @return The colorized image.
	 */
	public static StdImage createColorizedImage(Image image, Color color, boolean includeAlpha) {
		ColorFilter filter = new ColorFilter(color, includeAlpha);
		ImageProducer producer = new FilteredImageSource(image.getSource(), filter);
		return StdImage.getToolkitImage(Toolkit.getDefaultToolkit().createImage(producer));
	}

	@Override
	public int filterRGB(int x, int y, int argb) {
		if (mIncludeAlpha || (argb & OPAQUE) == OPAQUE) {
			int darkenBy = (int) (((argb >> 16 & 255) * 0.3 + (argb >> 8 & 255) * 0.59 + (argb & 255) * 0.11) / 2.55);
			argb = argb & OPAQUE | mRed * darkenBy / 100 << 16 | mGreen * darkenBy / 100 << 8 | mBlue * darkenBy / 100;
		}
		return argb;
	}
}
