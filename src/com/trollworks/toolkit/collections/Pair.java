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

package com.trollworks.toolkit.collections;

import com.trollworks.toolkit.utility.Numbers;

/** Creates a tuple with two values. */
public class Pair<F, S> implements Comparable<Pair<F, S>> {
	private F	mFirst;
	private S	mSecond;

	/**
	 * Creates a new {@link Pair}.
	 *
	 * @param first The first value of the pair.
	 * @param second The second value of the pair.
	 */
	public Pair(F first, S second) {
		mFirst = first;
		mSecond = second;
	}

	/** @return The first value of the pair. */
	public final F getFirst() {
		return mFirst;
	}

	/** @return The second value of the pair. */
	public final S getSecond() {
		return mSecond;
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Pair) {
			Pair<?, ?> other = (Pair<?, ?>) obj;
			return (mFirst == null ? other.mFirst == null : mFirst.equals(other.mFirst)) &&
							(mSecond == null ? other.mSecond == null : mSecond.equals(other.mSecond));
		}
		return false;
	}

	@Override
	public final int hashCode() {
		return (mFirst == null ? 22 : mFirst.hashCode()) ^ (mSecond == null ? 67 : mSecond.hashCode());
	}

	@SuppressWarnings("unchecked")
	@Override
	public final int compareTo(Pair<F, S> other) {
		int result;
		if (mFirst instanceof Comparable) {
			result = ((Comparable<F>) mFirst).compareTo(other.mFirst);
		} else {
			result = Numbers.compare(mFirst.hashCode(), other.mFirst.hashCode());
		}
		if (result == 0) {
			if (mSecond instanceof Comparable) {
				result = ((Comparable<S>) mSecond).compareTo(other.mSecond);
			} else {
				result = Numbers.compare(mSecond.hashCode(), other.mSecond.hashCode());
			}
		}
		return result;
	}
}
