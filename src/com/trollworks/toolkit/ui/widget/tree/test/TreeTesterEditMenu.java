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

package com.trollworks.toolkit.ui.widget.tree.test;

import com.trollworks.toolkit.ui.menu.DynamicMenuEnabler;
import com.trollworks.toolkit.ui.menu.DynamicMenuItem;
import com.trollworks.toolkit.ui.menu.edit.CopyCommand;
import com.trollworks.toolkit.ui.menu.edit.CutCommand;
import com.trollworks.toolkit.ui.menu.edit.DeleteCommand;
import com.trollworks.toolkit.ui.menu.edit.DuplicateCommand;
import com.trollworks.toolkit.ui.menu.edit.PasteCommand;
import com.trollworks.toolkit.ui.menu.edit.PreferencesCommand;
import com.trollworks.toolkit.ui.menu.edit.RedoCommand;
import com.trollworks.toolkit.ui.menu.edit.SelectAllCommand;
import com.trollworks.toolkit.ui.menu.edit.UndoCommand;
import com.trollworks.toolkit.utility.Platform;

import javax.swing.JMenu;

/** */
public class TreeTesterEditMenu extends JMenu {
	/** Creates a new {@link TreeTesterEditMenu}. */
	public TreeTesterEditMenu() {
		super("Edit"); //$NON-NLS-1$
		add(new DynamicMenuItem(UndoCommand.INSTANCE));
		add(new DynamicMenuItem(RedoCommand.INSTANCE));
		addSeparator();
		add(new DynamicMenuItem(CutCommand.INSTANCE));
		add(new DynamicMenuItem(CopyCommand.INSTANCE));
		add(new DynamicMenuItem(PasteCommand.INSTANCE));
		add(new DynamicMenuItem(DuplicateCommand.INSTANCE));
		add(new DynamicMenuItem(DeleteCommand.INSTANCE));
		add(new DynamicMenuItem(SelectAllCommand.INSTANCE));
		if (!Platform.isMacintosh()) {
			addSeparator();
			add(new DynamicMenuItem(PreferencesCommand.INSTANCE));
		}
		DynamicMenuEnabler.add(this);
	}
}
