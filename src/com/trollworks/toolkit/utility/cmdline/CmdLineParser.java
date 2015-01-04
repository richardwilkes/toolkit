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

package com.trollworks.toolkit.utility.cmdline;

import com.trollworks.toolkit.utility.Platform;

import java.util.ArrayList;

/** Provides parsing of a string to generate an argument list. */
public class CmdLineParser {
	/**
	 * @param line An unparsed command-line.
	 * @return The command-line, separated into separate arguments.
	 */
	public static ArrayList<String> parseIntoList(String line) {
		ArrayList<String> args = new ArrayList<>();
		StringBuilder buffer = new StringBuilder();
		int size = line.length();
		boolean inEscape = false;
		boolean inDoubleQuote = false;
		boolean inSingleQuote = false;
		boolean canEscape = !Platform.isWindows();

		for (int i = 0; i < size; i++) {
			char ch = line.charAt(i);

			if (inEscape) {
				inEscape = false;
			} else if (canEscape && ch == '\\') {
				inEscape = true;
			} else if (inDoubleQuote) {
				if (ch == '"') {
					inDoubleQuote = false;
				} else {
					buffer.append(ch);
				}
			} else if (inSingleQuote) {
				if (ch == '\'') {
					inSingleQuote = false;
				} else {
					buffer.append(ch);
				}
			} else if (ch == '"') {
				inDoubleQuote = true;
			} else if (ch == '\'') {
				inSingleQuote = true;
			} else if (ch == ' ' || ch == '\t') {
				if (buffer.length() > 0) {
					args.add(buffer.toString());
					buffer.setLength(0);
				}
			} else {
				buffer.append(ch);
			}
		}

		if (inEscape) {
			buffer.append('\\');
		} else if (inDoubleQuote) {
			buffer.insert(0, '"');
		} else if (inSingleQuote) {
			buffer.insert(0, '\'');
		}
		if (buffer.length() > 0) {
			args.add(buffer.toString());
		}

		return args;
	}

	/**
	 * @param line An unparsed command-line.
	 * @return The command-line, separated into separate arguments.
	 */
	public static String[] parseIntoArray(String line) {
		return parseIntoList(line).toArray(new String[0]);
	}
}
