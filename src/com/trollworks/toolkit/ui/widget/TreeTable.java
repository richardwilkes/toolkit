/*
 * Copyright (c) 1998-2020 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui.widget;

import com.trollworks.toolkit.collections.ReverseListIterator;
import com.trollworks.toolkit.ui.Colors;
import com.trollworks.toolkit.ui.RetinaIcon;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.utility.SelectionModel;
import com.trollworks.toolkit.utility.notification.BatchNotifierTarget;
import com.trollworks.toolkit.utility.notification.Notifier;
import com.trollworks.toolkit.utility.task.Tasks;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/** A widget that can display both tabular and hierarchical data. */
public class TreeTable extends JPanel implements FocusListener, KeyListener, MouseListener, MouseMotionListener, Scrollable, BatchNotifierTarget, SelectionModel {
    private static final int         DISCLOSURE_WIDTH       = Icons.getDisclosure(false, false).getIconWidth();
    private static final int         DISCLOSURE_HEIGHT      = Icons.getDisclosure(false, false).getIconHeight();
    private              Model       mModel;
    private              Renderer    mRenderer;
    private              boolean     mBatchMode;
    private              Set<String> mBatchNames            = new HashSet<>();
    private              Color       mDividerColor          = Color.LIGHT_GRAY;
    private              boolean     mShowDisclosureControl = true;
    private              boolean     mShowColumnDividers    = true;
    private              boolean     mShowRowDividers;
    private              int         mLastMouseX            = Integer.MIN_VALUE;
    private              int         mLastMouseY            = Integer.MIN_VALUE;
    private              Object      mOverRow;
    private              boolean     mOverDisclosure;

    /**
     * @param model    The {@link Model} to use.
     * @param renderer The {@link Renderer} to use.
     */
    public TreeTable(Model model, Renderer renderer) {
        setDoubleBuffered(true);
        setFocusable(true);
        setBackground(Color.WHITE);
        setModel(model);
        setRenderer(renderer);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        addFocusListener(this);
    }

    /** @return The current {@link Model}. */
    public Model getModel() {
        return mModel;
    }

    /** @param model The {@link Model} to begin using. */
    public void setModel(Model model) {
        if (mModel != null) {
            mModel.getNotifier().remove(this);
        }
        mModel = model;
        mModel.getNotifier().add(this, mModel.getStructureChangedNotification(), mModel.getContentChangedNotification(), mModel.getSelectionChangedNotification());
    }

    @Override
    public int getNotificationPriority() {
        return 0;
    }

    @Override
    public void enterBatchMode() {
        mBatchMode = true;
    }

    @Override
    public void handleNotification(Object producer, String name, Object data) {
        if (producer == mModel) {
            if (mBatchMode) {
                mBatchNames.add(name);
            } else {
                if (name.startsWith(mModel.getStructureChangedNotification())) {
                    Tasks.scheduleOnUIThread(() -> setSize(getPreferredSize()), 0, TimeUnit.MILLISECONDS, this);
                } else if (name.startsWith(mModel.getContentChangedNotification()) || name.startsWith(mModel.getSelectionChangedNotification())) {
                    repaint();
                }
            }
        }
    }

    @Override
    public void leaveBatchMode() {
        mBatchMode = false;
        for (String one : mBatchNames) {
            handleNotification(mModel, one, null);
        }
        mBatchNames.clear();
    }

    /** @return The current {@link Renderer}. */
    public Renderer getRenderer() {
        return mRenderer;
    }

    /** @param renderer The {@link Renderer} to begin using. */
    public void setRenderer(Renderer renderer) {
        mRenderer = renderer;
    }

    /** @return The color used when drawing the divider. */
    public final Color getDividerColor() {
        return mDividerColor;
    }

    /** @param color The color to use when drawing the divider. */
    public final void setDividerColor(Color color) {
        mDividerColor = color;
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = new Dimension(0, 0);
        for (Object row : mModel.getRootRows()) {
            calculateHeight(row, size);
        }
        if (mShowRowDividers && size.height > 0) {
            size.height--;
        }
        int count = mRenderer.getColumnCount(this);
        for (int i = 0; i < count; i++) {
            size.width += mRenderer.getPreferredColumnWidth(this, i);
        }
        if (mShowColumnDividers && count > 0) {
            size.width += count - 1;
        }
        return size;
    }

