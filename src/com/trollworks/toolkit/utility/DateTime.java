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

package com.trollworks.toolkit.utility;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Date and time handling. */
public class DateTime {
	private static final String		STARTING_WHITESPACE	= "^\\s*";																			//$NON-NLS-1$
	private static final String		ENDING_WHITESPACE	= "\\s*$";																			//$NON-NLS-1$
	private static final String		DATE				= "([\\d]{4})-([\\d]{2})-([\\d]{2})";												//$NON-NLS-1$
	private static final String		TIME				= "([\\d]{2}):([\\d]{2}):([\\d]{2})";												//$NON-NLS-1$
	private static final Pattern	DATE_PATTERN		= Pattern.compile(STARTING_WHITESPACE + DATE + ENDING_WHITESPACE);
	private static final Pattern	TIME_PATTERN		= Pattern.compile(STARTING_WHITESPACE + TIME + ENDING_WHITESPACE);
	private static final Pattern	DATE_TIME_PATTERN	= Pattern.compile(STARTING_WHITESPACE + DATE + "\\s+" + TIME + ENDING_WHITESPACE);	//$NON-NLS-1$

	/**
	 * @param milliseconds The time to format.
	 * @return A formatted time, like "14:22:34".
	 */
	public static final String formatTime(long milliseconds) {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.setTimeInMillis(milliseconds);
		return String.format("%tT", cal); //$NON-NLS-1$
	}

	/**
	 * @param text The text to process.
	 * @return The time or zero if the text does not match the expected pattern.
	 */
	public static final long extractTime(String text) {
		if (text != null) {
			Matcher matcher = TIME_PATTERN.matcher(text);
			if (matcher.find()) {
				long time = TimeUnit.MILLISECONDS.convert(Numbers.extractInteger(matcher.group(1), 0, false), TimeUnit.HOURS);
				time += TimeUnit.MILLISECONDS.convert(Numbers.extractInteger(matcher.group(2), 0, false), TimeUnit.MINUTES);
				time += TimeUnit.MILLISECONDS.convert(Numbers.extractInteger(matcher.group(3), 0, false), TimeUnit.SECONDS);
				return time;
			}
		}
		return 0;
	}

	/**
	 * @param milliseconds The date to format.
	 * @return A formatted date, like "2014-03-30".
	 */
	public static final String formatDate(long milliseconds) {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.setTimeInMillis(milliseconds);
		return String.format("%tF", cal); //$NON-NLS-1$
	}

	/**
	 * @param text The text to process.
	 * @return The date or zero if the text does not match the expected pattern.
	 */
	public static final long extractDate(String text) {
		if (text != null) {
			Matcher matcher = DATE_PATTERN.matcher(text);
			if (matcher.find()) {
				Calendar cal = Calendar.getInstance();
				cal.clear();
				int year = Numbers.extractInteger(matcher.group(1), 0, false);
				int month = Numbers.extractInteger(matcher.group(2), 1, false) - 1;
				int day = Numbers.extractInteger(matcher.group(3), 0, false);
				cal.set(year, month, day);
				return cal.getTimeInMillis();
			}
		}
		return 0;
	}

	/**
	 * @param milliseconds The date &amp; time to format.
	 * @return A formatted date &amp; time, like "2014-03-30 14:22:34".
	 */
	public static final String formatDateTime(long milliseconds) {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.setTimeInMillis(milliseconds);
		return String.format("%tF %tT", cal); //$NON-NLS-1$
	}

	/**
	 * @param text The text to process.
	 * @return The date &amp; time or zero if the text does not match the expected pattern.
	 */
	public static final long extractDateTime(String text) {
		if (text != null) {
			Matcher matcher = DATE_TIME_PATTERN.matcher(text);
			if (matcher.find()) {
				Calendar cal = Calendar.getInstance();
				cal.clear();
				int year = Numbers.extractInteger(matcher.group(1), 0, false);
				int month = Numbers.extractInteger(matcher.group(2), 1, false) - 1;
				int day = Numbers.extractInteger(matcher.group(3), 0, false);
				int hour = Numbers.extractInteger(matcher.group(4), 0, false);
				int minute = Numbers.extractInteger(matcher.group(5), 0, false);
				int second = Numbers.extractInteger(matcher.group(6), 0, false);
				cal.set(year, month, day, hour, minute, second);
				return cal.getTimeInMillis();
			}
		}
		return 0;
	}
}
