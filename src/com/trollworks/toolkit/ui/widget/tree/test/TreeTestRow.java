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

package com.trollworks.toolkit.ui.widget.tree.test;

import com.trollworks.toolkit.ui.image.StdImage;
import com.trollworks.toolkit.ui.widget.tree.TreeRow;

public class TreeTestRow extends TreeRow implements TreeRowWithName, TreeRowWithIcon {
	private String	mName;

	public TreeTestRow(String name) {
		mName = name;
	}

	@Override
	public String getName() {
		return mName;
	}

	@Override
	public String getSecond() {
		return Integer.toString(mName.length());
	}

	@Override
	public StdImage getIcon() {
		return StdImage.MINI_WARNING;
	}

	public static String getName(TreeRow row) {
		if (row instanceof TreeRowWithName) {
			return ((TreeRowWithName) row).getName();
		}
		return null;
	}

	public static String getSecond(TreeRow row) {
		if (row instanceof TreeRowWithName) {
			return ((TreeRowWithName) row).getSecond();
		}
		return null;
	}

	public static StdImage getIcon(TreeRow row) {
		if (row instanceof TreeRowWithIcon) {
			return ((TreeRowWithIcon) row).getIcon();
		}
		return null;
	}
}
