/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.trollworks.toolkit.ui.menu.file;

import com.apple.eawt.AppEvent.OpenFilesEvent;
import com.apple.eawt.OpenFilesHandler;
import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.ui.widget.AppWindow;
import com.trollworks.toolkit.ui.widget.StdFileDialog;
import com.trollworks.toolkit.utility.Debug;
import com.trollworks.toolkit.utility.Localization;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;

import javax.swing.JMenuItem;

/** Provides the "Open..." command. */
public class OpenCommand extends Command implements OpenFilesHandler {
	@Localize("Open\u2026")
	private static String			OPEN;

	static {
		Localization.initialize();
	}

	/** The action command this command will issue. */
	public static final String		CMD_OPEN	= "Open";				//$NON-NLS-1$

	/** The singleton {@link OpenCommand}. */
	public static final OpenCommand	INSTANCE	= new OpenCommand();

	private OpenCommand() {
		super(OPEN, CMD_OPEN, KeyEvent.VK_O);
	}

	@Override
	public void adjustForMenu(JMenuItem item) {
		// Do nothing. Always enabled.
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		open();
	}

	/** Ask the user to open a file. */
	public static void open() {
		Component focus = getFocusOwner();
		open(StdFileDialog.choose(focus, true, OPEN, null, null, FileType.getOpenableExtensions()));
	}

	/** @param file The file to open. */
	public static void open(File file) {
		if (file != null) {
			try {
				String name = file.getName();
				AppWindow window = AppWindow.findWindow(file);
				if (window == null) {
					for (FileType type : FileType.getOpenable()) {
						if (name.matches(StdFileDialog.createExtensionMatcher(type.getExtension()))) {
							Constructor<? extends AppWindow> constructor = type.getWindowClass().getConstructor(File.class);
							window = constructor.newInstance(file);
							window.setVisible(true);
							break;
						}
					}
				} else {
					window.toFront();
				}
				if (window != null) {
					RecentFilesMenu.addRecent(file);
				} else {
					throw new IOException("Unknown file extension"); //$NON-NLS-1$
				}
			} catch (Exception exception) {
				Debug.diagnoseLoadAndSave(exception);
				StdFileDialog.showCannotOpenMsg(getFocusOwner(), file.getName(), exception);
			}
		}
	}

	@Override
	public void openFiles(OpenFilesEvent event) {
		for (File file : event.getFiles()) {
			// We call this rather than directly to open(File) above to allow the file opening to be
			// deferred until startup has finished
			OpenDataFileCommand.open(file);
		}
	}
}
