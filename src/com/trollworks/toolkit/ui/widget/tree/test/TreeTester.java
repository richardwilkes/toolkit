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

package com.trollworks.toolkit.ui.widget.tree.test;

import com.trollworks.toolkit.ui.App;
import com.trollworks.toolkit.ui.GraphicsUtilities;
import com.trollworks.toolkit.ui.menu.StdMenuBar;
import com.trollworks.toolkit.utility.LaunchProxy;
import com.trollworks.toolkit.utility.Preferences;
import com.trollworks.toolkit.utility.Timing;
import com.trollworks.toolkit.utility.cmdline.CmdLine;

import java.awt.EventQueue;
import java.io.File;

public class TreeTester extends App {
    public static final void main(String[] args) {
        CmdLine cmdLine = new CmdLine();
        cmdLine.processArguments(args);
        LaunchProxy.configure(cmdLine.getArgumentsAsFiles());
        GraphicsUtilities.configureStandardUI();
        Preferences.setPreferenceFile(new File("/tmp/treetester.prf"));
        TreeTester app = new TreeTester();
        app.startup(cmdLine);
        EventQueue.invokeLater(app);
    }

    @Override
    public void configureApplication(CmdLine cmdLine) {
        StdMenuBar.configure(new TreeTesterEditMenuProvider());
    }

    @Override
    public void noWindowsAreOpenAtStartup(boolean finalChance) {
        if (finalChance) {
            Timing         timing = new Timing();
            TreeTestWindow win    = new TreeTestWindow("Test 1 Drop Copy & Move", true);
            win.setBounds(50, 50, 500, 500);
            win.setVisible(true);
            System.out.println("Tree 1 took " + timing);
            win = new TreeTestWindow("Test 2 Drop Move Only", false);
            win.setBounds(600, 50, 500, 500);
            win.setVisible(true);
            System.out.println("Tree 2 took " + timing);

            OutlineTestWindow win2 = new OutlineTestWindow("Outline");
            win2.setBounds(50, 600, 500, 500);
            win2.setVisible(true);
            System.out.println("Outline took " + timing);
        }
    }
}
