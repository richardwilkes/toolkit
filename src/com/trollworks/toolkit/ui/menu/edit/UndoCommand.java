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

import javax.swing.undo.UndoManager;

/** Provides the "Undo" command. */
public class UndoCommand extends Command {
	@Localize("Can't Undo")
	@Localize(locale = "ru", value = "Нельзя отменить")
	@Localize(locale = "de", value = "Kann nicht Rückgängig machen")
	@Localize(locale = "es", value = "No se puede deshacer")
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
		Undoable undoable = getTarget(Undoable.class);
		if (undoable != null) {
			UndoManager mgr = undoable.getUndoManager();
			setEnabled(mgr.canUndo());
			setTitle(mgr.getUndoPresentationName());
		} else {
			setEnabled(false);
			setTitle(CANT_UNDO);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Undoable undoable = getTarget(Undoable.class);
		if (undoable != null) {
			undoable.getUndoManager().undo();
		}
	}
}
