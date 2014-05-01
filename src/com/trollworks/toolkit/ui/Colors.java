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
	 * @param color The color being tested.
	 * @param threshold The threshold to check for, in the range 0 to 1.
	 * @return <code>true</code> if the specified color's brightness is above the threshold.
	 */
	public static boolean aboveBrightnessThreshold(Color color, float threshold) {
		return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[2] > threshold;
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
	 * @param percentage The amount to adjust the saturation by, in the range -1 to 1.
	 * @return The adjusted color.
	 */
	public static final Color adjustSaturation(Color color, float percentage) {
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		return new Color(Color.HSBtoRGB(hsb[0], Math.max(Math.min(hsb[1] + percentage, 1f), 0f), hsb[2]));
	}

	/**
	 * @param color The color to base the new color on.
	 * @param percentage The amount to adjust the brightness by, in the range -1 to 1.
	 * @return The adjusted color.
	 */
	public static final Color adjustBrightness(Color color, float percentage) {
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		return new Color(Color.HSBtoRGB(hsb[0], hsb[1], Math.max(Math.min(hsb[2] + percentage, 1f), 0f)));
	}

	/**
	 * @param color The color to base the new color on.
	 * @param percentage The amount to adjust the hue by, in the range -1 to 1.
	 * @return The adjusted color.
	 */
	public static final Color adjustHue(Color color, float percentage) {
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		return new Color(Color.HSBtoRGB(Math.max(Math.min(hsb[0] + percentage, 1f), 0f), hsb[1], hsb[2]));
	}
}
