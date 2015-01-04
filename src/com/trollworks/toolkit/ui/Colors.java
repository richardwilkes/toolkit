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

package com.trollworks.toolkit.ui;

import java.awt.Color;

/** Provides standardized color access. */
public class Colors {
	/**
	 * @param primary Whether to return the primary or secondary banding color.
	 * @return The color.
	 */
	public static Color getBanding(boolean primary) {
		return primary ? Color.WHITE : new Color(232, 255, 232);
	}

	/**
	 * @param color The color to check.
	 * @return <code>true</code> if each color channel is the same.
	 */
	public static boolean isMonochrome(Color color) {
		int green = color.getGreen();
		return color.getRed() == green && green == color.getBlue();
	}

	/**
	 * @param color The color to check.
	 * @return <code>true</code> if the color's perceived brightness is less than 50%.
	 */
	public static boolean isDim(Color color) {
		return perceivedBrightness(color) < 0.5;
	}

	/**
	 * @param color The color to check.
	 * @return <code>true</code> if the color's perceived brightness is greater than or equal to
	 *         50%.
	 */
	public static boolean isBright(Color color) {
		return perceivedBrightness(color) >= 0.5;
	}

	/**
	 * @param color The color to check.
	 * @return The perceived brightness. Less than 0.5 is a dark color.
	 */
	public static double perceivedBrightness(Color color) {
		double red = color.getRed() / 255.0;
		if (!isMonochrome(color)) {
			double green = color.getGreen() / 255.0;
			double blue = color.getBlue() / 255.0;
			return Math.sqrt(red * red * 0.241 + green * green * 0.691 + blue * blue * 0.068);
		}
		return red;
	}

	/**
	 * @param color1 The first color.
	 * @param color2 The second color.
	 * @param percentage How much of the second color to use.
	 * @return A color that is a blended version of the two passed in.
	 */
	public static final Color blend(Color color1, Color color2, int percentage) {
		int remaining = 100 - percentage;
		return new Color((color1.getRed() * remaining + color2.getRed() * percentage) / 100, (color1.getGreen() * remaining + color2.getGreen() * percentage) / 100, (color1.getBlue() * remaining + color2.getBlue() * percentage) / 100);
	}

	/**
	 * @param color The color to base the new color on.
	 * @param amount The amount to adjust the saturation by, in the range -1 to 1.
	 * @return The adjusted color.
	 */
	public static final Color adjustSaturation(Color color, float amount) {
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		return new Color(Color.HSBtoRGB(hsb[0], Math.max(Math.min(hsb[1] + amount, 1f), 0f), hsb[2]));
	}

	/**
	 * @param color The color to base the new color on.
	 * @param amount The amount to adjust the brightness by, in the range -1 to 1.
	 * @return The adjusted color.
	 */
	public static final Color adjustBrightness(Color color, float amount) {
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		return new Color(Color.HSBtoRGB(hsb[0], hsb[1], Math.max(Math.min(hsb[2] + amount, 1f), 0f)));
	}

	/**
	 * @param color The color to base the new color on.
	 * @param amount The amount to adjust the hue by, in the range -1 to 1.
	 * @return The adjusted color.
	 */
	public static final Color adjustHue(Color color, float amount) {
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		return new Color(Color.HSBtoRGB(Math.max(Math.min(hsb[0] + amount, 1f), 0f), hsb[1], hsb[2]));
	}

	/**
	 * @param color The color to work with.
	 * @param alpha The alpha to use.
	 * @return A new {@link Color} with the specified alpha.
	 */
	public static final Color getWithAlpha(Color color, int alpha) {
		return new Color(color.getRGB() & 0x00FFFFFF | alpha << 24, true);
	}
}