    private void calculateHeight(Object row, Dimension size) {
        int height = mRenderer.getRowHeight(this, row);
        if (mShowDisclosureControl) {
            int iconHeight = DISCLOSURE_HEIGHT;
            if (height < iconHeight) {
                height = iconHeight;
            }
        }
        if (mShowRowDividers) {
            height++;
        }
        size.height += height;
        if (mShowDisclosureControl) {
            int indent = (1 + getRowDepth(row)) * DISCLOSURE_WIDTH;
            if (size.width < indent) {
                size.width = indent;
            }
        }
        if (!mModel.isLeafRow(row) && mModel.isRowDisclosed(row)) {
            int count = mModel.getRowChildCount(row);
            for (int i = 0; i < count; i++) {
                calculateHeight(mModel.getRowChild(row, i), size);
            }
        }
    }

    @Override
    public void focusGained(FocusEvent event) {
        if (hasSelection()) {
            repaint();
        }
    }

    @Override
    public void focusLost(FocusEvent event) {
        if (hasSelection()) {
            repaint();
        }
    }

    public Object getLastRow() {
        List<Object> rootRows = mModel.getRootRows();
        if (!rootRows.isEmpty()) {
            return getLastRow(rootRows.get(rootRows.size() - 1));
        }
        return null;
    }

    private Object getLastRow(Object row) {
        if (mModel.isLeafRow(row) || !mModel.isRowDisclosed(row)) {
            return row;
        }
        int count = mModel.getRowChildCount(row);
        if (count == 0) {
            return row;
        }
        return getLastRow(mModel.getRowChild(row, count - 1));
    }

    public List<Object> getSelectionInRowOrder() {
        List<Object> selection = new ArrayList<>();
        for (Object row : mModel.getRootRows()) {
            getSelectionInRowOrder(row, selection);
        }
        return selection;
    }

    private void getSelectionInRowOrder(Object row, List<Object> selection) {
        if (isSelected(row)) {
            selection.add(row);
        }
        if (!mModel.isLeafRow(row) && mModel.isRowDisclosed(row)) {
            int count = mModel.getRowChildCount(row);
            for (int i = 0; i < count; i++) {
                getSelectionInRowOrder(mModel.getRowChild(row, i), selection);
            }
        }
    }

    public Object getFirstSelectedRow() {
        for (Object row : mModel.getRootRows()) {
            row = getFirstSelectedRow(row);
            if (row != null) {
                return row;
            }
        }
        return null;
    }

    private Object getFirstSelectedRow(Object row) {
        if (isSelected(row)) {
            return row;
        }
        if (!mModel.isLeafRow(row) && mModel.isRowDisclosed(row)) {
            int count = mModel.getRowChildCount(row);
            for (int i = 0; i < count; i++) {
                Object child = getFirstSelectedRow(mModel.getRowChild(row, i));
                if (child != null) {
                    return child;
                }
            }
        }
        return null;
    }

    public Object getLastSelectedRow() {
        for (Object row : new ReverseListIterator<>(mModel.getRootRows())) {
            row = getLastSelectedRow(row);
            if (row != null) {
                return row;
            }
        }
        return null;
    }

    private Object getLastSelectedRow(Object row) {
        if (!mModel.isLeafRow(row) && mModel.isRowDisclosed(row)) {
            for (int i = mModel.getRowChildCount(row); --i >= 0; ) {
                Object child = getLastSelectedRow(mModel.getRowChild(row, i));
                if (child != null) {
                    return child;
                }
            }
        }
        return isSelected(row) ? row : null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gc = (Graphics2D) g.create();
        try {
            Insets    insets = getInsets();
            Rectangle bounds = new Rectangle(insets.left, insets.top, getWidth() - (insets.left + insets.right), getHeight() - (insets.top + insets.bottom));
            Rectangle clip   = gc.getClipBounds();
            int       y      = bounds.y;
            boolean   active = isFocusOwner();
            for (Object row : mModel.getRootRows()) {
                y = drawRow(gc, row, bounds, clip, y, active);
                if (y >= clip.y + clip.height) {
                    break;
                }
            }
            if (mShowColumnDividers) {
                gc.setColor(mDividerColor);
                int x       = bounds.x;
                int columns = mRenderer.getColumnCount(this);
                for (int i = 0; i < columns; i++) {
                    x += mRenderer.getColumnWidth(this, i);
                    gc.drawLine(x, bounds.y, x, bounds.height);
                    x++;
                }
            }
        } finally {
            gc.dispose();
        }
    }

