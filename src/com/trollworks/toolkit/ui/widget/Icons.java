/*
 * Copyright (c) 1998-2016 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui.widget;

import com.trollworks.toolkit.ui.RetinaIcon;

/** Commonly used icons. */
public class Icons {
	/**
	 * @param open <code>true</code> for the 'open' version.
	 * @param roll <code>true</code> for the highlighted version.
	 */
	public static RetinaIcon getDisclosure(boolean open, boolean roll) {
		if (open) {
			return new RetinaIcon(roll ? "disclosure_down_roll" : "disclosure_down"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return new RetinaIcon(roll ? "disclosure_right_roll" : "disclosure_right"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
