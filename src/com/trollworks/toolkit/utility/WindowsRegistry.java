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

package com.trollworks.toolkit.utility;

import com.trollworks.toolkit.io.Log;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/** Provides access to the Windows Registry. */
@SuppressWarnings("nls")
public class WindowsRegistry {
	private static final Integer	READ_ACCESS			= Integer.valueOf(0x20019);
	private static final Integer	ALL_ACCESS			= Integer.valueOf(0xF003F);
	private static Method			METHOD_OPEN_KEY;
	private static Method			METHOD_CLOSE_KEY;
	private static Method			METHOD_QUERY_VALUE_EX;
	private static Method			METHOD_ENUM_VALUE;
	private static Method			METHOD_QUERY_INFO;
	private static Method			METHOD_ENUM_KEY_EX;
	private static Method			METHOD_CREATE_KEY_EX;
	private static Method			METHOD_SET_VALUE_EX;
	private static Method			METHOD_DELETE_KEY;
	private static Method			METHOD_DELETE_VALUE;
	private static String			SOFTWARE_CLASSES	= "Software\\Classes\\";
	private Preferences				mRoot;
	private Integer					mRootKey;

	/**
	 * Adds registry entries for mapping file extensions to an application and icons to files with
	 * those extensions.
	 *
	 * @param prefix The registry prefix to use.
	 * @param map A map of extensions (no leading period) to descriptions.
	 * @param appFile The application to execute for these file extensions.
	 * @param iconDir The icon directory, where Windows .ico files can be found for each extension,
	 *            in the form 'extension.ico'. For example, the extension 'xyz' would need an icon
	 *            file named 'xyz.ico' in this directory.
	 */
	public static final void register(String prefix, HashMap<String, String> map, Path appFile, Path iconDir) {
		String appPath = appFile.normalize().toAbsolutePath().toString();
		WindowsRegistry reg = new WindowsRegistry(true);
		for (String extension : map.keySet()) {
			// Make the entry that points to the app's information for the extension
			String key = SOFTWARE_CLASSES + "." + extension;
			reg.deleteKey(key);
			reg.createKey(key);
			String appKey = prefix + "_" + extension.toUpperCase();
			reg.writeStringValue(key, "", appKey);

			// Make the entry for the extension
			String baseKey = SOFTWARE_CLASSES + appKey;
			reg.deleteKey(baseKey);
			reg.createKey(baseKey);
			reg.writeStringValue(baseKey, "", map.get(extension));
			key = baseKey + "\\DefaultIcon";
			reg.deleteKey(key);
			reg.createKey(key);
			reg.writeStringValue(key, "", "\"" + iconDir.resolve(extension + ".ico").normalize().toAbsolutePath().toString() + "\"");
			key = baseKey + "\\shell";
			reg.deleteKey(key);
			reg.createKey(key);
			reg.writeStringValue(key, "", "open");
			key += "\\open";
			reg.deleteKey(key);
			reg.createKey(key);
			reg.writeStringValue(key, "", "&Open");
			key += "\\command";
			reg.deleteKey(key);
			reg.createKey(key);
			reg.writeStringValue(key, "", "\"" + appPath + "\" \"%1\"");
		}
	}

	public WindowsRegistry(boolean user) {
		if (!Platform.isWindows()) {
			throw new IllegalStateException("Only avaliable on a Windows platform");
		}
		if (user) {
			mRoot = Preferences.userRoot();
			mRootKey = Integer.valueOf(0x80000001);
		} else {
			mRoot = Preferences.systemRoot();
			mRootKey = Integer.valueOf(0x80000002);
		}
	}

	private static Method getMethod(Class<?> theClass, String methodName, Class<?>... params) {
		try {
			Method method = theClass.getDeclaredMethod(methodName, params);
			method.setAccessible(true);
			return method;
		} catch (Exception exception) {
			Log.error(exception);
			return null;
		}
	}

	class OpenResult {
		Integer	mHandle;
		boolean	mSuccess;

		OpenResult() {
			//
		}

		OpenResult(int[] result) {
			mHandle = Integer.valueOf(result[0]);
			mSuccess = result[1] == 0;
		}
	}

	private OpenResult openKey(String key, Integer accessType) {
		if (METHOD_OPEN_KEY == null) {
			METHOD_OPEN_KEY = getMethod(mRoot.getClass(), "WindowsRegOpenKey", int.class, byte[].class, int.class);
		}
		try {
			return new OpenResult((int[]) METHOD_OPEN_KEY.invoke(mRoot, new Object[] { mRootKey, toCstr(key), accessType }));
		} catch (Exception exception) {
			Log.error(exception);
			return new OpenResult();
		}
	}

	private void closeKey(Integer handle) {
		if (METHOD_CLOSE_KEY == null) {
			METHOD_CLOSE_KEY = getMethod(mRoot.getClass(), "WindowsRegCloseKey", int.class);
		}
		try {
			METHOD_CLOSE_KEY.invoke(mRoot, new Object[] { handle });
		} catch (Exception exception) {
			Log.error(exception);
		}
	}

	class QueryResult {
		int	mCount;
		int	mMaxValueLength;

		QueryResult() {
			//
		}

		QueryResult(int[] result) {
			mCount = result[0];
			mMaxValueLength = result[3];
		}
	}

	private QueryResult queryInfoKey(Integer handle) {
		if (METHOD_QUERY_INFO == null) {
			METHOD_QUERY_INFO = getMethod(mRoot.getClass(), "WindowsRegQueryInfoKey1", int.class);
		}
		try {
			return new QueryResult((int[]) METHOD_QUERY_INFO.invoke(mRoot, new Object[] { handle }));
		} catch (Exception exception) {
			Log.error(exception);
			return new QueryResult();
		}
	}

