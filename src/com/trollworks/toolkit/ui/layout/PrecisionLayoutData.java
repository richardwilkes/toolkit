/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.trollworks.toolkit.ui.layout;

import java.awt.Component;
import java.awt.Dimension;
import java.util.StringTokenizer;

/**
 * Data for components within a {@link PrecisionLayout}. Do not re-use {@link PrecisionLayoutData}
 * objects. Each component should have its own.
 */
public final class PrecisionLayoutData {
	public static final String	KEY_HALIGN		= "hAlign";	//$NON-NLS-1$
	public static final String	KEY_VALIGN		= "vAlign";	//$NON-NLS-1$
	public static final String	KEY_WIDTH		= "width";		//$NON-NLS-1$
	public static final String	KEY_HEIGHT		= "height";	//$NON-NLS-1$
	public static final String	KEY_MIN_WIDTH	= "minWidth";	//$NON-NLS-1$
	public static final String	KEY_MIN_HEIGHT	= "minHeight";	//$NON-NLS-1$
	public static final String	KEY_HSPAN		= "hSpan";		//$NON-NLS-1$
	public static final String	KEY_VSPAN		= "vSpan";		//$NON-NLS-1$
	public static final String	KEY_HGRAB		= "hGrab";		//$NON-NLS-1$
	public static final String	KEY_VGRAB		= "vGrab";		//$NON-NLS-1$
	public static final String	KEY_EXCLUDE		= "exclude";	//$NON-NLS-1$
	public static final String	KEY_MARGINS		= "margins";	//$NON-NLS-1$
	public static final String	KEY_TOP			= "top";		//$NON-NLS-1$
	public static final String	KEY_LEFT		= "left";		//$NON-NLS-1$
	public static final String	KEY_BOTTOM		= "bottom";	//$NON-NLS-1$
	public static final String	KEY_RIGHT		= "right";		//$NON-NLS-1$
	public static final String	VALUE_BEGINNING	= "beginning";	//$NON-NLS-1$
	public static final String	VALUE_MIDDLE	= "middle";	//$NON-NLS-1$
	public static final String	VALUE_END		= "end";		//$NON-NLS-1$
	public static final String	VALUE_FILL		= "fill";		//$NON-NLS-1$
	public static final int		DEFAULT			= -1;
	public static final int		BEGINNING		= 0;
	public static final int		MIDDLE			= 1;
	public static final int		END				= 2;
	public static final int		FILL			= 3;
	/**
	 * Specifies how components will be positioned vertically within a cell. The default value is
	 * {@link #MIDDLE}. Possible values are:
	 * <ul>
	 * <li>{@link #BEGINNING}: Position the component at the top of the cell</li>
	 * <li>{@link #MIDDLE}: Position the component in the vertical center of the cell</li>
	 * <li>{@link #END}: Position the component at the bottom of the cell</li>
	 * <li>{@link #FILL}: Resize the component to fill the cell vertically</li>
	 * </ul>
	 */
	public int					mVAlign			= MIDDLE;
	/**
	 * Specifies how components will be positioned horizontally within a cell. The default value is
	 * {@link #BEGINNING}. Possible values are:
	 * <ul>
	 * <li>{@link #BEGINNING}: Position the component at the left of the cell</li>
	 * <li>{@link #MIDDLE}: Position the component in the horizontal center of the cell</li>
	 * <li>{@link #END}: Position the component at the right of the cell</li>
	 * <li>{@link #FILL}: Resize the component to fill the cell horizontally</li>
	 * </ul>
	 */
	public int					mHAlign			= BEGINNING;
	/**
	 * Specifies the preferred width in pixels. A value of {@link #DEFAULT} indicates the component
	 * should be asked for its preferred size. The default value is {@link #DEFAULT}.
	 */
	public int					mWidthHint		= DEFAULT;
	/**
	 * Specifies the preferred height in pixels. A value of {@link #DEFAULT} indicates the component
	 * should be asked for its preferred size. The default value is {@link #DEFAULT}.
	 */
	public int					mHeightHint		= DEFAULT;
	/**
	 * Specifies the number of pixels of indentation that will be placed along the top side of the
	 * cell. The default value is 0.
	 */
	public int					mMarginTop		= 0;
	/**
	 * Specifies the number of pixels of indentation that will be placed along the left side of the
	 * cell. The default value is 0.
	 */
	public int					mMarginLeft		= 0;
	/**
	 * Specifies the number of pixels of indentation that will be placed along the bottom side of
	 * the cell. The default value is 0.
	 */
	public int					mMarginBottom	= 0;
	/**
	 * Specifies the number of pixels of indentation that will be placed along the right side of the
	 * cell. The default value is 0.
	 */
	public int					mMarginRight	= 0;
	/**
	 * Specifies the number of column cells that the component will take up. The default value is 1.
	 */
	public int					mHSpan			= 1;
	/**
	 * Specifies the number of row cells that the component will take up. The default value is 1.
	 */
	public int					mVSpan			= 1;
	/**
	 * Specifies whether the width of the cell changes depending on the size of the parent
	 * container. If <code>true</code>, the following rules apply to the width of the cell:
	 * <ul>
	 * <li>If extra horizontal space is available in the parent, the cell will grow to be wider than
	 * its preferred width. The new width will be "preferred width + delta" where delta is the extra
	 * horizontal space divided by the number of grabbing columns.</li>
	 * <li>If there is not enough horizontal space available in the parent, the cell will shrink
	 * until it reaches its minimum width as specified by {@link #mMinWidth}. The new width will be
	 * the maximum of "{@link #mMinWidth}" and "preferred width - delta", where delta is the amount
	 * of space missing divided by the number of grabbing columns.</li>
	 * </ul>
	 * The default value is <code>false</code>.
	 */
	public boolean				mHGrab			= false;
	/**
	 * Specifies whether the height of the cell changes depending on the size of the parent
	 * container. If <code>true</code>, the following rules apply to the height of the cell:
	 * <ul>
	 * <li>If extra vertical space is available in the parent, the cell will grow to be taller than
	 * its preferred height. The new height will be "preferred height + delta" where delta is the
	 * extra vertical space divided by the number of grabbing rows.</li>
	 * <li>If there is not enough vertical space available in the parent, the cell will shrink until
	 * it reaches its minimum height as specified by {@link #mMinHeight}. The new height will be the
	 * maximum of "{@link #mMinHeight}" and "preferred height - delta", where delta is the amount of
	 * space missing divided by the number of grabbing rows.</li>
	 * </ul>
	 * The default value is <code>false</code>.
	 */
	public boolean				mVGrab			= false;
	/**
	 * Specifies the minimum width in pixels. This value applies only if {@link #mHGrab} is
	 * <code>true</code>. A value of {@link #DEFAULT} means that the minimum width will be
	 * determined by calling {@link Component#getMinimumSize()}. The default value is
	 * {@link #DEFAULT}.
	 */
	public int					mMinWidth		= DEFAULT;
	/**
	 * Specifies the minimum height in pixels. This value applies only if {@link #mVGrab} is true. A
	 * value of {@link #DEFAULT} means that the minimum height will be determined by calling
	 * {@link Component#getMinimumSize()}. The default value is {@link #DEFAULT}.
	 */
	public int					mMinHeight		= DEFAULT;
	/**
	 * Informs the layout to ignore this component when sizing and positioning components. If this
	 * value is <code>true</code>, the size and position of the component will not be managed by the
	 * layout. If this value is <code>false</code>, the size and position of the component will be
	 * computed and assigned. The default value is <code>false</code>.
	 */
	public boolean				mExclude		= false;
	int							mCacheMinWidth;
	int							mCacheWidth;
	int							mCacheHeight;

