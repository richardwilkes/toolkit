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

package com.trollworks.toolkit.ui.widget;

import java.awt.LayoutManager2;

import javax.swing.JPanel;

/** A wrapper panel which is initially transparent. */
public class Wrapper extends JPanel {
	/** Creates a new {@link Wrapper}. */
	public Wrapper() {
		super();
		setOpaque(false);
	}

	/**
	 * Creates a new {@link Wrapper}.
	 *
	 * @param layout The layout to use.
	 */
	public Wrapper(LayoutManager2 layout) {
		super(layout);
		setOpaque(false);
	}
}
