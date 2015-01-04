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

import java.awt.GraphicsConfiguration;

/** A standard palette window. */
public abstract class PaletteWindow extends BaseWindow {
	private AppWindow	mAppWindow;

	/**
	 * Creates a new {@link PaletteWindow}.
	 *
	 * @param title The title of the window.
	 */
	public PaletteWindow(String title) {
		this(title, null);
	}

	/**
	 * Creates a new {@link PaletteWindow}.
	 *
	 * @param title The title of the window.
	 * @param gc The {@link GraphicsConfiguration} to use.
	 */
	public PaletteWindow(String title, GraphicsConfiguration gc) {
		super(title, gc);
		setFocusableWindowState(false);
		setAlwaysOnTop(true);
		getRootPane().putClientProperty("Window.style", "small"); //$NON-NLS-1$ //$NON-NLS-2$
		initialize();
		setAppWindow(AppWindow.getTopWindow());
		restoreBounds();
	}

	/**
	 * Called to initialize the {@link PaletteWindow}. At this point, no associated
	 * {@link AppWindow} will have been set. Immediately after this call completes, a call to
	 * {@link #adjustForAppWindow(AppWindow)} will be made.
	 */
	protected abstract void initialize();

	/** @return The {@link AppWindow} to work with. May be <code>null</code>. */
	public AppWindow getAppWindow() {
		return mAppWindow;
	}

	final void setAppWindow(AppWindow appWindow) {
		if (mAppWindow != appWindow) {
			mAppWindow = appWindow;
			adjustForAppWindow(mAppWindow);
		}
	}

	/**
	 * Called whenever the top-most {@link AppWindow} changes.
	 *
	 * @param window The new top-most {@link AppWindow}. May be <code>null</code>.
	 */
	protected abstract void adjustForAppWindow(AppWindow window);
}
