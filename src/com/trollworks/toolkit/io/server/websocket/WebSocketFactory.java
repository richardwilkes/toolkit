/*
 * Copyright (c) 1998-2017 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.io.server.websocket;

import com.trollworks.toolkit.io.server.http.Http;

/** A factory for creating {@link WebSocket}s from a {@link Http} connection. */
public interface WebSocketFactory {
    /**
     * @param http The {@link Http} to use.
     * @return The newly created {@link WebSocket}.
     */
    WebSocket createWebSocket(Http http);
}
