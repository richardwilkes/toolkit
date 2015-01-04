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
import com.trollworks.toolkit.io.UrlUtils;
import com.trollworks.toolkit.utility.Geometry;
import com.trollworks.toolkit.utility.Text;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

/** Json utilities. */
public class Json {
	private Reader	mReader;
	private int		mIndex;
	private int		mCharacter	= 1;
	private int		mLine		= 1;
	private char	mPrevious;
	private boolean	mEOF;
	private boolean	mUsePrevious;

	/**
	 * @param reader A {@link Reader} to load JSON data from.
	 * @return The result of loading the data.
	 */
	public static final Object parse(Reader reader) throws IOException {
		return new Json(reader).nextValue();
	}

	/**
	 * @param url A {@link URL} to load JSON data from.
	 * @return The result of loading the data.
	 */
	public static final Object parse(URL url) throws IOException {
		try (InputStream in = UrlUtils.setupConnection(url).getInputStream()) {
			return parse(in);
		}
	}

	/**
	 * @param stream An {@link InputStream} to load JSON data from. {@link Text#UTF8_ENCODING} will
	 *            be used as the encoding when reading from the stream.
	 * @return The result of loading the data.
	 */
	public static final Object parse(InputStream stream) throws IOException {
		return parse(stream, Text.UTF8_ENCODING);
	}

	/**
	 * @param stream An {@link InputStream} to load JSON data from.
	 * @param encoding The character encoding to use when reading from the stream.
	 * @return The result of loading the data.
	 */
	public static final Object parse(InputStream stream, String encoding) throws IOException {
		return parse(new InputStreamReader(stream, encoding));
	}

	/**
	 * @param string A {@link String} to load JSON data from.
	 * @return The result of loading the data.
	 */
	public static final Object parse(String string) throws IOException {
		return parse(new StringReader(string));
	}

	/**
	 * @param reader A {@link Reader} to load JSON data from.
	 * @return The result of loading the data if it is an array or <code>null</code> if it isn't.
	 */
	public static final JsonArray parseArray(Reader reader) throws IOException {
		Object result = parse(reader);
		return result instanceof JsonArray ? (JsonArray) result : null;
	}

	/**
	 * @param url A {@link URL} to load JSON data from.
	 * @return The result of loading the data if it is an array or <code>null</code> if it isn't.
	 */
	public static final JsonArray parseArray(URL url) throws IOException {
		Object result = parse(url);
		return result instanceof JsonArray ? (JsonArray) result : null;
	}

	/**
	 * @param stream An {@link InputStream} to load JSON data from. {@link Text#UTF8_ENCODING} will
	 *            be used as the encoding when reading from the stream.
	 * @return The result of loading the data if it is an array or <code>null</code> if it isn't.
	 */
	public static final JsonArray parseArray(InputStream stream) throws IOException {
		Object result = parse(stream);
		return result instanceof JsonArray ? (JsonArray) result : null;
	}

	/**
	 * @param stream An {@link InputStream} to load JSON data from.
	 * @param encoding The character encoding to use when reading from the stream.
	 * @return The result of loading the data if it is an array or <code>null</code> if it isn't.
	 */
	public static final JsonArray parseArray(InputStream stream, String encoding) throws IOException {
		Object result = parse(stream, encoding);
		return result instanceof JsonArray ? (JsonArray) result : null;
	}

	/**
	 * @param string A {@link String} to load JSON data from.
	 * @return The result of loading the data if it is an array or <code>null</code> if it isn't.
	 */
	public static final JsonArray parseArray(String string) throws IOException {
		Object result = parse(string);
		return result instanceof JsonArray ? (JsonArray) result : null;
	}

	/**
	 * @param reader A {@link Reader} to load JSON data from.
	 * @return The result of loading the data if it is a map or <code>null</code> if it isn't.
	 */
	public static final JsonMap parseMap(Reader reader) throws IOException {
		Object result = parse(reader);
		return result instanceof JsonMap ? (JsonMap) result : null;
	}

	/**
	 * @param url A {@link URL} to load JSON data from.
	 * @return The result of loading the data if it is a map or <code>null</code> if it isn't.
	 */
	public static final JsonMap parseMap(URL url) throws IOException {
		Object result = parse(url);
		return result instanceof JsonMap ? (JsonMap) result : null;
	}

