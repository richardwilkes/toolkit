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

package com.trollworks.toolkit.ui.menu.file;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.ui.widget.BaseWindow;
import com.trollworks.toolkit.utility.Localization;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/** Provides the "Close" command. */
public class CloseCommand extends Command {
	@Localize("Close")
	private static String				CLOSE;
	@Localize("Save")
	private static String				SAVE;
	@Localize("Save changes to \"{0}\"?")
	private static String				SAVE_CHANGES;

	static {
		Localization.initialize();
	}

	/** The action command this command will issue. */
	public static final String			CMD_CLOSE	= "Close";				//$NON-NLS-1$

	/** The singleton {@link CloseCommand}. */
	public static final CloseCommand	INSTANCE	= new CloseCommand();

	private CloseCommand() {
		super(CLOSE, CMD_CLOSE, KeyEvent.VK_W);
	}

	@Override
	public void adjustForMenu(JMenuItem item) {
		// Do nothing. Always enabled.
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		close(getActiveWindow(), true);
	}

	/**
	 * @param window The {@link Window} to close.
	 * @param quitIfLast Call {@link QuitCommand#attemptQuit()} if no windows are open when this
	 *            method completes.
	 * @return <code>true</code> if the {@link Window} was closed.
	 */
	public static boolean close(Window window, boolean quitIfLast) {
		if (window != null && !BaseWindow.hasOwnedWindowsShowing(window)) {
			if (window instanceof Saveable) {
				UIUtilities.forceFocusToAccept();
				Saveable saveable = (Saveable) window;
				if (saveable.isModified()) {
					int answer = JOptionPane.showConfirmDialog(window, MessageFormat.format(SAVE_CHANGES, ((Frame) window).getTitle()), SAVE, JOptionPane.YES_NO_CANCEL_OPTION);
					if (answer == JOptionPane.CANCEL_OPTION || answer == JOptionPane.CLOSED_OPTION) {
						return false;
					}
					if (answer == JOptionPane.YES_OPTION) {
						SaveCommand.save(saveable);
						if (saveable.isModified()) {
							return false;
						}
					}
				}
			}
			window.dispose();
		}
		if (quitIfLast) {
			for (Frame frame : Frame.getFrames()) {
				if (frame instanceof SignificantFrame) {
					if (frame.isVisible() || BaseWindow.hasOwnedWindowsShowing(frame)) {
						return true;
					}
				}
			}
			QuitCommand.INSTANCE.adjustForMenu(null);
			if (QuitCommand.INSTANCE.attemptQuit()) {
				System.exit(0);
			}
		}
		return true;
	}
}
