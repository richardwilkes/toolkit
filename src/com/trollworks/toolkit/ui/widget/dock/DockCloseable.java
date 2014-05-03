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

package com.trollworks.toolkit.ui.widget.dock;

/** {@link Dockable}s that wish to be closeable must implement this interface. */
public interface DockCloseable {
	/**
	 * Called when the user has asked for this {@link Dockable} to close.
	 *
	 * @return <code>true</code> if the {@link Dockable} should be removed from the {@link Dock}.
	 */
	boolean attemptClose();
}
