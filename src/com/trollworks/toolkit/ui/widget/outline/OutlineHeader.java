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

import com.trollworks.toolkit.ui.GraphicsUtilities;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

/** A header panel for use with {@link Outline}. */
public class OutlineHeader extends JPanel implements DragGestureListener, DropTargetListener, DragSourceListener, MouseListener, MouseMotionListener {
	private Outline	mOwner;
	private Column	mSortColumn;
	private boolean	mResizeOK;
	private boolean	mIgnoreResizeOK;
	private Color	mTopDividerColor;

	/**
	 * Creates a new outline header.
	 *
	 * @param owner The owning outline.
	 */
	public OutlineHeader(Outline owner) {
		super();
		mOwner = owner;
		setOpaque(true);
		addMouseListener(this);
		addMouseMotionListener(this);
		setAutoscrolls(true);
		ToolTipManager.sharedInstance().registerComponent(this);

		if (!GraphicsUtilities.inHeadlessPrintMode()) {
			DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
			setDropTarget(new DropTarget(this, this));
		}
	}

	/** @return The top divider color. */
	public Color getTopDividerColor() {
		return mTopDividerColor == null ? mOwner.getDividerColor() : mTopDividerColor;
	}

	/** @param color The new top divider color. Pass in <code>null</code> to restore defaults. */
	public void setTopDividerColor(Color color) {
		mTopDividerColor = color;
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		// Not used
	}

	@Override
	public void mouseEntered(MouseEvent event) {
		// Not used
	}

	@Override
	public void mouseExited(MouseEvent event) {
		// Not used
	}

	@Override
	public void mousePressed(MouseEvent event) {
		if (event.isPopupTrigger()) {
			Column column = mOwner.overColumn(event.getX());
			if (column != null && mOwner.allowColumnContextMenu()) {
				ArrayList<Column> selection = new ArrayList<>();
				selection.add(column);
			}
		} else if (mOwner.overColumnDivider(event.getX()) == null) {
			mSortColumn = mOwner.overColumn(event.getX());
		} else {
			mOwner.mousePressed(event);
		}
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		if (mSortColumn != null) {
			if (mSortColumn == mOwner.overColumn(event.getX())) {
				if (mOwner.isUserSortable()) {
					boolean sortAscending = mSortColumn.isSortAscending();
					if (mSortColumn.getSortSequence() != -1) {
						sortAscending = !sortAscending;
					}
					mOwner.setSort(mSortColumn, sortAscending, event.isShiftDown());
				}
			}
			mSortColumn = null;
		} else {
			mOwner.mouseReleased(event);
		}
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		mOwner.mouseDragged(event);
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		Cursor cursor = Cursor.getDefaultCursor();
		int x = event.getX();
		if (mOwner.overColumnDivider(x) != null) {
			if (mOwner.allowColumnResize()) {
				cursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
			}
		} else if (mOwner.overColumn(x) != null) {
			cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
		}
		setCursor(cursor);
	}

	/** @return The owning outline. */
	public Outline getOwner() {
		return mOwner;
	}

	@Override
	public Dimension getPreferredSize() {
		List<Column> columns = mOwner.getModel().getColumns();
		boolean drawDividers = mOwner.shouldDrawColumnDividers();
		Insets insets = getInsets();
		Dimension size = new Dimension(insets.left + insets.right, 0);
		ArrayList<Column> changed = new ArrayList<>();
		for (Column col : columns) {
			if (col.isVisible()) {
				int tmp = col.getWidth();
				if (tmp == -1) {
					tmp = col.getPreferredWidth(mOwner);
					col.setWidth(tmp);
					changed.add(col);
				}
				size.width += tmp + (drawDividers ? 1 : 0);
				tmp = col.getPreferredHeaderHeight() + 1;
				if (tmp > size.height) {
					size.height = tmp;
				}
			}
		}
		if (!changed.isEmpty()) {
			mOwner.updateRowHeightsIfNeeded(changed);
			mOwner.revalidateView();
		}
		size.height += insets.top + insets.bottom;
		return size;
	}

