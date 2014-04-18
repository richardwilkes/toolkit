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

import java.lang.reflect.Method;

/**
 * This stub class is intended to be compiled with Java 1.1, allowing it to be loaded and executed
 * even on very old JVM's. Should the minimum requirements not be achieved, then an error message is
 * displayed and the program exits. To be effective, a class with a single <code>main</code> method
 * and nothing else should be created and compiled with Java 1.1, something like this:
 *
 * <pre>
 * public class MyCoolApp {
 * 	public static void main(String[] args) {
 * 		Launcher.launch(&quot;1.8&quot;, &quot;com.trollworks.MyCoolAppMain&quot;, args);
 * 	}
 * }
 * </pre>
 */
public class Launcher {
	/**
	 * Attempts to launch the main application after verifying a minimum version of the Java runtime
	 * is being used.
	 *
	 * @param minimumJavaVersion The minimum Java version to allow, such as "1.8".
	 * @param mainClass The fully qualified main class name, such as "com.trollworks.MyCoolApp".
	 * @param args The command line arguments.
	 */
	public static void launch(String minimumJavaVersion, String mainClass, String[] args) {
		try {
			if (extractVersion(System.getProperty("java.version")) < extractVersion(minimumJavaVersion)) { //$NON-NLS-1$
				if (minimumJavaVersion.startsWith("1.")) { //$NON-NLS-1$
					minimumJavaVersion = minimumJavaVersion.substring(2);
				}
				System.err.println("Please install Java " + minimumJavaVersion + " or greater."); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				Method method = Class.forName(mainClass).getMethod("main", new Class[] { String[].class }); //$NON-NLS-1$
				method.invoke(null, new Object[] { args });
			}
		} catch (Throwable throwable) {
			throwable.printStackTrace(System.err);
		}
	}

	private static long extractVersion(String versionString) {
		char[] chars = versionString.toCharArray();
		long version = 0;
		int shift = 48;
		long value = 0;
		int max = chars.length;

		for (int i = 0; i < max; i++) {
			char ch = chars[i];
			if (ch >= '0' && ch <= '9') {
				value *= 10;
				value += ch - '0';
			} else if (ch == '.' || ch == '_') {
				if (value > 0xEFFF) {
					value = 0xEFFF;
				}
				version |= value << shift;
				value = 0;
				shift -= 16;
				if (shift < 0) {
					break;
				}
			} else {
				if (value > 0xEFFF) {
					value = 0xEFFF;
				}
				version |= value << shift;
				value = 0;
				shift -= 16;
				break;
			}
		}
		if (shift >= 0) {
			if (value > 0xEFFF) {
				value = 0xEFFF;
			}
			version |= value << shift;
		}
		return version;
	}
}
