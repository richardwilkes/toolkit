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

package com.trollworks.toolkit.ui.widget.outline;

import com.trollworks.toolkit.ui.TextDrawing;
import com.trollworks.toolkit.ui.image.StdImage;
import com.trollworks.toolkit.utility.NumericComparator;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;

import javax.swing.SwingConstants;
import javax.swing.UIManager;

/** Represents text cells in an {@link Outline}. */
public class TextCell implements Cell {
	/** The standard horizontal margin. */
	public static final int	H_MARGIN		= 2;
	/** The standard horizontal margin width. */
	public static final int	H_MARGIN_WIDTH	= H_MARGIN * 2;
	private int				mHAlignment;
	private boolean			mWrapped;

	/** Create a new text cell. */
	public TextCell() {
		this(SwingConstants.LEFT);
	}

	/**
	 * Create a new text cell.
	 *
	 * @param alignment The horizontal text alignment to use.
	 */
	public TextCell(int alignment) {
		this(alignment, false);
	}

	/**
	 * Create a new text cell.
	 *
	 * @param hAlignment The horizontal text alignment to use.
	 * @param wrapped Pass in <code>true</code> to enable wrapping.
	 */
	public TextCell(int hAlignment, boolean wrapped) {
		mHAlignment = hAlignment;
		mWrapped = wrapped;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int compare(Column column, Row one, Row two) {
		Object oneObj = one.getData(column);
		Object twoObj = two.getData(column);
		if (!(oneObj instanceof String) && oneObj.getClass() == twoObj.getClass() && oneObj instanceof Comparable<?>) {
			return ((Comparable<Object>) oneObj).compareTo(twoObj);
		}
		return NumericComparator.caselessCompareStrings(one.getDataAsText(column), two.getDataAsText(column));
	}

	/**
	 * @param selected Whether or not the selected version of the color is needed.
	 * @param active Whether or not the active version of the color is needed.
	 * @param row The row.
	 * @param column The column.
	 * @return The foreground color.
	 */
	@SuppressWarnings("static-method")
	public Color getColor(boolean selected, boolean active, Row row, Column column) {
		return Outline.getListForeground(selected, active);
	}

	@Override
	public int getPreferredWidth(Row row, Column column) {
		int width = TextDrawing.getPreferredSize(getFont(row, column), getPresentationText(row, column)).width;
		StdImage icon = row == null ? column.getIcon() : row.getIcon(column);
		if (icon != null) {
			width += icon.getWidth() + H_MARGIN;
		}
		return H_MARGIN_WIDTH + width;
	}

	@Override
	public int getPreferredHeight(Row row, Column column) {
		Font font = getFont(row, column);
		int minHeight = TextDrawing.getPreferredSize(font, "Mg").height; //$NON-NLS-1$
		int height = TextDrawing.getPreferredSize(font, getPresentationText(row, column)).height;
		StdImage icon = row == null ? column.getIcon() : row.getIcon(column);
		if (icon != null) {
			int iconHeight = icon.getHeight();
			if (height < iconHeight) {
				height = iconHeight;
			}
		}
		return minHeight > height ? minHeight : height;
	}

	@SuppressWarnings("static-method")
	public StdImage getIcon(Row row, Column column) {
		return row == null ? column.getIcon() : row.getIcon(column);
	}

	@Override
	public void drawCell(Outline outline, Graphics gc, Rectangle bounds, Row row, Column column, boolean selected, boolean active) {
		Font font = getFont(row, column);
		int ascent = gc.getFontMetrics(font).getAscent();
		StringTokenizer tokenizer = new StringTokenizer(getPresentationText(row, column), "\n", true); //$NON-NLS-1$
		int totalHeight = getPreferredHeight(row, column);
		int lineHeight = TextDrawing.getPreferredSize(font, "Mg").height; //$NON-NLS-1$
		int lineCount = 0;
		StdImage icon = getIcon(row, column);
		int left = icon == null ? 0 : icon.getWidth() + H_MARGIN;
		int cellWidth = bounds.width - (left + H_MARGIN_WIDTH);
		int vAlignment = getVAlignment();
		int hAlignment = getHAlignment();

		left += bounds.x + H_MARGIN;

		if (icon != null) {
			int iy = bounds.y;

			if (vAlignment != SwingConstants.TOP) {
				int ivDelta = bounds.height - icon.getHeight();

				if (vAlignment == SwingConstants.CENTER) {
					ivDelta /= 2;
				}
				iy += ivDelta;
			}
			gc.drawImage(icon, bounds.x + H_MARGIN, iy, null);
		}

		gc.setFont(font);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();

			if (token.equals("\n")) { //$NON-NLS-1$
				lineCount++;
			} else {
				String text = TextDrawing.truncateIfNecessary(font, token, cellWidth, getTruncationPolicy());
				int x = left;
				int y = bounds.y + ascent + lineHeight * lineCount;

				if (hAlignment != SwingConstants.LEFT) {
					int hDelta = cellWidth - TextDrawing.getWidth(font, text);

					if (hAlignment == SwingConstants.CENTER) {
						hDelta /= 2;
					}
					x += hDelta;
				}

				if (vAlignment != SwingConstants.TOP) {
					float vDelta = bounds.height - totalHeight;

					if (vAlignment == SwingConstants.CENTER) {
						vDelta /= 2;
					}
					y += vDelta;
				}

				gc.setColor(getColor(selected, active, row, column));
				gc.drawString(text, x, y);
			}
		}
	}

