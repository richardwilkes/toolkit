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

import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.PreferencesHandler;
import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.ui.preferences.PreferencesWindow;
import com.trollworks.toolkit.utility.Localization;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/** Provides the "Preferences..." command. */
public class PreferencesCommand extends Command implements PreferencesHandler {
	@Localize("Preferences\u2026")
	@Localize(locale = "ru", value = "Настройки\u2026")
	@Localize(locale = "de", value = "Einstellungen\u2026")
	@Localize(locale = "es", value = "Preferencias\u2026")
	private static String					PREFERENCES;

	static {
		Localization.initialize();
	}

	/** The action command this command will issue. */
	public static final String				CMD_PREFERENCES	= "Preferences";			//$NON-NLS-1$

	/** The singleton {@link PreferencesCommand}. */
	public static final PreferencesCommand	INSTANCE		= new PreferencesCommand();

	private PreferencesCommand() {
		super(PREFERENCES, CMD_PREFERENCES, KeyEvent.VK_COMMA);
	}

	@Override
	public void adjust() {
		setEnabled(!UIUtilities.inModalState());
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		PreferencesWindow.display();
	}

	@Override
	public void handlePreferences(PreferencesEvent event) {
		PreferencesWindow.display();
	}
}
