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

package com.trollworks.toolkit.ui.widget.tree.test;

import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.ui.menu.DynamicMenuEnabler;
import com.trollworks.toolkit.ui.menu.DynamicMenuItem;
import com.trollworks.toolkit.ui.menu.MenuProvider;
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

import java.util.Collections;
import java.util.Set;

import javax.swing.JMenu;

public class TreeTesterEditMenuProvider implements MenuProvider {
	@Override
	public Set<Command> getModifiableCommands() {
		return Collections.emptySet();
	}

	@Override
	public JMenu createMenu() {
		JMenu menu = new JMenu("Edit"); //$NON-NLS-1$
		menu.add(new DynamicMenuItem(UndoCommand.INSTANCE));
		menu.add(new DynamicMenuItem(RedoCommand.INSTANCE));
		menu.addSeparator();
		menu.add(new DynamicMenuItem(CutCommand.INSTANCE));
		menu.add(new DynamicMenuItem(CopyCommand.INSTANCE));
		menu.add(new DynamicMenuItem(PasteCommand.INSTANCE));
		menu.add(new DynamicMenuItem(DuplicateCommand.INSTANCE));
		menu.add(new DynamicMenuItem(DeleteCommand.INSTANCE));
		menu.add(new DynamicMenuItem(SelectAllCommand.INSTANCE));
		if (!Platform.isMacintosh()) {
			menu.addSeparator();
			menu.add(new DynamicMenuItem(PreferencesCommand.INSTANCE));
		}
		DynamicMenuEnabler.add(menu);
		return menu;
	}
}
