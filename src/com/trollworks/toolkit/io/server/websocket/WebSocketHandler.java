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

package com.trollworks.toolkit.io.server.websocket;

/** Provides callbacks for handling a {@link WebSocket}. */
public interface WebSocketHandler {
	/**
	 * Called when a {@link WebSocket} is initially established.
	 *
	 * @param webSocket The {@link WebSocket} being established.
	 */
	void webSocketConnected(WebSocket webSocket);

	/**
	 * Called when text data has been received.
	 *
	 * @param webSocket The {@link WebSocket} the data was received from.
	 * @param data The text.
	 */
	void webSocketTextData(WebSocket webSocket, String data);

	/**
	 * Called when binary data has been received.
	 *
	 * @param webSocket The {@link WebSocket} the data was received from.
	 * @param data The data.
	 */
	void webSocketBinaryData(WebSocket webSocket, byte[] data);

	/**
	 * Called when the {@link WebSocket} is closed.
	 *
	 * @param webSocket The {@link WebSocket} being closed.
	 */
	void webSocketClosed(WebSocket webSocket);
}