	/**
	 * @param stream An {@link InputStream} to load JSON data from. {@link Text#UTF8_ENCODING} will
	 *            be used as the encoding when reading from the stream.
	 * @return The result of loading the data if it is a map or <code>null</code> if it isn't.
	 */
	public static final JsonMap parseMap(InputStream stream) throws IOException {
		Object result = parse(stream);
		return result instanceof JsonMap ? (JsonMap) result : null;
	}

	/**
	 * @param stream An {@link InputStream} to load JSON data from.
	 * @param encoding The character encoding to use when reading from the stream.
	 * @return The result of loading the data if it is a map or <code>null</code> if it isn't.
	 */
	public static final JsonMap parseMap(InputStream stream, String encoding) throws IOException {
		Object result = parse(stream, encoding);
		return result instanceof JsonMap ? (JsonMap) result : null;
	}

	/**
	 * @param string A {@link String} to load JSON data from.
	 * @return The result of loading the data if it is a map or <code>null</code> if it isn't.
	 */
	public static final JsonMap parseMap(String string) throws IOException {
		Object result = parse(string);
		return result instanceof JsonMap ? (JsonMap) result : null;
	}

	/**
	 * @param value The value to encode as a JSON string.
	 * @return The encoded {@link String}.
	 */
	public static final String toString(Object value) {
		if (JsonNull.INSTANCE.equals(value)) {
			return JsonNull.INSTANCE.toString();
		}
		if (value instanceof Number) {
			String str = value.toString();
			if (str.indexOf('.') > 0 && str.indexOf('e') < 0 && str.indexOf('E') < 0) {
				while (str.endsWith("0")) { //$NON-NLS-1$
					str = str.substring(0, str.length() - 1);
				}
				if (str.endsWith(".")) { //$NON-NLS-1$
					str = str.substring(0, str.length() - 1);
				}
			}
			return str;
		}
		if (value instanceof Boolean || value instanceof JsonMap || value instanceof JsonArray) {
			return value.toString();
		}
		if (value instanceof Map || value instanceof Collection || value.getClass().isArray()) {
			return wrap(value).toString();
		}
		if (value instanceof Point) {
			return quote(Geometry.toString((Point) value));
		}
		if (value instanceof Rectangle) {
			return quote(Geometry.toString((Rectangle) value));
		}
		return quote(value.toString());
	}

