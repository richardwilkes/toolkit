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

package com.trollworks.toolkit.utility.units;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.text.Numbers;

import java.text.MessageFormat;

/** Common weight units. */
public enum WeightUnits implements Units {
	/** Ounces. */
	OUNCES(1.0 / 16.0) {
		@Override
		public String getDescription() {
			return OUNCES_DESCRIPTION;
		}

		@Override
		public String toString() {
			return OUNCES_TITLE;
		}
	},
	/** Pounds. */
	POUNDS(1.0) {
		@Override
		public String getDescription() {
			return POUNDS_DESCRIPTION;
		}

		@Override
		public String toString() {
			return POUNDS_TITLE;
		}
	},
	/** Short Tons */
	SHORT_TONS(2000.0) {
		@Override
		public String getDescription() {
			return SHORT_TONS_DESCRIPTION;
		}

		@Override
		public String toString() {
			return SHORT_TONS_TITLE;
		}
	},
	/** Long Tons */
	LONG_TONS(2240.0) {
		@Override
		public String getDescription() {
			return LONG_TONS_DESCRIPTION;
		}

		@Override
		public String toString() {
			return LONG_TONS_TITLE;
		}
	},
	/** Metric Tons */
	METRIC_TONS(2205.0) {
		@Override
		public String getDescription() {
			return METRIC_TONS_DESCRIPTION;
		}

		@Override
		public String toString() {
			return METRIC_TONS_TITLE;
		}
	},
	/** Kilograms. */
	KILOGRAMS(2.205) {
		@Override
		public String getDescription() {
			return KILOGRAMS_DESCRIPTION;
		}

		@Override
		public String toString() {
			return KILOGRAMS_TITLE;
		}
	},
	/** Grams. Must come after Kilograms since it's abbreviation is a subset. */
	GRAMS(0.002205) {
		@Override
		public String getDescription() {
			return GRAMS_DESCRIPTION;
		}

		@Override
		public String toString() {
			return GRAMS_TITLE;
		}
	};

	@Localize("Ounces (oz)")
	static String	OUNCES_DESCRIPTION;
	@Localize("oz")
	static String	OUNCES_TITLE;
	@Localize("Pounds (lb)")
	static String	POUNDS_DESCRIPTION;
	@Localize("lb")
	static String	POUNDS_TITLE;
	@Localize("Grams (g)")
	static String	GRAMS_DESCRIPTION;
	@Localize("g")
	static String	GRAMS_TITLE;
	@Localize("Kilograms (kg)")
	static String	KILOGRAMS_DESCRIPTION;
	@Localize("kg")
	static String	KILOGRAMS_TITLE;
	@Localize("Short Tons (tn)")
	static String	SHORT_TONS_DESCRIPTION;
	@Localize("tn")
	static String	SHORT_TONS_TITLE;
	@Localize("Long Tons (lt)")
	static String	LONG_TONS_DESCRIPTION;
	@Localize("lt")
	static String	LONG_TONS_TITLE;
	@Localize("Metric Tons (t)")
	static String	METRIC_TONS_DESCRIPTION;
	@Localize("t")
	static String	METRIC_TONS_TITLE;
	@Localize("{0} {1}")
	static String	FORMAT;

	private double	mFactor;

	static {
		Localization.initialize();
	}

	private WeightUnits(double factor) {
		mFactor = factor;
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
		return MessageFormat.format(FORMAT, Numbers.trimTrailingZerosAfterDecimal(textValue, localize), toString());
	}

	@Override
	public Units[] getCompatibleUnits() {
		return values();
	}
}
