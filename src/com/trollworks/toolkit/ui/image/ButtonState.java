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

package com.trollworks.toolkit.ui.image;

@SuppressWarnings("nls")
public enum ButtonState {
	NORMAL(""),
	PRESSED("_pressed"),
	OVER("_over"),
	OVER_SELECTED("_over_selected"),
	DISABLED("_disabled"),
	DISABLED_SELECTED("_disabled_selected"),
	SELECTED("_selected");

	private String	mSuffix;

	private ButtonState(String suffix) {
		mSuffix = suffix;
	}

	/**
	 * @param baseName The base name of the icon, without any suffixes applied.
	 * @return The full name of the icon, without an extension.
	 */
	public String getIconName(String baseName) {
		return baseName + mSuffix;
	}
}
