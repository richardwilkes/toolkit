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

package com.trollworks.toolkit.utility.undo;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.utility.Localization;

import javax.swing.undo.CompoundEdit;

/** Provides a convenient way to collect multiple undos into a single undo. */
public class MultipleUndo extends CompoundEdit {
	@Localize("Redo ")
	@Localize(locale = "ru", value = "Повторить ")
	@Localize(locale = "de", value = "Wiederherstellen: ")
	@Localize(locale = "es", value = "Rehacer ")
	private static String	REDO_PREFIX;
	@Localize("Undo ")
	@Localize(locale = "ru", value = "Отменить ")
	@Localize(locale = "de", value = "Rückgängig: ")
	@Localize(locale = "es", value = "Deshacer ")
	private static String	UNDO_PREFIX;

	static {
		Localization.initialize();
	}

	private String			mName;

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
