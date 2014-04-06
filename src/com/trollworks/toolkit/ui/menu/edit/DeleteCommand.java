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
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

/** Provides the "Delete" command. */
public class DeleteCommand extends Command {
	@Localize("Delete")
	private static String				DELETE;

	static {
		Localization.initialize();
	}
	/** The action command this command will issue. */
	public static final String			CMD_DELETE	= "Delete";			//$NON-NLS-1$

	/** The singleton {@link DeleteCommand}. */
	public static final DeleteCommand	INSTANCE	= new DeleteCommand();

	private DeleteCommand() {
		super(DELETE, CMD_DELETE, KeyEvent.VK_DELETE);
	}

	@Override
	public void adjustForMenu(JMenuItem item) {
		boolean isEnabled = false;
		boolean checkWindow = false;
		Component comp = getFocusOwner();
		if (comp != null && comp.isEnabled()) {
			if (comp instanceof JTextComponent) {
				JTextComponent textComp = (JTextComponent) comp;
				if (textComp.isEditable()) {
					isEnabled = textComp.getDocument().getLength() > 0;
				}
			} else if (comp instanceof Deletable) {
				isEnabled = ((Deletable) comp).canDeleteSelection();
			} else {
				checkWindow = true;
			}
		} else {
			checkWindow = true;
		}
		if (checkWindow) {
			Window window = getActiveWindow();
			if (window instanceof Deletable) {
				isEnabled = ((Deletable) window).canDeleteSelection();
			}
		}
		setEnabled(isEnabled);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		boolean checkWindow = false;
		Component comp = getFocusOwner();
		if (comp.isEnabled()) {
			if (comp instanceof JTextComponent) {
				JTextComponent textComp = (JTextComponent) comp;
				ActionListener listener = textComp.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
				if (listener != null) {
					listener.actionPerformed(event);
				}
			} else if (comp instanceof Deletable) {
				((Deletable) comp).deleteSelection();
			} else {
				checkWindow = true;
			}
		} else {
			checkWindow = true;
		}
		if (checkWindow) {
			Window window = getActiveWindow();
			if (window instanceof Deletable) {
				((Deletable) window).deleteSelection();
			}
		}
	}
}
