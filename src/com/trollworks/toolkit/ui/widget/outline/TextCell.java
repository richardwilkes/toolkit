/*
 * Copyright (c) 1998-2017 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui.widget.outline;

import com.trollworks.toolkit.ui.Colors;
import com.trollworks.toolkit.ui.RetinaIcon;
import com.trollworks.toolkit.ui.TextDrawing;
import com.trollworks.toolkit.ui.scale.Scale;
import com.trollworks.toolkit.ui.widget.Icons;
import com.trollworks.toolkit.utility.text.NumericComparator;

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
    public static final int H_MARGIN = 2;
    private int             mHAlignment;
    private boolean         mWrapped;

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
     * @param wrapped    Pass in <code>true</code> to enable wrapping.
     */
    public TextCell(int hAlignment, boolean wrapped) {
        mHAlignment = hAlignment;
        mWrapped    = wrapped;
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
     * @param active   Whether or not the active version of the color is needed.
     * @param row      The row.
     * @param column   The column.
     * @return The foreground color.
     */
    @SuppressWarnings("static-method")
    public Color getColor(boolean selected, boolean active, Row row, Column column) {
        return Colors.getListForeground(selected, active);
    }

    @Override
    public int getPreferredWidth(Outline outline, Row row, Column column) {
        Scale      scale         = Scale.get(outline);
        int        scaledHMargin = scale.scale(H_MARGIN);
        int        width         = TextDrawing.getPreferredSize(scale.scale(getFont(row, column)), getPresentationText(outline, row, column)).width;
        RetinaIcon icon          = getIcon(row, column);
        if (icon != null) {
            width += scale.scale(icon.getIconWidth()) + scaledHMargin;
        }
        return scaledHMargin + width + scaledHMargin;
    }

    @Override
    public int getPreferredHeight(Outline outline, Row row, Column column) {
        Scale scale     = Scale.get(outline);
        Font  font      = scale.scale(getFont(row, column));
        int   minHeight = TextDrawing.getPreferredSize(font, "Mg").height; //$NON-NLS-1$
        int   height    = TextDrawing.getPreferredSize(font, getPresentationText(outline, row, column)).height;
        if (row == null) {
            height = adjustHeight(scale, column.getIcon(), height);
        } else {
            height = adjustHeight(scale, Icons.getDisclosure(true, true), height);
            height = adjustHeight(scale, row.getIcon(column), height);
        }
        return minHeight > height ? minHeight : height;
    }

    private static int adjustHeight(Scale scale, RetinaIcon icon, int height) {
        if (icon != null) {
            int iconHeight = scale.scale(icon.getIconHeight());
            if (height < iconHeight) {
                height = iconHeight;
            }
        }
        return height;
    }

    @SuppressWarnings("static-method")
    public RetinaIcon getIcon(Row row, Column column) {
        return row == null ? column.getIcon() : row.getIcon(column);
    }

    @Override
    public void drawCell(Outline outline, Graphics gc, Rectangle bounds, Row row, Column column, boolean selected, boolean active) {
        Scale           scale         = Scale.get(outline);
        Font            font          = scale.scale(getFont(row, column));
        int             ascent        = gc.getFontMetrics(font).getAscent();
        StringTokenizer tokenizer     = new StringTokenizer(getPresentationText(outline, row, column), "\n", true); //$NON-NLS-1$
        int             totalHeight   = getPreferredHeight(outline, row, column);
        int             lineHeight    = TextDrawing.getPreferredSize(font, "Mg").height; //$NON-NLS-1$
        int             lineCount     = 0;
        RetinaIcon      icon          = getIcon(row, column);
        int             scaledHMargin = scale.scale(H_MARGIN);
        int             left          = icon == null ? 0 : scale.scale(icon.getIconWidth()) + scaledHMargin;
        int             cellWidth     = bounds.width - (scaledHMargin + left + scaledHMargin);
        int             vAlignment    = getVAlignment();
        int             hAlignment    = getHAlignment();

        left += bounds.x + scaledHMargin;

        if (icon != null) {
            int iy = bounds.y;
            if (vAlignment != SwingConstants.TOP) {
                int ivDelta = bounds.height - scale.scale(icon.getIconHeight());
                if (vAlignment == SwingConstants.CENTER) {
                    ivDelta /= 2;
                }
                iy += ivDelta;
            }
            icon.paintIcon(outline, gc, bounds.x + scaledHMargin, iy);
        }

        gc.setFont(font);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals("\n")) { //$NON-NLS-1$
                lineCount++;
            } else {
                String text = TextDrawing.truncateIfNecessary(font, token, cellWidth, getTruncationPolicy());
                int    x    = left;
                int    y    = bounds.y + ascent + lineHeight * lineCount;
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
     * @param outline The outline being used.
     * @param row     The row.
     * @param column  The column.
     * @return The data of this cell as a string that is prepared for display.
     */
    protected String getPresentationText(Outline outline, Row row, Column column) {
        String text = getData(row, column, false);
        if (!mWrapped || row == null) {
            return text;
        }
        int width = column.getWidth();
        if (width == -1) {
            return text;
        }
        Scale scale         = Scale.get(outline);
        int   scaledHMargin = scale.scale(H_MARGIN);
        return TextDrawing.wrapToPixelWidth(scale.scale(getFont(row, column)), text, width - (scaledHMargin + scale.scale(row.getOwner().getIndentWidth(row, column)) + scaledHMargin));
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
     * @param row    The row.
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

    @SuppressWarnings("static-method")
    protected String getToolTip(Row row, Column column, boolean nullOK) {
        if (row != null) {
            String text = row.getToolTip(column);
            return text == null ? nullOK ? null : "" : text; //$NON-NLS-1$
        }
        return column.toString();
    }

    /**
     * @param row    The row.
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
    public String getToolTipText(Outline outline, MouseEvent event, Rectangle bounds, Row row, Column column) {
        Scale scale = Scale.get(outline);
        if (row.alwaysShowToolTip(column) || getPreferredWidth(outline, row, column) - scale.scale(H_MARGIN) > column.getWidth() - scale.scale(row.getOwner().getIndentWidth(row, column))) {
            return getToolTip(row, column, true);
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
