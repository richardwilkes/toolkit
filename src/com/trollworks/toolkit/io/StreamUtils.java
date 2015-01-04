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

package com.trollworks.toolkit.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/** Utility methods for use with streams. */
public class StreamUtils {
	/**
	 * Fills the specified buffer with bytes from the stream. Will not return until the buffer is
	 * full or an {@link IOException} occurs.
	 *
	 * @param in The stream to read from.
	 * @param buffer The buffer to place the bytes into.
	 */
	public static final void readFully(InputStream in, byte buffer[]) throws IOException {
		readFully(in, buffer, 0, buffer.length);
	}

	/**
	 * Fills the specified buffer with bytes from the stream. Will not return until the requested
	 * number of bytes have been read or an {@link IOException} occurs.
	 *
	 * @param in The stream to read from.
	 * @param buffer The buffer to place the bytes into.
	 * @param offset The position within the buffer to start placing bytes.
	 * @param length The number of bytes to read.
	 */
	public static final void readFully(InputStream in, byte buffer[], int offset, int length) throws IOException {
		int total = 0;
		while (total < length) {
			int read = in.read(buffer, offset + total, length - total);
			if (read < 0) {
				throw new EOFException();
			}
			total += read;
		}
	}

	/**
	 * Skips the specified number of bytes within the stream. Will not return until the requested
	 * number of bytes have been skipped or an {@link IOException} occurs.
	 *
	 * @param in The stream to skip bytes within.
	 * @param length The number of bytes to skip.
	 */
	public static final void skipFully(InputStream in, long length) throws IOException {
		long total = 0;
		long skipped = 0;
		while (total < length && (skipped = in.skip(length - total)) > 0) {
			total += skipped;
		}
	}
}
