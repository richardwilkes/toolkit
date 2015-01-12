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

package com.trollworks.toolkit.ui.print;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.utility.Localization;

import javax.print.attribute.standard.Chromaticity;

/** Constants representing the various ink chromaticity possibilities. */
public enum InkChromaticity {
	/** Maps to {@link Chromaticity#COLOR}. */
	COLOR {
		@Override
		public Chromaticity getChromaticity() {
			return Chromaticity.COLOR;
		}

		@Override
		public String toString() {
			return COLOR_TITLE;
		}
	},
	/** Maps to {@link Chromaticity#MONOCHROME}. */
	MONOCHROME {
		@Override
		public Chromaticity getChromaticity() {
			return Chromaticity.MONOCHROME;
		}

		@Override
		public String toString() {
			return MONOCHROME_TITLE;
		}
	};

	@Localize("Color")
	@Localize(locale = "ru", value = "Цвет")
	@Localize(locale = "de", value = "Farbe")
	@Localize(locale = "es", value = "Color")
	static String	COLOR_TITLE;
	@Localize("Monochrome")
	@Localize(locale = "ru", value = "Черно-белый")
	@Localize(locale = "de", value = "Graustufen")
	@Localize(locale = "es", value = "Monocromo")
	static String	MONOCHROME_TITLE;

	static {
		Localization.initialize();
	}

	/** @return The chromaticity attribute. */
	public abstract Chromaticity getChromaticity();

	/**
	 * @param chromaticity The {@link Chromaticity} to load from.
	 * @return The chromaticity.
	 */
	public static final InkChromaticity get(Chromaticity chromaticity) {
		for (InkChromaticity one : values()) {
			if (one.getChromaticity() == chromaticity) {
				return one;
			}
		}
		return MONOCHROME;
	}
}
