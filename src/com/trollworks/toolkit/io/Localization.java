/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is com.trollworks.toolkit.
 *
 * The Initial Developer of the Original Code is Richard A. Wilkes.
 * Portions created by the Initial Developer are Copyright (C) 2014,
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */

package com.trollworks.toolkit.io;

import com.trollworks.toolkit.annotation.Localize;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;

/**
 * Loads localized messages into classes. This provides similar functionality to the
 * org.eclipse.osgi.util.NLS class, but this way we can get localized strings without requiring any
 * part of Eclipse.
 */
@SuppressWarnings("nls")
public class Localization extends Properties implements PrivilegedAction<Object> {
	private static final long		serialVersionUID	= -1744934790648567577L;
	private static final String		EXTENSION			= ".properties";
	private static final int		MOD_EXPECTED		= Modifier.STATIC;
	private static final int		MOD_MASK			= MOD_EXPECTED | Modifier.FINAL;
	private static final String[]	SUFFIXES;
	private Class<?>				mClass;
	private HashMap<String, Field>	mFields				= new HashMap<>();
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
		Field field = mFields.remove(key);
		if (field != null) {
			try {
				if (!mIsAccessible || (field.getModifiers() & Modifier.PUBLIC) == 0) {
					field.setAccessible(true);
				}
				field.set(null, value);
			} catch (Exception e) {
				System.err.println("Unable to set value of localized message for '" + key + "' in " + mBundleName);
			}
		} else {
			System.err.println("Unused localized message for '" + key + "' in " + mBundleName);
		}
		return null;
	}
}