    private int drawRow(Graphics2D gc, Object row, Rectangle bounds, Rectangle clip, int y, boolean active) {
        int height = mRenderer.getRowHeight(this, row);
        if (y + height > clip.y) {
            int     x           = bounds.x;
            boolean rowSelected = isSelected(row);
            if (rowSelected) {
                gc.setColor(Colors.getListBackground(true, active));
                gc.fillRect(x, y, bounds.x + bounds.width - x, height);
            }
            int columns          = mRenderer.getColumnCount(this);
            int disclosureColumn = mShowDisclosureControl ? mRenderer.getDisclosureColumn() : -1;
            for (int i = 0; i < columns; i++) {
                if (i == disclosureColumn) {
                    x += getRowDepth(row) * DISCLOSURE_WIDTH;
                    if (x + DISCLOSURE_WIDTH > clip.x) {
                        if (!mModel.isLeafRow(row)) {
                            RetinaIcon icon = Icons.getDisclosure(mModel.isRowDisclosed(row), mLastMouseY >= y && mLastMouseY < y + height && mLastMouseX >= x && mLastMouseX < x + DISCLOSURE_WIDTH);
                            gc.setClip(clip.intersection(new Rectangle(x, y, DISCLOSURE_WIDTH, height)));
                            icon.paintIcon(this, gc, x, y + (height - DISCLOSURE_HEIGHT) / 2);
                        }
                    }
                    x += DISCLOSURE_WIDTH;
                }
                int width = mRenderer.getColumnWidth(this, i);
                if (x + width > clip.x) {
                    Rectangle cellBounds = new Rectangle(x, y, width, height);
                    gc.setClip(clip.intersection(cellBounds));
                    gc.translate(x, y);
                    mRenderer.drawCell(this, gc, row, i, width, height, rowSelected, active);
                    gc.translate(-x, -y);
                }
                x += width;
                if (mShowColumnDividers) {
                    x++;
                }
                if (x >= clip.x + clip.width) {
                    break;
                }
            }
            gc.setClip(clip);
        }
        y += height;
        if (mShowRowDividers && y >= clip.y) {
            gc.setColor(mDividerColor);
            gc.drawLine(bounds.x, y, bounds.width, y);
            y++;
        }
        if (y < clip.y + clip.height && !mModel.isLeafRow(row) && mModel.isRowDisclosed(row)) {
            int count = mModel.getRowChildCount(row);
            for (int i = 0; i < count; i++) {
                y = drawRow(gc, mModel.getRowChild(row, i), bounds, clip, y, active);
                if (y >= clip.y + clip.height) {
                    break;
                }
            }
        }
        return y;
    }

    /** @return {@code true} if the disclosure controls should be shown. */
    public final boolean showDisclosureControl() {
        return mShowDisclosureControl;
    }

    /** @param show {@code true} if the disclosure controls should be shown. */
    public final void setShowDisclosureControl(boolean show) {
        mShowDisclosureControl = show;
    }

    /** @return {@code true} if the column dividers should be shown. */
    public final boolean showColumnDividers() {
        return mShowColumnDividers;
    }

    /** @param show {@code true} if the column dividers should be shown. */
    public final void setShowColumnDividers(boolean show) {
        mShowColumnDividers = show;
    }

    /** @return {@code true} if the row dividers should be shown. */
    public final boolean showRowDividers() {
        return mShowRowDividers;
    }

    /** @param show {@code true} if the row dividers should be shown. */
    public final void setShowRowDividers(boolean show) {
        mShowRowDividers = show;
    }

