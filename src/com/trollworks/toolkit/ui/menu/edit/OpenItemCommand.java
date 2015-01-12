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

package com.trollworks.toolkit.ui.menu.edit;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.utility.Localization;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/** Provides the "Open Item" command. */
public class OpenItemCommand extends Command {
	@Localize("Open Item")
	@Localize(locale = "ru", value = "Открыть элемент")
	@Localize(locale = "de", value = "Element öffnen")
	@Localize(locale = "es", value = "Abrir elemento")
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
	public void adjust() {
		boolean enable = false;
		Openable openable = getTarget(Openable.class);
		if (openable != null) {
			enable = openable.canOpenSelection();
		}
		setEnabled(enable);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Openable openable = getTarget(Openable.class);
		if (openable != null) {
			openable.openSelection();
		}
	}
}
