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
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.text.JTextComponent;

/** Provides the "Paste" command. */
public class PasteCommand extends Command {
	@Localize("Paste")
	private static String				PASTE;

	static {
		Localization.initialize();
	}

	/** The action command this command will issue. */
	public static final String			CMD_PASTE	= "Paste";				//$NON-NLS-1$

	/** The singleton {@link PasteCommand}. */
	public static final PasteCommand	INSTANCE	= new PasteCommand();

	private PasteCommand() {
		super(PASTE, CMD_PASTE, KeyEvent.VK_V);
	}

	@Override
	public void adjustForMenu(JMenuItem item) {
		boolean isEnabled = false;
		Component comp = getFocusOwner();
		if (comp instanceof JTextComponent && comp.isEnabled()) {
			JTextComponent textComp = (JTextComponent) comp;
			if (textComp.isEditable()) {
				try {
					isEnabled = comp.getToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.stringFlavor);
				} catch (Exception exception) {
					// Ignore.
				}
			}
		}
		setEnabled(isEnabled);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		((JTextComponent) getFocusOwner()).paste();
	}
}
