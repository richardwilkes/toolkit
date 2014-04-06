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

package com.trollworks.toolkit.collections;

/** A utility for consistent extraction of an {@link Enum} value from a text buffer. */
public class Enums {
	private static final String	EMPTY		= "";	//$NON-NLS-1$
	private static final String	COMMA		= ",";	//$NON-NLS-1$
	private static final String	UNDERSCORE	= "_";	//$NON-NLS-1$
	private static final String	SPACE		= " ";	//$NON-NLS-1$

	/**
	 * @param <T> The type of {@link Enum}.
	 * @param buffer The buffer to load from.
	 * @param values The possible values.
	 * @param defaultValue The default value to use in case of no match.
	 * @return The {@link Enum} representing the buffer.
	 */
	public static final <T extends Enum<?>> T extract(String buffer, T[] values, T defaultValue) {
		T value = extract(buffer, values);
		return value != null ? value : defaultValue;
	}

	/**
	 * @param <T> The type of {@link Enum}.
	 * @param buffer The buffer to load from.
	 * @param values The possible values.
	 * @return The {@link Enum} representing the buffer, or <code>null</code> if a match could not
	 *         be found.
	 */
	public static final <T extends Enum<?>> T extract(String buffer, T[] values) {
		if (buffer != null) {
			for (T type : values) {
				if (type.name().equalsIgnoreCase(buffer)) {
					return type;
				}
			}

			// If that failed, replace any embedded underscores in the name with
			// spaces and try again
			for (T type : values) {
				if (type.name().replaceAll(UNDERSCORE, SPACE).equalsIgnoreCase(buffer)) {
					return type;
				}
			}

			// If that failed, replace any embedded underscores in the name with
			// commas and try again
			for (T type : values) {
				if (type.name().replaceAll(UNDERSCORE, COMMA).equalsIgnoreCase(buffer)) {
					return type;
				}
			}

			// If that failed, remove any embedded underscores in the name and try
			// again
			for (T type : values) {
				if (type.name().replaceAll(UNDERSCORE, EMPTY).equalsIgnoreCase(buffer)) {
					return type;
				}
			}
		}
		return null;
	}
}
