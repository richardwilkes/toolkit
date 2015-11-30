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

package com.trollworks.toolkit.ui.widget.treetable;

import com.trollworks.toolkit.ui.RetinaIcon;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.widget.Icons;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/** A widget that can display both tabular and hierarchical data. */
public class TreeTable extends JPanel implements MouseListener, MouseMotionListener, TreeTableModelListener, SelectionModelListener, Scrollable {
	private static final int	DISCLOSURE_WIDTH		= Icons.getDisclosure(false, false).getIconWidth();
	private static final int	DISCLOSURE_HEIGHT		= Icons.getDisclosure(false, false).getIconHeight();
	private TreeTableModel		mModel;
	private TreeTableRenderer	mRenderer;
	private Color				mDividerColor			= Color.LIGHT_GRAY;
	private boolean				mShowDisclosureControl	= true;
	private boolean				mShowColumnDividers		= true;
	private boolean				mShowRowDividers;
	private int					mLastMouseX				= Integer.MIN_VALUE;
	private int					mLastMouseY				= Integer.MIN_VALUE;
	private Object				mOverRow;
	private boolean				mOverDisclosure;

	/**
	 * @param model The {@link TreeTableModel} to use.
	 * @param renderer The {@link TreeTableRenderer} to use.
	 */
	public TreeTable(TreeTableModel model, TreeTableRenderer renderer) {
		setFocusable(true);
		setBackground(Color.WHITE);
		setModel(model);
		setRenderer(renderer);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	/** @return The current {@link TreeTableModel}. */
	public TreeTableModel getModel() {
		return mModel;
	}

	/** @param model The {@link TreeTableModel} to begin using. */
	public void setModel(TreeTableModel model) {
		if (mModel != null) {
			mModel.removeTreeTableModelListener(this);
			mModel.getSelectionModel().removeSelectionModelListener(this);
		}
		mModel = model;
		mModel.addTreeTableModelListener(this);
		mModel.getSelectionModel().addSelectionModelListener(this);
	}

	/** @return The current {@link TreeTableRenderer}. */
	public TreeTableRenderer getRenderer() {
		return mRenderer;
	}

	/** @param renderer The {@link TreeTableRenderer} to begin using. */
	public void setRenderer(TreeTableRenderer renderer) {
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
		int width = mShowDisclosureControl ? DISCLOSURE_WIDTH : 0;
		int count = mRenderer.getColumnCount(this);
		for (int i = 0; i < count; i++) {
			width += mRenderer.getPreferredColumnWidth(this, i);
			if (mShowColumnDividers) {
				width++;
			}
		}
		if (mShowColumnDividers && width > 0) {
			width--;
		}
		int height = 0;
		for (Object row : mModel.getRootRows()) {
			height += calculateHeight(row);
		}
		if (mShowRowDividers && height > 0) {
			height--;
		}
		return new Dimension(width, height);
	}

	private int calculateHeight(Object row) {
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
		if (!mModel.isLeafRow(row) && mModel.isRowDisclosed(row)) {
			int count = mModel.getRowChildCount(row);
			for (int i = 0; i < count; i++) {
				height += calculateHeight(mModel.getRowChild(row, i));
			}
		}
		return height;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D gc = (Graphics2D) g.create();
		try {
			Insets insets = getInsets();
			Rectangle bounds = new Rectangle(insets.left, insets.top, getWidth() - (insets.left + insets.right), getHeight() - (insets.top + insets.bottom));
			Rectangle clip = gc.getClipBounds();
			int y = bounds.y;
			for (Object row : mModel.getRootRows()) {
				y = drawRow(gc, row, bounds, clip, y);
				if (y >= clip.y + clip.height) {
					break;
				}
			}
			if (mShowColumnDividers) {
				gc.setColor(mDividerColor);
				int x = bounds.x;
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

	private int drawRow(Graphics2D gc, Object row, Rectangle bounds, Rectangle clip, int y) {
		int height = mRenderer.getRowHeight(this, row);
		if (y + height > clip.y) {
			int x = bounds.x;
			if (mShowDisclosureControl) {
				int iconWidth = DISCLOSURE_WIDTH;
				if (x + iconWidth > clip.x) {
					if (!mModel.isLeafRow(row)) {
						RetinaIcon icon = Icons.getDisclosure(mModel.isRowDisclosed(row), mLastMouseY >= y && mLastMouseY < y + DISCLOSURE_HEIGHT && mLastMouseX >= x && mLastMouseX < x + iconWidth);
						icon.paintIcon(this, gc, x, y);
					}
				}
				x += iconWidth;
			}
			boolean rowSelected = mModel.getSelectionModel().isSelected(row);
			if (rowSelected) {
				gc.setColor(UIManager.getColor("List.selectionBackground")); //$NON-NLS-1$
				gc.fillRect(x, y, bounds.x + bounds.width - x, height);
			}
			int columns = mRenderer.getColumnCount(this);
			for (int i = 0; i < columns; i++) {
				int width = mRenderer.getColumnWidth(this, i);
				if (x + width > clip.x) {
					Rectangle cellBounds = new Rectangle(x, y, width, height);
					gc.setClip(clip.intersection(cellBounds));
					gc.translate(x, y);
					mRenderer.drawCell(this, gc, row, i, width, height, rowSelected);
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
				y = drawRow(gc, mModel.getRowChild(row, i), bounds, clip, y);
				if (y >= clip.y + clip.height) {
					break;
				}
			}
		}
		return y;
	}

	/** @return <code>true</code> if the disclosure controls should be shown. */
	public final boolean showDisclosureControl() {
		return mShowDisclosureControl;
	}

	/** @param show <code>true</code> if the disclosure controls should be shown. */
	public final void setShowDisclosureControl(boolean show) {
		mShowDisclosureControl = show;
	}

	/** @return <code>true</code> if the column dividers should be shown. */
	public final boolean showColumnDividers() {
		return mShowColumnDividers;
	}

	/** @param show <code>true</code> if the column dividers should be shown. */
	public final void setShowColumnDividers(boolean show) {
		mShowColumnDividers = show;
	}

	/** @return <code>true</code> if the row dividers should be shown. */
	public final boolean showRowDividers() {
		return mShowRowDividers;
	}

	/** @param show <code>true</code> if the row dividers should be shown. */
	public final void setShowRowDividers(boolean show) {
		mShowRowDividers = show;
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		// Unused
	}

	@Override
	public void mousePressed(MouseEvent event) {
		requestFocusInWindow();
		SelectionModel selectionModel = mModel.getSelectionModel();
		Object row = getRowAt(event.getY());
		if (row != null) {
			if (event.isMetaDown()) {
				if (selectionModel.isSelected(row)) {
					selectionModel.deselect(row);
				} else {
					selectionModel.select(row, true);
				}
			} else {
				selectionModel.select(row, false);
			}
		} else {
			selectionModel.clear();
		}
		int column = getColumnAt(row, event.getX());
		if (column == -2) {
			mModel.setRowDisclosed(row, !mModel.isRowDisclosed(row));
			if (UIUtilities.getAncestorOfType(this, JScrollPane.class) != null) {
				setSize(getPreferredSize());
			}
		} else if (column != -1) {
			Rectangle cellBounds = getCellBounds(row, column);
			mRenderer.mousePressed(this, row, column, event.getX() - cellBounds.x, event.getY() - cellBounds.y, cellBounds.width, cellBounds.height, event.getButton(), event.getClickCount(), event.getModifiers(), event.isPopupTrigger());
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
		Object wasOverRow = mOverRow;
		boolean wasOverDisclosure = mOverDisclosure;
		mOverRow = null;
		mOverDisclosure = false;
		if (mOverDisclosure != wasOverDisclosure || wasOverDisclosure && mOverRow != wasOverRow) {
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
		Object wasOverRow = mOverRow;
		boolean wasOverDisclosure = mOverDisclosure;
		mOverRow = getRowAt(mLastMouseY);
		mOverDisclosure = isOverDisclosure(mOverRow, mLastMouseX);
		if (mOverDisclosure != wasOverDisclosure || wasOverDisclosure && mOverRow != wasOverRow) {
			repaint();
		}
	}

	/**
	 * @param row The row object to check.
	 * @param x The x-coordinate to check.
	 * @return <code>true</code> if the x-coordinate is over the disclosure control.
	 */
	public boolean isOverDisclosure(Object row, int x) {
		if (row != null && !mModel.isLeafRow(row)) {
			int left = getInsets().left;
			return x >= left && x < left + DISCLOSURE_WIDTH;
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

	/**
	 * @param y The y-coordinate to check.
	 * @return The row object at the specified y-coordinate, or <code>null</code> if there isn't
	 *         one.
	 */
	public Object getRowAt(int y) {
		int top = getInsets().top;
		if (y > top) {
			int[] pos = new int[] { top };
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
			if (mShowDisclosureControl) {
				int width = DISCLOSURE_WIDTH;
				if (x >= left && x < left + width) {
					if (!mModel.isLeafRow(row)) {
						return -2;
					}
				}
				left += width;
			}
			if (x >= left) {
				int columns = mRenderer.getColumnCount(this);
				for (int i = 0; i < columns; i++) {
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
		int width = getWidth();
		width -= insets.left + insets.right;
		if (mShowDisclosureControl) {
			width -= DISCLOSURE_WIDTH;
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
		int top = getInsets().top;
		int[] pos = new int[] { top };
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
	 * @param row The row object to check.
	 * @param modelColumnIndex The {@link TreeTableModel}'s column index to check.
	 * @return The bounding rectangle of the cell.
	 */
	public Rectangle getCellBounds(Object row, int modelColumnIndex) {
		if (row != null) {
			int left = getInsets().left;
			if (mShowDisclosureControl) {
				left += DISCLOSURE_WIDTH;
			}
			int columns = mRenderer.getColumnCount(this);
			for (int i = 0; i < columns; i++) {
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
	public void modelWasUpdated() {
		repaint();
	}

	@Override
	public void selectionChanged() {
		repaint();
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
}