	@Override
	protected void paintComponent(Graphics gc) {
		super.paintComponent(GraphicsUtilities.prepare(gc));
		Rectangle clip = gc.getClipBounds();
		Insets insets = getInsets();
		int height = getHeight();
		Rectangle bounds = new Rectangle(insets.left, insets.top, getWidth() - (insets.left + insets.right), height - (insets.top + insets.bottom));
		boolean drawDividers = mOwner.shouldDrawColumnDividers();
		gc.setColor(getTopDividerColor());
		gc.drawLine(clip.x, height - 1, clip.x + clip.width, height - 1);
		Color dividerColor = mOwner.getDividerColor();
		for (Column col : mOwner.getModel().getColumns()) {
			if (col.isVisible()) {
				bounds.width = col.getWidth();
				if (clip.intersects(bounds)) {
					boolean dragging = mOwner.getSourceDragColumn() == col;
					Composite savedComposite = null;
					if (dragging) {
						savedComposite = ((Graphics2D) gc).getComposite();
						((Graphics2D) gc).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
					}
					col.drawHeaderCell(mOwner, gc, bounds);
					if (dragging) {
						((Graphics2D) gc).setComposite(savedComposite);
					}
				}
				bounds.x += bounds.width;
				if (drawDividers) {
					gc.setColor(dividerColor);
					gc.drawLine(bounds.x, bounds.y, bounds.x, bounds.y + bounds.height);
					bounds.x++;
				}
			}
		}
	}

	@Override
	public void repaint(Rectangle bounds) {
		if (mOwner != null) {
			mOwner.repaintHeader(bounds);
		}
	}

	/**
	 * The real version of {@link #repaint(Rectangle)}.
	 *
	 * @param bounds The bounds to repaint.
	 */
	void repaintInternal(Rectangle bounds) {
		super.repaint(bounds);
	}

	/**
	 * @param column The column.
	 * @return The bounds of the specified header column.
	 */
	public Rectangle getColumnBounds(Column column) {
		Insets insets = getInsets();
		Rectangle bounds = new Rectangle(insets.left, insets.top, getWidth() - (insets.left + insets.right), getHeight() - (insets.top + insets.bottom));
		bounds.x = mOwner.getColumnStart(column);
		bounds.width = column.getWidth();
		return bounds;
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		Column column = mOwner.overColumn(event.getX());
		if (column != null) {
			return column.getHeaderCell().getToolTipText(event, getColumnBounds(column), null, column);
		}
		return super.getToolTipText(event);
	}

	@Override
	public void dragGestureRecognized(DragGestureEvent dge) {
		if (mSortColumn != null && mOwner.allowColumnDrag()) {
			mOwner.setSourceDragColumn(mSortColumn);
			if (DragSource.isDragImageSupported()) {
				Point pt = dge.getDragOrigin();
				dge.startDrag(null, mOwner.getColumnDragImage(mSortColumn), new Point(-(pt.x - mOwner.getColumnStart(mSortColumn)), -pt.y), mSortColumn, this);
			} else {
				dge.startDrag(null, mSortColumn, this);
			}
			mSortColumn = null;
		}
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		if (mOwner.getSourceDragColumn() != null) {
			mOwner.dragEnter(dtde);
		} else {
			dtde.rejectDrag();
		}
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		if (mOwner.getSourceDragColumn() != null) {
			mOwner.dragOver(dtde);
		} else {
			dtde.rejectDrag();
		}
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		if (mOwner.getSourceDragColumn() != null) {
			mOwner.dropActionChanged(dtde);
		} else {
			dtde.rejectDrag();
		}
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		if (mOwner.getSourceDragColumn() != null) {
			mOwner.dragExit(dte);
		}
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		mOwner.drop(dtde);
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		if (mIgnoreResizeOK || mResizeOK) {
			super.setBounds(x, y, width, height);
		}
	}

	/** @param resizeOK Whether resizing is allowed or not. */
	void setResizeOK(boolean resizeOK) {
		mResizeOK = resizeOK;
	}

	/** @param ignoreResizeOK Whether {@link #setResizeOK(boolean)} is ignored. */
	public void setIgnoreResizeOK(boolean ignoreResizeOK) {
		mIgnoreResizeOK = ignoreResizeOK;
	}

	@Override
	public void dragEnter(DragSourceDragEvent dsde) {
		// Nothing to do...
	}

	@Override
	public void dragOver(DragSourceDragEvent dsde) {
		// Nothing to do...
	}

	@Override
	public void dropActionChanged(DragSourceDragEvent dsde) {
		// Nothing to do...
	}

	@Override
	public void dragDropEnd(DragSourceDropEvent dsde) {
		mOwner.setSourceDragColumn(null);
	}

	@Override
	public void dragExit(DragSourceEvent dse) {
		// Nothing to do...
	}
}
