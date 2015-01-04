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
import java.nio.channels.SocketChannel;

/** Used to create a {@link Session} for in-bound connections. */
public interface SessionFactory {
	/**
	 * @param server The {@link NioServer} that will be providing the connection.
	 * @param channel The {@link SocketChannel} that was connected.
	 * @return The newly created {@link Session}.
	 */
	Session createSession(NioServer server, SocketChannel channel) throws IOException;
}
