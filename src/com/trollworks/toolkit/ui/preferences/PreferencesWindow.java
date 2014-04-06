/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui.preferences;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.image.ToolkitImage;
import com.trollworks.toolkit.ui.widget.AppWindow;
import com.trollworks.toolkit.utility.Localization;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/** A window for editing application preferences. */
public class PreferencesWindow extends AppWindow implements ActionListener, ChangeListener {
	@Localize("Preferences")
	private static String												PREFERENCES;
	@Localize("Reset to Factory Defaults")
	private static String												RESET;

	static {
		Localization.initialize();
	}

	private static final String											PREFIX		= "PreferencesWindow."; //$NON-NLS-1$
	private static PreferencesWindow									INSTANCE	= null;
	private static final ArrayList<Class<? extends PreferencePanel>>	CATEGORIES	= new ArrayList<>();
	private JTabbedPane													mTabPanel;
	private JButton														mResetButton;

	/**
	 * Adds a category of preference items.
	 *
	 * @param category The class that creates the preference panel for the category.
	 */
	public static void addCategory(Class<? extends PreferencePanel> category) {
		CATEGORIES.add(category);
	}

	/** Displays the preferences window. */
	public static void display() {
		if (INSTANCE == null) {
			INSTANCE = new PreferencesWindow();
		}
		INSTANCE.setVisible(true);
	}

	private PreferencesWindow() {
		super(PREFERENCES, ToolkitImage.getPreferencesIcon(), ToolkitImage.getPreferencesIcon());
		Container content = getContentPane();
		mTabPanel = new JTabbedPane();
		for (Class<? extends PreferencePanel> panelClass : CATEGORIES) {
			try {
				Constructor<? extends PreferencePanel> constructor = panelClass.getConstructor(PreferencesWindow.class);
				addTab(constructor.newInstance(this));
			} catch (Exception exception) {
				System.out.println("Exception trying to load " + panelClass); //$NON-NLS-1$
				exception.printStackTrace();
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
		INSTANCE = null;
		super.dispose();
	}

	private JPanel createResetPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		mResetButton = new JButton(RESET);
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
}
