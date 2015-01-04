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

/** The supported HTTP request methods. */
public enum HttpMethod {
	GET,
	POST,
	HEAD;

	/**
	 * @param text The text representation of the HTTP request method.
	 * @return The {@link HttpMethod}, or <code>null</code> if no match can be found.
	 */
	static HttpMethod lookup(String text) {
		for (HttpMethod method : HttpMethod.values()) {
			if (method.toString().equalsIgnoreCase(text)) {
				return method;
			}
		}
		return null;
	}
}
