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
import java.util.ArrayList;
import java.util.List;

/** Represents an array in JSON. */
public class JsonArray extends JsonCollection {
	private List<Object>	mList	= new ArrayList<>();

	/**
	 * @param index The index to retrieve.
	 * @return The value associated with the index or <code>null</code> if no such index exists.
	 */
	public Object get(int index) {
		return index < 0 || index >= size() ? null : mList.get(index);
	}

	/**
	 * @param index The index to retrieve.
	 * @return The value associated with the index or an empty string if no such index exists.
	 */
	public String getString(int index) {
		Object value = get(index);
		return value != null ? value.toString() : ""; //$NON-NLS-1$
	}

	/**
	 * @param index The index to retrieve.
	 * @return The value associated with the index or <code>false</code> if no such index exists or
	 *         the value cannot be converted to a boolean.
	 */
	public boolean getBoolean(int index) {
		Object value = get(index);
		if (Boolean.TRUE.equals(value) || value instanceof String && "true".equalsIgnoreCase((String) value)) { //$NON-NLS-1$
			return true;
		}
		return false;
	}

	/**
	 * @param index The index to retrieve.
	 * @return The value associated with the index or zero if no such index exists or the value
	 *         cannot be converted to an integer.
	 */
	public int getInt(int index) {
		Object value = get(index);
		try {
			return value instanceof Number ? ((Number) value).intValue() : Integer.parseInt((String) value);
		} catch (Exception exception) {
			return 0;
		}
	}

	/**
	 * @param index The index to retrieve.
	 * @return The value associated with the index or zero if no such index exists or the value
	 *         cannot be converted to a long.
	 */
	public long getLong(int index) {
		Object value = get(index);
		try {
			return value instanceof Number ? ((Number) value).longValue() : Long.parseLong((String) value);
		} catch (Exception exception) {
			return 0;
		}
	}

	/**
	 * @param index The index to retrieve.
	 * @return The value associated with the index or zero if no such index exists or the value
	 *         cannot be converted to a double.
	 */
	public double getDouble(int index) {
		Object value = get(index);
		try {
			return value instanceof Number ? ((Number) value).doubleValue() : Double.valueOf((String) value).doubleValue();
		} catch (Exception exception) {
			return 0;
		}
	}

	/**
	 * @param index The index to retrieve.
	 * @return The value associated with the index or an empty array if no such index exists or the
	 *         value cannot be converted to a {@link JsonArray}.
	 */
	public JsonArray getArray(int index) {
		Object value = get(index);
		if (value instanceof JsonArray) {
			return (JsonArray) value;
		}
		return new JsonArray();
	}

	/**
	 * @param index The index to retrieve.
	 * @return The value associated with the index or an empty map if no such index exists or the
	 *         value cannot be converted to a {@link JsonMap}.
	 */
	public JsonMap getMap(int index) {
		Object value = get(index);
		if (value instanceof JsonMap) {
			return (JsonMap) value;
		}
		return new JsonMap();
	}

	/**
	 * @param index The index to retrieve.
	 * @return The value associated with the index or a {@link Point} with a value of
	 *         <code>0,0</code> if no such index exists or the value cannot be converted to a
	 *         {@link Point}.
	 */
	public Point getPoint(int index) {
		try {
			return Geometry.toPoint(getString(index));
		} catch (Exception exception) {
			return new Point();
		}
	}

	/**
	 * @param index The index to retrieve.
	 * @return The value associated with the index or a {@link Rectangle} with a value of
	 *         <code>0,0,0,0</code> if no such index exists or the value cannot be converted to a
	 *         {@link Rectangle}.
	 */
	public Rectangle getRectangle(int index) {
		try {
			return Geometry.toRectangle(getString(index));
		} catch (Exception exception) {
			return new Rectangle();
		}
	}

	/** @return The number of elements in the array. */
	public int size() {
		return mList.size();
	}

	/**
	 * Adds a value to the end of the array.
	 *
	 * @param value The value to store.
	 */
	public void put(Object value) {
		mList.add(Json.wrap(value));
	}

	/**
	 * Adds a value to the end of the array.
	 *
	 * @param value The value to store.
	 */
	public void put(boolean value) {
		put(Boolean.valueOf(value));
	}

