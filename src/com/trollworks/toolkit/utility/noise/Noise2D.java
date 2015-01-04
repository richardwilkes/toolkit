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

package com.trollworks.toolkit.utility.noise;

/** All 2D noise generators will implement this interface. */
public interface Noise2D {
	/**
	 * @param x The horizontal coordinate to produce a noise value for.
	 * @param y The vertical coordinate to produce a noise value for.
	 * @return A value between -1 and 1, inclusive.
	 */
	double noise(double x, double y);
}
