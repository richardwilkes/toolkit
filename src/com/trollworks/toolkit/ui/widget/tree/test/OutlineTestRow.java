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
import com.trollworks.toolkit.ui.widget.outline.Column;
import com.trollworks.toolkit.ui.widget.outline.Row;

public class OutlineTestRow extends Row {
	private String	mName;

	public OutlineTestRow(String name) {
		mName = name;
	}

	public String getName() {
		return mName;
	}

	public String getSecond() {
		return Integer.toString(mName.length());
	}

	@SuppressWarnings("static-method")
	public StdImage getIcon() {
		return StdImage.MINI_WARNING;
	}

	@Override
	public Object getData(Column column) {
		switch (column.getID()) {
			case 0:
				return getName();
			default:
				return Integer.valueOf(mName.length());
		}
	}

	@Override
	public String getDataAsText(Column column) {
		switch (column.getID()) {
			case 0:
				return getName();
			default:
				return getSecond();
		}
	}

	@Override
	public void setData(Column column, Object data) {
		// Unused
	}
}
