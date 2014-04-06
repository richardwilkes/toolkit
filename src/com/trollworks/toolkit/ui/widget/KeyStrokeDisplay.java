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

package com.trollworks.toolkit.ui.widget;

import static java.awt.event.KeyEvent.*;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/** Displays and captures keystrokes typed. */
public class KeyStrokeDisplay extends JLabel implements KeyListener {
	public static final String	LEFT_ARROW_SYMBOL		= "\u2190"; //$NON-NLS-1$
	public static final String	UP_ARROW_SYMBOL			= "\u2191"; //$NON-NLS-1$
	public static final String	RIGHT_ARROW_SYMBOL		= "\u2192"; //$NON-NLS-1$
	public static final String	DOWN_ARROW_SYMBOL		= "\u2193"; //$NON-NLS-1$
	public static final String	HOME_SYMBOL				= "\u2196"; //$NON-NLS-1$
	public static final String	END_SYMBOL				= "\u2198"; //$NON-NLS-1$
	public static final String	PAGE_UP_SYMBOL			= "\u21de"; //$NON-NLS-1$
	public static final String	PAGE_DOWN_SYMBOL		= "\u21df"; //$NON-NLS-1$
	public static final String	TAB_SYMBOL				= "\u21e5"; //$NON-NLS-1$
	public static final String	SHIFT_SYMBOL			= "\u21e7"; //$NON-NLS-1$
	public static final String	CAPSLOCK_SYMBOL			= "\u21ea"; //$NON-NLS-1$
	public static final String	CONTROL_SYMBOL			= "\u2303"; //$NON-NLS-1$
	public static final String	COMMAND_SYMBOL			= "\u2318"; //$NON-NLS-1$
	public static final String	ENTER_SYMBOL			= "\u2324"; //$NON-NLS-1$
	public static final String	OPTION_SYMBOL			= "\u2325"; //$NON-NLS-1$
	public static final String	FORWARD_DELETE_SYMBOL	= "\u2326"; //$NON-NLS-1$
	public static final String	CLEAR_SYMBOL			= "\u2327"; //$NON-NLS-1$
	public static final String	DELETE_SYMBOL			= "\u232b"; //$NON-NLS-1$
	public static final String	ESCAPE_SYMBOL			= "\u238b"; //$NON-NLS-1$
	public static final String	RETURN_SYMBOL			= "\u23ce"; //$NON-NLS-1$
	private KeyStroke			mKeyStroke;

	/**
	 * Creates a new {@link KeyStrokeDisplay}.
	 *
	 * @param ks The {@link KeyStroke} to start with.
	 */
	public KeyStrokeDisplay(KeyStroke ks) {
		super("Command-Alt-Option-Control-Shift-Meta-Z", SwingConstants.CENTER); //$NON-NLS-1$
		setOpaque(true);
		setBackground(Color.WHITE);
		setBorder(new CompoundBorder(LineBorder.createBlackLineBorder(), new EmptyBorder(2, 5, 2, 5)));
		addKeyListener(this);
		mKeyStroke = ks;
		setPreferredSize(getPreferredSize());
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
				if ((modifiers & InputEvent.CTRL_MASK) != 0) {
					buffer.append(CONTROL_SYMBOL);
				}
				if ((modifiers & InputEvent.ALT_MASK) != 0) {
					buffer.append(OPTION_SYMBOL);
				}
				if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
					buffer.append(SHIFT_SYMBOL);
				}
				if ((modifiers & InputEvent.META_MASK) != 0) {
					buffer.append(COMMAND_SYMBOL);
				}
			}
			int keyCode = ks.getKeyCode();
			switch (keyCode) {
				case 0:
					buffer.append(ks.getKeyChar());
					break;
				case VK_LEFT:
					buffer.append(LEFT_ARROW_SYMBOL);
					break;
				case VK_UP:
					buffer.append(UP_ARROW_SYMBOL);
					break;
				case VK_RIGHT:
					buffer.append(RIGHT_ARROW_SYMBOL);
					break;
				case VK_DOWN:
					buffer.append(DOWN_ARROW_SYMBOL);
					break;
				case VK_HOME:
					buffer.append(HOME_SYMBOL);
					break;
				case VK_END:
					buffer.append(END_SYMBOL);
					break;
				case VK_PAGE_UP:
					buffer.append(PAGE_UP_SYMBOL);
					break;
				case VK_PAGE_DOWN:
					buffer.append(PAGE_DOWN_SYMBOL);
					break;
				case VK_TAB:
					buffer.append(TAB_SYMBOL);
					break;
				case VK_CAPS_LOCK:
					buffer.append(CAPSLOCK_SYMBOL);
					break;
				case VK_ENTER:
					buffer.append(ENTER_SYMBOL);
					break;
				case VK_DELETE:
					buffer.append(FORWARD_DELETE_SYMBOL);
					break;
				case VK_BACK_SPACE:
					buffer.append(DELETE_SYMBOL);
					break;
				case VK_ESCAPE:
					buffer.append(ESCAPE_SYMBOL);
					break;
				case VK_CLEAR:
					buffer.append(CLEAR_SYMBOL);
					break;
				case VK_BACK_QUOTE:
					buffer.append("`"); //$NON-NLS-1$
					break;
				case VK_MINUS:
					buffer.append("-"); //$NON-NLS-1$
					break;
				case VK_EQUALS:
					buffer.append("="); //$NON-NLS-1$
					break;
				case VK_OPEN_BRACKET:
					buffer.append("["); //$NON-NLS-1$
					break;
				case VK_CLOSE_BRACKET:
					buffer.append("]"); //$NON-NLS-1$
					break;
				case VK_BACK_SLASH:
					buffer.append("\\"); //$NON-NLS-1$
					break;
				case VK_SEMICOLON:
					buffer.append(";"); //$NON-NLS-1$
					break;
				case VK_QUOTE:
					buffer.append("'"); //$NON-NLS-1$
					break;
				case VK_COMMA:
					buffer.append(","); //$NON-NLS-1$
					break;
				case VK_PERIOD:
					buffer.append("."); //$NON-NLS-1$
					break;
				case VK_SLASH:
					buffer.append("/"); //$NON-NLS-1$
					break;
				default:
					buffer.append(getKeyText(keyCode));
					break;
			}
		}
		return buffer.toString();
	}
}
