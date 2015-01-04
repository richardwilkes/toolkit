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

/** Provides storage of processed command-line arguments. */
class CmdLineData {
	private CmdLineOption	mOption;
	private String			mArgument;

	/**
	 * Creates a new {@link CmdLineData}.
	 *
	 * @param option The option.
	 */
	CmdLineData(CmdLineOption option) {
		mOption = option;
	}

	/**
	 * Creates a new {@link CmdLineData}.
	 *
	 * @param argument The original command-line argument.
	 */
	CmdLineData(String argument) {
		mArgument = argument;
	}

	/**
	 * Creates a new {@link CmdLineData}.
	 *
	 * @param option The option.
	 * @param argument The option's argument.
	 */
	CmdLineData(CmdLineOption option, String argument) {
		mOption = option;
		mArgument = argument;
	}

	/** @return Whether this is an option. */
	boolean isOption() {
		return mOption != null;
	}

	/** @return The option, or <code>null</code> if this is not an option. */
	CmdLineOption getOption() {
		return mOption;
	}

	/** @return The option's argument, or the original command line argument. */
	String getArgument() {
		return mArgument;
	}
}
