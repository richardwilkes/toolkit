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

package com.trollworks.toolkit.ui.menu.edit;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.utility.Localization;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;

/** Provides the "Open Item" command. */
public class OpenItemCommand extends Command {
	@Localize("Open Item")
	private static String				OPEN_ITEM;

	static {
		Localization.initialize();
	}

	/** The action command this command will issue. */
	public static final String			CMD_OPEN_ITEM	= "Open Item";				//$NON-NLS-1$

	/** The singleton {@link OpenItemCommand}. */
	public static final OpenItemCommand	INSTANCE		= new OpenItemCommand();

	private OpenItemCommand() {
		super(OPEN_ITEM, CMD_OPEN_ITEM, KeyEvent.VK_ENTER);
	}

	@Override
	public void adjustForMenu(JMenuItem item) {
		boolean isEnabled = false;
		boolean checkWindow = false;
		Component comp = getFocusOwner();
		if (comp != null && comp.isEnabled()) {
			if (comp instanceof Openable) {
				isEnabled = ((Openable) comp).canOpenSelection();
			} else {
				checkWindow = true;
			}
		} else {
			checkWindow = true;
		}
		if (checkWindow) {
			Window window = getActiveWindow();
			if (window instanceof Openable) {
				isEnabled = ((Openable) window).canOpenSelection();
			}
		}
		setEnabled(isEnabled);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		boolean checkWindow = false;
		Component comp = getFocusOwner();
		if (comp.isEnabled()) {
			if (comp instanceof Openable) {
				((Openable) comp).openSelection();
			} else {
				checkWindow = true;
			}
		} else {
			checkWindow = true;
		}
		if (checkWindow) {
			Window window = getActiveWindow();
			if (window instanceof Openable) {
				((Openable) window).openSelection();
			}
		}
	}
}
