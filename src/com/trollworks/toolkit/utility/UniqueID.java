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

import java.util.Random;

/**
 * Provides a reasonably unique ID.
 * <p>
 * Support is also provided for using the ID as an "upgradable" ID that has both a sub-ID and a time
 * stamp, such that an object being tracked with a unique ID could determine if another object was a
 * newer, more up-to-date version of itself.
 */
public class UniqueID {
	private static final Random	RANDOM	= new Random();
	private long				mTimeStamp;
	private final long			mSubID;

	/** Creates a unique ID. */
	public UniqueID() {
		this(System.currentTimeMillis(), RANDOM.nextLong());
	}

	/**
	 * Creates a unique ID.
	 *
	 * @param uniqueID An ID obtained by called {@link #toString()} on a previous instance of
	 *            {@link UniqueID}.
	 */
	public UniqueID(String uniqueID) {
		long id;
		try {
			int colon = uniqueID.indexOf(':');
			mTimeStamp = Long.parseLong(uniqueID.substring(0, colon), Character.MAX_RADIX);
			id = Long.parseLong(uniqueID.substring(colon + 1), Character.MAX_RADIX);
		} catch (Exception exception) {
			mTimeStamp = System.currentTimeMillis();
			id = RANDOM.nextLong();
		}
		mSubID = id;
	}

	/**
	 * Creates a unique ID.
	 *
	 * @param timeStamp The time stamp for this unique ID. Typically, the result of a call to
	 *            {@link System#currentTimeMillis()}.
	 * @param subID The sub-ID for this unique ID. Typically, the result of a call to
	 *            {@link Random#nextLong()}.
	 */
	public UniqueID(long timeStamp, long subID) {
		mTimeStamp = timeStamp;
		mSubID = subID;
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof UniqueID) {
			UniqueID other = (UniqueID) obj;
			return mSubID == other.mSubID && mTimeStamp == other.mTimeStamp;
		}
		return false;
	}

	/**
	 * @param obj The object to check.
	 * @return <code>true</code> if the passed-in object is also a {@link UniqueID} and their
	 *         sub-ID's match.
	 */
	public final boolean subIDEquals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof UniqueID) {
			UniqueID other = (UniqueID) obj;
			return mSubID == other.mSubID;
		}
		return false;
	}

	/**
	 * @param obj The object to check.
	 * @return <code>true</code> if the passed-in object is also a {@link UniqueID}, their sub-ID's
	 *         match, and the passed-in object's time stamp is newer.
	 */
	public final boolean subIDEqualsAndNewer(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof UniqueID) {
			UniqueID other = (UniqueID) obj;
			return mSubID == other.mSubID && mTimeStamp < other.mTimeStamp;
		}
		return false;
	}

	@Override
	public final int hashCode() {
		return (int) mSubID;
	}

	@Override
	public final String toString() {
		return Long.toString(mTimeStamp, Character.MAX_RADIX) + ":" + Long.toString(mSubID, Character.MAX_RADIX); //$NON-NLS-1$
	}

	/** @return The sub-ID. */
	public final long getSubID() {
		return mSubID;
	}

	/** @return The time stamp. */
	public final long getTimeStamp() {
		return mTimeStamp;
	}

	/** @param timeStamp The time stamp to set. */
	public final void setTimeStamp(long timeStamp) {
		mTimeStamp = timeStamp;
	}
}
