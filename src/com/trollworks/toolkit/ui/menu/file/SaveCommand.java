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
import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.utility.Localization;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JMenuItem;

/** Provides the "Save" command. */
public class SaveCommand extends Command {
	@Localize("Save")
	private static String			SAVE;

	static {
		Localization.initialize();
	}

	/** The action command this command will issue. */
	public static final String		CMD_SAVE	= "Save";				//$NON-NLS-1$

	/** The singleton {@link SaveCommand}. */
	public static final SaveCommand	INSTANCE	= new SaveCommand();

	private SaveCommand() {
		super(SAVE, CMD_SAVE, KeyEvent.VK_S);
	}

	@Override
	public void adjustForMenu(JMenuItem item) {
		Window window = getActiveWindow();
		if (window instanceof Saveable) {
			setEnabled(((Saveable) window).isModified());
		} else {
			setEnabled(false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		save((Saveable) getActiveWindow());
	}

	/**
	 * Allows the user to save the file.
	 *
	 * @param saveable The {@link Saveable} to work on.
	 * @return The file(s) actually written to.
	 */
	public static File[] save(Saveable saveable) {
		File file = saveable.getBackingFile();
		if (file != null) {
			File[] files = saveable.saveTo(file);
			for (File one : files) {
				RecentFilesMenu.addRecent(one);
			}
			return files;

		}
		return SaveAsCommand.saveAs(saveable);
	}
}
