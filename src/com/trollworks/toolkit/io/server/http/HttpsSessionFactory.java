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

import com.trollworks.toolkit.io.server.NioServer;
import com.trollworks.toolkit.io.server.Session;
import com.trollworks.toolkit.io.server.SessionFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/** Provides a {@link SessionFactory} for HTTPS sessions. */
public class HttpsSessionFactory implements SessionFactory {
	private HttpSessionFactory	mHttpSessionFactory;

	/** @param httpSessionFactory The {@link HttpSessionFactory} to delegate unencrypted data to. */
	public HttpsSessionFactory(HttpSessionFactory httpSessionFactory) {
		mHttpSessionFactory = httpSessionFactory;
	}

	@Override
	public Session createSession(NioServer server, SocketChannel channel) throws IOException {
		return new Session(server, channel, server.getSSLContext(), new Http(mHttpSessionFactory));
	}
}
