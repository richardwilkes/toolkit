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

package com.trollworks.toolkit.ui.menu.window;

import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.ui.menu.DynamicMenuEnabler;
import com.trollworks.toolkit.ui.menu.MenuProvider;
import com.trollworks.toolkit.ui.menu.StdMenuBar;
import com.trollworks.toolkit.ui.widget.AppWindow;
import com.trollworks.toolkit.utility.I18n;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

/** Provides the standard "Window" menu. */
public class WindowMenuProvider implements MenuProvider {
    public static final String NAME = "Window";

    /** Updates the available menu items. */
    public static void update() {
        List<AppWindow> windows = AppWindow.getAllAppWindows();
        Collections.sort(windows);
        for (AppWindow window : windows) {
            JMenu windowMenu = StdMenuBar.findMenuByName(window.getJMenuBar(), NAME);
            if (windowMenu != null) {
                windowMenu.removeAll();
                for (AppWindow one : windows) {
                    windowMenu.add(new JCheckBoxMenuItem(new SwitchToWindowCommand(one)));
                }
            }
        }
    }

    @Override
    public Set<Command> getModifiableCommands() {
        return Collections.emptySet();
    }

    @Override
    public JMenu createMenu() {
        JMenu menu = new JMenu(I18n.Text("Window"));
        menu.setName(NAME);
        DynamicMenuEnabler.add(menu);
        return menu;
    }
}
