/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.trollworks.toolkit.ui.menu.file;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.ui.print.PrintManager;
import com.trollworks.toolkit.ui.widget.AppWindow;
import com.trollworks.toolkit.ui.widget.WindowUtils;
import com.trollworks.toolkit.utility.Localization;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.Printable;

import javax.swing.JMenuItem;

/** Provides the "Page Setup..." command. */
public class PageSetupCommand extends Command {
	@Localize("Page Setup\u2026")
	private static String					PAGE_SETUP;
	@Localize("There is no system printer available.")
	private static String					NO_PRINTER_SELECTED;

	static {
		Localization.initialize();
	}

	/** The action command this command will issue. */
	public static final String				CMD_PAGE_SETUP	= "PageSetup";				//$NON-NLS-1$

	/** The singleton {@link PageSetupCommand}. */
	public static final PageSetupCommand	INSTANCE		= new PageSetupCommand();

	private PageSetupCommand() {
		super(PAGE_SETUP, CMD_PAGE_SETUP, KeyEvent.VK_P, SHIFTED_COMMAND_MODIFIER);
	}

	@Override
	public void adjustForMenu(JMenuItem item) {
		Window window = getActiveWindow();
		setEnabled(window instanceof AppWindow && window instanceof Printable);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		AppWindow window = (AppWindow) getActiveWindow();
		PrintManager mgr = window.getPrintManager();
		if (mgr != null) {
			mgr.pageSetup(window);
		} else {
			WindowUtils.showError(window, NO_PRINTER_SELECTED);
		}
	}
}
