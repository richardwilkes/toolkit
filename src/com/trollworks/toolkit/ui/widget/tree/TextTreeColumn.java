/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.trollworks.toolkit.ui.widget.tree;

import com.trollworks.toolkit.ui.Fonts;
import com.trollworks.toolkit.ui.TextDrawing;
import com.trollworks.toolkit.utility.NumericComparator;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.swing.SwingConstants;

/** Displays text in a {@link TreeColumn}. */
public class TextTreeColumn extends TreeColumn {
	private static final int														ICON_GAP			= 2;
	private static final int														HMARGIN				= 2;
	private static final HashMap<Class<? extends TreeRow>, HashMap<String, Method>>	FIELD_LOOKUP_CACHE	= new HashMap<>();
	private String																	mField;
	private String																	mIconField;
	private int																		mAlignment;
	private int																		mTruncationPolicy	= SwingConstants.CENTER;
	private WrappingMode															mWrappingMode;

	public enum WrappingMode {
		NORMAL,
		WRAPPED,
		SINGLE_LINE;
	}

	/**
	 * Creates a new left-aligned {@link TextTreeColumn} with no wrapping.
	 *
	 * @param name The name of the {@link TreeColumn}.
	 * @param field The name of the field to display. This name will be used to access the
	 *            corresponding method on the {@link TreeRow}. For example, passing in "name", will
	 *            cause the getName() method to be called to retrieve the data to display.
	 */
	public TextTreeColumn(String name, String field) {
		this(name, field, SwingConstants.LEFT);
	}

	/**
	 * Creates a new {@link TextTreeColumn} with no wrapping.
	 *
	 * @param name The name of the {@link TreeColumn}.
	 * @param field The name of the field to display. This name will be used to access the
	 *            corresponding method on the {@link TreeRow}. For example, passing in "name", will
	 *            cause the getName() method to be called to retrieve the data to display.
	 * @param alignment The horizontal text alignment.
	 */
	public TextTreeColumn(String name, String field, int alignment) {
		this(name, field, null, alignment, WrappingMode.NORMAL);
	}

	/**
	 * Creates a new {@link TextTreeColumn}.
	 *
	 * @param name The name of the {@link TreeColumn}.
	 * @param field The name of the field to display. This name will be used to access the
	 *            corresponding method on the {@link TreeRow}. For example, passing in "name", will
	 *            cause the getName() method to be called to retrieve the data to display.
	 * @param alignment The horizontal text alignment.
	 * @param wrappingMode The text wrapping mode.
	 */
	public TextTreeColumn(String name, String field, int alignment, WrappingMode wrappingMode) {
		this(name, field, null, alignment, wrappingMode);
	}

	/**
	 * Creates a new left-aligned {@link TextTreeColumn} with no wrapping.
	 *
	 * @param name The name of the {@link TreeColumn}.
	 * @param field The name of the field to display. This name will be used to access the
	 *            corresponding method on the {@link TreeRow}. For example, passing in "name", will
	 *            cause the getName() method to be called to retrieve the data to display.
	 * @param iconField The name of the icon field to display. Used in the same way as the "field"
	 *            parameter.
	 */
	public TextTreeColumn(String name, String field, String iconField) {
		this(name, field, iconField, SwingConstants.LEFT);
	}

	/**
	 * Creates a new {@link TextTreeColumn} with no wrapping.
	 *
	 * @param name The name of the {@link TreeColumn}.
	 * @param field The name of the field to display. This name will be used to access the
	 *            corresponding method on the {@link TreeRow}. For example, passing in "name", will
	 *            cause the getName() method to be called to retrieve the data to display.
	 * @param iconField The name of the icon field to display. Used in the same way as the "field"
	 *            parameter.
	 * @param alignment The horizontal text alignment.
	 */
	public TextTreeColumn(String name, String field, String iconField, int alignment) {
		this(name, field, iconField, alignment, WrappingMode.NORMAL);
	}

	/**
	 * Creates a new {@link TextTreeColumn}.
	 *
	 * @param name The name of the {@link TreeColumn}.
	 * @param field The name of the field to display. This name will be used to access the
	 *            corresponding method on the {@link TreeRow}. For example, passing in "name", will
	 *            cause the getName() method to be called to retrieve the data to display.
	 * @param iconField The name of the icon field to display. Used in the same way as the "field"
	 *            parameter.
	 * @param alignment The horizontal text alignment.
	 * @param wrappingMode The text wrapping mode.
	 */
	public TextTreeColumn(String name, String field, String iconField, int alignment, WrappingMode wrappingMode) {
		super(name);
		mField = createMethodName(field);
		mIconField = createMethodName(iconField);
		mAlignment = alignment;
		mWrappingMode = wrappingMode;
	}

