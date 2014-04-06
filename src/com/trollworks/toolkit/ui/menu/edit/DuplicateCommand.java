/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
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

/** Provides the "Duplicate" command. */
public class DuplicateCommand extends Command {
	@Localize("Duplicate")
	private static String					DUPLICATE;

	static {
		Localization.initialize();
	}

	/** The action command this command will issue. */
	public static final String				CMD_DUPLICATE	= "Duplicate";				//$NON-NLS-1$
	/** The singleton {@link DuplicateCommand}. */
	public static final DuplicateCommand	INSTANCE		= new DuplicateCommand();

	private DuplicateCommand() {
		super(DUPLICATE, CMD_DUPLICATE, KeyEvent.VK_D);
	}

	@Override
	public void adjustForMenu(JMenuItem item) {
		Duplicatable focus = findFocus();
		setEnabled(focus != null ? focus.canDuplicateSelection() : false);
	}

	private static Duplicatable findFocus() {
		Component comp = getFocusOwner();
		while (comp != null && comp.isEnabled()) {
			if (comp instanceof Duplicatable) {
				return (Duplicatable) comp;
			}
			comp = comp.getParent();
		}
		Window window = getActiveWindow();
		if (window instanceof Duplicatable) {
			return (Duplicatable) window;
		}
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Duplicatable focus = findFocus();
		if (focus != null) {
			focus.duplicateSelection();
		}
	}
}