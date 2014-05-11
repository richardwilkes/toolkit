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

package com.trollworks.toolkit.ui.menu.help;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.App;
import com.trollworks.toolkit.ui.GraphicsUtilities;
import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.ui.widget.AppWindow;
import com.trollworks.toolkit.utility.BundleInfo;
import com.trollworks.toolkit.utility.Localization;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

/** Provides the "About" command. */
public class AboutCommand extends Command implements AboutHandler {
	@Localize("About {0}")
	private static String				ABOUT;

	static {
		Localization.initialize();
	}

	/** The action command this command will issue. */
	public static final String			CMD_ABOUT		= "About";				//$NON-NLS-1$
	/** The singleton {@link AboutCommand}. */
	public static final AboutCommand	INSTANCE		= new AboutCommand();
	static AppWindow					ABOUT_WINDOW	= null;

	private AboutCommand() {
		super(MessageFormat.format(ABOUT, BundleInfo.getDefault().getName()), CMD_ABOUT);
	}

	@Override
	public void adjustForMenu(JMenuItem item) {
		setEnabled(true);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		show();
	}

	@Override
	public void handleAbout(AboutEvent event) {
		show();
	}

	private void show() {
		if (ABOUT_WINDOW != null) {
			if (ABOUT_WINDOW.isDisplayable() && ABOUT_WINDOW.isVisible()) {
				ABOUT_WINDOW.toFront();
				return;
			}
		}

		JPanel aboutPanel = App.createAboutPanel();
		if (aboutPanel != null) {
			ABOUT_WINDOW = new AppWindow(getTitle());
			ABOUT_WINDOW.add(aboutPanel);
			ABOUT_WINDOW.setResizable(false);
			ABOUT_WINDOW.pack();
			Dimension size = ABOUT_WINDOW.getSize();
			Rectangle bounds = ABOUT_WINDOW.getGraphicsConfiguration().getBounds();
			ABOUT_WINDOW.setLocation((bounds.width - size.width) / 2, (bounds.height - size.height) / 3);
			GraphicsUtilities.forceOnScreen(ABOUT_WINDOW);
			ABOUT_WINDOW.setVisible(true);
			ABOUT_WINDOW.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent windowEvent) {
					ABOUT_WINDOW = null;
				}
			});
		}
	}
}
