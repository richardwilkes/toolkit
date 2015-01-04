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

package com.trollworks.toolkit.ui.menu.window;

import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.ui.widget.AppWindow;

import java.awt.event.ActionEvent;

/** A command that will switch to a specific window. */
public class SwitchToWindowCommand extends Command {
	private AppWindow	mWindow;

	/**
	 * Creates a new {@link SwitchToWindowCommand}.
	 *
	 * @param window The window to switch to.
	 */
	public SwitchToWindowCommand(AppWindow window) {
		super(window.getTitle(), "SwitchToWindow[" + window.getTitle() + "]", window.getMenuIcon()); //$NON-NLS-1$ //$NON-NLS-2$
		mWindow = window;
	}

	@Override
	public void adjust() {
		setMarked(getActiveWindow() == mWindow);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		mWindow.toFront();
	}
}
