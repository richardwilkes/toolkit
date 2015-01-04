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

@SuppressWarnings("nls")
/** Stores a HTTP status code. */
public enum HttpStatusCode {
	SWITCHING_PROTOCOLS(101, "Switching Protocols"),
	OK(200, "OK"),
	CREATED(201, "Created"),
	ACCEPTED(202, "Accepted"),
	NO_CONTENT(204, "No Content"),
	PARTIAL_CONTENT(206, "Partial Content"),
	REDIRECT(301, "Moved Permanently"),
	NOT_MODIFIED(304, "Not Modified"),
	BAD_REQUEST(400, "Bad Request"),
	UNAUTHORIZED(401, "Unauthorized"),
	FORBIDDEN(403, "Forbidden"),
	NOT_FOUND(404, "Not Found"),
	ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
	RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
	INTERNAL_ERROR(500, "Internal Server Error");

	private final int		mRequestStatus;
	private final String	mDescription;

	private HttpStatusCode(int requestStatus, String description) {
		mRequestStatus = requestStatus;
		mDescription = description;
	}

	/** @return The integer value of the status code. */
	public final int getRequestStatus() {
		return mRequestStatus;
	}

	/** @return A text description of the status code. */
	public final String getDescription() {
		return mRequestStatus + " " + mDescription;
	}
}
