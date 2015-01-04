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

/** Provides a Perlin noise generator. */
public class PerlinNoise2D implements Noise2D {
	private int	mTable[]	= new int[512];

	/** @param seed The seed to be passed to the random number generator when creating the state. */
	public PerlinNoise2D(long seed) {
		Random random = new Random(seed);
		for (int i = 0; i < 256; i++) {
			mTable[256 + i] = mTable[i] = random.nextInt(256);
		}
	}

	@Override
	public final double noise(double x, double y) {
		int x1 = (int) Math.floor(x) & 255;
		int y1 = (int) Math.floor(y) & 255;
		x -= Math.floor(x);
		y -= Math.floor(y);
		int a = mTable[x1] + y1;
		int aa = mTable[a];
		int ab = mTable[a + 1];
		int b = mTable[x1 + 1] + y1;
		int ba = mTable[b];
		int bb = mTable[b + 1];
		double u = fade(x);
		double laa = lerp(u, grad(mTable[aa], x, y, 0), grad(mTable[ba], x - 1, y, 0));
		double lab = lerp(u, grad(mTable[ab], x, y - 1, 0), grad(mTable[bb], x - 1, y - 1, 0));
		double lba = lerp(u, grad(mTable[aa + 1], x, y, -1), grad(mTable[ba + 1], x - 1, y, -1));
		double lbb = lerp(u, grad(mTable[ab + 1], x, y - 1, -1), grad(mTable[bb + 1], x - 1, y - 1, -1));
		double v = fade(y);
		return lerp(0, lerp(v, laa, lab), lerp(v, lba, lbb));
	}

	private static final double fade(double t) {
		return t * t * t * (t * (t * 6 - 15) + 10);
	}

	private static final double lerp(double t, double a, double b) {
		return a + t * (b - a);
	}

	private static final double grad(int hash, double x, double y, double z) {
		hash &= 15;
		double u = hash < 8 ? x : y;
		if ((hash & 1) != 0) {
			u = -u;
		}
		double v;
		if (hash < 4) {
			v = y;
		} else if (hash == 12 || hash == 14) {
			v = x;
		} else {
			v = z;
		}
		if ((hash & 2) != 0) {
			v = -v;
		}
		return u + v;
	}
}