	/**
	 * @param object The object to wrap for storage inside a {@link JsonCollection}.
	 * @return The wrapped version of the object, which may be the original object passed in.
	 */
	public static final Object wrap(Object object) {
		if (JsonNull.INSTANCE.equals(object)) {
			return JsonNull.INSTANCE;
		}
		if (object instanceof JsonMap || object instanceof JsonArray || object instanceof Byte || object instanceof Character || object instanceof Short || object instanceof Integer || object instanceof Long || object instanceof Boolean || object instanceof Float || object instanceof Double || object instanceof String) {
			return object;
		}
		if (object instanceof Collection) {
			JsonArray array = new JsonArray();
			for (Object one : (Collection<?>) object) {
				array.put(wrap(one));
			}
			return array;
		}
		if (object.getClass().isArray()) {
			JsonArray array = new JsonArray();
			if (object instanceof Object[]) {
				Object[] objs = (Object[]) object;
				for (Object obj : objs) {
					array.put(wrap(obj));
				}
			} else if (object instanceof byte[]) {
				byte[] values = (byte[]) object;
				for (byte value : values) {
					array.put(wrap(Byte.valueOf(value)));
				}
			} else if (object instanceof char[]) {
				char[] values = (char[]) object;
				for (char value : values) {
					array.put(wrap(Character.valueOf(value)));
				}
			} else if (object instanceof short[]) {
				short[] values = (short[]) object;
				for (short value : values) {
					array.put(wrap(Short.valueOf(value)));
				}
			} else if (object instanceof int[]) {
				int[] values = (int[]) object;
				for (int value : values) {
					array.put(wrap(Integer.valueOf(value)));
				}
			} else if (object instanceof long[]) {
				long[] values = (long[]) object;
				for (long value : values) {
					array.put(wrap(Long.valueOf(value)));
				}
			} else if (object instanceof float[]) {
				float[] values = (float[]) object;
				for (float value : values) {
					array.put(wrap(Float.valueOf(value)));
				}
			} else if (object instanceof double[]) {
				double[] values = (double[]) object;
				for (double value : values) {
					array.put(wrap(Double.valueOf(value)));
				}
			} else if (object instanceof boolean[]) {
				boolean[] values = (boolean[]) object;
				for (boolean value : values) {
					array.put(wrap(Boolean.valueOf(value)));
				}
			}
			return array;
		}
		if (object instanceof Map) {
			JsonMap map = new JsonMap();
			for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
				map.put(entry.getKey().toString(), entry.getValue());
			}
			return map;
		}
		if (object instanceof Point) {
			return Geometry.toString((Point) object);
		}
		if (object instanceof Rectangle) {
			return Geometry.toString((Rectangle) object);
		}
		return object.toString();
	}

	/**
	 * @param string The string to quote.
	 * @return The quoted {@link String}, suitable for storage inside a JSON object.
	 */
	public static final String quote(String string) {
		int len;
		if (string == null || (len = string.length()) == 0) {
			return "\"\""; //$NON-NLS-1$
		}
		StringBuffer buffer = new StringBuffer(len + 4);
		buffer.append('"');
		char ch = 0;
		for (int i = 0; i < len; i++) {
			char last = ch;
			ch = string.charAt(i);
			switch (ch) {
				case '\\':
				case '"':
					buffer.append('\\');
					buffer.append(ch);
					break;
				case '/':
					if (last == '<') {
						buffer.append('\\');
					}
					buffer.append(ch);
					break;
				case '\b':
					buffer.append("\\b"); //$NON-NLS-1$
					break;
				case '\t':
					buffer.append("\\t"); //$NON-NLS-1$
					break;
				case '\n':
					buffer.append("\\n"); //$NON-NLS-1$
					break;
				case '\f':
					buffer.append("\\f"); //$NON-NLS-1$
					break;
				case '\r':
					buffer.append("\\r"); //$NON-NLS-1$
					break;
				default:
					if (ch < ' ' || ch >= '\u0080' && ch < '\u00a0' || ch >= '\u2000' && ch < '\u2100') {
						String hex = "000" + Integer.toHexString(ch); //$NON-NLS-1$
						buffer.append("\\u" + hex.substring(hex.length() - 4)); //$NON-NLS-1$
					} else {
						buffer.append(ch);
					}
					break;
			}
		}
		buffer.append('"');
		return buffer.toString();
	}

	private Json(Reader reader) {
		mReader = reader;
	}

	private char next() throws IOException {
		int c;
		if (mUsePrevious) {
			mUsePrevious = false;
			c = mPrevious;
		} else {
			c = mReader.read();
			if (c <= 0) { // End of stream
				mEOF = true;
				c = 0;
			}
		}
		mIndex++;
		if (mPrevious == '\r') {
			mLine++;
			mCharacter = c == '\n' ? 0 : 1;
		} else if (c == '\n') {
			mLine++;
			mCharacter = 0;
		} else {
			mCharacter++;
		}
		mPrevious = (char) c;
		return mPrevious;
	}

	private char nextSkippingWhitespace() throws IOException {
		for (;;) {
			char c = next();
			if (c == 0 || c > ' ') {
				return c;
			}
		}
	}

	private Object nextValue() throws IOException {
		char c = nextSkippingWhitespace();
		String s;

		switch (c) {
			case '"':
			case '\'':
				return nextString(c);
			case '{':
				back();
				return nextMap();
			case '[':
			case '(':
				back();
				return nextArray();
			default:
				break;
		}

		StringBuffer sb = new StringBuffer();
		while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) { //$NON-NLS-1$
			sb.append(c);
			c = next();
		}
		back();

		s = sb.toString().trim();
		if (s.equals("")) { //$NON-NLS-1$
			throw syntaxError("Missing value"); //$NON-NLS-1$
		}
		if (s.equalsIgnoreCase("true")) { //$NON-NLS-1$
			return Boolean.TRUE;
		}
		if (s.equalsIgnoreCase("false")) { //$NON-NLS-1$
			return Boolean.FALSE;
		}
		if (s.equalsIgnoreCase("null")) { //$NON-NLS-1$
			return JsonNull.INSTANCE;
		}

		char b = s.charAt(0);
		if (b >= '0' && b <= '9' || b == '.' || b == '-' || b == '+') {
			if (b == '0' && s.length() > 2 && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
				try {
					return new Integer(Integer.parseInt(s.substring(2), 16));
				} catch (Exception ignore) {
					Log.error(ignore);
				}
			}
			try {
				if (s.indexOf('.') > -1 || s.indexOf('e') > -1 || s.indexOf('E') > -1) {
					return Double.valueOf(s);
				}
				Long myLong = new Long(s);
				if (myLong.longValue() == myLong.intValue()) {
					return new Integer(myLong.intValue());
				}
				return myLong;
			} catch (Exception ignore) {
				Log.error(ignore);
			}
		}
		return s;
	}

	private JsonArray nextArray() throws IOException {
		char c = nextSkippingWhitespace();
		char q;
		if (c == '[') {
			q = ']';
		} else if (c == '(') {
			q = ')';
		} else {
			throw syntaxError("A JSONArray text must start with '['"); //$NON-NLS-1$
		}
		JsonArray array = new JsonArray();
		if (nextSkippingWhitespace() == ']') {
			return array;
		}
		back();
		for (;;) {
			if (nextSkippingWhitespace() == ',') {
				back();
				array.put((Object) null);
			} else {
				back();
				array.put(nextValue());
			}
			c = nextSkippingWhitespace();
			switch (c) {
				case ';':
				case ',':
					if (nextSkippingWhitespace() == ']') {
						return array;
					}
					back();
					break;
				case ']':
				case ')':
					if (q != c) {
						throw syntaxError("Expected a '" + new Character(q) + "'"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					return array;
				default:
					throw syntaxError("Expected a ',' or ']'"); //$NON-NLS-1$
			}
		}
	}

	private JsonMap nextMap() throws IOException {
		char c;
		String key;

		if (nextSkippingWhitespace() != '{') {
			throw syntaxError("A JSONObject text must begin with '{'"); //$NON-NLS-1$
		}
		JsonMap map = new JsonMap();
		while (true) {
			c = nextSkippingWhitespace();
			switch (c) {
				case 0:
					throw syntaxError("A JSONObject text must end with '}'"); //$NON-NLS-1$
				case '}':
					return map;
				default:
					back();
					key = nextValue().toString();
			}

			c = nextSkippingWhitespace();
			if (c == '=') {
				if (next() != '>') {
					back();
				}
			} else if (c != ':') {
				throw syntaxError("Expected a ':' after a key"); //$NON-NLS-1$
			}
			if (map.has(key)) {
				throw new IOException("Duplicate key \"" + key + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			}
			map.put(key, nextValue());

			switch (nextSkippingWhitespace()) {
				case ';':
				case ',':
					if (nextSkippingWhitespace() == '}') {
						return map;
					}
					back();
					break;
				case '}':
					return map;
				default:
					throw syntaxError("Expected a ',' or '}'"); //$NON-NLS-1$
			}
		}
	}

	private String nextString(char quote) throws IOException {
		char c;
		StringBuffer buffer = new StringBuffer();
		for (;;) {
			c = next();
			switch (c) {
				case 0:
				case '\n':
				case '\r':
					throw syntaxError("Unterminated string"); //$NON-NLS-1$
				case '\\':
					c = next();
					switch (c) {
						case 'b':
							buffer.append('\b');
							break;
						case 't':
							buffer.append('\t');
							break;
						case 'n':
							buffer.append('\n');
							break;
						case 'f':
							buffer.append('\f');
							break;
						case 'r':
							buffer.append('\r');
							break;
						case 'u':
							buffer.append((char) Integer.parseInt(next(4), 16));
							break;
						case '"':
						case '\'':
						case '\\':
						case '/':
							buffer.append(c);
							break;
						default:
							throw syntaxError("Illegal escape."); //$NON-NLS-1$
					}
					break;
				default:
					if (c == quote) {
						return buffer.toString();
					}
					buffer.append(c);
			}
		}
	}

	private void back() {
		if (mUsePrevious || mIndex <= 0) {
			throw new IllegalStateException("Stepping back two steps is not supported"); //$NON-NLS-1$
		}
		mIndex--;
		mCharacter--;
		mUsePrevious = true;
		mEOF = false;
	}

	private String next(int n) throws IOException {
		if (n == 0) {
			return ""; //$NON-NLS-1$
		}

		char[] buffer = new char[n];
		int pos = 0;

		while (pos < n) {
			buffer[pos] = next();
			if (mEOF && !mUsePrevious) {
				throw syntaxError("Substring bounds error"); //$NON-NLS-1$
			}
			pos++;
		}
		return new String(buffer);
	}

	private IOException syntaxError(String message) {
		return new IOException(message + toString());
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return " at " + mIndex + " [character " + mCharacter + " line " + mLine + "]";
	}
}
