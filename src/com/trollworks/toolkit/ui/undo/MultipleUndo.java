/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.trollworks.toolkit.ui.undo;

import javax.swing.UIManager;
import javax.swing.undo.CompoundEdit;

/** Provides a convenient way to collect multiple undos into a single undo. */
public class MultipleUndo extends CompoundEdit {
	private static final String	SPACE		= " ";															//$NON-NLS-1$
	private static final String	REDO_PREFIX	= UIManager.getString("AbstractUndoableEdit.redoText") + SPACE; //$NON-NLS-1$
	private static final String	UNDO_PREFIX	= UIManager.getString("AbstractUndoableEdit.undoText") + SPACE; //$NON-NLS-1$
	private String				mName;

	/**
	 * Create a multiple undo edit.
	 *
	 * @param name The name of the undo edit.
	 */
	public MultipleUndo(String name) {
		super();
		mName = name;
	}

	@Override
	public String getPresentationName() {
		return mName;
	}

	@Override
	public String getRedoPresentationName() {
		return REDO_PREFIX + mName;
	}

	@Override
	public String getUndoPresentationName() {
		return UNDO_PREFIX + mName;
	}
}
