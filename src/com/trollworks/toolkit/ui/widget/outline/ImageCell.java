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

import com.trollworks.toolkit.ui.image.StdImage;
import com.trollworks.toolkit.utility.NumericComparator;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.SwingConstants;

/** Represents image cells in a {@link Outline}. */
public class ImageCell implements Cell {
	private int	mHAlignment;
	private int	mVAlignment;

	/** Create a new image cell renderer. */
	public ImageCell() {
		this(SwingConstants.CENTER, SwingConstants.CENTER);
	}

	/**
	 * Create a new image cell renderer.
	 *
	 * @param hAlignment The image horizontal alignment to use.
	 * @param vAlignment The image vertical alignment to use.
	 */
	public ImageCell(int hAlignment, int vAlignment) {
		mHAlignment = hAlignment;
		mVAlignment = vAlignment;
	}

	@Override
	public int compare(Column column, Row one, Row two) {
		String oneText = one.getDataAsText(column);
		String twoText = two.getDataAsText(column);

		return NumericComparator.caselessCompareStrings(oneText != null ? oneText : "", twoText != null ? twoText : ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @param row The row to use.
	 * @param column The column to use.
	 * @param selected Whether the row is selected.
	 * @param active Whether the outline is active.
	 * @return The icon, if any.
	 */
	@SuppressWarnings("static-method")
	protected StdImage getIcon(Row row, Column column, boolean selected, boolean active) {
		Object data = row.getData(column);
		return data instanceof StdImage ? (StdImage) data : null;
	}

	@Override
	public void drawCell(Outline outline, Graphics gc, Rectangle bounds, Row row, Column column, boolean selected, boolean active) {
		if (row != null) {
			StdImage image = getIcon(row, column, selected, active);

			if (image != null) {
				int x = bounds.x;
				int y = bounds.y;

				if (mHAlignment != SwingConstants.LEFT) {
					int hDelta = bounds.width - image.getWidth();

					if (mHAlignment == SwingConstants.CENTER) {
						hDelta /= 2;
					}
					x += hDelta;
				}

				if (mVAlignment != SwingConstants.TOP) {
					int vDelta = bounds.height - image.getHeight();

					if (mVAlignment == SwingConstants.CENTER) {
						vDelta /= 2;
					}
					y += vDelta;
				}

				gc.drawImage(image, x, y, null);
			}
		}
	}

	@Override
	public int getPreferredWidth(Row row, Column column) {
		Object data = row != null ? row.getData(column) : null;

		return data instanceof StdImage ? ((StdImage) data).getWidth() : 0;
	}

	@Override
	public int getPreferredHeight(Row row, Column column) {
		Object data = row != null ? row.getData(column) : null;

		return data instanceof StdImage ? ((StdImage) data).getHeight() : 0;
	}

	@Override
	public Cursor getCursor(MouseEvent event, Rectangle bounds, Row row, Column column) {
		return Cursor.getDefaultCursor();
	}

	@Override
	public String getToolTipText(MouseEvent event, Rectangle bounds, Row row, Column column) {
		return null;
	}

	@Override
	public boolean participatesInDynamicRowLayout() {
		return false;
	}

	@Override
	public void mouseClicked(MouseEvent event, Rectangle bounds, Row row, Column column) {
		// Does nothing
	}
}
