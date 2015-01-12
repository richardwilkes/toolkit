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

package com.trollworks.toolkit.ui.preferences;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.Fonts;
import com.trollworks.toolkit.ui.GraphicsUtilities;
import com.trollworks.toolkit.ui.layout.Alignment;
import com.trollworks.toolkit.ui.layout.FlexComponent;
import com.trollworks.toolkit.ui.layout.FlexGrid;
import com.trollworks.toolkit.ui.widget.FontPanel;
import com.trollworks.toolkit.utility.Localization;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.UIManager;

/** The font preferences panel. */
public class FontPreferences extends PreferencePanel implements ActionListener {
	@Localize("Fonts")
	@Localize(locale = "ru", value = "Шрифты")
	@Localize(locale = "de", value = "Schriftarten")
	@Localize(locale = "es", value = "Fuentes")
	private static String	FONTS;

	static {
		Localization.initialize();
	}

	private FontPanel[]		mFontPanels;
	private boolean			mIgnore;

	/**
	 * Creates a new {@link FontPreferences}.
	 *
	 * @param owner The owning {@link PreferencesWindow}.
	 */
	public FontPreferences(PreferencesWindow owner) {
		super(FONTS, owner);
		FlexGrid grid = new FlexGrid();
		String[] keys = Fonts.getKeys();
		mFontPanels = new FontPanel[keys.length];
		int i = 0;
		for (String key : keys) {
			grid.add(new FlexComponent(createLabel(Fonts.getDescription(key), null), Alignment.RIGHT_BOTTOM, Alignment.CENTER), i, 0);
			mFontPanels[i] = new FontPanel(UIManager.getFont(key));
			mFontPanels[i].setActionCommand(key);
			mFontPanels[i].addActionListener(this);
			add(mFontPanels[i]);
			grid.add(mFontPanels[i], i, 1);
			i++;
		}
		grid.apply(this);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (!mIgnore) {
			Object source = event.getSource();
			if (source instanceof FontPanel) {
				boolean adjusted = false;
				for (FontPanel panel : mFontPanels) {
					if (panel == source) {
						Font font = panel.getCurrentFont();
						if (!font.equals(UIManager.getFont(panel.getActionCommand()))) {
							UIManager.put(panel.getActionCommand(), font);
							adjusted = true;
						}
						break;
					}
				}
				if (adjusted) {
					GraphicsUtilities.forceRepaintAndInvalidate();
					Fonts.notifyOfFontChanges();
				}
			}

			adjustResetButton();
		}
	}

	@Override
	public void reset() {
		Fonts.restoreDefaults();
		mIgnore = true;
		for (FontPanel panel : mFontPanels) {
			panel.setCurrentFont(UIManager.getFont(panel.getActionCommand()));
		}
		mIgnore = false;
		GraphicsUtilities.forceRepaintAndInvalidate();
		Fonts.notifyOfFontChanges();
	}

	@Override
	public boolean isSetToDefaults() {
		return Fonts.isSetToDefaults();
	}
}
