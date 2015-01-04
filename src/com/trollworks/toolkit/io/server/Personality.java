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

package com.trollworks.toolkit.io.server;

import java.io.IOException;
import java.nio.ByteBuffer;

/** A {@link Personality} provides the actual data handling for a {@link Session}. */
public abstract class Personality {
	private Session	mSession;

	/** @return The {@link Session} this {@link Personality} is associated with. */
	public final Session getSession() {
		return mSession;
	}

	/** @param session The {@link Session} to work with. */
	final void setSession(Session session) {
		mSession = session;
	}

	/** @return <code>true</code> if the associated {@link Session} is secure. */
	public final boolean isSecure() {
		return getSession().isSecure();
	}

	/**
	 * Called by the associated {@link Session} to process any data it has received.
	 *
	 * @param buffer The data to process. It is not required that all the data within the passed in
	 *            buffer be consumed in a single call, however, the {@link Session} will continue to
	 *            call {@link #processInput(ByteBuffer)} with the same buffer until the entire
	 *            buffer has been consumed.
	 */
	public abstract void processInput(ByteBuffer buffer) throws IOException;

	/**
	 * @param buffer The data to send. A copy of the data is not made, so do not modify it once
	 *            passed to this method.
	 */
	public final void send(ByteBuffer buffer) {
		mSession.send(buffer);
	}

	/**
	 * Requests that the associated {@link Session} be closed.
	 *
	 * @param dueToError Pass in <code>true</code> if an error condition triggered this request.
	 */
	public final void requestClose(boolean dueToError) {
		mSession.requestClose(dueToError);
	}

	/** Called when the {@link Session} is closing down. */
	public abstract void closing() throws IOException;
}