    public void selectUp(boolean extend) {
        if (hasSelection()) {
            Set<Object> selection = getSelection();
            if (selection.size() == 1) {
                Rectangle bounds = getRowBounds(selection.iterator().next());
                Object    row    = getRowAt(bounds.y - 1);
                if (row != null) {
                    select(row, extend);
                    scrollRectToVisible(getRowBounds(row));
                }
            } else {
                Object row = getFirstSelectedRow();
                if (extend) {
                    Rectangle bounds = getRowBounds(row);
                    row = getRowAt(bounds.y - 1);
                }
                if (row != null) {
                    select(row, extend);
                    scrollRectToVisible(getRowBounds(row));
                }
            }
        } else {
            List<Object> rootRows = mModel.getRootRows();
            if (!rootRows.isEmpty()) {
                Object row = getLastRow();
                select(row, false);
                scrollRectToVisible(getRowBounds(row));
            }
        }
    }

    public void selectDown(boolean extend) {
        if (hasSelection()) {
            Set<Object> selection = getSelection();
            if (selection.size() == 1) {
                Rectangle bounds = getRowBounds(selection.iterator().next());
                Object    row    = getRowAt(bounds.y + bounds.height);
                if (row != null) {
                    select(row, extend);
                    scrollRectToVisible(getRowBounds(row));
                }
            } else {
                Object row = getLastSelectedRow();
                if (extend) {
                    Rectangle bounds = getRowBounds(row);
                    row = getRowAt(bounds.y + bounds.height);
                }
                if (row != null) {
                    select(row, extend);
                    scrollRectToVisible(getRowBounds(row));
                }
            }
        } else {
            List<Object> rootRows = mModel.getRootRows();
            if (!rootRows.isEmpty()) {
                Object row = rootRows.get(0);
                select(row, false);
                scrollRectToVisible(getRowBounds(row));
            }
        }
    }

    public void selectHome() {
        List<Object> rootRows = mModel.getRootRows();
        if (!rootRows.isEmpty()) {
            Object row = rootRows.get(0);
            select(row, false);
            scrollRectToVisible(getRowBounds(row));
        }
    }

    public void selectEnd() {
        List<Object> rootRows = mModel.getRootRows();
        if (!rootRows.isEmpty()) {
            Object row = getLastRow();
            select(row, false);
            scrollRectToVisible(getRowBounds(row));
        }
    }

    public void openSelectedRows() {
        Notifier notifier = mModel.getNotifier();
        notifier.startBatch();
        for (Object row : getSelection()) {
            if (!mModel.isLeafRow(row) && !mModel.isRowDisclosed(row)) {
                mModel.setRowDisclosed(row, true);
            }
        }
        notifier.endBatch();
    }

    public void closeSelectedRows() {
        Notifier notifier = mModel.getNotifier();
        notifier.startBatch();
        for (Object row : getSelection()) {
            if (!mModel.isLeafRow(row) && mModel.isRowDisclosed(row)) {
                mModel.setRowDisclosed(row, false);
            }
        }
        notifier.endBatch();
    }

