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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.undo.UndoManager;

/** Provides the "Undo" command. */
public class UndoCommand extends Command {
	@Localize("Can't Undo")
	private static String			CANT_UNDO;

	static {
		Localization.initialize();
	}

	/** The action command this command will issue. */
	public static final String		CMD_UNDO	= "Undo";				//$NON-NLS-1$

	/** The singleton {@link UndoCommand}. */
	public static final UndoCommand	INSTANCE	= new UndoCommand();

	private UndoCommand() {
		super(CANT_UNDO, CMD_UNDO, KeyEvent.VK_Z);
	}

	@Override
	public void adjust() {
		Window window = getActiveWindow();
		if (window instanceof Undoable) {
			UndoManager mgr = ((Undoable) window).getUndoManager();
			setEnabled(mgr.canUndo());
			setTitle(mgr.getUndoPresentationName());
		} else {
			setEnabled(false);
			setTitle(CANT_UNDO);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		((Undoable) getActiveWindow()).getUndoManager().undo();
	}
}