	public String readString(String key, String valueName) {
		try {
			OpenResult openResult = openKey(key, READ_ACCESS);
			if (openResult.mSuccess) {
				if (METHOD_QUERY_VALUE_EX == null) {
					METHOD_QUERY_VALUE_EX = getMethod(mRoot.getClass(), "WindowsRegQueryValueEx", int.class, byte[].class);
				}
				byte[] bytes = (byte[]) METHOD_QUERY_VALUE_EX.invoke(mRoot, new Object[] { openResult.mHandle, toCstr(valueName) });
				closeKey(openResult.mHandle);
				return bytes != null ? new String(bytes) : null;
			}
		} catch (Exception exception) {
			Log.error(exception);
		}
		return null;
	}

	public Map<String, String> readStringValues(String key) {
		try {
			HashMap<String, String> results = new HashMap<>();
			OpenResult openResult = openKey(key, READ_ACCESS);
			if (openResult.mSuccess) {
				QueryResult queryResult = queryInfoKey(openResult.mHandle);
				for (int i = 0; i < queryResult.mCount; i++) {
					if (METHOD_ENUM_VALUE == null) {
						METHOD_ENUM_VALUE = getMethod(mRoot.getClass(), "WindowsRegEnumValue", int.class, int.class, int.class);
					}
					byte[] name = (byte[]) METHOD_ENUM_VALUE.invoke(mRoot, new Object[] { openResult.mHandle, Integer.valueOf(i), Integer.valueOf(queryResult.mMaxValueLength + 1) });
					String value = readString(key, new String(name));
					results.put(new String(name), value);
				}
				closeKey(openResult.mHandle);
				return results;
			}
		} catch (Exception exception) {
			Log.error(exception);
		}
		return null;
	}

	public List<String> readStringSubKeys(String key) {
		try {
			List<String> results = new ArrayList<>();
			OpenResult openResult = openKey(key, READ_ACCESS);
			if (openResult.mSuccess) {
				QueryResult queryResult = queryInfoKey(openResult.mHandle);
				for (int i = 0; i < queryResult.mCount; i++) {
					if (METHOD_ENUM_KEY_EX == null) {
						METHOD_ENUM_KEY_EX = getMethod(mRoot.getClass(), "WindowsRegEnumKeyEx", int.class, int.class, int.class);
					}
					byte[] name = (byte[]) METHOD_ENUM_KEY_EX.invoke(mRoot, new Object[] { openResult.mHandle, Integer.valueOf(i), Integer.valueOf(queryResult.mMaxValueLength + 1) });
					results.add(new String(name));
				}
				closeKey(openResult.mHandle);
			}
			return results;
		} catch (Exception exception) {
			Log.error(exception);
		}
		return null;
	}

	public boolean createKey(String key) {
		try {
			if (METHOD_CREATE_KEY_EX == null) {
				METHOD_CREATE_KEY_EX = getMethod(mRoot.getClass(), "WindowsRegCreateKeyEx", int.class, byte[].class);
			}
			OpenResult result = new OpenResult((int[]) METHOD_CREATE_KEY_EX.invoke(mRoot, new Object[] { mRootKey, toCstr(key) }));
			if (result.mSuccess) {
				closeKey(result.mHandle);
				return true;
			}
		} catch (Exception exception) {
			Log.error(exception);
		}
		return false;
	}

	public boolean writeStringValue(String key, String valueName, String value) {
		try {
			OpenResult openResult = openKey(key, ALL_ACCESS);
			if (openResult.mSuccess) {
				if (METHOD_SET_VALUE_EX == null) {
					METHOD_SET_VALUE_EX = getMethod(mRoot.getClass(), "WindowsRegSetValueEx", int.class, byte[].class, byte[].class);
				}
				METHOD_SET_VALUE_EX.invoke(mRoot, new Object[] { openResult.mHandle, toCstr(valueName), toCstr(value) });
				closeKey(openResult.mHandle);
				return true;
			}
		} catch (Exception exception) {
			Log.error(exception);
		}
		return false;
	}

	public boolean deleteKey(String key) {
		try {
			if (METHOD_DELETE_KEY == null) {
				METHOD_DELETE_KEY = getMethod(mRoot.getClass(), "WindowsRegDeleteKey", int.class, byte[].class);
			}
			return ((Integer) METHOD_DELETE_KEY.invoke(mRoot, new Object[] { mRootKey, toCstr(key) })).intValue() == 0;
		} catch (Exception exception) {
			Log.error(exception);
		}
		return false;
	}

	public boolean deleteValue(String key, String value) {
		try {
			OpenResult openResult = openKey(key, ALL_ACCESS);
			if (openResult.mSuccess) {
				if (METHOD_DELETE_VALUE == null) {
					METHOD_DELETE_VALUE = getMethod(mRoot.getClass(), "WindowsRegDeleteValue", int.class, byte[].class);
				}
				int result = ((Integer) METHOD_DELETE_VALUE.invoke(mRoot, new Object[] { openResult.mHandle, toCstr(value) })).intValue();
				closeKey(openResult.mHandle);
				return result == 0;
			}
		} catch (Exception exception) {
			Log.error(exception);
		}
		return false;
	}

	private static byte[] toCstr(String str) {
		int length = str.length();
		byte[] result = new byte[length + 1];
		for (int i = 0; i < length; i++) {
			result[i] = (byte) str.charAt(i);
		}
		result[length] = 0;
		return result;
	}
}
