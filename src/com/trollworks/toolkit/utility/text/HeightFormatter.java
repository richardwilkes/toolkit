/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.trollworks.toolkit.utility.text;

import com.trollworks.toolkit.utility.units.LengthValue;

import java.text.ParseException;

import javax.swing.JFormattedTextField;

/** Provides height field conversion. */
public class HeightFormatter extends JFormattedTextField.AbstractFormatter {
	@Override
	public Object stringToValue(String text) throws ParseException {
		return LengthValue.extract(text, true);
	}

	@Override
	public String valueToString(Object value) throws ParseException {
		return ((LengthValue) value).toString();
	}
}