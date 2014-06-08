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

package com.trollworks.toolkit.utility;

import com.trollworks.toolkit.annotation.Localize;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Loads localized messages into classes. This provides similar functionality to the
 * org.eclipse.osgi.util.NLS class, but this way we can get localized strings without requiring any
 * part of Eclipse.
 */
@SuppressWarnings("nls")
public class Localization extends Properties implements PrivilegedAction<Object> {
	private static final String		EXTENSION			= ".properties";
	private static final int		MOD_EXPECTED		= Modifier.STATIC;
	private static final int		MOD_MASK			= MOD_EXPECTED | Modifier.FINAL;
	private static final String[]	SUFFIXES;
	private Class<?>				mClass;
	private Map<String, Field>		mFields				= new HashMap<>();
	private Set<Object>				mAlreadyProcessed	= new HashSet<>();
	private String					mBundleName;
	private boolean					mIsAccessible;

	static {
		String nl = Locale.getDefault().toString();
		ArrayList<String> result = new ArrayList<>(4);
		while (true) {
			result.add('_' + nl + EXTENSION);
			int lastSeparator = nl.lastIndexOf('_');
			if (lastSeparator == -1) {
				break;
			}
			nl = nl.substring(0, lastSeparator);
		}
		result.add(EXTENSION);
		SUFFIXES = result.toArray(new String[result.size()]);
	}

	/**
	 * Initialize the calling class with the values from its message bundle. <b>NOTE</b>: This can
	 * only be called if the calling class shares the same {@link ClassLoader} as the
	 * {@link Localization} class. Classes from Eclipse plugins, for example, do <b>NOT</b> share
	 * class loaders and cannot use this method unless they are in the same plugin as the
	 * {@link Localization} class.
	 */
	public static void initialize() {
		try {
			initialize(Class.forName(new Exception().getStackTrace()[1].getClassName()));
		} catch (Throwable throwable) {
			throwable.printStackTrace(System.err);
		}
	}

	/**
	 * Initialize the specified class with the values from its message bundle.
	 *
	 * @param theClass The class to process.
	 */
	public static void initialize(final Class<?> theClass) {
		Localization action = new Localization(theClass);
		if (System.getSecurityManager() == null) {
			action.run();
		} else {
			AccessController.doPrivileged(action);
		}
	}

	private Localization(Class<?> theClass) {
		mClass = theClass;
		mBundleName = theClass.getName();
		mIsAccessible = (mClass.getModifiers() & Modifier.PUBLIC) != 0;
	}

	@Override
	public Object run() {
		Field[] fieldArray = mClass.getDeclaredFields();
		ClassLoader loader = mClass.getClassLoader();
		String root = mBundleName.replace('.', '/');
		String[] variants = new String[SUFFIXES.length];
		int len = fieldArray.length;

		for (int i = 0; i < len; i++) {
			Field field = fieldArray[i];
			if (field.isAnnotationPresent(Localize.class) && (field.getModifiers() & MOD_MASK) == MOD_EXPECTED) {
				mFields.put(field.getName(), field);
			}
		}

		for (int i = 0; i < variants.length; i++) {
			variants[i] = root + SUFFIXES[i];
		}

		for (String variant : variants) {
			try (InputStream input = loader == null ? ClassLoader.getSystemResourceAsStream(variant) : loader.getResourceAsStream(variant);) {
				if (input != null) {
					load(input);
				}
			} catch (IOException exception) {
				System.err.println("Error: Unable to load " + variant); //$NON-NLS-1$
				exception.printStackTrace(System.err);
			} finally {
				clear();
			}
		}

		for (Field field : mFields.values()) {
			String name = field.getName();
			System.err.println("Missing localized message for '" + name + "' in " + mBundleName);
			try {
				if (!mIsAccessible || (field.getModifiers() & Modifier.PUBLIC) == 0) {
					field.setAccessible(true);
				}
				field.set(null, "*!*" + name + "*!*");
			} catch (Exception exception) {
				System.err.println("Unable to set default value of localized message for '" + name + "' in " + mBundleName);
			}
		}

		return null;
	}

	@Override
	public synchronized Object put(Object key, Object value) {
		if (!mAlreadyProcessed.contains(key)) {
			Field field = mFields.remove(key);
			if (field != null) {
				try {
					if (!mIsAccessible || (field.getModifiers() & Modifier.PUBLIC) == 0) {
						field.setAccessible(true);
					}
					field.set(null, value);
					mAlreadyProcessed.add(key);
				} catch (Exception e) {
					System.err.println("Unable to set value of localized message for '" + key + "' in " + mBundleName);
				}
			} else {
				System.err.println("Unused localized message for '" + key + "' in " + mBundleName);
			}
		}
		return null;
	}
}
