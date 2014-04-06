/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.trollworks.toolkit.utility.units;

/** Specifies the methods a type of unit must implement. */
public interface Units {
	/**
	 * @param value The value to format.
	 * @param localize Whether or not the number should be localized.
	 * @return The formatted value.
	 */
	String format(double value, boolean localize);

	/**
	 * Converts from a specified units type into this units type.
	 *
	 * @param units The units to convert from.
	 * @param value The value to convert.
	 * @return The new value, in units of this type.
	 */
	double convert(Units units, double value);

	/**
	 * Normalizes a value to a common scale.
	 *
	 * @param value The value to normalize.
	 * @return The normalized value.
	 */
	double normalize(double value);

	/** @return The factor used. */
	double getFactor();

	String getDescription();

	/** @return The reference name of the units (not localized). */
	String name();

	/** @return An array of compatible units. */
	Units[] getCompatibleUnits();
}
