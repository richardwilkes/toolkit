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

import com.trollworks.toolkit.ui.App;
import com.trollworks.toolkit.ui.GraphicsUtilities;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.ui.menu.file.CloseHandler;
import com.trollworks.toolkit.ui.widget.AppWindow;
import com.trollworks.toolkit.utility.BundleInfo;
import com.trollworks.toolkit.utility.I18n;

import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import javax.swing.JPanel;

/** Provides the "About" command. */
public class AboutCommand extends Command implements AboutHandler {
    /** The action command this command will issue. */
    public static final String       CMD_ABOUT = "About";
    /** The singleton {@link AboutCommand}. */
    public static final AboutCommand INSTANCE  = new AboutCommand();
    AppWindow mWindow;

    private AboutCommand() {
        super(MessageFormat.format(I18n.Text("About {0}"), BundleInfo.getDefault().getName()), CMD_ABOUT);
    }

    @Override
    public void adjust() {
        setEnabled(!UIUtilities.inModalState());
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        show();
    }

    @Override
    public void handleAbout(AboutEvent event) {
        show();
    }

    private void show() {
        if (!UIUtilities.inModalState()) {
            if (mWindow != null) {
                if (mWindow.isDisplayable() && mWindow.isVisible()) {
                    mWindow.toFront();
                    return;
                }
            }
            JPanel aboutPanel = App.createAboutPanel();
            if (aboutPanel != null) {
                mWindow = new AboutWindow(getTitle(), aboutPanel);
            }
        }
    }

    private class AboutWindow extends AppWindow implements CloseHandler {
        AboutWindow(String title, JPanel content) {
            super(title);
            getContentPane().add(content);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent windowEvent) {
                    mWindow = null;
                }
            });
            GraphicsUtilities.packAndCenterWindowOn(this, null);
            setVisible(true);
            setResizable(false);
        }

        @Override
        public boolean mayAttemptClose() {
            return true;
        }

        @Override
        public boolean attemptClose() {
            dispose();
            return true;
        }
    }
}
