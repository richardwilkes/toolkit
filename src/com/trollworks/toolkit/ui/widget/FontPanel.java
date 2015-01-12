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

package com.trollworks.toolkit.ui.widget;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.utility.Localization;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

/** A standard font selection panel. */
public class FontPanel extends ActionPanel implements ActionListener {
	@Localize("Plain")
	@Localize(locale = "ru", value = "Обычный")
	@Localize(locale = "de", value = "Normal")
	@Localize(locale = "es", value = "Normal")
	private static String			PLAIN;
	@Localize("Bold")
	@Localize(locale = "ru", value = "Полужирный")
	@Localize(locale = "de", value = "Fett")
	@Localize(locale = "es", value = "Negrita")
	private static String			BOLD;
	@Localize("Italic")
	@Localize(locale = "ru", value = "Курсив")
	@Localize(locale = "de", value = "Kursiv")
	@Localize(locale = "es", value = "Cursiva")
	private static String			ITALIC;
	@Localize("Bold Italic")
	@Localize(locale = "ru", value = "Полужирный Курсив")
	@Localize(locale = "de", value = "Fett Kursiv")
	@Localize(locale = "es", value = "Cursiva Negrita")
	private static String			BOLD_ITALIC;
	@Localize("Changes the font")
	@Localize(locale = "ru", value = "Изменить шрифт")
	@Localize(locale = "de", value = "Ändert die Schriftart")
	@Localize(locale = "es", value = "Cambiar la fuente")
	private static String			NAME_TOOLTIP;
	@Localize("Changes the font size")
	@Localize(locale = "ru", value = "Изменить размер шрифта")
	@Localize(locale = "de", value = "Ändert die Schriftgröße")
	@Localize(locale = "es", value = "Cambiar el tamaño de la fuente")
	private static String			SIZE_TOOLTIP;
	@Localize("Changes the font style")
	@Localize(locale = "ru", value = "Изменить стиль шрифта")
	@Localize(locale = "de", value = "Ändert den Schriftstil")
	@Localize(locale = "es", value = "Cambiar el estilo de la fuente")
	private static String			STYLE_TOOLTIP;

	static {
		Localization.initialize();
	}

	private static final String[]	STD_STYLES	= { PLAIN, BOLD, ITALIC, BOLD_ITALIC };
	private JComboBox<Integer>		mFontSizeMenu;
	private JComboBox<String>		mFontNameMenu;
	private JComboBox<String>		mFontStyleMenu;
	private boolean					mNoNotify;

	/**
	 * Creates a new font panel.
	 *
	 * @param font The font to start with.
	 */
	public FontPanel(Font font) {
		super(new FlowLayout(FlowLayout.LEFT, 5, 0));
		setOpaque(false);

		mFontNameMenu = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
		mFontNameMenu.setOpaque(false);
		mFontNameMenu.setToolTipText(NAME_TOOLTIP);
		mFontNameMenu.setMaximumRowCount(25);
		mFontNameMenu.addActionListener(this);
		UIUtilities.setOnlySize(mFontNameMenu, mFontNameMenu.getPreferredSize());
		add(mFontNameMenu);

		Integer[] sizes = new Integer[10];
		for (int i = 0; i < 7; i++) {
			sizes[i] = new Integer(6 + i);
		}
		sizes[7] = new Integer(14);
		sizes[8] = new Integer(16);
		sizes[9] = new Integer(18);
		mFontSizeMenu = new JComboBox<>(sizes);
		mFontSizeMenu.setOpaque(false);
		mFontSizeMenu.setToolTipText(SIZE_TOOLTIP);
		mFontSizeMenu.setMaximumRowCount(sizes.length);
		mFontSizeMenu.addActionListener(this);
		UIUtilities.setOnlySize(mFontSizeMenu, mFontSizeMenu.getPreferredSize());
		add(mFontSizeMenu);

		mFontStyleMenu = new JComboBox<>(STD_STYLES);
		mFontStyleMenu.setOpaque(false);
		mFontStyleMenu.setToolTipText(STYLE_TOOLTIP);
		mFontStyleMenu.addActionListener(this);
		UIUtilities.setOnlySize(mFontStyleMenu, mFontStyleMenu.getPreferredSize());
		add(mFontStyleMenu);

		setCurrentFont(font);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		notifyActionListeners();
	}

	@Override
	public void notifyActionListeners(ActionEvent event) {
		if (!mNoNotify) {
			super.notifyActionListeners(event);
		}
	}

	/** @return The font this panel has been set to. */
	public Font getCurrentFont() {
		return new Font((String) mFontNameMenu.getSelectedItem(), mFontStyleMenu.getSelectedIndex(), ((Integer) mFontSizeMenu.getSelectedItem()).intValue());
	}

	/** @param font The new font. */
	public void setCurrentFont(Font font) {
		mNoNotify = true;
		mFontNameMenu.setSelectedItem(font.getName());
		if (mFontNameMenu.getSelectedItem() == null) {
			mFontNameMenu.setSelectedIndex(0);
		}
		mFontSizeMenu.setSelectedItem(new Integer(font.getSize()));
		if (mFontSizeMenu.getSelectedItem() == null) {
			mFontSizeMenu.setSelectedIndex(3);
		}
		mFontStyleMenu.setSelectedIndex(font.getStyle());
		if (mFontStyleMenu.getSelectedItem() == null) {
			mFontStyleMenu.setSelectedIndex(0);
		}
		mNoNotify = false;
		notifyActionListeners();
	}
}
