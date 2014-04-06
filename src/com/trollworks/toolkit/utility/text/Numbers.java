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

package com.trollworks.toolkit.utility.text;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Date;

/** Utilities for handling numbers in text form. */
public class Numbers {
	private static final String			HEX_PREFIX	= "0x"; //$NON-NLS-1$
	private static final DecimalFormat	NUMBER_FORMAT;
	private static final DecimalFormat	NUMBER_PLUS_FORMAT;

	static {
		NUMBER_FORMAT = (DecimalFormat) NumberFormat.getNumberInstance();
		NUMBER_FORMAT.setMaximumFractionDigits(5);

		NUMBER_PLUS_FORMAT = (DecimalFormat) NUMBER_FORMAT.clone();
		NUMBER_PLUS_FORMAT.setPositivePrefix("+"); //$NON-NLS-1$
	}

	/**
	 * @param buffer The string to convert.
	 * @return The value of the string.
	 */
	public static boolean getBoolean(String buffer) {
		buffer = normalize(buffer);
		return "true".equalsIgnoreCase(buffer) || "yes".equalsIgnoreCase(buffer) || "on".equalsIgnoreCase(buffer) || "1".equals(buffer); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	/**
	 * @param buffer The string to convert.
	 * @return The value of the string.
	 */
	public static short getShort(String buffer) {
		return getShort(buffer, (short) 0);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @return The value of the string.
	 */
	public static short getShort(String buffer, short def) {
		return getShort(buffer, def, Short.MIN_VALUE, Short.MAX_VALUE);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @param min The minimum value permitted.
	 * @param max The maximum value permitted.
	 * @return The value of the string.
	 */
	public static short getShort(String buffer, short def, short min, short max) {
		int radix = 10;
		buffer = normalize(buffer);
		if (buffer != null && buffer.startsWith(HEX_PREFIX)) {
			buffer = buffer.substring(HEX_PREFIX.length());
			radix = 16;
		}
		return getShort(buffer, radix, def, min, max);
	}

	/**
	 * @param buffer The string to convert.
	 * @param radix The radix to use during conversion.
	 * @param def The default value to use if extraction fails.
	 * @param min The minimum value permitted.
	 * @param max The maximum value permitted.
	 * @return The value of the string.
	 */
	public static short getShort(String buffer, int radix, short def, short min, short max) {
		try {
			buffer = normalize(buffer);
			// Special-case hex values, since the standard output generates unsigned values, some of
			// which cannot be loaded by the standard parser
			if (radix == 16 && buffer.length() == 4) {
				Character.digit(buffer.charAt(0), radix);
				Short.parseShort(buffer.substring(1), radix);
			} else {
				def = Short.parseShort(normalize(buffer), radix);
			}
		} catch (Exception exception) {
			// Ignore
		}
		if (min > def) {
			def = min;
		} else if (max < def) {
			def = max;
		}
		return def;
	}

	/**
	 * @param buffer The string to convert.
	 * @return The value of the string.
	 */
	public static short getLocalizedShort(String buffer) {
		return getLocalizedShort(buffer, (short) 0);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @return The value of the string.
	 */
	public static short getLocalizedShort(String buffer, short def) {
		return getLocalizedShort(buffer, def, Short.MIN_VALUE, Short.MAX_VALUE);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @param min The minimum value permitted.
	 * @param max The maximum value permitted.
	 * @return The value of the string.
	 */
	public static short getLocalizedShort(String buffer, short def, short min, short max) {
		try {
			def = NUMBER_FORMAT.parse(normalize(buffer)).shortValue();
		} catch (Exception exception) {
			// Ignore
		}
		if (min > def) {
			def = min;
		} else if (max < def) {
			def = max;
		}
		return def;
	}

	/**
	 * @param buffer The string to convert.
	 * @return The value of the string.
	 */
	public static int getInteger(String buffer) {
		return getInteger(buffer, 0);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @return The value of the string.
	 */
	public static int getInteger(String buffer, int def) {
		return getInteger(buffer, def, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @param min The minimum value permitted.
	 * @param max The maximum value permitted.
	 * @return The value of the string.
	 */
	public static int getInteger(String buffer, int def, int min, int max) {
		int radix = 10;
		buffer = normalize(buffer);
		if (buffer != null && buffer.startsWith(HEX_PREFIX)) {
			buffer = buffer.substring(HEX_PREFIX.length());
			radix = 16;
		}
		return getInteger(buffer, radix, def, min, max);
	}

	/**
	 * @param buffer The string to convert.
	 * @param radix The radix to use during conversion.
	 * @param def The default value to use if extraction fails.
	 * @param min The minimum value permitted.
	 * @param max The maximum value permitted.
	 * @return The value of the string.
	 */
	public static int getInteger(String buffer, int radix, int def, int min, int max) {
		try {
			buffer = normalize(buffer);
			// Special-case hex values, since the standard output generates unsigned values, some of
			// which cannot be loaded by the standard parser
			if (radix == 16 && buffer.length() == 8) {
				int value = Character.digit(buffer.charAt(0), radix);
				def = value << 28 | Integer.parseInt(buffer.substring(1), radix);
			} else {
				def = Integer.parseInt(normalize(buffer), radix);
			}
		} catch (Exception exception) {
			// Ignore
		}
		if (min > def) {
			def = min;
		} else if (max < def) {
			def = max;
		}
		return def;
	}

	/**
	 * @param buffer The string to convert.
	 * @return The value of the string.
	 */
	public static int getLocalizedInteger(String buffer) {
		return getLocalizedInteger(buffer, 0);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @return The value of the string.
	 */
	public static int getLocalizedInteger(String buffer, int def) {
		return getLocalizedInteger(buffer, def, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @param min The minimum value permitted.
	 * @param max The maximum value permitted.
	 * @return The value of the string.
	 */
	public static int getLocalizedInteger(String buffer, int def, int min, int max) {
		try {
			def = NUMBER_FORMAT.parse(normalize(buffer)).intValue();
		} catch (Exception exception) {
			// Ignore
		}
		if (min > def) {
			def = min;
		} else if (max < def) {
			def = max;
		}
		return def;
	}

	/**
	 * @param buffer The string to convert.
	 * @return The value of the string.
	 */
	public static long getLong(String buffer) {
		return getLong(buffer, 0);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @return The value of the string.
	 */
	public static long getLong(String buffer, long def) {
		return getLong(buffer, def, Long.MIN_VALUE, Long.MAX_VALUE);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @param min The minimum value permitted.
	 * @param max The maximum value permitted.
	 * @return The value of the string.
	 */
	public static long getLong(String buffer, long def, long min, long max) {
		int radix = 10;
		buffer = normalize(buffer);
		if (buffer != null && buffer.startsWith(HEX_PREFIX)) {
			buffer = buffer.substring(HEX_PREFIX.length());
			radix = 16;
		}
		return getLong(buffer, radix, def, min, max);
	}

	/**
	 * @param buffer The string to convert.
	 * @param radix The radix to use during conversion.
	 * @param def The default value to use if extraction fails.
	 * @param min The minimum value permitted.
	 * @param max The maximum value permitted.
	 * @return The value of the string.
	 */
	public static long getLong(String buffer, int radix, long def, long min, long max) {
		try {
			buffer = normalize(buffer);
			// Special-case hex values, since the standard output generates unsigned values, some of
			// which cannot be loaded by the standard parser
			if (radix == 16 && buffer.length() == 16) {
				long value = Character.digit(buffer.charAt(0), radix);
				def = value << 60 | Long.parseLong(buffer.substring(1), radix);
			} else {
				def = Long.parseLong(buffer, radix);
			}
		} catch (Exception exception) {
			// Ignore
		}
		if (min > def) {
			def = min;
		} else if (max < def) {
			def = max;
		}
		return def;
	}

	/**
	 * @param buffer The string to convert.
	 * @return The value of the string.
	 */
	public static long getLocalizedLong(String buffer) {
		return getLocalizedLong(buffer, 0);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @return The value of the string.
	 */
	public static long getLocalizedLong(String buffer, long def) {
		return getLocalizedLong(buffer, def, Long.MIN_VALUE, Long.MAX_VALUE);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @param min The minimum value permitted.
	 * @param max The maximum value permitted.
	 * @return The value of the string.
	 */
	public static long getLocalizedLong(String buffer, long def, long min, long max) {
		try {
			def = NUMBER_FORMAT.parse(normalize(buffer)).longValue();
		} catch (Exception exception) {
			// Ignore
		}
		if (min > def) {
			def = min;
		} else if (max < def) {
			def = max;
		}
		return def;
	}

	/**
	 * @param buffer The string to convert.
	 * @return The value of the string.
	 */
	public static float getFloat(String buffer) {
		return getFloat(buffer, 0);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @return The value of the string.
	 */
	public static float getFloat(String buffer, float def) {
		return getFloat(buffer, def, -Float.MAX_VALUE, Float.MAX_VALUE);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @param min The minimum value permitted.
	 * @param max The maximum value permitted.
	 * @return The value of the string.
	 */
	public static float getFloat(String buffer, float def, float min, float max) {
		try {
			def = Float.parseFloat(normalize(buffer));
		} catch (Exception exception) {
			// Ignore
		}
		if (min > def) {
			def = min;
		} else if (max < def) {
			def = max;
		}
		return def;
	}

	/**
	 * @param buffer The string to convert.
	 * @return The value of the string.
	 */
	public static float getLocalizedFloat(String buffer) {
		return getLocalizedFloat(buffer, 0);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @return The value of the string.
	 */
	public static float getLocalizedFloat(String buffer, float def) {
		return getLocalizedFloat(buffer, def, -Float.MAX_VALUE, Float.MAX_VALUE);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @param min The minimum value permitted.
	 * @param max The maximum value permitted.
	 * @return The value of the string.
	 */
	public static float getLocalizedFloat(String buffer, float def, float min, float max) {
		try {
			def = NUMBER_FORMAT.parse(normalize(buffer)).floatValue();
		} catch (Exception exception) {
			// Ignore
		}
		if (min > def) {
			def = min;
		} else if (max < def) {
			def = max;
		}
		return def;
	}

	/**
	 * @param buffer The string to convert.
	 * @return The value of the string.
	 */
	public static double getDouble(String buffer) {
		return getDouble(buffer, 0);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @return The value of the string.
	 */
	public static double getDouble(String buffer, double def) {
		return getDouble(buffer, def, -Double.MAX_VALUE, Double.MAX_VALUE);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @param min The minimum value permitted.
	 * @param max The maximum value permitted.
	 * @return The value of the string.
	 */
	public static double getDouble(String buffer, double def, double min, double max) {
		try {
			def = Double.parseDouble(normalize(buffer));
		} catch (Exception exception) {
			// Ignore
		}
		if (min > def) {
			def = min;
		} else if (max < def) {
			def = max;
		}
		return def;
	}

	/**
	 * @param buffer The string to convert.
	 * @return The value of the string.
	 */
	public static double getLocalizedDouble(String buffer) {
		return getLocalizedDouble(buffer, 0);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @return The value of the string.
	 */
	public static double getLocalizedDouble(String buffer, double def) {
		return getLocalizedDouble(buffer, def, -Double.MAX_VALUE, Double.MAX_VALUE);
	}

	/**
	 * @param buffer The string to convert.
	 * @param def The default value to use if extraction fails.
	 * @param min The minimum value permitted.
	 * @param max The maximum value permitted.
	 * @return The value of the string.
	 */
	public static double getLocalizedDouble(String buffer, double def, double min, double max) {
		try {
			def = NUMBER_FORMAT.parse(normalize(buffer)).doubleValue();
		} catch (Exception exception) {
			// Ignore
		}
		if (min > def) {
			def = min;
		} else if (max < def) {
			def = max;
		}
		return def;
	}

	/**
	 * Normalizes a number string by removing an leading/trailing spaces and removing a leading '+',
	 * if present.
	 *
	 * @param buffer The number string to normalize. May be <code>null</code>.
	 * @return The normalized number string, or <code>null</code>.
	 */
	public static String normalize(String buffer) {
		if (buffer != null) {
			buffer = buffer.trim();
			if (buffer.length() > 0 && buffer.charAt(0) == '+') {
				buffer = buffer.substring(1);
			}
		}
		return buffer;
	}

	public static String trimTrailingZerosAfterDecimal(String buffer, boolean localized) {
		if (buffer != null) {
			buffer = buffer.trim();
			int dot = buffer.indexOf(localized ? DecimalFormatSymbols.getInstance().getDecimalSeparator() : '.');
			if (dot != -1) {
				int pos = buffer.length() - 1;
				while (true) {
					if (buffer.charAt(pos) != '0') {
						break;
					}
					pos--;
				}
				if (dot != pos) {
					pos++;
				}
				buffer = buffer.substring(0, pos);
			}
		}
		return buffer;
	}

	/**
	 * @param value The value to format as hexadecimal.
	 * @return The formatted string.
	 */
	public static String hex(int value) {
		return HEX_PREFIX + Integer.toHexString(value);
	}

	/**
	 * @param value The value to format as hexadecimal.
	 * @return The formatted string.
	 */
	public static String hex(long value) {
		return HEX_PREFIX + Long.toHexString(value);
	}

	/**
	 * @param value The value to format.
	 * @return The formatted string.
	 */
	public static String format(long value) {
		return NUMBER_FORMAT.format(value);
	}

	/**
	 * @param value The value to format.
	 * @return The formatted string.
	 */
	public static String formatWithForcedSign(long value) {
		return NUMBER_PLUS_FORMAT.format(value);
	}

	/**
	 * @param value The value to format.
	 * @return The formatted string.
	 */
	public static String format(double value) {
		return NUMBER_FORMAT.format(value);
	}

	/**
	 * @param value The value to format.
	 * @return The formatted string.
	 */
	public static String formatWithForcedSign(double value) {
		return NUMBER_PLUS_FORMAT.format(value);
	}

	/**
	 * @param buffer The string to convert.
	 * @return The number of milliseconds since midnight, January 1, 1970.
	 */
	public static long getDate(String buffer) {
		if (buffer != null) {
			buffer = buffer.trim();
			for (int i = DateFormat.FULL; i <= DateFormat.SHORT; i++) {
				try {
					return DateFormat.getDateInstance(i).parse(buffer).getTime();
				} catch (Exception exception) {
					// Ignore
				}
			}
		}
		return System.currentTimeMillis();
	}

	/**
	 * @param buffer The string to convert.
	 * @return The number of milliseconds since midnight, January 1, 1970.
	 */
	public static long getDateTime(String buffer) {
		if (buffer != null) {
			buffer = buffer.trim();
			for (int i = DateFormat.FULL; i <= DateFormat.SHORT; i++) {
				for (int j = DateFormat.FULL; j <= DateFormat.SHORT; j++) {
					try {
						return DateFormat.getDateTimeInstance(i, j).parse(buffer).getTime();
					} catch (Exception exception) {
						// Ignore
					}
				}
			}
		}
		return System.currentTimeMillis();
	}

	/**
	 * @param dateTime The date time value to format.
	 * @param format The format string to use. The first argument will be the time and the second
	 *            will be the date.
	 * @return The formatted date and time.
	 */
	public static String formatDateTime(long dateTime, String format) {
		Date date = new Date(dateTime);
		return MessageFormat.format(format, DateFormat.getTimeInstance(DateFormat.SHORT).format(date), DateFormat.getDateInstance(DateFormat.MEDIUM).format(date));
	}

	/**
	 * @param inches The height, in inches.
	 * @return The formatted height, in feet/inches format, as appropriate.
	 */
	public static String formatHeight(int inches) {
		int feet = inches / 12;
		inches %= 12;
		if (feet > 0) {
			String buffer = format(feet) + '\'';
			if (inches > 0) {
				return buffer + ' ' + format(inches) + '"';
			}
			return buffer;
		}
		return format(inches) + '"';
	}

	/**
	 * @param buffer The formatted height string, as produced by {@link #formatHeight(int)}.
	 * @return The number of inches it represents.
	 */
	public static int getHeight(String buffer) {
		if (buffer != null) {
			int feetMark = buffer.indexOf('\'');
			int inchesMark = buffer.indexOf('"');
			if (feetMark == -1) {
				if (inchesMark == -1) {
					return getLocalizedInteger(buffer, 0, 0, Integer.MAX_VALUE);
				}
				return getLocalizedInteger(buffer.substring(0, inchesMark), 0, 0, Integer.MAX_VALUE);
			}
			int inches = getLocalizedInteger(buffer.substring(inchesMark != -1 && feetMark > inchesMark ? inchesMark + 1 : 0, feetMark), 0, 0, Integer.MAX_VALUE) * 12;
			if (inchesMark != -1) {
				inches += getLocalizedInteger(buffer.substring(feetMark < inchesMark ? feetMark + 1 : 0, inchesMark), 0, 0, Integer.MAX_VALUE);
			}
			return inches;
		}
		return 0;
	}
}
