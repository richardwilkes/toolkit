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

package com.trollworks.toolkit.ui.menu;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

/** Utilities for dealing with menu bars. */
public class MenuBarUtilities {
	/**
	 * @param bar The {@link JMenuBar} to search.
	 * @param type The {@link Class} to look for as a top-level {@link JMenu}.
	 * @return The found {@link JMenu}, or <code>null</code>.
	 */
	public static JMenu findMenu(JMenuBar bar, Class<? extends JMenu> type) {
		if (bar != null) {
			int count = bar.getMenuCount();
			for (int i = 0; i < count; i++) {
				JMenu menu = bar.getMenu(i);
				if (type.isInstance(menu)) {
					return menu;
				}
			}
		}
		return null;
	}
}
