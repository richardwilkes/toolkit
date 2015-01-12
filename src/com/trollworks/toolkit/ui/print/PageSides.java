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

import javax.print.attribute.standard.Sides;

/** Constants representing the various page side possibilities. */
public enum PageSides {
	/** Maps to {@link Sides#ONE_SIDED}. */
	SINGLE {
		@Override
		public Sides getSides() {
			return Sides.ONE_SIDED;
		}

		@Override
		public String toString() {
			return SINGLE_TITLE;
		}
	},
	/** Maps to {@link Sides#DUPLEX}. */
	DUPLEX {
		@Override
		public Sides getSides() {
			return Sides.DUPLEX;
		}

		@Override
		public String toString() {
			return DUPLEX_TITLE;
		}
	},
	/** Maps to {@link Sides#TUMBLE}. */
	TUMBLE {
		@Override
		public Sides getSides() {
			return Sides.TUMBLE;
		}

		@Override
		public String toString() {
			return TUMBLE_TITLE;
		}
	};

	@Localize("Single")
	@Localize(locale = "ru", value = "Один")
	@Localize(locale = "de", value = "Einseitig")
	@Localize(locale = "es", value = "A una cara")
	static String	SINGLE_TITLE;
	@Localize("Duplex")
	@Localize(locale = "ru", value = "Двухсторонний")
	@Localize(locale = "de", value = "Lange Seite")
	@Localize(locale = "es", value = "A dos Caras")
	static String	DUPLEX_TITLE;
	@Localize("Tumble")
	@Localize(locale = "ru", value = "Ручная подача")
	@Localize(locale = "de", value = "Kurze Seite")
	@Localize(locale = "es", value = "Boca abajo")
	static String	TUMBLE_TITLE;

	static {
		Localization.initialize();
	}

	/** @return The sides attribute. */
	public abstract Sides getSides();

	/**
	 * @param sides The {@link Sides} to load from.
	 * @return The sides.
	 */
	public static final PageSides get(Sides sides) {
		for (PageSides one : values()) {
			if (one.getSides() == sides) {
				return one;
			}
		}
		return SINGLE;
	}
}
