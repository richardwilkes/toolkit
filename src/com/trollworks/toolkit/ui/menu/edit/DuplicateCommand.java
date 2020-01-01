/*
 * Copyright (c) 1998-2020 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui.menu.edit;

import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.utility.I18n;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/** Provides the "Duplicate" command. */
public class DuplicateCommand extends Command {
    /** The action command this command will issue. */
    public static final String           CMD_DUPLICATE = "Duplicate";
    /** The singleton {@link DuplicateCommand}. */
    public static final DuplicateCommand INSTANCE      = new DuplicateCommand();

    private DuplicateCommand() {
        super(I18n.Text("Duplicate"), CMD_DUPLICATE, KeyEvent.VK_D);
    }

    @Override
    public void adjust() {
        boolean      enable       = false;
        Duplicatable duplicatable = getTarget(Duplicatable.class);
        if (duplicatable != null) {
            enable = duplicatable.canDuplicateSelection();
        }
        setEnabled(enable);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Duplicatable duplicatable = getTarget(Duplicatable.class);
        if (duplicatable != null) {
            duplicatable.duplicateSelection();
        }
    }
}
