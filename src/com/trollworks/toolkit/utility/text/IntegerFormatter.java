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

package com.trollworks.toolkit.utility.text;

import java.text.ParseException;

import javax.swing.JFormattedTextField;

/** Provides integer field conversion. */
public class IntegerFormatter extends JFormattedTextField.AbstractFormatter {
	private int		mMinValue;
	private int		mMaxValue;
	private boolean	mForceSign;
	private boolean	mBlankOnZero;

	/**
	 * Creates a new {@link IntegerFormatter}.
	 *
	 * @param forceSign Whether or not a plus sign should be forced for positive numbers.
	 */
	public IntegerFormatter(boolean forceSign) {
		this(Integer.MIN_VALUE, Integer.MAX_VALUE, forceSign, false);
	}

	/**
	 * Creates a new {@link IntegerFormatter}.
	 *
	 * @param minValue The minimum value allowed.
	 * @param maxValue The maximum value allowed.
	 * @param forceSign Whether or not a plus sign should be forced for positive numbers.
	 */
	public IntegerFormatter(int minValue, int maxValue, boolean forceSign) {
		this(minValue, maxValue, forceSign, false);
	}

	/**
	 * Creates a new {@link IntegerFormatter}.
	 *
	 * @param minValue The minimum value allowed.
	 * @param maxValue The maximum value allowed.
	 * @param forceSign Whether or not a plus sign should be forced for positive numbers.
	 * @param blankOnZero When <code>true</code>, a value of zero resolves to the empty string when
	 *            calling {@link #valueToString(Object)}.
	 */
	public IntegerFormatter(int minValue, int maxValue, boolean forceSign, boolean blankOnZero) {
		mMinValue = minValue;
		mMaxValue = maxValue;
		mForceSign = forceSign;
		mBlankOnZero = blankOnZero;
	}

	@Override
	public Object stringToValue(String text) throws ParseException {
		return new Integer(Math.min(Math.max(Numbers.getLocalizedInteger(text, mMinValue <= 0 && mMaxValue >= 0 ? 0 : mMinValue), mMinValue), mMaxValue));
	}

	@Override
	public String valueToString(Object value) throws ParseException {
		int val = ((Integer) value).intValue();
		if (mBlankOnZero && val == 0) {
			return ""; //$NON-NLS-1$
		}
		return mForceSign ? Numbers.formatWithForcedSign(val) : Numbers.format(val);
	}
}
