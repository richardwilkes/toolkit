/*
 * Copyright (c) 1998-2020 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.collections;

import java.util.Objects;

/** Creates a tuple with two values. */
public class Pair<F, S> implements Comparable<Pair<F, S>> {
    private F mFirst;
    private S mSecond;

    /**
     * Creates a new {@link Pair}.
     *
     * @param first  The first value of the pair.
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
            return Objects.equals(mFirst, other.mFirst) && Objects.equals(mSecond, other.mSecond);
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
        result = mFirst instanceof Comparable ? ((Comparable<F>) mFirst).compareTo(other.mFirst) : Integer.compare(mFirst.hashCode(), other.mFirst.hashCode());
        if (result == 0) {
            result = mSecond instanceof Comparable ? ((Comparable<S>) mSecond).compareTo(other.mSecond) : Integer.compare(mSecond.hashCode(), other.mSecond.hashCode());
        }
        return result;
    }
}
