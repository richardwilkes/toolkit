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

import java.util.Random;

/** Provides a Simplex noise generator. */
public class SimplexNoise2D implements Noise2D {
	private static final double	F2			= 0.5 * (Math.sqrt(3.0) - 1.0);
	private static final double	G2			= (3.0 - Math.sqrt(3.0)) / 6.0;
	private static final int[]	X			= { 1, -1, 1, -1, 1, -1, 1, -1, 0, 0, 0, 0 };
	private static final int[]	Y			= { 1, 1, -1, -1, 0, 0, 0, 0, 1, -1, 1, -1 };
	private final int[]			mTable		= new int[512];
	private final int[]			mTableMod12	= new int[512];

	/** @param seed The seed to be passed to the random number generator when creating the state. */
	public SimplexNoise2D(long seed) {
		Random random = new Random(seed);
		for (int i = 0; i < 256; i++) {
			mTable[i + 256] = mTable[i] = random.nextInt(256);
			mTableMod12[i + 256] = mTableMod12[i] = mTable[i] % 12;
		}
	}

	@Override
	public double noise(double xin, double yin) {
		double skew = (xin + yin) * F2;
		int i = (int) Math.floor(xin + skew);
		int j = (int) Math.floor(yin + skew);
		double t = (i + j) * G2;
		int ii = i & 255;
		int jj = j & 255;

		double x0 = xin - (i - t);
		double y0 = yin - (j - t);
		double t0 = 0.5 - x0 * x0 - y0 * y0;
		int gi0 = mTableMod12[ii + mTable[jj]];
		double n0 = t0 < 0 ? 0.0 : t0 * t0 * t0 * t0 * (X[gi0] * x0 + Y[gi0] * y0);

		int i1;
		int j1;
		if (x0 > y0) {
			i1 = 1;
			j1 = 0;
		} else {
			i1 = 0;
			j1 = 1;
		}
		double x1 = x0 - i1 + G2;
		double y1 = y0 - j1 + G2;
		double t1 = 0.5 - x1 * x1 - y1 * y1;
		int gi1 = mTableMod12[ii + i1 + mTable[jj + j1]];
		double n1 = t1 < 0 ? 0.0 : t1 * t1 * t1 * t1 * (X[gi1] * x1 + Y[gi1] * y1);

		double x2 = x0 - 1.0 + 2.0 * G2;
		double y2 = y0 - 1.0 + 2.0 * G2;
		double t2 = 0.5 - x2 * x2 - y2 * y2;
		int gi2 = mTableMod12[ii + 1 + mTable[jj + 1]];
		double n2 = t2 < 0 ? 0.0 : t2 * t2 * t2 * t2 * (X[gi2] * x2 + Y[gi2] * y2);

		return 70.0 * (n0 + n1 + n2);
	}
}
