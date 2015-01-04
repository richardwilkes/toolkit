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

package com.trollworks.toolkit.io.server.http;

import java.io.IOException;

/** Provides an {@link IOException} that can be easily transformed into a {@link HttpResponse}. */
public class HttpResponseException extends IOException {
	private final HttpStatusCode	mStatus;

	/**
	 * @param status The {@link HttpStatusCode} to use.
	 * @param message The message to use.
	 */
	public HttpResponseException(HttpStatusCode status, String message) {
		super(message);
		mStatus = status;
	}

	/**
	 * @param status The {@link HttpStatusCode} to use.
	 * @param message The message to use.
	 * @param exception The exception to wrap.
	 */
	public HttpResponseException(HttpStatusCode status, String message, Exception exception) {
		super(message, exception);
		mStatus = status;
	}

	/** @return The {@link HttpStatusCode}. */
	public HttpStatusCode getStatus() {
		return mStatus;
	}
}
