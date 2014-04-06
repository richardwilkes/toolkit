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

/** Common length units. */
public enum LengthUnits implements Units {
	/** Points (1/72 of an inch). */
	POINTS(1.0 / 72.0) {
		@Override
		public String getDescription() {
			return POINTS_DESCRIPTION;
		}

		@Override
		public String toString() {
			return POINTS_TITLE;
		}
	},
	/** Inches. */
	INCHES(1.0) {
		@Override
		public String getDescription() {
			return INCHES_DESCRIPTION;
		}

		@Override
		public String toString() {
			return INCHES_TITLE;
		}
	},
	/** Feet. */
	FEET(12.0) {
		@Override
		public String getDescription() {
			return FEET_DESCRIPTION;
		}

		@Override
		public String toString() {
			return FEET_TITLE;
		}
	},
	/** Feet and Inches */
	FEET_AND_INCHES(1.0) {
		@Override
		public String getDescription() {
			return FEET_AND_INCHES_DESCRIPTION;
		}

		@Override
		public String toString() {
			return FEET_AND_INCHES_TITLE;
		}

		@Override
		public String format(double value, boolean localize) {
			int feet = (int) (Math.floor(value) / 12);
			value -= 12.0 * feet;
			if (feet > 0) {
				String buffer = formatNumber(feet, localize) + '\'';
				if (value > 0) {
					return buffer + ' ' + formatNumber(value, localize) + '"';
				}
				return buffer;
			}
			return formatNumber(value, localize) + '"';
		}

		private String formatNumber(double value, boolean localize) {
			return Numbers.trimTrailingZerosAfterDecimal(localize ? Numbers.format(value) : Double.toString(value), localize);
		}
	},
	/** Yards. */
	YARDS(36.0) {
		@Override
		public String getDescription() {
			return YARDS_DESCRIPTION;
		}

		@Override
		public String toString() {
			return YARDS_TITLE;
		}
	},
	/** Miles. */
	MILES(5280.0 * 12.0) {
		@Override
		public String getDescription() {
			return MILES_DESCRIPTION;
		}

		@Override
		public String toString() {
			return MILES_TITLE;
		}
	},
	/** Millimeters. */
	MILLIMETERS(0.1 / 2.54) {
		@Override
		public String getDescription() {
			return MILLIMETERS_DESCRIPTION;
		}

		@Override
		public String toString() {
			return MILLIMETERS_TITLE;
		}
	},
	/** Centimeters. */
	CENTIMETERS(1.0 / 2.54) {
		@Override
		public String getDescription() {
			return CENTIMETERS_DESCRIPTION;
		}

		@Override
		public String toString() {
			return CENTIMETERS_TITLE;
		}
	},
	/** Kilometers. */
	KILOMETERS(100000.0 / 2.54) {
		@Override
		public String getDescription() {
			return KILOMETERS_DESCRIPTION;
		}

		@Override
		public String toString() {
			return KILOMETERS_TITLE;
		}
	},
	/** Meters. Must be after all the other 'meter' types. */
	METERS(100.0 / 2.54) {
		@Override
		public String getDescription() {
			return METERS_DESCRIPTION;
		}

		@Override
		public String toString() {
			return METERS_TITLE;
		}
	};

	@Localize("Points (pt)")
	static String	POINTS_DESCRIPTION;
	@Localize("pt")
	static String	POINTS_TITLE;
	@Localize("Inches (in)")
	static String	INCHES_DESCRIPTION;
	@Localize("in")
	static String	INCHES_TITLE;
	@Localize("Feet (ft)")
	static String	FEET_DESCRIPTION;
	@Localize("ft")
	static String	FEET_TITLE;
	@Localize("Feet (') & Inches (\")")
	static String	FEET_AND_INCHES_DESCRIPTION;
	@Localize("'")
	static String	FEET_AND_INCHES_TITLE;
	@Localize("Yards (yd)")
	static String	YARDS_DESCRIPTION;
	@Localize("yd")
	static String	YARDS_TITLE;
	@Localize("Miles (mi)")
	static String	MILES_DESCRIPTION;
	@Localize("mi")
	static String	MILES_TITLE;
	@Localize("Millimeters (mm)")
	static String	MILLIMETERS_DESCRIPTION;
	@Localize("mm")
	static String	MILLIMETERS_TITLE;
	@Localize("Centimeters (cm)")
	static String	CENTIMETERS_DESCRIPTION;
	@Localize("cm")
	static String	CENTIMETERS_TITLE;
	@Localize("Meters (m)")
	static String	METERS_DESCRIPTION;
	@Localize("m")
	static String	METERS_TITLE;
	@Localize("Kilometers (km)")
	static String	KILOMETERS_DESCRIPTION;
	@Localize("km")
	static String	KILOMETERS_TITLE;
	@Localize("{0} {1}")
	static String	FORMAT;

	static {
		Localization.initialize();
	}

	private double	mFactor;

	private LengthUnits(double factor) {
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
