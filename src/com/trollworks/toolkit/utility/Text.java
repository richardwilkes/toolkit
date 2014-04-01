/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is com.trollworks.toolkit.
 *
 * The Initial Developer of the Original Code is Richard A. Wilkes.
 * Portions created by the Initial Developer are Copyright (C) 1998-2014,
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */

package com.trollworks.toolkit.utility;

import com.trollworks.localization.Localization;
import com.trollworks.localization.Localize;

/** Provides text manipulation. */
public class Text {
	public static final String	UTF8_ENCODING	= "UTF-8";	//$NON-NLS-1$
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
