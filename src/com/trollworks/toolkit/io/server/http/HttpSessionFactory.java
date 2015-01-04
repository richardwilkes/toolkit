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
import com.trollworks.toolkit.io.server.websocket.WebSocketFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Provides a {@link SessionFactory} for HTTP sessions. */
public class HttpSessionFactory implements SessionFactory {
	private Path							mRootPath;
	private Map<String, HttpRequestHandler>	mHttpHandlers		= new HashMap<>();
	private Map<String, WebSocketFactory>	mWebSocketFactories	= new HashMap<>();

	/** @param rootPath The path to the root web directory. */
	public HttpSessionFactory(Path rootPath) {
		mRootPath = rootPath.toAbsolutePath().normalize();
	}

	/** @return The path to the root web directory. */
	public final Path getRootPath() {
		return mRootPath;
	}

	/**
	 * @param uri The URI to register a {@link HttpRequestHandler} for.
	 * @param handler The {@link HttpRequestHandler} to use for the specified URI.
	 * @return Any existing {@link HttpRequestHandler} for the specified URI, or <code>null</code>
	 *         if there was none.
	 */
	public final synchronized HttpRequestHandler registerHttpHandler(String uri, HttpRequestHandler handler) {
		return mHttpHandlers.put(uri, handler);
	}

	/**
	 * @param uri The URI to get a {@link HttpRequestHandler} for.
	 * @return The {@link HttpRequestHandler} for the specified URI, or <code>null</code> if there
	 *         is none.
	 */
	public final synchronized HttpRequestHandler getHttpHandler(String uri) {
		return mHttpHandlers.get(uri);
	}

	/**
	 * @param uri The URI to register a {@link WebSocketFactory} for.
	 * @param handler The {@link WebSocketFactory} to use for the specified URI.
	 * @return Any existing {@link WebSocketFactory} for the specified URI, or <code>null</code> if
	 *         there was none.
	 */
	public final synchronized WebSocketFactory registerWebSocketFactory(String uri, WebSocketFactory handler) {
		return mWebSocketFactories.put(uri, handler);
	}

	/**
	 * @param uri The URI to get a {@link WebSocketFactory} for.
	 * @return The {@link WebSocketFactory} for the specified URI, or <code>null</code> if there is
	 *         none.
	 */
	public final synchronized WebSocketFactory getWebSocketFactory(String uri) {
		return mWebSocketFactories.get(uri);
	}

	@Override
	public Session createSession(NioServer server, SocketChannel channel) throws IOException {
		return new Session(server, channel, null, new Http(this));
	}
}
