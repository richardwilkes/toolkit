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

package com.trollworks.toolkit.ui.menu.window;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.menu.DynamicMenuEnabler;
import com.trollworks.toolkit.ui.menu.StdMenuBar;
import com.trollworks.toolkit.ui.widget.AppWindow;
import com.trollworks.toolkit.utility.Localization;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

/** The standard "Window" menu. */
public class WindowMenu extends JMenu {
	@Localize("Window")
	private static String	WINDOW;

	static {
		Localization.initialize();
	}

	/** Creates a new {@link WindowMenu}. */
	public WindowMenu() {
		super(WINDOW);
		DynamicMenuEnabler.add(this);
	}

	/** Updates the available menu items. */
	public static void update() {
		ArrayList<AppWindow> windows = AppWindow.getAllWindows();
		Collections.sort(windows);
		for (AppWindow window : windows) {
			WindowMenu windowMenu = (WindowMenu) StdMenuBar.findMenu(window.getJMenuBar(), WindowMenu.class);
			if (windowMenu != null) {
				windowMenu.removeAll();
				for (AppWindow one : windows) {
					windowMenu.add(new JCheckBoxMenuItem(new SwitchToWindowCommand(one)));
				}
			}
		}
	}
}
