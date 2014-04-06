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

package com.trollworks.toolkit.ui;

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
import java.util.Properties;

/**
 * Loads localized messages into classes. This provides similar functionality to the
 * org.eclipse.osgi.util.NLS class, but this way we can get localized strings without requiring any
 * part of Eclipse.
 */
public class LocalizedMessages extends Properties implements PrivilegedAction<Object> {
	private static final String		PREFIX			= "MSG_";							//$NON-NLS-1$
	private static final String		IN				= "\" in: ";						//$NON-NLS-1$
	private static final String		EXTENSION		= ".properties";					//$NON-NLS-1$
	private static final int		MOD_EXPECTED	= Modifier.STATIC;
	private static final int		MOD_MASK		= MOD_EXPECTED | Modifier.FINAL;
	private static final String[]	SUFFIXES;
	private Class<?>				mClass;
	private HashSet<Field>			mInitialized	= new HashSet<>();
	private HashMap<String, Field>	mFields			= new HashMap<>();
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
	 * Initialize the specified class with the values from its message bundle.
	 *
	 * @param theClass The class to process.
	 */
	public static void initialize(final Class<?> theClass) {
		LocalizedMessages action = new LocalizedMessages(theClass);
		if (System.getSecurityManager() == null) {
			action.run();
		} else {
			AccessController.doPrivileged(action);
		}
	}

	private LocalizedMessages(Class<?> theClass) {
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
			mFields.put(fieldArray[i].getName(), fieldArray[i]);
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

		for (int i = 0; i < len; i++) {
			Field field = fieldArray[i];

			if (field.getName().startsWith(PREFIX) && (field.getModifiers() & MOD_MASK) == MOD_EXPECTED && !mInitialized.contains(field)) {
				try {
					String warning = "Warning: Missing message for \"" + field.getName() + IN + mBundleName; //$NON-NLS-1$

					System.err.println(warning);
					if (!mIsAccessible || (field.getModifiers() & Modifier.PUBLIC) == 0) {
						field.setAccessible(true);
					}
					field.set(null, warning);
				} catch (Exception exception) {
					// Should not be possible
				}
			}
		}

		return null;
	}

	@Override
	public synchronized Object put(Object key, Object value) {
		Field field = mFields.get(PREFIX + key);

		if (field == null) {
			System.err.println("Warning: Unused message for \"" + key + IN + mBundleName); //$NON-NLS-1$
			return null;
		}

		if (!mInitialized.contains(field)) {
			int modifiers = field.getModifiers();
			mInitialized.add(field);
			if ((modifiers & MOD_MASK) != MOD_EXPECTED) {
				System.err.println("Warning: Incorrect field modifiers for \"" + field.getName() + IN + mBundleName); //$NON-NLS-1$
				return null;
			}
			try {
				if (!mIsAccessible || (modifiers & Modifier.PUBLIC) == 0) {
					field.setAccessible(true);
				}
				field.set(null, value);
			} catch (Exception e) {
				// Should not be possible
			}
		}
		return null;
	}
}