	private static String createMethodName(String field) {
		if (field != null) {
			return "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1); //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public int calculatePreferredHeight(TreeRow row, int width) {
		Font font = getFont(row);
		BufferedImage icon = getIcon(row);
		if (mWrappingMode == WrappingMode.SINGLE_LINE) {
			int height = TextDrawing.getFontHeight(font);
			if (icon != null) {
				int iconHeight = icon.getHeight();
				if (iconHeight > height) {
					height = iconHeight;
				}
			}
			return height;
		}
		width -= HMARGIN + HMARGIN;
		if (icon != null) {
			width -= icon.getWidth() + ICON_GAP;
		}
		String text = getPresentationText(row, font, width, true);
		return calculatePreferredHeight(font, text, icon);
	}

	private static int calculatePreferredHeight(Font font, String text, BufferedImage icon) {
		int height = TextDrawing.getPreferredHeight(font, text);
		if (height == 0) {
			height = TextDrawing.getFontHeight(font);
		}
		if (icon != null) {
			int iconHeight = icon.getHeight();
			if (iconHeight > height) {
				height = iconHeight;
			}
		}
		return height;
	}

	@Override
	public int calculatePreferredWidth(TreeRow row) {
		int width = TextDrawing.getPreferredSize(getFont(row), getText(row)).width;
		BufferedImage icon = getIcon(row);
		if (icon != null) {
			width += icon.getWidth() + ICON_GAP;
		}
		return HMARGIN + width + HMARGIN;
	}

	@Override
	public void draw(Graphics2D gc, TreePanel panel, TreeRow row, int position, int top, int left, int width, boolean selected, boolean active) {
		left += HMARGIN;
		width -= HMARGIN + HMARGIN;
		BufferedImage icon = getIcon(row);
		if (icon != null) {
			gc.drawImage(icon, left, top, null);
			int iconSize = icon.getWidth() + ICON_GAP;
			left += iconSize;
			width -= iconSize;
		}
		Font font = getFont(row);
		gc.setFont(font);
		String text = getPresentationText(row, font, width, false);
		int totalHeight = calculatePreferredHeight(font, text, icon);
		gc.setColor(getColor(panel, row, position, selected, active));
		TextDrawing.draw(gc, new Rectangle(left, top, width, totalHeight), text, mAlignment, SwingConstants.TOP);
	}

	/**
	 * @param row The {@link TreeRow} to extract information from.
	 * @param font The {@link Font} to use.
	 * @param width The adjusted width of the column. This may be less than {@link #getWidth()} due
	 *            to display of disclosure controls.
	 * @param forHeightOnly Will be <code>true</code> when only the number of lines matters.
	 * @return The text to display, wrapped if necessary.
	 */
	protected String getPresentationText(TreeRow row, Font font, int width, boolean forHeightOnly) {
		String text = getText(row);
		if (mWrappingMode == WrappingMode.WRAPPED) {
			return TextDrawing.wrapToPixelWidth(font, text, width);
		}
		if (mWrappingMode == WrappingMode.SINGLE_LINE) {
			int cut = text.indexOf('\n');
			if (cut != -1) {
				text = text.substring(0, cut);
			}
		}
		return forHeightOnly ? text : TextDrawing.truncateIfNecessary(font, text, width, mTruncationPolicy);
	}

	/**
	 * @param row The {@link TreeRow} to extract information from.
	 * @return The text to display.
	 */
	protected String getText(TreeRow row) {
		return getFieldContents(row, mField, ""); //$NON-NLS-1$
	}

	/**
	 * @param row The {@link TreeRow} to extract information from.
	 * @return The text to display.
	 */
	protected BufferedImage getIcon(TreeRow row) {
		return getFieldContents(row, mIconField, null);
	}

	private static <T> T getFieldContents(TreeRow row, String fieldName, T def) {
		if (fieldName != null) {
			try {
				Class<? extends TreeRow> clazz = row.getClass();
				HashMap<String, Method> map = FIELD_LOOKUP_CACHE.get(clazz);
				if (map == null) {
					map = new HashMap<>();
					FIELD_LOOKUP_CACHE.put(clazz, map);
				}
				Method method = map.get(fieldName);
				if (method == null) {
					method = clazz.getMethod(fieldName, (Class[]) null);
					map.put(fieldName, method);
				}
				@SuppressWarnings("unchecked")
				T result = (T) method.invoke(row, (Object[]) null);
				if (result != null) {
					def = result;
				}
			} catch (Exception exception) {
				// Don't care
			}
		}
		return def;
	}

	/**
	 * @param row The {@link TreeRow} to extract information from.
	 * @return The {@link Font} to use.
	 */
	@SuppressWarnings("static-method")
	public Font getFont(TreeRow row) {
		return Fonts.getDefaultFont();
	}

	/**
	 * @param panel The owning {@link TreePanel}.
	 * @param row The {@link TreeRow} to extract information from.
	 * @param position The {@link TreeRow}'s position in the linear view.
	 * @param selected Whether or not the {@link TreeRow} is currently selected.
	 * @param active Whether or not the active state should be displayed.
	 * @return The foreground color.
	 */
	@SuppressWarnings("static-method")
	public Color getColor(TreePanel panel, TreeRow row, int position, boolean selected, boolean active) {
		return panel.getDefaultRowForeground(position, selected, active);
	}

	@Override
	public int compare(TreeRow r1, TreeRow r2) {
		return NumericComparator.caselessCompareStrings(getText(r1), getText(r2));
	}
}
