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

package com.trollworks.toolkit.ui.widget.treetable;

/**
 * Objects that want to be notified when a {@link TreeTableModel} is modified must implement this
 * interface.
 */
public interface TreeTableModelListener {
	public static final int	FLAG_STRUCTURE_MODIFIED	= 1 << 0;
	public static final int	FLAG_CONTENT_MODIFIED	= 1 << 1;

	/**
	 * Called when the {@link TreeTableModel} has been modified.
	 *
	 * @param flags An or'd set of flags that provide a hint as to what has changed.
	 */
	void modelWasUpdated(int flags);
}
