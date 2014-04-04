/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.trollworks.toolkit.utility;

import java.io.ByteArrayOutputStream;

/** Base64 encoding and decoding. */
public class Base64 {
	private static final char	PAD			= '=';
	private static final char[]	ALPHABET	= { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };
	private static final byte[]	NIBBLES		= new byte[256];

	static {
		for (int i = 0; i < 256; i++) {
			NIBBLES[i] = -1;
		}
		for (byte b = 0; b < 64; b++) {
			NIBBLES[(byte) ALPHABET[b]] = b;
		}
		NIBBLES[(byte) PAD] = 0;
	}

	/**
	 * Encodes a byte array into Base64 notation.
	 *
	 * @param bytes The data to convert.
	 * @param maxLineLength The maximum line length to use. Values less than 1 indicate no maximum.
	 */
	public static final String encodeAsString(byte[] bytes, int maxLineLength) {
		char[] encoded = encode(bytes);
		if (maxLineLength < 1) {
			return new String(encoded);
		}
		StringBuilder buffer = new StringBuilder(encoded.length + encoded.length / maxLineLength);
		for (int i = 0; i < encoded.length; i += maxLineLength) {
			buffer.append(encoded, i, Math.min(encoded.length - i, maxLineLength));
			if (i + maxLineLength < encoded.length) {
				buffer.append('\n');
			}
		}
		return buffer.toString();
	}

	/**
	 * Encodes a byte array into Base64 notation.
	 *
	 * @param bytes The data to convert.
	 * @return The encoded characters.
	 */
	public static char[] encode(byte[] bytes) {
		int length = bytes.length;
		char encoded[] = new char[(length + 2) / 3 * 4];
		int encodedIndex = 0;
		int index = 0;
		int stop = length / 3 * 3;
		while (index < stop) {
			byte b0 = bytes[index++];
			encoded[encodedIndex++] = ALPHABET[b0 >>> 2 & 0x3F];
			byte b1 = bytes[index++];
			encoded[encodedIndex++] = ALPHABET[b0 << 4 & 0x3F | b1 >>> 4 & 0x0F];
			byte b2 = bytes[index++];
			encoded[encodedIndex++] = ALPHABET[b1 << 2 & 0x3F | b2 >>> 6 & 0x03];
			encoded[encodedIndex++] = ALPHABET[b2 & 0x3F];
		}
		if (length != index) {
			length %= 3;
			if (length == 2 || length == 1) {
				byte b0 = bytes[index++];
				encoded[encodedIndex++] = ALPHABET[b0 >>> 2 & 0x3F];
				if (length == 2) {
					byte b1 = bytes[index++];
					encoded[encodedIndex++] = ALPHABET[b0 << 4 & 0x3F | b1 >>> 4 & 0x0F];
					encoded[encodedIndex++] = ALPHABET[b1 << 2 & 0x3F];
				} else {
					encoded[encodedIndex++] = ALPHABET[b0 << 4 & 0x3F];
					encoded[encodedIndex++] = PAD;
				}
				encoded[encodedIndex++] = PAD;
			}
		}
		return encoded;
	}

	/**
	 * Decodes data from Base64 notation.
	 *
	 * @param encoded The string to decode.
	 * @return The decoded data.
	 */
	public static byte[] decode(String encoded) {
		byte nibbles[] = new byte[4];
		int s = 0;
		int length = encoded.length();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(4 * length / 3);
		int index = 0;
		while (index < length) {
			char c = encoded.charAt(index++);
			if (c == PAD) {
				break;
			}
			if (Character.isWhitespace(c)) {
				continue;
			}
			byte nibble = NIBBLES[c];
			if (nibble < 0) {
				throw new IllegalArgumentException("Not B64 encoded"); //$NON-NLS-1$
			}
			nibbles[s++] = NIBBLES[c];
			switch (s) {
				case 1:
				default:
					break;
				case 2:
					baos.write(nibbles[0] << 2 | nibbles[1] >>> 4);
					break;
				case 3:
					baos.write(nibbles[1] << 4 | nibbles[2] >>> 2);
					break;
				case 4:
					baos.write(nibbles[2] << 6 | nibbles[3]);
					s = 0;
					break;
			}
		}
		return baos.toByteArray();
	}
}
