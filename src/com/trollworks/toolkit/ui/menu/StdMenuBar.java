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

import com.trollworks.toolkit.workarounds.GtkMenuWorkaround;

import java.util.TreeSet;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

/** The standard menu bar. */
public class StdMenuBar extends JMenuBar {
	private static MenuProvider[]	MENU_PROVIDERS;
	private static Command[]		COMMANDS;

	static {
		GtkMenuWorkaround.installGtkPopupBugWorkaround();
	}

	/**
	 * Call to configure the standard menu bar. Should be called once prior to actual use of this
	 * class.
	 *
	 * @param providers The {@link MenuProvider}s that will contribute to this menu bar.
	 */
	public static final void configure(MenuProvider... providers) {
		MENU_PROVIDERS = new MenuProvider[providers.length];
		System.arraycopy(providers, 0, MENU_PROVIDERS, 0, providers.length);
		TreeSet<Command> set = new TreeSet<>();
		for (MenuProvider provider : providers) {
			set.addAll(provider.getModifiableCommands());
		}
		COMMANDS = set.toArray(new Command[set.size()]);
	}

	/** @return The {@link Command}s that can have their accelerators modified. */
	public static final Command[] getCommands() {
		return COMMANDS;
	}

	/**
	 * @param bar The {@link JMenuBar} to search.
	 * @param name The name (as returned by {@link JMenu#getName()} to look for as a top-level
	 *            {@link JMenu}.
	 * @return The found {@link JMenu}, or <code>null</code>.
	 */
	public static JMenu findMenuByName(JMenuBar bar, String name) {
		if (bar != null) {
			int count = bar.getMenuCount();
			for (int i = 0; i < count; i++) {
				JMenu menu = bar.getMenu(i);
				if (name.equals(menu.getName())) {
					return menu;
				}
			}
		}
		return null;
	}

	/** Creates a new {@link StdMenuBar}. */
	public StdMenuBar() {
		for (MenuProvider provider : MENU_PROVIDERS) {
			add(provider.createMenu());
		}
	}
}
