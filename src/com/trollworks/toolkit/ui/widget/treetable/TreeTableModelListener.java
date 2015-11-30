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
	/** Called when the {@link TreeTableModel} has been modified. */
	void modelWasUpdated();
}
