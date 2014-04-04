/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.trollworks.toolkit.utility;

/** Defines constants for each platform we support. */
@SuppressWarnings("nls")
public enum Platform {
	/** The constant used for the Linux platform. */
	LINUX,
	/** The constant used for the Macintosh platform. */
	MAC,
	/** The constant used for the Windows platform. */
	WINDOWS,
	/** The constant used for unknown platforms. */
	UNKNOWN;

	private static final Platform	CURRENT;

	static {
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac")) {
			CURRENT = MAC;
		} else if (osName.startsWith("Win")) {
			CURRENT = WINDOWS;
		} else if (osName.startsWith("Linux")) {
			CURRENT = LINUX;
		} else {
			CURRENT = UNKNOWN;
		}
	}

	/** @return <code>true</code> if Macintosh is the platform being run on. */
	public static final boolean isMacintosh() {
		return CURRENT == MAC;
	}

	/** @return <code>true</code> if Windows is the platform being run on. */
	public static final boolean isWindows() {
		return CURRENT == WINDOWS;
	}

	/** @return <code>true</code> if Linux is the platform being run on. */
	public static final boolean isLinux() {
		return CURRENT == LINUX;
	}

	/** @return <code>true</code> if platform being run on is Unix-based. */
	public static final boolean isUnix() {
		return isMacintosh() || isLinux();
	}
}
