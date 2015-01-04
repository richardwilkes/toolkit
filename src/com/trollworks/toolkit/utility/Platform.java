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

/** Defines constants for each platform we support. */
@SuppressWarnings("nls")
public enum Platform {
	/** The constant used for the Linux platform. */
	LINUX("linux", "", "lib", ".so", ".sh"),
	/** The constant used for the Macintosh platform. */
	MAC("mac", "", "lib", ".jnilib", ".sh"),
	/** The constant used for the Windows platform. */
	WINDOWS("windows", ".exe", "", ".dll", ".bat"),
	/** The constant used for unknown platforms. */
	UNKNOWN("unknown", "", "", "", "");

	private String	mDirName;
	private String	mExtension;
	private String	mDynamicLibraryPrefix;
	private String	mDynamicLibraryExtension;
	private String	mScriptExtension;

	private Platform(String dirName, String extension, String dynamicLibraryPrefix, String dynamicLibraryExtension, String scriptExtension) {
		mDirName = dirName;
		mExtension = extension;
		mDynamicLibraryPrefix = dynamicLibraryPrefix;
		mDynamicLibraryExtension = dynamicLibraryExtension;
		mScriptExtension = scriptExtension;
	}

	public final String getDynamicLibraryPath(String libName) {
		return mDirName + "/" + System.getProperty("os.arch") + "/" + mDynamicLibraryPrefix + libName + mDynamicLibraryExtension;
	}

	/** @return The directory name to use for the platform. */
	public final String getDirName() {
		return mDirName;
	}

	/** @return The extension for executables on the platform. */
	public final String getExecutableExtension() {
		return mExtension;
	}

	/** @return The prefix for dynamic libraries on the platform. */
	public final String getDynamicLibraryPrefix() {
		return mDynamicLibraryPrefix;
	}

	/** @return The extension for dynamic libraries on the platform. */
	public final String getDynamicLibraryExtension() {
		return mDynamicLibraryExtension;
	}

	/** @return The extension for scripts on the platform. */
	public final String getScriptExtension() {
		return mScriptExtension;
	}

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

	/** @return The platform being run on. */
	public static final Platform getPlatform() {
		return CURRENT;
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
