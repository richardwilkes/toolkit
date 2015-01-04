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

import java.util.Set;

import javax.swing.JMenu;

/** Provides a single top-level menu for the menu bar. */
public interface MenuProvider {
	/** @return The set of {@link Command}s that can have their accelerators modified. */
	Set<Command> getModifiableCommands();

	/** @return A newly created menu. */
	JMenu createMenu();
}
