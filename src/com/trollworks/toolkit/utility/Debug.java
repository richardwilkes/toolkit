/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is com.trollworks.toolkit.
 *
 * The Initial Developer of the Original Code is Richard A. Wilkes.
 * Portions created by the Initial Developer are Copyright (C) 1998-2014,
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */

package com.trollworks.toolkit.utility;

/** Provides various debugging utilities. */
public class Debug {
	/** Controls whether we are in 'development mode' or not. */
	public static final boolean	DEV_MODE	= false;
	private long				mStartTime;

	/**
	 * Determines whether the specified key is set, looking first in the system properties and
	 * falling back to the system environment if it is not set at all in the system properties.
	 *
	 * @param key The key to check.
	 * @return <code>true</code> if the key is enabled.
	 */
	public static final boolean isKeySet(String key) {
		String value = System.getProperty(key);
		if (value == null) {
			value = System.getenv(key);
		}
		return Numbers.extractBoolean(value);
	}

	/**
	 * Extracts the class name, message and stack trace from the specified {@link Throwable}. The
	 * stack trace will be formatted such that Eclipse's console will make each node into a
	 * hyperlink.
	 *
	 * @param throwable The {@link Throwable} to process.
	 * @return The formatted {@link Throwable}.
	 */
	public static final String toString(Throwable throwable) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(throwable.getClass().getSimpleName());
		buffer.append(": "); //$NON-NLS-1$
		buffer.append(throwable.getMessage());
		buffer.append(": "); //$NON-NLS-1$
		stackTrace(throwable, buffer);
		return buffer.toString();
	}

	/**
	 * Extracts a stack trace from the specified {@link Throwable}. The stack trace will be
	 * formatted such that Eclipse's console will make each node into a hyperlink.
	 *
	 * @param throwable The {@link Throwable} to process.
	 * @param buffer The buffer to store the result in.
	 * @return The {@link StringBuilder} that was passed in.
	 */
	public static final StringBuilder stackTrace(Throwable throwable, StringBuilder buffer) {
		return stackTrace(throwable, 0, buffer);
	}

	/**
	 * Extracts a stack trace from the specified {@link Throwable}. The stack trace will be
	 * formatted such that Eclipse's console will make each node into a hyperlink.
	 *
	 * @param throwable The {@link Throwable} to process.
	 * @param startAt The point in the stack to start processing.
	 * @param buffer The buffer to store the result in.
	 * @return The {@link StringBuilder} that was passed in.
	 */
	public static final StringBuilder stackTrace(Throwable throwable, int startAt, StringBuilder buffer) {
		StackTraceElement[] stackTrace = throwable.getStackTrace();
		for (int i = startAt; i < stackTrace.length; i++) {
			if (i > startAt) {
				buffer.append(" < "); //$NON-NLS-1$
			}
			buffer.append('(');
			buffer.append(stackTrace[i].getFileName());
			buffer.append(':');
			buffer.append(stackTrace[i].getLineNumber());
			buffer.append(')');
		}
		return buffer;
	}

	public Debug() {
		mStartTime = System.nanoTime();
	}

	/**
	 * @return The elapsed time in seconds since this object was created or reset. Calling this
	 *         method resets the timer.
	 */
	public final double elapsed() {
		long start = mStartTime;
		mStartTime = System.nanoTime();
		return (mStartTime - start) / 1000000.0;
	}

	/**
	 * @return The elapsed time, formatted as seconds. Calling this method resets the timer.
	 */
	@Override
	public final String toString() {
		return String.format("%,.6f", Double.valueOf(elapsed())); //$NON-NLS-1$
	}
}
