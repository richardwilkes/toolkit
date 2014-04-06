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

package com.trollworks.toolkit.ui;

import com.trollworks.toolkit.utility.Platform;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * Adds registry entries for mapping file extensions to an application and icons to files with those
 * extensions.
 */
public class WindowsRegistry implements Runnable {
	private static final String		DOT				= ".";	//$NON-NLS-1$
	private static final String		UNDERSCORE		= "_";	//$NON-NLS-1$
	private static final String		START_BRACKET	= "[";	//$NON-NLS-1$
	private static final String		END_BRACKET		= "]";	//$NON-NLS-1$
	private String					mPrefix;
	private HashMap<String, String>	mMap;
	private File					mAppFile;
	private File					mIconDir;

	/**
	 * @param prefix The registry prefix to use.
	 * @param map A map of extensions (no leading period) to descriptions.
	 * @param appFile The application to execute for these file extensions.
	 * @param iconDir The icon directory, where Windows .ico files can be found for each extension,
	 *            in the form 'extension.ico'. For example, the extension 'xyz' would need an icon
	 *            file named 'xyz.ico' in this directory.
	 */
	public static final void register(String prefix, HashMap<String, String> map, File appFile, File iconDir) {
		if (Platform.isWindows()) {
			Thread thread = new Thread(new WindowsRegistry(prefix, map, appFile, iconDir), WindowsRegistry.class.getSimpleName());

			thread.setPriority(Thread.NORM_PRIORITY);
			thread.setDaemon(true);
			thread.start();
		}
	}

	private WindowsRegistry(String prefix, HashMap<String, String> map, File appFile, File iconDir) {
		mPrefix = prefix;
		mMap = map;
		mAppFile = appFile;
		mIconDir = iconDir;
	}

	@Override
	public void run() {
		try {
			String appPath = mAppFile.getCanonicalPath().replaceAll("\\\\", "\\\\\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
			File regFile = File.createTempFile("reg", ".reg"); //$NON-NLS-1$ //$NON-NLS-2$
			try (PrintWriter writer = new PrintWriter(regFile);) {
				writer.println("REGEDIT4"); //$NON-NLS-1$
				writer.println();
				for (String key : mMap.keySet()) {
					writeRegistryEntry(writer, appPath, key);
				}
			}
			new ProcessBuilder("regedit", "/S", regFile.getCanonicalPath()).start().waitFor(); //$NON-NLS-1$ //$NON-NLS-2$
			regFile.delete();
		} catch (Exception exception) {
			// Ignore
		}
	}

	private void writeRegistryEntry(PrintWriter writer, String appPath, String extension) throws IOException {
		String upper = extension.toUpperCase();
		StringBuilder builder = new StringBuilder("HKEY_CLASSES_ROOT\\"); //$NON-NLS-1$

		writer.println("[-" + builder.toString() + DOT + extension + END_BRACKET); //$NON-NLS-1$
		writer.println();

		writer.println(START_BRACKET + builder.toString() + DOT + extension + END_BRACKET);
		writer.println("@=\"" + mPrefix + UNDERSCORE + upper + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		writer.println();

		builder.append(mPrefix + UNDERSCORE + upper);
		writer.println("[-" + builder.toString() + END_BRACKET); //$NON-NLS-1$
		writer.println();

		writer.println(START_BRACKET + builder.toString() + END_BRACKET);
		writer.println("@=\"" + mMap.get(extension) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		writer.println();

		writer.println(START_BRACKET + builder.toString() + "\\DefaultIcon]"); //$NON-NLS-1$
		writer.println("@=\"\\\"" + new File(mIconDir, extension + ".ico").getCanonicalPath().replaceAll("\\\\", "\\\\\\\\") + "\\\"\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		writer.println();

		builder.append("\\shell"); //$NON-NLS-1$
		writer.println(START_BRACKET + builder.toString() + END_BRACKET);
		writer.println("@=\"open\""); //$NON-NLS-1$
		writer.println();

		builder.append("\\open"); //$NON-NLS-1$
		writer.println(START_BRACKET + builder.toString() + END_BRACKET);
		writer.println("@=\"&Open\""); //$NON-NLS-1$
		writer.println();

		builder.append("\\command"); //$NON-NLS-1$
		writer.println(START_BRACKET + builder.toString() + END_BRACKET);
		writer.println("@=\"\\\"" + appPath + "\\\" \\\"%1\\\"\""); //$NON-NLS-1$ //$NON-NLS-2$
		writer.println();
	}
}
