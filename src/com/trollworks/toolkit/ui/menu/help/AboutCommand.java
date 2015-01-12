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

package com.trollworks.toolkit.ui.menu.help;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.App;
import com.trollworks.toolkit.ui.UIUtilities;
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

import javax.swing.JPanel;

/** Provides the "About" command. */
public class AboutCommand extends Command implements AboutHandler {
	@Localize("About {0}")
	@Localize(locale = "ru", value = "О программе {0}")
	@Localize(locale = "de", value = "Über {0}")
	@Localize(locale = "es", value = "Acerca de {0}")
	private static String				ABOUT;

	static {
		Localization.initialize();
	}

	/** The action command this command will issue. */
	public static final String			CMD_ABOUT	= "About";				//$NON-NLS-1$
	/** The singleton {@link AboutCommand}. */
	public static final AboutCommand	INSTANCE	= new AboutCommand();
	AppWindow							mWindow		= null;

	private AboutCommand() {
		super(MessageFormat.format(ABOUT, BundleInfo.getDefault().getName()), CMD_ABOUT);
	}

	@Override
	public void adjust() {
		setEnabled(!UIUtilities.inModalState());
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
		if (!UIUtilities.inModalState()) {
			if (mWindow != null) {
				if (mWindow.isDisplayable() && mWindow.isVisible()) {
					mWindow.toFront();
					return;
				}
			}
			JPanel aboutPanel = App.createAboutPanel();
			if (aboutPanel != null) {
				mWindow = new AppWindow(getTitle());
				mWindow.getContentPane().add(aboutPanel);
				mWindow.pack();
				mWindow.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent windowEvent) {
						mWindow = null;
					}
				});
				Rectangle bounds = mWindow.getGraphicsConfiguration().getBounds();
				Dimension size = mWindow.getSize();
				mWindow.setLocation(bounds.x + (bounds.width - size.width) / 2, bounds.y + (bounds.height - size.height) / 3);
				mWindow.setVisible(true);
				mWindow.setResizable(false);
			}
		}
	}
}
