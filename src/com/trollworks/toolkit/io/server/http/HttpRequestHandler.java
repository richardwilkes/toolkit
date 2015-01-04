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

/** Defines the method required to handle a HTTP request. */
public interface HttpRequestHandler {
	/**
	 * @param http The {@link Http} object the request came from.
	 * @return The {@link HttpResponse} to return to the remote end.
	 */
	HttpResponse handleHttpRequest(Http http) throws IOException;
}