    @Override
    public void keyTyped(KeyEvent event) {
        // Unused
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (!event.isConsumed()) {
            switch (event.getKeyCode()) {
            case KeyEvent.VK_UP:
                selectUp(event.isShiftDown());
                break;
            case KeyEvent.VK_DOWN:
                selectDown(event.isShiftDown());
                break;
            case KeyEvent.VK_LEFT:
                closeSelectedRows();
                break;
            case KeyEvent.VK_RIGHT:
                openSelectedRows();
                break;
            case KeyEvent.VK_HOME:
                selectHome();
                break;
            case KeyEvent.VK_END:
                selectEnd();
                break;
            default:
                return;
            }
            event.consume();
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        // Unused
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        // Unused
    }

    @Override
    public void mousePressed(MouseEvent event) {
        requestFocusInWindow();
        Object row = getRowAt(event.getY());
        if (row != null) {
            if (event.isMetaDown()) {
                if (isSelected(row)) {
                    deselect(row);
                } else {
                    select(row, true);
                }
            } else {
                select(row, false);
            }
        } else {
            clearSelection();
        }
        int column = getColumnAt(row, event.getX());
        if (column == -2) {
            mModel.setRowDisclosed(row, !mModel.isRowDisclosed(row));
        } else if (column != -1) {
            Rectangle cellBounds = getCellBounds(row, column);
            mRenderer.mousePressed(this, row, column, event.getX() - cellBounds.x, event.getY() - cellBounds.y, cellBounds.width, cellBounds.height, event.getButton(), event.getClickCount(), event.getModifiersEx(), event.isPopupTrigger());
        }
    }

    @Override
    public void mouseReleased(MouseEvent eevent) {
        // Unused
    }

    @Override
    public void mouseEntered(MouseEvent event) {
        mouseMoved(event);
    }

    @Override
    public void mouseExited(MouseEvent event) {
        clearMouseState();
    }

    private void clearMouseState() {
        mLastMouseX = Integer.MIN_VALUE;
        mLastMouseY = Integer.MIN_VALUE;
        boolean wasOverDisclosure = mOverDisclosure;
        mOverRow = null;
        mOverDisclosure = false;
        if (wasOverDisclosure) {
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        // Unused
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        mLastMouseX = event.getX();
        mLastMouseY = event.getY();
        Object  wasOverRow        = mOverRow;
        boolean wasOverDisclosure = mOverDisclosure;
        mOverRow = getRowAt(mLastMouseY);
        mOverDisclosure = isOverDisclosure(mOverRow, mLastMouseX);
        if (mOverDisclosure != wasOverDisclosure || wasOverDisclosure && mOverRow != wasOverRow) {
            repaint();
        }
    }

    /**
     * @param row The row object to check.
     * @param x   The x-coordinate to check.
     * @return {@code true} if the x-coordinate is over the disclosure control.
     */
    public boolean isOverDisclosure(Object row, int x) {
        if (mShowDisclosureControl && row != null && !mModel.isLeafRow(row)) {
            int column = mRenderer.getDisclosureColumn();
            if (column != -1) {
                int left = getInsets().left;
                for (int i = 0; i < column; i++) {
                    left += mRenderer.getColumnWidth(this, i);
                    if (mShowColumnDividers) {
                        left++;
                    }
                }
                left += getRowDepth(row) * DISCLOSURE_WIDTH;
                return x >= left && x < left + DISCLOSURE_WIDTH;
            }
        }
        return false;
    }

    public int getRowDepth(Object row) {
        int depth = -1;
        while (true) {
            row = mModel.getRowParent(row);
            depth++;
            if (row == null) {
                break;
            }
        }
        return depth;
    }

    public int getDeepestDisclosedDepth() {
        int deepest = 0;
        for (Object row : mModel.getRootRows()) {
            deepest = getDeepestDisclosedDepth(row, 0, deepest);
        }
        return deepest;
    }

    private int getDeepestDisclosedDepth(Object row, int depth, int deepest) {
        if (deepest < depth) {
            deepest = depth;
        }
        if (!mModel.isLeafRow(row) && mModel.isRowDisclosed(row)) {
            int count = mModel.getRowChildCount(row);
            for (int i = 0; i < count; i++) {
                deepest = getDeepestDisclosedDepth(mModel.getRowChild(row, i), depth + 1, deepest);
            }
        }
        return deepest;
    }

    /**
     * @param y The y-coordinate to check.
     * @return The row object at the specified y-coordinate, or {@code null} if there isn't one.
     */
    public Object getRowAt(int y) {
        int top = getInsets().top;
        if (y > top) {
            int[] pos = {top};
            for (Object row : mModel.getRootRows()) {
                Object result = getRowAt(row, y, pos);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private Object getRowAt(Object row, int y, int[] pos) {
        pos[0] += mRenderer.getRowHeight(this, row);
        if (mShowRowDividers) {
            pos[0]++;
        }
        if (y < pos[0]) {
            return row;
        }
        if (!mModel.isLeafRow(row) && mModel.isRowDisclosed(row)) {
            int count = mModel.getRowChildCount(row);
            for (int i = 0; i < count; i++) {
                Object result = getRowAt(mModel.getRowChild(row, i), y, pos);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private int getColumnAt(Object row, int x) {
        if (row != null) {
            int left = getInsets().left;
            if (x >= left) {
                int disclosureColumn = mShowDisclosureControl ? mRenderer.getDisclosureColumn() : -1;
                int columns          = mRenderer.getColumnCount(this);
                for (int i = 0; i < columns; i++) {
                    if (disclosureColumn == i) {
                        left += getRowDepth(row) * DISCLOSURE_WIDTH;
                        if (x >= left && x < left + DISCLOSURE_WIDTH) {
                            if (!mModel.isLeafRow(row)) {
                                return -2;
                            }
                        }
                        left += DISCLOSURE_WIDTH;
                    }
                    int width = mRenderer.getColumnWidth(this, i);
                    if (left + width > x) {
                        return i;
                    }
                    left += width;
                    if (mShowColumnDividers) {
                        left++;
                    }
                }
            }
        }
        return -1;
    }

    public int getAvailableRowWidth() {
        Insets insets = getInsets();
        int    width  = getWidth();
        width -= insets.left + insets.right;
        if (mShowDisclosureControl) {
            width -= (1 + getDeepestDisclosedDepth()) * DISCLOSURE_WIDTH;
        }
        if (mShowColumnDividers) {
            width -= mRenderer.getColumnCount(this) - 1;
        }
        return width;
    }

    /**
     * @param row The row object to check.
     * @return The bounding rectangle of the row.
     */
    public Rectangle getRowBounds(Object row) {
        int   top = getInsets().top;
        int[] pos = {top};
        for (Object one : mModel.getRootRows()) {
            Rectangle result = getRowAt(one, row, pos);
            if (result != null) {
                return result;
            }
        }
        return new Rectangle();
    }

    private Rectangle getRowAt(Object row, Object match, int[] pos) {
        int height = mRenderer.getRowHeight(this, row);
        if (row == match) {
            return new Rectangle(0, pos[0], getWidth(), height);
        }
        pos[0] += height;
        if (mShowRowDividers) {
            pos[0]++;
        }
        if (!mModel.isLeafRow(row) && mModel.isRowDisclosed(row)) {
            int count = mModel.getRowChildCount(row);
            for (int i = 0; i < count; i++) {
                Rectangle result = getRowAt(mModel.getRowChild(row, i), match, pos);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * @param row              The row object to check.
     * @param modelColumnIndex The {@link Model}'s column index to check.
     * @return The bounding rectangle of the cell.
     */
    public Rectangle getCellBounds(Object row, int modelColumnIndex) {
        if (row != null) {
            int left             = getInsets().left;
            int disclosureColumn = mShowDisclosureControl ? mRenderer.getDisclosureColumn() : -1;
            int columns          = mRenderer.getColumnCount(this);
            for (int i = 0; i < columns; i++) {
                if (i == disclosureColumn) {
                    left += (1 + getRowDepth(row)) * DISCLOSURE_WIDTH;
                }
                int width = mRenderer.getColumnWidth(this, i);
                if (i == modelColumnIndex) {
                    Rectangle bounds = getRowBounds(row);
                    bounds.x = left;
                    bounds.width = width;
                    return bounds;
                }
                left += width;
                if (mShowColumnDividers) {
                    left++;
                }
            }
        }
        return new Rectangle();
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? 16 : 20;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return UIUtilities.shouldTrackViewportWidth(this);
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return UIUtilities.shouldTrackViewportHeight(this);
    }

    @Override
    public boolean hasSelection() {
        return mModel.hasSelection();
    }

    @Override
    public Set<Object> getSelection() {
        return mModel.getSelection();
    }

    @Override
    public boolean isSelected(Object obj) {
        return mModel.isSelected(obj);
    }

    @Override
    public void select(Object obj, boolean add) {
        mModel.select(obj, add);
    }

    @Override
    public void select(Collection<?> objs, boolean add) {
        mModel.select(objs, add);
    }

    @Override
    public void deselect(Object obj) {
        mModel.deselect(obj);
    }

    @Override
    public void deselect(Collection<?> objs) {
        mModel.deselect(objs);
    }

    @Override
    public void clearSelection() {
        mModel.clearSelection();
    }

    /** Objects that want to provide data to a {@link TreeTable} must implement this interface. */
    public interface Model extends SelectionModel {
        /** @return The {@link Notifier} that will be used for notifications of changes. */
        Notifier getNotifier();

        /** @return The notification name sent when structure changes occur. */
        String getStructureChangedNotification();

        /** @return The notification name sent when content changes occur. */
        String getContentChangedNotification();

        /** @return The notification name sent when selection changes occur. */
        String getSelectionChangedNotification();

        /** @return A {@link List} containing the root row objects. May be empty. */
        List<Object> getRootRows();

        /**
         * @param row The row object to check.
         * @return {@code true} if the row is not capable of having child rows.
         */
        boolean isLeafRow(Object row);

        /**
         * @param row The row object to check.
         * @return {@code true} if the row is in the disclosed (open) state.
         */
        boolean isRowDisclosed(Object row);

        /**
         * @param row       The row object to modify.
         * @param disclosed The disclosure state to set.
         */
        void setRowDisclosed(Object row, boolean disclosed);

        /**
         * @param row The row object to work on.
         * @return The number of direct children the row contains.
         */
        int getRowChildCount(Object row);

        /**
         * @param row   The row object to work on.
         * @param index The index specifying which child to return.
         * @return The child at the specified index.
         */
        Object getRowChild(Object row, int index);

        /**
         * @param row   The row object to work on.
         * @param child The child row object.
         * @return The child's index within the row.
         */
        int getIndexOfRowChild(Object row, Object child);

        /**
         * @param row The row object to work on.
         * @return The row object's parent row object, or {@code null} if the passed in row is a
         *         root.
         */
        Object getRowParent(Object row);
    }

    /**
     * Objects that want to provide rendering and UI interaction services for cells within a {@link
     * TreeTable} must implement this interface.
     */
    public interface Renderer {
        /**
         * @param table The {@link TreeTable} being rendered.
         * @return The number of columns that will be displayed.
         */
        int getColumnCount(TreeTable table);

        /**
         * @param table  The {@link TreeTable} being rendered.
         * @param column The column index to check.
         * @return The preferred width of the column.
         */
        int getPreferredColumnWidth(TreeTable table, int column);

        /**
         * @param table  The {@link TreeTable} being rendered.
         * @param column The column index to check.
         * @return The width of the column.
         */
        int getColumnWidth(TreeTable table, int column);

        /** @return The column that should contain the disclosure controls, if they are present. */
        int getDisclosureColumn();

        /**
         * @param table The {@link TreeTable} containing the row.
         * @param row   The row object to check.
         * @return The height of the row.
         */
        int getRowHeight(TreeTable table, Object row);

        /**
         * Draws the specified cell.
         *
         * @param table    The {@link TreeTable} being rendered.
         * @param gc       The graphics context. The origin will be set to the upper-left corner of
         *                 the cell.
         * @param row      The row being rendered.
         * @param column   The column index being rendered.
         * @param width    The width of the cell.
         * @param height   The height of the cell.
         * @param selected {@code true} if the row is currently selected.
         * @param active   {@code true} if the widget is currently active.
         */
        void drawCell(TreeTable table, Graphics2D gc, Object row, int column, int width, int height, boolean selected, boolean active);

        /**
         * @param table        The {@link TreeTable} being clicked on.
         * @param row          The row being clicked on.
         * @param column       The column index being clicked on.
         * @param x            The x-coordinate of the mouse in cell-relative coordinates.
         * @param y            The y-coordinate of the mouse in cell-relative coordinates.
         * @param width        The width of the cell.
         * @param height       The height of the cell.
         * @param button       The button that is pressed.
         * @param clickCount   The number of clicks made by this button so far.
         * @param modifiers    The key modifiers at the time of the event.
         * @param popupTrigger {@code true} if this should trigger a contextual menu.
         */
        void mousePressed(TreeTable table, Object row, int column, int x, int y, int width, int height, int button, int clickCount, int modifiers, boolean popupTrigger);
    }
}
