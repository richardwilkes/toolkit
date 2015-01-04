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

/** Provides basic timing facilities. */
public final class Timing {
	private long	mBase;

	/** Creates a new {@link Timing}. */
	public Timing() {
		reset();
	}

	/** Resets the base time to this instant. */
	public final void reset() {
		mBase = System.nanoTime();
	}

	/** @return The number of elapsed nanoseconds since the timing object was created or last reset. */
	public final long elapsed() {
		return System.nanoTime() - mBase;
	}

	/**
	 * @return The number of elapsed nanoseconds since the timing object was created or last reset,
	 *         then resets it.
	 */
	public final long elapsedThenReset() {
		long oldBase = mBase;
		mBase = System.nanoTime();
		return mBase - oldBase;
	}

	/** @return The number of elapsed seconds since the timing object was created or last reset. */
	public final double elapsedSeconds() {
		return elapsed() / 1000000000.0;
	}

	/**
	 * @return The number of elapsed seconds since the timing object was created or last reset, then
	 *         resets it.
	 */
	public final double elapsedSecondsThenReset() {
		return elapsedThenReset() / 1000000000.0;
	}

	public final String toStringWithNanoResolution() {
		return String.format("%,.9fs", Double.valueOf(elapsedSeconds())); //$NON-NLS-1$
	}

	public final String toStringWithNanoResolutionThenReset() {
		return String.format("%,.9fs", Double.valueOf(elapsedSecondsThenReset())); //$NON-NLS-1$
	}

	public final String toStringWithMicroResolution() {
		return String.format("%,.6fs", Double.valueOf(elapsedSeconds())); //$NON-NLS-1$
	}

	public final String toStringWithMicroResolutionThenReset() {
		return String.format("%,.6fs", Double.valueOf(elapsedSecondsThenReset())); //$NON-NLS-1$
	}

	public final String toStringWithMilliResolution() {
		return String.format("%,.3fs", Double.valueOf(elapsedSeconds())); //$NON-NLS-1$
	}

	public final String toStringWithMilliResolutionThenReset() {
		return String.format("%,.3fs", Double.valueOf(elapsedSecondsThenReset())); //$NON-NLS-1$
	}

	@Override
	public final String toString() {
		return Numbers.trimTrailingZeroes(toStringWithMicroResolutionThenReset(), true);
	}
}