	/**
	 * @param row The row.
	 * @param column The column.
	 * @return The data of this cell as a string that is prepared for display.
	 */
	protected String getPresentationText(Row row, Column column) {
		String text = getData(row, column, false);
		if (!mWrapped || row == null) {
			return text;
		}
		int width = column.getWidth();
		if (width == -1) {
			return text;
		}
		return TextDrawing.wrapToPixelWidth(getFont(row, column), text, width - (H_MARGIN_WIDTH + row.getOwner().getIndentWidth(row, column)));
	}

	@Override
	public Cursor getCursor(MouseEvent event, Rectangle bounds, Row row, Column column) {
		return Cursor.getDefaultCursor();
	}

	/** @return The truncation policy. */
	@SuppressWarnings("static-method")
	public int getTruncationPolicy() {
		return SwingConstants.CENTER;
	}

	/**
	 * @param row The row.
	 * @param column The column.
	 * @param nullOK <code>true</code> if <code>null</code> may be returned.
	 * @return The data of this cell as a string.
	 */
	@SuppressWarnings("static-method")
	protected String getData(Row row, Column column, boolean nullOK) {
		if (row != null) {
			String text = row.getDataAsText(column);

			return text == null ? nullOK ? null : "" : text; //$NON-NLS-1$
		}
		return column.toString();
	}

	/**
	 * @param row The row.
	 * @param column The column.
	 * @return The font.
	 */
	@SuppressWarnings("static-method")
	public Font getFont(Row row, Column column) {
		return UIManager.getFont("TextField.font"); //$NON-NLS-1$
	}

	/** @return The horizontal alignment. */
	public int getHAlignment() {
		return mHAlignment;
	}

	/** @param alignment The horizontal alignment. */
	public void setHAlignment(int alignment) {
		mHAlignment = alignment;
	}

	/** @return The vertical alignment. */
	@SuppressWarnings("static-method")
	public int getVAlignment() {
		return SwingConstants.TOP;
	}

	@Override
	public String getToolTipText(MouseEvent event, Rectangle bounds, Row row, Column column) {
		if (getPreferredWidth(row, column) - H_MARGIN > column.getWidth() - row.getOwner().getIndentWidth(row, column)) {
			return getData(row, column, true);
		}
		return null;
	}

	@Override
	public boolean participatesInDynamicRowLayout() {
		return mWrapped;
	}

	@Override
	public void mouseClicked(MouseEvent event, Rectangle bounds, Row row, Column column) {
		// Does nothing
	}
}
