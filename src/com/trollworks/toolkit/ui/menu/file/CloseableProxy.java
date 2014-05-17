/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui.menu.file;

import java.awt.Window;

/**
 * Objects (such as {@link Window}s) that want to control how or if they are closed may implement
 * this interface to override the standard behavior of the {@link CloseCommand}.
 */
public interface CloseableProxy {
	/** @return <code>true</code> if {@link #attemptClose()} may be called. */
	boolean mayAttemptClose();

	/**
	 * Called to try and close the specified object. Note that if the object implements this
	 * interface, any standard closing action will not be attempted, regardless of what this method
	 * actually does.
	 */
	void attemptClose();
}
