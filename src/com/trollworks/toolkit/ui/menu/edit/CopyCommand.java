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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.text.JTextComponent;

/** Provides the "Copy" command. */
public class CopyCommand extends Command {
	@Localize("Copy")
	private static String			COPY;

	static {
		Localization.initialize();
	}

	/** The action command this command will issue. */
	public static final String		CMD_COPY	= "Copy";				//$NON-NLS-1$

	/** The singleton {@link CopyCommand}. */
	public static final CopyCommand	INSTANCE	= new CopyCommand();

	private CopyCommand() {
		super(COPY, CMD_COPY, KeyEvent.VK_C);
	}

	@Override
	public void adjustForMenu(JMenuItem item) {
		boolean isEnabled = false;
		Component comp = getFocusOwner();
		if (comp instanceof JTextComponent) {
			JTextComponent textComp = (JTextComponent) comp;
			String text = textComp.getSelectedText();
			isEnabled = text != null && text.length() > 0;
		}
		setEnabled(isEnabled);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		((JTextComponent) getFocusOwner()).copy();
	}
}