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

import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.ui.widget.AppWindow;

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

/** A command that will switch to a specific window. */
public class SwitchToWindowCommand extends Command {
	private AppWindow	mWindow;

	/**
	 * Creates a new {@link SwitchToWindowCommand}.
	 *
	 * @param window The window to switch to.
	 */
	public SwitchToWindowCommand(AppWindow window) {
		super(window.getTitle(), "SwitchToWindow[" + window.getTitle() + "]", getWindowIcon(window)); //$NON-NLS-1$ //$NON-NLS-2$
		mWindow = window;
	}

	private static final ImageIcon getWindowIcon(AppWindow window) {
		BufferedImage icon = window.getMenuIcon();
		return icon != null ? new ImageIcon(icon) : null;
	}

	@Override
	public void adjustForMenu(JMenuItem item) {
		setMarked(getActiveWindow() == mWindow);
		updateMark(item);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		mWindow.toFront();
	}
}
