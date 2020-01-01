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

package com.trollworks.toolkit.ui.preferences;

import com.trollworks.toolkit.io.Log;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.image.StdImage;
import com.trollworks.toolkit.ui.menu.file.CloseHandler;
import com.trollworks.toolkit.ui.widget.AppWindow;
import com.trollworks.toolkit.utility.I18n;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/** A window for editing application preferences. */
public class PreferencesWindow extends AppWindow implements ActionListener, ChangeListener, CloseHandler {
    private static final String                           PREFIX     = "PreferencesWindow.";
    private static       PreferencesWindow                INSTANCE;
    private static final List<PreferenceCategoryProvider> CATEGORIES = new ArrayList<>();
    private              JTabbedPane                      mTabPanel;
    private              JButton                          mResetButton;

    /**
     * Adds a category of preference items.
     *
     * @param category The {@link PreferenceCategoryProvider} for the category.
     */
    public static void addCategory(PreferenceCategoryProvider category) {
        CATEGORIES.add(category);
    }

    /** Displays the preferences window. */
    public static void display() {
        if (!UIUtilities.inModalState()) {
            PreferencesWindow wnd;
            synchronized (PreferencesWindow.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PreferencesWindow();
                }
                wnd = INSTANCE;
            }
            wnd.setVisible(true);
        }
    }

    private PreferencesWindow() {
        super(I18n.Text("Preferences"), StdImage.PREFERENCES);
        Container content = getContentPane();
        mTabPanel = new JTabbedPane();
        for (PreferenceCategoryProvider category : CATEGORIES) {
            try {
                addTab(category.create(this));
            } catch (Exception exception) {
                Log.error("Trying to load " + category, exception);
            }
        }
        mTabPanel.addChangeListener(this);
        content.add(mTabPanel);
        content.add(createResetPanel(), BorderLayout.SOUTH);
        adjustResetButton();
        restoreBounds();
    }

    private void addTab(PreferencePanel panel) {
        mTabPanel.addTab(panel.toString(), panel);
    }

    @Override
    public void dispose() {
        synchronized (PreferencesWindow.class) {
            INSTANCE = null;
        }
        super.dispose();
    }

    private JPanel createResetPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        mResetButton = new JButton(I18n.Text("Reset to Factory Defaults"));
        mResetButton.addActionListener(this);
        panel.add(mResetButton);
        return panel;
    }

    /** Call to adjust the reset button to the current panel. */
    public void adjustResetButton() {
        mResetButton.setEnabled(!((PreferencePanel) mTabPanel.getSelectedComponent()).isSetToDefaults());
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        ((PreferencePanel) mTabPanel.getSelectedComponent()).reset();
        adjustResetButton();
    }

    @Override
    public String getWindowPrefsPrefix() {
        return PREFIX;
    }

    @Override
    public void stateChanged(ChangeEvent event) {
        adjustResetButton();
    }

    @Override
    public boolean mayAttemptClose() {
        return true;
    }

    @Override
    public boolean attemptClose() {
        windowClosing(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        return true;
    }
}
