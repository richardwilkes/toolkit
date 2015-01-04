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

package com.trollworks.toolkit.io.json;

import com.trollworks.toolkit.io.Log;
import com.trollworks.toolkit.utility.Geometry;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Represents a map in JSON. */
public class JsonMap extends JsonCollection {
	private Map<String, Object>	mMap	= new HashMap<>();

	/**
	 * @param key The key to check for.
	 * @return <code>true</code> if the key is present in the map.
	 */
	public boolean has(String key) {
		return mMap.containsKey(key);
	}

	/**
	 * @return The set of keys in this map.
	 */
	public Set<String> keySet() {
		return mMap.keySet();
	}

	/**
	 * @param key The key to retrieve.
	 * @return The value associated with the key or <code>null</code> if no key matches.
	 */
	public Object get(String key) {
		return key == null ? null : mMap.get(key);
	}

	/**
	 * @param key The key to retrieve.
	 * @return The value associated with the key or an empty string if no key matches.
	 */
	public String getString(String key) {
		Object value = get(key);
		return value != null ? value.toString() : ""; //$NON-NLS-1$
	}

	/**
	 * @param key The key to retrieve.
	 * @return The value associated with the key or <code>false</code> if no key matches or the
	 *         value cannot be converted to a boolean.
	 */
	public boolean getBoolean(String key) {
		Object value = get(key);
		if (Boolean.TRUE.equals(value) || value instanceof String && "true".equalsIgnoreCase((String) value)) { //$NON-NLS-1$
			return true;
		}
		return false;
	}

	/**
	 * @param key The key to retrieve.
	 * @return The value associated with the key or zero if no key matches or the value cannot be
	 *         converted to an integer.
	 */
	public int getInt(String key) {
		Object value = get(key);
		try {
			return value instanceof Number ? ((Number) value).intValue() : Integer.parseInt((String) value);
		} catch (Exception exception) {
			return 0;
		}
	}

	/**
	 * @param key The key to retrieve.
	 * @return The value associated with the key or zero if no key matches or the value cannot be
	 *         converted to a long.
	 */
	public long getLong(String key) {
		Object value = get(key);
		try {
			return value instanceof Number ? ((Number) value).longValue() : Long.parseLong((String) value);
		} catch (Exception exception) {
			return 0;
		}
	}

	/**
	 * @param key The key to retrieve.
	 * @return The value associated with the key or zero if no key matches or the value cannot be
	 *         converted to a double.
	 */
	public double getDouble(String key) {
		Object value = get(key);
		try {
			return value instanceof Number ? ((Number) value).doubleValue() : Double.valueOf((String) value).doubleValue();
		} catch (Exception exception) {
			return 0;
		}
	}

	/**
	 * @param key The key to retrieve.
	 * @return The value associated with the key or an empty array if no key matches or the value
	 *         cannot be converted to a {@link JsonArray}.
	 */
	public JsonArray getArray(String key) {
		Object value = get(key);
		if (value instanceof JsonArray) {
			return (JsonArray) value;
		}
		return new JsonArray();
	}

	/**
	 * @param key The key to retrieve.
	 * @return The value associated with the key or an empty map if no key matches or the value
	 *         cannot be converted to a {@link JsonMap}.
	 */
	public JsonMap getMap(String key) {
		Object value = get(key);
		if (value instanceof JsonMap) {
			return (JsonMap) value;
		}
		return new JsonMap();
	}

	/**
	 * @param key The key to retrieve.
	 * @return The value associated with the key or a {@link Point} with a value of <code>0,0</code>
	 *         if no key matches or the value cannot be converted to a {@link Point}.
	 */
	public Point getPoint(String key) {
		try {
			return Geometry.toPoint(getString(key));
		} catch (NumberFormatException nfe) {
			return new Point();
		}
	}

	/**
	 * @param key The key to retrieve.
	 * @return The value associated with the key or a {@link Rectangle} with a value of
	 *         <code>0,0,0,0</code> if no key matches or the value cannot be converted to a
	 *         {@link Rectangle}.
	 */
	public Rectangle getRectangle(String key) {
		try {
			return Geometry.toRectangle(getString(key));
		} catch (NumberFormatException nfe) {
			return new Rectangle();
		}
	}

	/**
	 * @param key The key to store the value with.
	 * @param value The value to store.
	 */
	public void put(String key, Object value) {
		if (key != null) {
			mMap.put(key, Json.wrap(value));
		}
	}

	/**
	 * @param key The key to store the value with.
	 * @param value The value to store.
	 */
	public void put(String key, boolean value) {
		put(key, Boolean.valueOf(value));
	}

	/**
	 * @param key The key to store the value with.
	 * @param value The value to store.
	 */
	public void put(String key, short value) {
		put(key, Short.valueOf(value));
	}

	/**
	 * @param key The key to store the value with.
	 * @param value The value to store.
	 */
	public void put(String key, int value) {
		put(key, Integer.valueOf(value));
	}

	/**
	 * @param key The key to store the value with.
	 * @param value The value to store.
	 */
	public void put(String key, long value) {
		put(key, Long.valueOf(value));
	}

	/**
	 * @param key The key to store the value with.
	 * @param value The value to store.
	 */
	public void put(String key, float value) {
		put(key, Float.valueOf(value));
	}

	/**
	 * @param key The key to store the value with.
	 * @param value The value to store.
	 */
	public void put(String key, double value) {
		put(key, Double.valueOf(value));
	}

	/** @param key The key to remove from the map. */
	public Object remove(String key) {
		return mMap.remove(key);
	}

	@Override
	public void appendTo(Appendable out) {
		try {
			boolean needComma = false;
			out.append('{');
			for (Map.Entry<String, Object> entry : mMap.entrySet()) {
				if (needComma) {
					out.append(',');
				} else {
					needComma = true;
				}
				out.append(Json.quote(entry.getKey()));
				out.append(':');
				Object v = entry.getValue();
				if (v instanceof JsonCollection) {
					((JsonCollection) v).appendTo(out);
				} else {
					out.append(Json.toString(v));
				}
			}
			out.append('}');
		} catch (IOException exception) {
			Log.error(exception);
		}
	}
}
