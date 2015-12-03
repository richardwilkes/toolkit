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

package com.trollworks.toolkit.utility.selection;


/**
 * Objects that want to be notified when a {@link SelectionModel} is modified must implement this
 * interface.
 */
public interface SelectionModelListener {
	/** Called when the {@link SelectionModel} changes. */
	void selectionChanged();
}
