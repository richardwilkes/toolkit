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

import com.trollworks.toolkit.annotation.Localize;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;

/** Provides text manipulation. */
public class Text {
	@Localize("a")
	private static String		A;
	@Localize("an")
	private static String		AN;
	@Localize("was")
	@Localize(locale = "ru", value = "был")
	@Localize(locale = "de", value = "wurde")
	@Localize(locale = "es", value = "era")
	private static String		WAS;
	@Localize("were")
	@Localize(locale = "ru", value = "где")
	@Localize(locale = "de", value = "wurden")
	@Localize(locale = "es", value = "eran")
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

	/**
	 * @param data The bytes to compute a hash for.
	 * @return A SHA-1 hash for the input data.
	 */
	public final static byte[] computeSHA1Hash(byte[] data) throws NoSuchAlgorithmException {
		return computeSHA1Hash(data, 0, data.length);
	}

	/**
	 * @param data The bytes to compute a hash for.
	 * @param offset The starting index.
	 * @param length The number of bytes to use.
	 * @return A SHA-1 hash for the input data.
	 */
	public final static byte[] computeSHA1Hash(byte[] data, int offset, int length) throws NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
		sha1.update(data, offset, length);
		return sha1.digest();
	}

	/**
	 * @param data The data to create a hex string for.
	 * @return A string of two character hexadecimal values for each byte.
	 */
	public final static String bytesToHex(byte[] data) {
		return bytesToHex(data, 0, data.length);
	}

	/**
	 * @param data The data to create a hex string for.
	 * @param offset The starting index.
	 * @param length The number of bytes to use.
	 * @return A string of two character hexadecimal values for each byte.
	 */
	public final static String bytesToHex(byte[] data, int offset, int length) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < length; i++) {
			String hex = Integer.toHexString(data[i + offset] & 0xFF);
			if (hex.length() < 2) {
				buffer.append('0');
			}
			buffer.append(hex);
		}
		return buffer.toString();
	}

	/**
	 * @param text The text to reflow.
	 * @return The revised text.
	 */
	@SuppressWarnings("nls")
	public static final String reflow(String text) {
		if (text == null) {
			return "";
		}
		int count = 0;
		StringBuilder buffer = new StringBuilder();
		StringTokenizer tokenizer = new StringTokenizer(text.replaceAll("[\\x00-\\x08]", "").replaceAll("[\\x0b\\x0c]", "").replaceAll("[\\x0e-\\x1f]", "").replaceAll("[\\x7f-\\x9f]", "").replaceAll("\r\n", "\n").replace('\r', '\n').replaceAll("[ \t\f]+", " ").trim(), "\n", true);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.equals("\n")) {
				count++;
			} else {
				if (count == 1) {
					buffer.append(" ");
				} else if (count > 1) {
					buffer.append("\n\n");
				}
				count = 0;
				buffer.append(token);
			}
		}
		return buffer.toString();
	}
}
