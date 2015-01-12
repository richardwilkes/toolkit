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

package com.trollworks.toolkit.ui.menu.file;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.ui.print.PrintManager;
import com.trollworks.toolkit.ui.widget.WindowUtils;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.PrintProxy;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/** Provides the "Page Setup..." command. */
public class PageSetupCommand extends Command {
	@Localize("Page Setup\u2026")
	@Localize(locale = "ru", value = "Настройка страницы\u2026")
	@Localize(locale = "de", value = "Seite einrichten\u2026")
	@Localize(locale = "es", value = "Configurar página")
	private static String					PAGE_SETUP;
	@Localize("There is no system printer available.")
	@Localize(locale = "ru", value = "Нет доступного системного принтера")
	@Localize(locale = "de", value = "Es wurde kein Standard-Drucker gefunden.")
	@Localize(locale = "es", value = "No hay impresora disponible en el sistema.")
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
	public void adjust() {
		setEnabled(getTarget(PrintProxy.class) != null);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		PrintProxy proxy = getTarget(PrintProxy.class);
		if (proxy != null) {
			PrintManager mgr = proxy.getPrintManager();
			if (mgr != null) {
				mgr.pageSetup(proxy);
			} else {
				WindowUtils.showError(UIUtilities.getComponentForDialog(proxy), NO_PRINTER_SELECTED);
			}
		}
	}
}
