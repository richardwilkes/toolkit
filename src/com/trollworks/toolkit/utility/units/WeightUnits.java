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

package com.trollworks.toolkit.utility.units;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.text.Numbers;

import java.text.MessageFormat;

/** Common weight units. */
public enum WeightUnits implements Units {
	/** Ounces. */
	OZ(1.0 / 16.0, false) {
		@Override
		public String getLocalizedName() {
			return OUNCES_DESCRIPTION;
		}
	},
	/** Pounds. */
	LB(1.0, false) {
		@Override
		public String getLocalizedName() {
			return POUNDS_DESCRIPTION;
		}
	},
	/** Short Tons */
	TN(2000.0, false) {
		@Override
		public String getLocalizedName() {
			return SHORT_TONS_DESCRIPTION;
		}
	},
	/** Long Tons */
	LT(2240.0, false) {
		@Override
		public String getLocalizedName() {
			return LONG_TONS_DESCRIPTION;
		}
	},
	/** Metric Tons. Must come after Long Tons and Short Tons since it's abbreviation is a subset. */
	T(2205.0, true) {
		@Override
		public String getLocalizedName() {
			return METRIC_TONS_DESCRIPTION;
		}
	},
	/** Kilograms. */
	KG(2.205, true) {
		@Override
		public String getLocalizedName() {
			return KILOGRAMS_DESCRIPTION;
		}
	},
	/** Grams. Must come after Kilograms since it's abbreviation is a subset. */
	G(0.002205, true) {
		@Override
		public String getLocalizedName() {
			return GRAMS_DESCRIPTION;
		}
	};

	@Localize("Ounces")
	@Localize(locale = "ru", value = "Унция")
	@Localize(locale = "de", value = "Unzen")
	@Localize(locale = "es", value = "Onzas")
	static String	OUNCES_DESCRIPTION;
	@Localize("Pounds")
	@Localize(locale = "ru", value = "Фунт")
	@Localize(locale = "de", value = "Pfund")
	@Localize(locale = "es", value = "Libras")
	static String	POUNDS_DESCRIPTION;
	@Localize("Grams")
	@Localize(locale = "ru", value = "Граммы")
	@Localize(locale = "de", value = "Gramm")
	@Localize(locale = "es", value = "Gramos")
	static String	GRAMS_DESCRIPTION;
	@Localize("Kilograms")
	@Localize(locale = "ru", value = "Килограммы")
	@Localize(locale = "de", value = "Kilogramm")
	@Localize(locale = "es", value = "Kilogramos")
	static String	KILOGRAMS_DESCRIPTION;
	@Localize("Short Tons")
	@Localize(locale = "ru", value = "Американские тонны")
	@Localize(locale = "de", value = "Amerikanische Tonnen")
	@Localize(locale = "es", value = "Tonelada Corta")
	static String	SHORT_TONS_DESCRIPTION;
	@Localize("Long Tons")
	@Localize(locale = "ru", value = "Английские тонны")
	@Localize(locale = "de", value = "Britische Tonnen")
	@Localize(locale = "es", value = "Tonelada Larga")
	static String	LONG_TONS_DESCRIPTION;
	@Localize("Metric Tons")
	@Localize(locale = "ru", value = "Метрические тонны")
	@Localize(locale = "de", value = "Tonnen")
	@Localize(locale = "es", value = "Tonelada")
	static String	METRIC_TONS_DESCRIPTION;
	@Localize("{0} {1}")
	static String	FORMAT;
	@Localize("%s (%s)")
	static String	DESCRIPTION_FORMAT;

	private double	mFactor;
	private boolean	mIsMetric;

	static {
		Localization.initialize();
	}

	private WeightUnits(double factor, boolean isMetric) {
		mFactor = factor;
		mIsMetric = isMetric;
	}

	@Override
	public double convert(Units units, double value) {
		return value * units.getFactor() / mFactor;
	}

	@Override
	public double normalize(double value) {
		return value / mFactor;
	}

	@Override
	public double getFactor() {
		return mFactor;
	}

	@Override
	public String format(double value, boolean localize) {
		String textValue = localize ? Numbers.format(value) : Double.toString(value);
		return MessageFormat.format(FORMAT, Numbers.trimTrailingZerosAfterDecimal(textValue, localize), getAbbreviation());
	}

	@Override
	public WeightUnits[] getCompatibleUnits() {
		return values();
	}

	@Override
	public String getAbbreviation() {
		return name().toLowerCase();
	}

	@Override
	public String getDescription() {
		return String.format(DESCRIPTION_FORMAT, getLocalizedName(), getAbbreviation());
	}

	public boolean isMetric() {
		return mIsMetric;
	}
}
