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

/** Describes a command-line option. */
public class CmdLineOption {
	private String[]	mNames;
	private String		mDescription;
	private String		mArgumentLabel;

	/**
	 * Creates a new {@link CmdLineOption}.
	 *
	 * @param description The description of this option.
	 * @param argumentLabel The name of this option's argument, if it has one. Use <code>null</code>
	 *            if it doesn't.
	 * @param names One or more names that can be used to invoke this option.
	 */
	public CmdLineOption(String description, String argumentLabel, String... names) {
		mNames = new String[names.length];
		mDescription = description;
		mArgumentLabel = argumentLabel;
		for (int i = 0; i < names.length; i++) {
			mNames[i] = names[i].trim().toLowerCase();
		}
	}

	/** @return The description of this option. */
	public String getDescription() {
		return mDescription;
	}

	/** @return The names this option can be invoked with. */
	public String[] getNames() {
		return mNames;
	}

	/** @return Whether this option takes an argument. */
	public boolean takesArgument() {
		return mArgumentLabel != null;
	}

	/** @return The argument label, if any. */
	public String getArgumentLabel() {
		return mArgumentLabel;
	}

	@Override
	public String toString() {
		return (Platform.isWindows() ? '/' : '-') + mNames[0];
	}
}
