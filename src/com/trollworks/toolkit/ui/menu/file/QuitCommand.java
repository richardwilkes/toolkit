/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.trollworks.toolkit.ui.menu.file;

import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.Fonts;
import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.ui.preferences.Preferences;
import com.trollworks.toolkit.ui.widget.BaseWindow;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.Platform;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;

/** Provides the "Quit"/"Exit" command. */
public class QuitCommand extends Command implements QuitHandler {
	@Localize("Quit")
	private static String			QUIT;
	@Localize("Exit")
	private static String			EXIT;

	static {
		Localization.initialize();
	}

	/** The action command this command will issue. */
	public static final String		CMD_QUIT	= "Quit";				//$NON-NLS-1$

	/** The singleton {@link QuitCommand}. */
	public static final QuitCommand	INSTANCE	= new QuitCommand();

	private QuitCommand() {
		super(Platform.isMacintosh() ? QUIT : EXIT, CMD_QUIT, KeyEvent.VK_Q);
	}

	@Override
	public void adjustForMenu(JMenuItem item) {
		for (Frame frame : Frame.getFrames()) {
			if (frame.isVisible() && BaseWindow.hasOwnedWindowsShowing(frame)) {
				setEnabled(false);
				return;
			}
		}
		setEnabled(true);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (attemptQuit()) {
			System.exit(0);
		}
	}

	/**
	 * Attempts to quit.
	 *
	 * @result <code>true</code> if the quit was successful and the caller should call
	 *         {@link System#exit(int)}.
	 */
	public boolean attemptQuit() {
		if (isEnabled()) {
			if (closeFrames(true)) {
				if (closeFrames(false)) {
					try {
						RecentFilesMenu.saveToPreferences();
						Fonts.saveToPreferences();
						Preferences.getInstance().save();
					} catch (Exception exception) {
						// Ignore, since preferences may not have been initialized...
					}
					return true;
				}
			}
		}
		return false;
	}

	private static boolean closeFrames(boolean significant) {
		for (Frame frame : Frame.getFrames()) {
			if (frame instanceof SignificantFrame == significant && frame.isVisible()) {
				try {
					if (!CloseCommand.close(frame, false)) {
						return false;
					}
				} catch (Exception exception) {
					exception.printStackTrace(System.err);
				}
			}
		}
		return true;
	}

	@Override
	public void handleQuitRequestWith(QuitEvent event, QuitResponse response) {
		if (attemptQuit()) {
			response.performQuit();
		} else {
			response.cancelQuit();
		}
	}
}