	public PrecisionLayoutData() {
		super();
	}

	/**
	 * Creates a new {@link PrecisionLayoutData} from a text description. Valid keywords are:
	 * <ul>
	 * <li>hAlign - sets {@link #mHAlign}</li>
	 * <li>vAlign - sets {@link #mVAlign}</li>
	 * <li>width - sets {@link #mWidthHint}</li>
	 * <li>height - sets {@link #mHeightHint}</li>
	 * <li>minWidth - sets {@link #mMinWidth}</li>
	 * <li>minHeight - sets {@link #mMinHeight}</li>
	 * <li>hSpan - sets {@link #mHSpan}</li>
	 * <li>vSpan - sets {@link #mVSpan}</li>
	 * <li>hGrab - sets {@link #mHGrab}</li>
	 * <li>vGrab - sets {@link #mVGrab}</li>
	 * <li>margins - sets {@link #mMarginTop}, {@link #mMarginLeft}, {@link #mMarginBottom} and
	 * {@link #mMarginRight} to the same value</li>
	 * <li>top - sets {@link #mMarginTop}</li>
	 * <li>left - sets {@link #mMarginLeft}</li>
	 * <li>bottom - sets {@link #mMarginBottom}</li>
	 * <li>right - sets {@link #mMarginRight}</li>
	 * <li>exclude - sets {@link #mExclude}</li>
	 * </ul>
	 *
	 * @param settings The settings string to parse.
	 */
	public PrecisionLayoutData(String settings) {
		StringTokenizer tokenizer = new StringTokenizer(settings);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			int sep = token.indexOf(':');
			String key = token.substring(0, sep);
			String value = token.substring(sep + 1);
			if (KEY_HALIGN.equalsIgnoreCase(key)) {
				mHAlign = decodeAlignment(value, true);
			} else if (KEY_VALIGN.equalsIgnoreCase(key)) {
				mVAlign = decodeAlignment(value, true);
			} else if (KEY_WIDTH.equalsIgnoreCase(key)) {
				mWidthHint = Integer.parseInt(value);
			} else if (KEY_HEIGHT.equalsIgnoreCase(key)) {
				mHeightHint = Integer.parseInt(value);
			} else if (KEY_MIN_WIDTH.equalsIgnoreCase(key)) {
				mMinWidth = Integer.parseInt(value);
			} else if (KEY_MIN_HEIGHT.equalsIgnoreCase(key)) {
				mMinHeight = Integer.parseInt(value);
			} else if (KEY_HSPAN.equalsIgnoreCase(key)) {
				mHSpan = Integer.parseInt(value);
			} else if (KEY_VSPAN.equalsIgnoreCase(key)) {
				mVSpan = Integer.parseInt(value);
			} else if (KEY_HGRAB.equalsIgnoreCase(key)) {
				mHGrab = decodeBoolean(value);
			} else if (KEY_VGRAB.equalsIgnoreCase(key)) {
				mVGrab = decodeBoolean(value);
			} else if (KEY_EXCLUDE.equalsIgnoreCase(key)) {
				mExclude = decodeBoolean(value);
			} else if (KEY_MARGINS.equalsIgnoreCase(key)) {
				mMarginTop = Integer.parseInt(value);
				mMarginLeft = mMarginTop;
				mMarginBottom = mMarginTop;
				mMarginRight = mMarginTop;
			} else if (KEY_TOP.equalsIgnoreCase(key)) {
				mMarginTop = Integer.parseInt(value);
			} else if (KEY_LEFT.equalsIgnoreCase(key)) {
				mMarginLeft = Integer.parseInt(value);
			} else if (KEY_BOTTOM.equalsIgnoreCase(key)) {
				mMarginBottom = Integer.parseInt(value);
			} else if (KEY_RIGHT.equalsIgnoreCase(key)) {
				mMarginRight = Integer.parseInt(value);
			} else {
				throw new IllegalArgumentException(token);
			}
		}
	}

	static boolean decodeBoolean(String bool) {
		return "true".equalsIgnoreCase(bool) || "yes".equalsIgnoreCase(bool) || "y".equalsIgnoreCase(bool) || "1".equalsIgnoreCase(bool); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	static int decodeAlignment(String alignment, boolean permitFill) {
		if (VALUE_BEGINNING.equalsIgnoreCase(alignment)) {
			return BEGINNING;
		} else if (VALUE_MIDDLE.equalsIgnoreCase(alignment)) {
			return MIDDLE;
		} else if (VALUE_END.equalsIgnoreCase(alignment)) {
			return END;
		} else if (permitFill && VALUE_FILL.equalsIgnoreCase(alignment)) {
			return FILL;
		}
		throw new IllegalArgumentException(alignment);
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		emitAlign(KEY_HALIGN, mHAlign, buffer);
		emitAlign(KEY_VALIGN, mVAlign, buffer);
		emit(KEY_WIDTH, mWidthHint, buffer);
		emit(KEY_HEIGHT, mHeightHint, buffer);
		emit(KEY_MIN_WIDTH, mMinWidth, buffer);
		emit(KEY_MIN_HEIGHT, mMinHeight, buffer);
		emit(KEY_HSPAN, mHSpan, buffer);
		emit(KEY_VSPAN, mVSpan, buffer);
		emit(KEY_HGRAB, mHGrab, buffer);
		emit(KEY_VGRAB, mVGrab, buffer);
		if (mMarginTop == mMarginLeft && mMarginTop == mMarginBottom && mMarginTop == mMarginRight) {
			emit(KEY_MARGINS, mMarginTop, buffer);
		} else {
			emit(KEY_TOP, mMarginTop, buffer);
			emit(KEY_LEFT, mMarginLeft, buffer);
			emit(KEY_BOTTOM, mMarginBottom, buffer);
			emit(KEY_RIGHT, mMarginBottom, buffer);
		}
		emit(KEY_EXCLUDE, mExclude, buffer);
		return buffer.toString();
	}

	static void emit(String key, boolean value, StringBuilder buffer) {
		if (buffer.length() > 0) {
			buffer.append(' ');
		}
		buffer.append(key);
		buffer.append(':');
		buffer.append(value);
	}

	static void emit(String key, int value, StringBuilder buffer) {
		if (buffer.length() > 0) {
			buffer.append(' ');
		}
		buffer.append(key);
		buffer.append(':');
		buffer.append(value);
	}

	static void emitAlign(String key, int value, StringBuilder buffer) {
		if (buffer.length() > 0) {
			buffer.append(' ');
		}
		buffer.append(key);
		buffer.append(':');
		if (value == BEGINNING) {
			buffer.append(VALUE_BEGINNING);
		} else if (value == MIDDLE) {
			buffer.append(VALUE_MIDDLE);
		} else if (value == END) {
			buffer.append(VALUE_END);
		} else if (value == FILL) {
			buffer.append(VALUE_FILL);
		} else {
			buffer.append(value);
		}
	}

	void computeSize(Component component, int wHint, int hHint, boolean useMinimumSize) {
		Dimension size = null;
		if (wHint != DEFAULT || hHint != DEFAULT) {
			size = component.getMinimumSize();
			mCacheMinWidth = mMinWidth != DEFAULT ? mMinWidth : size.width;
			if (wHint != DEFAULT && wHint < mCacheMinWidth) {
				wHint = mCacheMinWidth;
			}
			int minHeight = mMinHeight != DEFAULT ? mMinHeight : size.height;
			if (hHint != DEFAULT && hHint < minHeight) {
				hHint = minHeight;
			}
			size = component.getMaximumSize();
			if (wHint != DEFAULT && wHint > size.width) {
				wHint = size.width;
			}
			if (hHint != DEFAULT && hHint > size.height) {
				hHint = size.height;
			}
		}
		if (useMinimumSize) {
			size = component.getMinimumSize();
			mCacheMinWidth = mMinWidth != DEFAULT ? mMinWidth : size.width;
		} else {
			size = component.getPreferredSize();
		}
		if (mWidthHint != DEFAULT) {
			size.width = mWidthHint;
		}
		if (mMinWidth != DEFAULT && size.width < mMinWidth) {
			size.width = mMinWidth;
		}
		if (mHeightHint != DEFAULT) {
			size.height = mHeightHint;
		}
		if (mMinHeight != DEFAULT && size.height < mMinHeight) {
			size.height = mMinHeight;
		}
		if (wHint != DEFAULT) {
			size.width = wHint;
		}
		if (hHint != DEFAULT) {
			size.height = hHint;
		}
		mCacheWidth = size.width;
		mCacheHeight = size.height;
	}
}
