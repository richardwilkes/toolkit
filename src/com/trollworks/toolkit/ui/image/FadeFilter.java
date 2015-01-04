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

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

/** Fades an image by blending the pixels with white or black. */
public class FadeFilter extends RGBImageFilter {
	private static final int	OPAQUE	= 0xFF000000;
	private int					mPercentage;
	private boolean				mUseWhite;

	/**
	 * Constructs an image filter that fades an image by blending the pixels with the specified
	 * percentage of white or black.
	 *
	 * @param percentage The percentage of white or black to use.
	 * @param useWhite Whether to use white or black.
	 */
	public FadeFilter(int percentage, boolean useWhite) {
		mPercentage = percentage;
		mUseWhite = useWhite;
		canFilterIndexColorModel = true;
	}

	/**
	 * Creates a faded image.
	 *
	 * @param image The image to fade.
	 * @param percentage The percentage of white or black to use.
	 * @param useWhite Whether to use white or black.
	 * @return The faded image.
	 */
	public static StdImage createFadedImage(Image image, int percentage, boolean useWhite) {
		FadeFilter filter = new FadeFilter(percentage, useWhite);
		ImageProducer producer = new FilteredImageSource(image.getSource(), filter);
		return StdImage.getToolkitImage(Toolkit.getDefaultToolkit().createImage(producer));
	}

	@Override
	public int filterRGB(int x, int y, int argb) {
		int red = argb >> 16 & 0xFF;
		int green = argb >> 8 & 0xFF;
		int blue = argb & 0xFF;
		int p1 = 100 - mPercentage;
		int p2 = mUseWhite ? 255 * mPercentage : 0;

		red = (red * p1 + p2) / 100;
		green = (green * p1 + p2) / 100;
		blue = (blue * p1 + p2) / 100;
		return argb & OPAQUE | red << 16 | green << 8 | blue;
	}
}
