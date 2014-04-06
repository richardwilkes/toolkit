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

package com.trollworks.toolkit.ui.menu;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

/** The standard menu bar. */
public class StdMenuBar extends JMenuBar {
	private static Class<?>[]	MENU_CLASSES;
	private static Command[]	COMMANDS;

	/**
	 * Call to configure the standard menu bar. Should be called prior to actual use of this class.
	 *
	 * @param menuClasses The {@link JMenu} classes that will contribute to this menu bar.
	 */
	@SuppressWarnings("unchecked")
	public static final void configure(Class<? extends JMenu>... menuClasses) {
		MENU_CLASSES = new Class<?>[menuClasses.length];
		System.arraycopy(menuClasses, 0, MENU_CLASSES, 0, menuClasses.length);
		TreeSet<Command> set = new TreeSet<>();
		for (Class<?> one : MENU_CLASSES) {
			try {
				Method method = one.getMethod("getCommands"); //$NON-NLS-1$
				int modifiers = method.getModifiers();
				if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers) && Set.class.isAssignableFrom(method.getReturnType())) {
					for (Object cmd : (Set<Object>) method.invoke((Object) null, (Object[]) null)) {
						if (cmd instanceof Command) {
							set.add((Command) cmd);
						}
					}
				}
			} catch (Exception exception) {
				// Ignore
			}
		}
		COMMANDS = set.toArray(new Command[set.size()]);
	}

	/** @return The {@link Command}s that can have their accelerators modified. */
	public static final Command[] getCommands() {
		return COMMANDS;
	}

	/** Creates a new {@link StdMenuBar}. */
	public StdMenuBar() {
		for (Class<?> menuClass : MENU_CLASSES) {
			try {
				add((JMenu) menuClass.newInstance());
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

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
