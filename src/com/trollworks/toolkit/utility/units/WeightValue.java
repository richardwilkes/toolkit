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

import com.trollworks.toolkit.utility.text.Numbers;

/** Holds a value and {@link WeightUnits} pair. */
public class WeightValue extends UnitsValue<WeightUnits> {
	/**
	 * @param buffer The buffer to extract a {@link WeightValue} from.
	 * @param localized <code>true</code> if the string might have localized notation within it.
	 * @return The result.
	 */
	public static WeightValue extract(String buffer, boolean localized) {
		WeightUnits units = WeightUnits.POUNDS;
		if (buffer != null) {
			buffer = buffer.trim();
			for (WeightUnits lu : WeightUnits.values()) {
				String text = lu.toString();
				if (buffer.endsWith(text)) {
					units = lu;
					buffer = buffer.substring(0, buffer.length() - text.length());
					break;
				}
			}
		}
		return new WeightValue(localized ? Numbers.getLocalizedDouble(buffer, 0) : Numbers.getDouble(buffer, 0), units);
	}

	/**
	 * Creates a new {@link UnitsValue}.
	 *
	 * @param value The value to use.
	 * @param units The {@link Units} to use.
	 */
	public WeightValue(double value, WeightUnits units) {
		super(value, units);
	}

	/**
	 * Creates a new {@link WeightValue} from an existing one.
	 *
	 * @param other The {@link WeightValue} to clone.
	 */
	public WeightValue(WeightValue other) {
		super(other);
	}

	@Override
	public WeightUnits getDefaultUnits() {
		return WeightUnits.POUNDS;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		return obj instanceof WeightValue && super.equals(obj);
	}
}
