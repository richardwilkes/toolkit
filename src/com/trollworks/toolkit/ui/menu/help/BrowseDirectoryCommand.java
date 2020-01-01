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

package com.trollworks.toolkit.ui.menu.help;

import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.ui.widget.WindowUtils;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;

/** A command that will open a specific directory using the OS-preferred mechanism. */
public class BrowseDirectoryCommand extends Command {
    private File mDir;

    /**
     * Creates a new {@link BrowseDirectoryCommand}.
     *
     * @param title The title to use.
     * @param dir   The file to open.
     */
    public BrowseDirectoryCommand(String title, File dir) {
        super(title, "BrowseDirectory[" + dir.getName() + "]");
        mDir = dir;
    }

    @Override
    public void adjust() {
        // Not used. Always enabled.
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        try {
            Desktop.getDesktop().browseFileDirectory(mDir.getCanonicalFile());
        } catch (Exception exception) {
            WindowUtils.showError(null, exception.getMessage());
        }
    }
}
