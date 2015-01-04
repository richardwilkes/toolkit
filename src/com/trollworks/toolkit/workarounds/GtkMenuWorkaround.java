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

package com.trollworks.toolkit.workarounds;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

/**
 * Swing menus have problems on Linux when the GTK LaF is used (see Oracle bug #6925412). This class
 * provides a workaround for the problem by using reflection to change the GTK style objects of
 * Swing so menu borders have a minimum thickness of 1 and menu separators have a minimum vertical
 * thickness of 1.<br>
 * <br>
 * Based on code provided by Klaus Reimer <k@ailis.de>.
 */
@SuppressWarnings("nls")
public class GtkMenuWorkaround implements PrivilegedAction<Object> {
	private LookAndFeel	mLookAndFeel;
	private Class<?>	mClass;

	public static void installGtkPopupBugWorkaround() {
		GtkMenuWorkaround workaround = new GtkMenuWorkaround();
		if (workaround.shouldRun()) {
			if (System.getSecurityManager() == null) {
				workaround.run();
			} else {
				AccessController.doPrivileged(workaround);
			}
		}
	}

	private GtkMenuWorkaround() {
		mLookAndFeel = UIManager.getLookAndFeel();
		mClass = mLookAndFeel.getClass();
	}

	private boolean shouldRun() {
		return mClass.getName().equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
	}

	@Override
	public Object run() {
		try {
			Field field = mClass.getDeclaredField("styleFactory");
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			Object styleFactory = field.get(mLookAndFeel);
			field.setAccessible(accessible);
			Object style = getGtkStyle(styleFactory, new JPopupMenu(), "POPUP_MENU");
			fixGtkThickness(style, "yThickness");
			fixGtkThickness(style, "xThickness");
			style = getGtkStyle(styleFactory, new JSeparator(), "POPUP_MENU_SEPARATOR");
			fixGtkThickness(style, "yThickness");
		} catch (Exception exception) {
			// Silently ignored. Workaround can't be applied.
		}
		return null;
	}

	private static void fixGtkThickness(Object style, String fieldName) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = style.getClass().getDeclaredField(fieldName);
		boolean accessible = field.isAccessible();
		field.setAccessible(true);
		field.setInt(style, Math.max(1, field.getInt(style)));
		field.setAccessible(accessible);
	}

	private static Object getGtkStyle(Object styleFactory, JComponent component, String regionName) throws Exception {
		Class<?> regionClass = Class.forName("javax.swing.plaf.synth.Region");
		Field field = regionClass.getField(regionName);
		Object region = field.get(regionClass);
		Class<?> styleFactoryClass = styleFactory.getClass();
		Method method = styleFactoryClass.getMethod("getStyle", new Class<?>[] { JComponent.class, regionClass });
		boolean accessible = method.isAccessible();
		method.setAccessible(true);
		Object style = method.invoke(styleFactory, component, region);
		method.setAccessible(accessible);
		return style;
	}
}
