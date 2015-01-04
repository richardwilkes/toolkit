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

import static java.awt.event.KeyEvent.*;

import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.utility.Platform;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/** Displays and captures keystrokes typed. */
public class KeyStrokeDisplay extends JLabel implements KeyListener {
	private KeyStroke	mKeyStroke;

	/**
	 * Creates a new {@link KeyStrokeDisplay}.
	 *
	 * @param ks The {@link KeyStroke} to start with.
	 */
	public KeyStrokeDisplay(KeyStroke ks) {
		super(getKeyStrokeDisplay(KeyStroke.getKeyStroke('Z', InputEvent.META_MASK | InputEvent.ALT_MASK | InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)), SwingConstants.CENTER);
		setOpaque(true);
		setBackground(Color.WHITE);
		setBorder(new CompoundBorder(LineBorder.createBlackLineBorder(), new EmptyBorder(2, 5, 2, 5)));
		addKeyListener(this);
		mKeyStroke = ks;
		UIUtilities.setOnlySize(this, getPreferredSize());
		setText(getKeyStrokeDisplay(mKeyStroke));
	}

	@Override
	public void keyPressed(KeyEvent event) {
		KeyStroke ks = KeyStroke.getKeyStrokeForEvent(event);
		int code = ks.getKeyCode();
		if (code != VK_SHIFT && code != VK_CONTROL && code != VK_META && code != VK_ALT && code != VK_CAPS_LOCK && code != VK_ESCAPE) {
			mKeyStroke = ks;
			setText(getKeyStrokeDisplay(mKeyStroke));
		}
	}

	@Override
	public void keyReleased(KeyEvent event) {
		// Not used.
	}

	@Override
	public void keyTyped(KeyEvent event) {
		// Not used.
	}

	/** @return The {@link KeyStroke}. */
	public KeyStroke getKeyStroke() {
		return mKeyStroke;
	}

	/**
	 * @param ks The {@link KeyStroke} to use.
	 * @return The text that represents the {@link KeyStroke}.
	 */
	public static String getKeyStrokeDisplay(KeyStroke ks) {
		StringBuilder buffer = new StringBuilder();
		if (ks != null) {
			int modifiers = ks.getModifiers();
			if (modifiers > 0) {
				String modifierText = KeyEvent.getKeyModifiersText(modifiers);
				if (Platform.isMacintosh()) {
					buffer.append(modifierText.replaceAll("\\+", "")); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					buffer.append(modifierText);
					String delimiter = UIManager.getString("MenuItem.acceleratorDelimiter"); //$NON-NLS-1$
					if (delimiter == null) {
						delimiter = "+"; //$NON-NLS-1$
					}
					buffer.append(delimiter);
				}
			}
			int keyCode = ks.getKeyCode();
			if (keyCode != 0) {
				buffer.append(KeyEvent.getKeyText(keyCode));
			} else {
				buffer.append(ks.getKeyChar());
			}
		}
		return buffer.toString();
	}
}
