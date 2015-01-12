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
import com.trollworks.toolkit.ui.WindowSizeEnforcer;
import com.trollworks.toolkit.utility.Localization;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;

/** Utilities for use with windows. */
public class WindowUtils {
	@Localize("Error")
	@Localize(locale = "ru", value = "Ошибка")
	@Localize(locale = "de", value = "Fehler")
	@Localize(locale = "es", value = "Error")
	private static String	ERROR;
	@Localize("Warning")
	@Localize(locale = "ru", value = "Внимание")
	@Localize(locale = "de", value = "Warnung")
	@Localize(locale = "es", value = "Aviso")
	private static String	WARNING;

	static {
		Localization.initialize();
	}

	/**
	 * @param comp The {@link Component} to use for determining the parent {@link Frame} or
	 *            {@link Dialog}.
	 * @param msg The message to display.
	 */
	public static void showError(Component comp, String msg) {
		JOptionPane.showMessageDialog(comp, msg, ERROR, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * @param comp The {@link Component} to use for determining the parent {@link Frame} or
	 *            {@link Dialog}.
	 * @param msg The message to display.
	 */
	public static void showWarning(Component comp, String msg) {
		JOptionPane.showMessageDialog(comp, msg, WARNING, JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Shows a confirmation dialog with custom options.
	 *
	 * @param comp The {@link Component} to use. May be <code>null</code>.
	 * @param message The message.
	 * @param title The title to use.
	 * @param optionType The type of option dialog. Use the {@link JOptionPane} constants.
	 * @param options The options to display.
	 * @param initialValue The initial option.
	 * @return See the documentation for {@link JOptionPane}.
	 */
	public static int showConfirmDialog(Component comp, String message, String title, int optionType, Object[] options, Object initialValue) {
		return showOptionDialog(comp, message, title, false, optionType, JOptionPane.QUESTION_MESSAGE, null, options, initialValue);
	}

	/**
	 * Shows an option dialog.
	 *
	 * @param parentComponent The parent {@link Component} to use. May be <code>null</code>.
	 * @param message The message. May be a {@link Component}.
	 * @param title The title to use.
	 * @param resizable Whether to allow the dialog to be resized by the user.
	 * @param optionType The type of option dialog. Use the {@link JOptionPane} constants.
	 * @param messageType The type of message. Use the {@link JOptionPane} constants.
	 * @param icon The icon to use. May be <code>null</code>.
	 * @param options The options to display. May be <code>null</code>.
	 * @param initialValue The initial option.
	 * @return See the documentation for {@link JOptionPane}.
	 */
	public static int showOptionDialog(Component parentComponent, Object message, String title, boolean resizable, int optionType, int messageType, Icon icon, Object[] options, Object initialValue) {
		JOptionPane pane = new JOptionPane(message, messageType, optionType, icon, options, initialValue);
		pane.setUI(new SizeAwareBasicOptionPaneUI(pane.getUI()));
		pane.setInitialValue(initialValue);
		pane.setComponentOrientation((parentComponent == null ? JOptionPane.getRootFrame() : parentComponent).getComponentOrientation());

		final JDialog dialog = pane.createDialog(getWindowForComponent(parentComponent), title);
		WindowSizeEnforcer.monitor(dialog);
		pane.selectInitialValue();
		dialog.setResizable(resizable);
		final Component field = getFirstFocusableField(message);
		if (field != null) {
			dialog.addWindowFocusListener(new WindowAdapter() {
				@Override
				public void windowGainedFocus(WindowEvent event) {
					field.requestFocus();
					dialog.removeWindowFocusListener(this);
				}
			});
		}
		dialog.setVisible(true);
		dialog.dispose();
		pane.setMessage(null);

		Object selectedValue = pane.getValue();
		if (selectedValue != null) {
			if (options == null) {
				if (selectedValue instanceof Integer) {
					return ((Integer) selectedValue).intValue();
				}
			} else {
				for (int i = 0; i < options.length; i++) {
					if (options[i].equals(selectedValue)) {
						return i;
					}
				}
			}
		}
		return JOptionPane.CLOSED_OPTION;
	}

	private static Component getFirstFocusableField(Object comp) {
		if (comp instanceof JTextComponent || comp instanceof KeyStrokeDisplay) {
			return (Component) comp;
		}
		if (comp instanceof Container) {
			for (Component child : ((Container) comp).getComponents()) {
				Component field = getFirstFocusableField(child);
				if (field != null) {
					return field;
				}
			}
		}
		return null;
	}

	/**
	 * @param comp The {@link Component} to use. May be <code>null</code>.
	 * @return The most logical {@link Window} associated with the component.
	 */
	public static Window getWindowForComponent(Component comp) {
		while (true) {
			if (comp == null) {
				return JOptionPane.getRootFrame();
			}
			if (comp instanceof Frame || comp instanceof Dialog) {
				return (Window) comp;
			}
			comp = comp.getParent();
		}
	}
}
