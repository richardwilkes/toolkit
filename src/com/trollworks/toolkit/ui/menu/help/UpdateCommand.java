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

package com.trollworks.toolkit.ui.menu.help;

import com.trollworks.toolkit.ui.UpdateChecker;
import com.trollworks.toolkit.ui.menu.Command;

import java.awt.event.ActionEvent;

/** Provides the "Update" command. */
public class UpdateCommand extends Command {
	/** The action command this command will issue. */
	public static final String			CMD_CHECK_FOR_UPDATE	= "CheckForUpdate";	//$NON-NLS-1$
	/** The singleton {@link UpdateCommand}. */
	public static final UpdateCommand	INSTANCE				= new UpdateCommand();

	private UpdateCommand() {
		super(UpdateChecker.getResult(), CMD_CHECK_FOR_UPDATE);
	}

	@Override
	public void adjust() {
		setTitle(UpdateChecker.getResult());
		setEnabled(UpdateChecker.isNewVersionAvailable());
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		UpdateChecker.goToUpdate();
	}
}
