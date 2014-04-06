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

package com.trollworks.toolkit.utility.text;

import com.trollworks.toolkit.utility.units.WeightValue;

import java.text.ParseException;

import javax.swing.JFormattedTextField;

/** Provides weight field conversion. */
public class WeightFormatter extends JFormattedTextField.AbstractFormatter {
	@Override
	public Object stringToValue(String text) throws ParseException {
		return WeightValue.extract(text, true);
	}

	@Override
	public String valueToString(Object value) throws ParseException {
		return ((WeightValue) value).toString();
	}
}
