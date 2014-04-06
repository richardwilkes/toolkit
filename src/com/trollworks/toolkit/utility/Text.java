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

package com.trollworks.toolkit.utility;

import com.trollworks.toolkit.annotation.Localize;

/** Provides text manipulation. */
public class Text {
	@Localize("a")
	private static String		A;
	@Localize("an")
	private static String		AN;
	@Localize("was")
	private static String		WAS;
	@Localize("were")
	private static String		WERE;

	static {
		Localization.initialize();
	}

	public static final String	UTF8_ENCODING	= "UTF-8";	//$NON-NLS-1$

	/**
	 * @param text The text to check.
	 * @return "a" or "an", as appropriate for the text that will be following it.
	 */
	public static final String aAn(String text) {
		return Text.startsWithVowel(text) ? AN : A;
	}

	/**
	 * @param amount The number of items.
	 * @return "was" or "were", as appropriate for the number of items.
	 */
	public static final String wasWere(int amount) {
		return amount == 1 ? WAS : WERE;
	}

	/**
	 * @param ch The character to check.
	 * @return <code>true</code> if the character is a vowel.
	 */
	public static final boolean isVowel(char ch) {
		ch = Character.toLowerCase(ch);
		return ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u';
	}

	/**
	 * @param text The text to check.
	 * @return <code>true</code> if the text starts with a vowel.
	 */
	public static final boolean startsWithVowel(String text) {
		if (text != null && !text.isEmpty()) {
			return isVowel(text.charAt(0));
		}
		return false;
	}
}