	/**
	 * Adds a value to the end of the array.
	 *
	 * @param value The value to store.
	 */
	public void put(short value) {
		put(Short.valueOf(value));
	}

	/**
	 * Adds a value to the end of the array.
	 *
	 * @param value The value to store.
	 */
	public void put(int value) {
		put(Integer.valueOf(value));
	}

	/**
	 * Adds a value to the end of the array.
	 *
	 * @param value The value to store.
	 */
	public void put(long value) {
		put(Long.valueOf(value));
	}

	/**
	 * Adds a value to the end of the array.
	 *
	 * @param value The value to store.
	 */
	public void put(float value) {
		put(Float.valueOf(value));
	}

	/**
	 * Adds a value to the end of the array.
	 *
	 * @param value The value to store.
	 */
	public void put(double value) {
		put(Double.valueOf(value));
	}

	/**
	 * Adds a value to the array.
	 *
	 * @param index The index to insert the value at. Must be greater than or equal to zero. If the
	 *            index is past the end of the current set of values, <code>null</code>'s will be
	 *            inserted as padding.
	 * @param value The value to store.
	 */
	public void put(int index, Object value) {
		if (index >= 0) {
			value = Json.wrap(value);
			if (index < size()) {
				mList.set(index, value);
			} else {
				while (index != size()) {
					put(JsonNull.INSTANCE);
				}
				put(value);
			}
		}
	}

	/**
	 * Adds a value to the array.
	 *
	 * @param index The index to insert the value at. Must be greater than or equal to zero. If the
	 *            index is past the end of the current set of values, <code>null</code>'s will be
	 *            inserted as padding.
	 * @param value The value to store.
	 */
	public void put(int index, boolean value) {
		put(index, Boolean.valueOf(value));
	}

	/**
	 * Adds a value to the array.
	 *
	 * @param index The index to insert the value at. Must be greater than or equal to zero. If the
	 *            index is past the end of the current set of values, <code>null</code>'s will be
	 *            inserted as padding.
	 * @param value The value to store.
	 */
	public void put(int index, short value) {
		put(index, Short.valueOf(value));
	}

	/**
	 * Adds a value to the array.
	 *
	 * @param index The index to insert the value at. Must be greater than or equal to zero. If the
	 *            index is past the end of the current set of values, <code>null</code>'s will be
	 *            inserted as padding.
	 * @param value The value to store.
	 */
	public void put(int index, int value) {
		put(index, Integer.valueOf(value));
	}

	/**
	 * Adds a value to the array.
	 *
	 * @param index The index to insert the value at. Must be greater than or equal to zero. If the
	 *            index is past the end of the current set of values, <code>null</code>'s will be
	 *            inserted as padding.
	 * @param value The value to store.
	 */
	public void put(int index, long value) {
		put(index, Long.valueOf(value));
	}

	/**
	 * Adds a value to the array.
	 *
	 * @param index The index to insert the value at. Must be greater than or equal to zero. If the
	 *            index is past the end of the current set of values, <code>null</code>'s will be
	 *            inserted as padding.
	 * @param value The value to store.
	 */
	public void put(int index, float value) {
		put(index, Float.valueOf(value));
	}

	/**
	 * Adds a value to the array.
	 *
	 * @param index The index to insert the value at. Must be greater than or equal to zero. If the
	 *            index is past the end of the current set of values, <code>null</code>'s will be
	 *            inserted as padding.
	 * @param value The value to store.
	 */
	public void put(int index, double value) {
		put(index, Double.valueOf(value));
	}

	@Override
	public void appendTo(Appendable out) {
		try {
			boolean needComma = false;
			int len = size();
			out.append('[');
			for (int i = 0; i < len; i++) {
				if (needComma) {
					out.append(',');
				} else {
					needComma = true;
				}
				Object v = mList.get(i);
				if (v instanceof JsonCollection) {
					((JsonCollection) v).appendTo(out);
				} else {
					out.append(Json.toString(v));
				}
			}
			out.append(']');
		} catch (IOException exception) {
			Log.error(exception);
		}
	}
}
