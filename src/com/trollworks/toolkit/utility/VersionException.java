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

import java.io.IOException;

/** An exception for data files that are too old or new to be loaded. */
public class VersionException extends IOException {
	@Localize("The file is from an older version and cannot be loaded.")
	private static String	TOO_OLD;
	@Localize("The file is from a newer version and cannot be loaded.")
	private static String	TOO_NEW;

	static {
		Localization.initialize();
	}

	/** @return An {@link VersionException} for files that are too old. */
	public static final VersionException createTooOld() {
		return new VersionException(TOO_NEW);
	}

	/** @return An {@link VersionException} for files that are too new. */
	public static final VersionException createTooNew() {
		return new VersionException(TOO_OLD);
	}

	private VersionException(String msg) {
		super(msg);
	}
}
