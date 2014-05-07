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

package com.trollworks.toolkit.ui.widget.dock;

import com.trollworks.toolkit.io.Log;
import com.trollworks.toolkit.ui.MouseCapture;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.image.Cursors;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.IllegalComponentStateException;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JPanel;

/** Provides an area where {@link Dockable} components can be displayed and rearranged. */
public class Dock extends JPanel implements MouseListener, MouseMotionListener, PropertyChangeListener, DropTargetListener {
	private static final String	PERMANENT_FOCUS_OWNER_KEY	= "permanentFocusOwner";			//$NON-NLS-1$
	private static final int	GRIP_GAP					= 1;
	private static final int	GRIP_WIDTH					= 4;
	private static final int	GRIP_HEIGHT					= 2;
	private static final int	GRIP_LENGTH					= GRIP_HEIGHT * 5 + GRIP_GAP * 4;
	public static final int		DIVIDER_SIZE				= GRIP_WIDTH + 4;
	private static final int	DRAG_THRESHOLD				= 5;
	private static final long	DRAG_DELAY					= 250;
	private long				mDividerDragStartedAt;
	private int					mDividerDragStartX;
	private int					mDividerDragStartY;
	private DockLayout			mDividerDragLayout;
	private int					mDividerDragInitialEventPosition;
	private int					mDividerDragInitialDividerPosition;
	private boolean				mDividerDragIsValid;
	private Dockable			mDragDockable;
	private DockLayoutNode		mDragOverNode;
	private DockLocation		mDragOverLocation;
	private DockContainer		mMaximizedContainer;

	/** Creates a new, empty {@link Dock}. */
	public Dock() {
		super(new DockLayout(), true);
		setBorder(null);
		addMouseListener(this);
		addMouseMotionListener(this);
		setFocusCycleRoot(true);
		setDropTarget(new DropTarget(this, DnDConstants.ACTION_MOVE, this));
	}

	/**
	 * Docks a {@link Dockable} within this {@link Dock}. If the {@link Dockable} already exists in
	 * this {@link Dock}, it will be moved to the new location.
	 *
	 * @param dockable The {@link Dockable} to install into this {@link Dock}.
	 * @param location The location within the top level to install the {@link Dockable}.
	 */
	public void dock(Dockable dockable, DockLocation location) {
		dock(dockable, getLayout(), location);
	}

	/**
	 * Docks a {@link Dockable} within this {@link Dock}. If the {@link Dockable} already exists in
	 * this {@link Dock}, it will be moved to the new location.
	 *
	 * @param dockable The {@link Dockable} to install into this {@link Dock}.
	 * @param target The target {@link Dockable}.
	 * @param locationRelativeToTarget The location relative to the target to install the
	 *            {@link Dockable}. You may pass in <code>null</code> to have it stack with the
	 *            target.
	 */
	public void dock(Dockable dockable, Dockable target, DockLocation locationRelativeToTarget) {
		DockContainer dc = getDockContainer(target);
		if (dc != null) {
			dock(dockable, dc, locationRelativeToTarget);
		}
	}

	/**
	 * Docks a {@link Dockable} within this {@link Dock}. If the {@link Dockable} already exists in
	 * this {@link Dock}, it will be moved to the new location.
	 *
	 * @param dockable The {@link Dockable} to install into this {@link Dock}.
	 * @param target The target {@link DockLayoutNode}.
	 * @param locationRelativeToTarget The location relative to the target to install the
	 *            {@link Dockable}. If the target is a {@link DockContainer}, you may pass in
	 *            <code>null</code> to have it stack with the target.
	 */
	public void dock(Dockable dockable, DockLayoutNode target, DockLocation locationRelativeToTarget) {
		DockLayout layout = getLayout();
		if (locationRelativeToTarget == null) {
			if (target instanceof DockContainer) {
				DockContainer dc = getDockContainer(dockable);
				if (dc != target) {
					if (dc != null) {
						dc.close(dockable);
					}
					((DockContainer) target).stack(dockable);
				}
				return;
			}
			// Arbitrary choice...
			locationRelativeToTarget = DockLocation.EAST;
		}
		if (layout.contains(target)) {
			DockContainer dc = getDockContainer(dockable);
			if (dc == null) {
				dc = new DockContainer(dockable);
				layout.dock(dc, target, locationRelativeToTarget);
				addImpl(dc, null, -1);
			} else {
				layout.dock(dc, target, locationRelativeToTarget);
			}
		}
	}

	@Override
	protected void paintComponent(Graphics gc) {
		Rectangle bounds = gc.getClipBounds();
		if (bounds == null) {
			bounds = new Rectangle(0, 0, getWidth(), getHeight());
		}
		gc.setColor(getBackground());
		gc.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
		drawDividers(gc, getLayout(), bounds);
	}

	@Override
	protected void paintChildren(Graphics gc) {
		super.paintChildren(gc);
		if (mDragOverNode != null) {
			Rectangle bounds = getDragOverBounds();
			gc.setColor(DockColors.DROP_AREA);
			gc.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			gc.setColor(DockColors.DROP_AREA_INNER_BORDER);
			gc.drawRect(bounds.x + 1, bounds.y + 1, bounds.width - 3, bounds.height - 3);
			gc.setColor(DockColors.DROP_AREA_OUTER_BORDER);
			gc.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
		}
	}

	private void drawDividers(Graphics gc, DockLayout layout, Rectangle clip) {
		if (clip.intersects(layout.getX() - 1, layout.getY() - 1, layout.getWidth() + 2, layout.getHeight() + 2)) {
			DockLayoutNode[] children = layout.getChildren();
			if (layout.isFull()) {
				if (layout.isHorizontal()) {
					drawHorizontalGripper(gc, children[1]);
				} else {
					drawVerticalGripper(gc, children[1]);
				}
			}
			drawDockLayoutNode(gc, children[0], clip);
			drawDockLayoutNode(gc, children[1], clip);
		}
	}

	private static void drawHorizontalGripper(Graphics gc, DockLayoutNode secondary) {
		int x = secondary.getX() - DIVIDER_SIZE + (DIVIDER_SIZE - GRIP_WIDTH) / 2;
		int y = secondary.getY() + (secondary.getHeight() - GRIP_LENGTH) / 2;
		int top = GRIP_HEIGHT / 2;
		int bottom = GRIP_HEIGHT - top;
		for (int yy = y; yy < y + GRIP_LENGTH; yy += GRIP_HEIGHT + GRIP_GAP) {
			gc.setColor(DockColors.HIGHLIGHT);
			gc.fillRect(x, yy, GRIP_WIDTH, top);
			gc.setColor(DockColors.SHADOW);
			gc.fillRect(x, yy + top, GRIP_WIDTH, bottom);
		}
	}

	private static void drawVerticalGripper(Graphics gc, DockLayoutNode secondary) {
		int x = secondary.getX() + (secondary.getWidth() - GRIP_LENGTH) / 2;
		int y = secondary.getY() - DIVIDER_SIZE + (DIVIDER_SIZE - GRIP_WIDTH) / 2;
		int top = GRIP_HEIGHT / 2;
		int bottom = GRIP_HEIGHT - top;
		for (int xx = x; xx < x + GRIP_LENGTH; xx += GRIP_HEIGHT + GRIP_GAP) {
			gc.setColor(DockColors.HIGHLIGHT);
			gc.fillRect(xx, y, top, GRIP_WIDTH);
			gc.setColor(DockColors.SHADOW);
			gc.fillRect(xx + top, y, bottom, GRIP_WIDTH);
		}
	}

	private void drawDockLayoutNode(Graphics gc, DockLayoutNode node, Rectangle clip) {
		if (node instanceof DockLayout) {
			drawDividers(gc, (DockLayout) node, clip);
		} else if (node != null) {
			int layoutWidth = node.getWidth();
			if (layoutWidth > 0) {
				int layoutHeight = node.getHeight();
				if (layoutHeight > 0) {
					gc.setColor(DockColors.SHADOW);
					gc.drawRect(node.getX() - 1, node.getY() - 1, node.getWidth() + 1, node.getHeight() + 1);
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent event) {
		updateCursor(event);
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		updateCursor(event);
	}

	@Override
	public void mousePressed(MouseEvent event) {
		DockLayoutNode over = over(event.getX(), event.getY(), true);
		if (over instanceof DockLayout) {
			mDividerDragLayout = (DockLayout) over;
			mDividerDragStartedAt = event.getWhen();
			mDividerDragStartX = event.getX();
			mDividerDragStartY = event.getY();
			mDividerDragInitialEventPosition = mDividerDragLayout.isHorizontal() ? event.getX() : event.getY();
			mDividerDragInitialDividerPosition = mDividerDragLayout.getDividerPosition();
			mDividerDragIsValid = false;
			MouseCapture.start(this);
		}
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		dragDivider(event);
	}

	private void dragDivider(MouseEvent event) {
		if (mDividerDragLayout != null) {
			if (!mDividerDragIsValid) {
				mDividerDragIsValid = Math.abs(mDividerDragStartX - event.getX()) > DRAG_THRESHOLD || Math.abs(mDividerDragStartY - event.getY()) > DRAG_THRESHOLD || event.getWhen() - mDividerDragStartedAt > DRAG_DELAY;
			}
			if (mDividerDragIsValid) {
				int pos = mDividerDragInitialDividerPosition - (mDividerDragInitialEventPosition - (mDividerDragLayout.isHorizontal() ? event.getX() : event.getY()));
				mDividerDragLayout.setDividerPosition(pos < 0 ? 0 : pos);
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		if (mDividerDragLayout != null) {
			if (mDividerDragIsValid) {
				dragDivider(event);
			}
			mDividerDragLayout = null;
			MouseCapture.stop(this);
		}
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		// Unused
	}

	@Override
	public void mouseExited(MouseEvent event) {
		setCursor(null);
	}

	private void updateCursor(MouseEvent event) {
		DockLayoutNode over = over(event.getX(), event.getY(), true);
		setCursor(over instanceof DockLayout ? ((DockLayout) over).isHorizontal() ? Cursors.HORIZONTAL_RESIZE : Cursors.VERTICAL_RESIZE : null);
	}

	private static boolean containedBy(DockLayoutNode node, int x, int y) {
		if (node != null) {
			int edgeX = node.getX();
			if (x >= edgeX && x < edgeX + node.getWidth()) {
				int edgeY = node.getY();
				return y >= edgeY && y < edgeY + node.getHeight();
			}
		}
		return false;
	}

	/**
	 * @param x The horizontal coordinate to check.
	 * @param y The vertical coordinate to check.
	 * @param headerOnly Pass in <code>true</code> if only the header of a {@link DockContainer}
	 *            counts for purposes of hit detection, or <code>false</code> if the whole thing
	 *            counts.
	 * @return A {@link DockLayout} if we're over the divider, a {@link DockContainer} if we're over
	 *         the dock header or component (depending on the value of <code>headerOnly</code>), or
	 *         <code>null</code> if we're not over anything we care about.
	 */
	DockLayoutNode over(int x, int y, boolean headerOnly) {
		return over(getLayout(), x, y, headerOnly);
	}

	private DockLayoutNode over(DockLayoutNode node, int x, int y, boolean headerOnly) {
		if (containedBy(node, x, y)) {
			if (node instanceof DockLayout) {
				DockLayout layout = (DockLayout) node;
				for (DockLayoutNode child : layout.getChildren()) {
					if (containedBy(child, x, y)) {
						return over(child, x, y, headerOnly);
					}
				}
				if (layout.isFull()) {
					return node;
				}
			} else if (node instanceof DockContainer) {
				DockContainer dc = (DockContainer) node;
				if (headerOnly) {
					Rectangle bounds = dc.getHeader().getBounds();
					bounds.x += dc.getX();
					bounds.y += dc.getY();
					if (bounds.contains(x, y)) {
						return dc;
					}
				} else {
					return dc;
				}
			}
		}
		return null;
	}

	@Override
	public void setLayout(LayoutManager mgr) {
		if (mgr instanceof DockLayout) {
			super.setLayout(mgr);
		} else {
			throw new IllegalArgumentException("Must use a DockLayout."); //$NON-NLS-1$
		}
	}

	@Override
	public DockLayout getLayout() {
		return (DockLayout) super.getLayout();
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		dump(buffer, 0, getLayout());
		buffer.setLength(buffer.length() - 1);
		return buffer.toString();
	}

	@SuppressWarnings("nls")
	private static void dump(StringBuilder buffer, int depth, DockLayoutNode node) {
		if (node instanceof DockLayout) {
			DockLayout layout = (DockLayout) node;
			pad(buffer, depth);
			buffer.append(layout);
			buffer.append('\n');
			depth++;
			for (DockLayoutNode child : layout.getChildren()) {
				if (child != null) {
					dump(buffer, depth, child);
				}
			}
		} else if (node instanceof DockContainer) {
			pad(buffer, depth);
			buffer.append(node);
			buffer.append('\n');
			List<Dockable> dockables = ((DockContainer) node).getDockables();
			int size = dockables.size();
			for (int i = 0; i < size; i++) {
				pad(buffer, depth);
				buffer.append(".[");
				buffer.append(i);
				buffer.append("] ");
				buffer.append(dockables.get(i).getTitle());
				buffer.append('\n');
			}
		}
	}

	@SuppressWarnings("nls")
	private static void pad(StringBuilder buffer, int depth) {
		for (int i = 0; i < depth; i++) {
			buffer.append("...");
		}
	}

	/**
	 * Use one of {@link #dock(Dockable, DockLocation)},
	 * {@link #dock(Dockable, Dockable, DockLocation)}, or
	 * {@link #dock(Dockable, DockLayoutNode, DockLocation)} instead.
	 */
	@Override
	public final Component add(Component comp) {
		throw createIllegalComponentStateException();
	}

	/**
	 * Use one of {@link #dock(Dockable, DockLocation)},
	 * {@link #dock(Dockable, Dockable, DockLocation)}, or
	 * {@link #dock(Dockable, DockLayoutNode, DockLocation)} instead.
	 */
	@Override
	public final Component add(Component comp, int index) {
		throw createIllegalComponentStateException();
	}

	/**
	 * Use one of {@link #dock(Dockable, DockLocation)},
	 * {@link #dock(Dockable, Dockable, DockLocation)}, or
	 * {@link #dock(Dockable, DockLayoutNode, DockLocation)} instead.
	 */
	@Override
	public final void add(Component comp, Object constraints) {
		throw createIllegalComponentStateException();
	}

	/**
	 * Use one of {@link #dock(Dockable, DockLocation)},
	 * {@link #dock(Dockable, Dockable, DockLocation)}, or
	 * {@link #dock(Dockable, DockLayoutNode, DockLocation)} instead.
	 */
	@Override
	public final void add(Component comp, Object constraints, int index) {
		throw createIllegalComponentStateException();
	}

	/**
	 * Use one of {@link #dock(Dockable, DockLocation)},
	 * {@link #dock(Dockable, Dockable, DockLocation)}, or
	 * {@link #dock(Dockable, DockLayoutNode, DockLocation)} instead.
	 */
	@Override
	public final Component add(String name, Component comp) {
		throw createIllegalComponentStateException();
	}

	private static final IllegalComponentStateException createIllegalComponentStateException() {
		return new IllegalComponentStateException("Use one of the dock() methods instead"); //$NON-NLS-1$
	}

	@Override
	public void addNotify() {
		super.addNotify();
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(PERMANENT_FOCUS_OWNER_KEY, this);
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener(PERMANENT_FOCUS_OWNER_KEY, this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		getLayout().forEachDockContainer((dc) -> dc.updateActiveHighlight());
	}

	/** @return The current maximized {@link DockContainer}, or <code>null</code>. */
	public DockContainer getMaximizedContainer() {
		return mMaximizedContainer;
	}

	/**
	 * Causes the {@link DockContainer} to fill the entire {@link Dock} area.
	 *
	 * @param dc The {@link DockContainer} to maximize.
	 */
	public void maximize(DockContainer dc) {
		if (mMaximizedContainer != null) {
			mMaximizedContainer.getHeader().adjustToRestoredState();
		}
		mMaximizedContainer = dc;
		mMaximizedContainer.getHeader().adjustToMaximizedState();
		getLayout().forEachDockContainer((target) -> target.setVisible(target == mMaximizedContainer));
		revalidate();
		mMaximizedContainer.transferFocus();
		repaint();
	}

	/** Restores the current maximized {@link DockContainer} to its normal state. */
	public void restore() {
		if (mMaximizedContainer != null) {
			mMaximizedContainer.getHeader().adjustToRestoredState();
			mMaximizedContainer = null;
			getLayout().forEachDockContainer((dc) -> dc.setVisible(true));
			revalidate();
			repaint();
		}
	}

	/**
	 * @param dockable The {@link Dockable} to determine the {@link DockContainer} for.
	 * @return The {@link DockContainer} that contains the {@link Dockable}, or <code>null</code> if
	 *         it is not present or the {@link DockContainer} is not a child of this {@link Dock}.
	 */
	public DockContainer getDockContainer(Dockable dockable) {
		DockContainer dc = (DockContainer) UIUtilities.getAncestorOfType(dockable.getContent(), DockContainer.class);
		if (dc != null) {
			if (dc.getDock() != this) {
				dc = null;
			}
		}
		return dc;
	}

	private Dockable getDockableInDrag(DropTargetDragEvent dtde) {
		if (dtde.getDropAction() == DnDConstants.ACTION_MOVE) {
			try {
				if (dtde.isDataFlavorSupported(DockableTransferable.DATA_FLAVOR)) {
					Dockable dockable = (Dockable) dtde.getTransferable().getTransferData(DockableTransferable.DATA_FLAVOR);
					if (getDockContainer(dockable) != null) {
						return dockable;
					}
				}
			} catch (Exception exception) {
				Log.error(exception);
			}
		}
		return null;
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		mDragDockable = getDockableInDrag(dtde);
		if (mDragDockable != null) {
			mDragOverNode = null;
			mDragOverLocation = null;
			updateForDragOver(dtde.getLocation());
			dtde.acceptDrag(DnDConstants.ACTION_MOVE);
		} else {
			dtde.rejectDrag();
		}
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		if (mDragDockable != null) {
			updateForDragOver(dtde.getLocation());
			dtde.acceptDrag(DnDConstants.ACTION_MOVE);
		} else {
			dtde.rejectDrag();
		}
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		if (mDragDockable != null) {
			clearDragState();
		}
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		mDragDockable = getDockableInDrag(dtde);
		if (mDragDockable != null) {
			updateForDragOver(dtde.getLocation());
			dtde.acceptDrag(DnDConstants.ACTION_MOVE);
		} else {
			clearDragState();
			dtde.rejectDrag();
		}
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		if (mDragDockable != null) {
			if (mDragOverNode != null) {
				getLayout().dock(getDockContainer(mDragDockable), mDragOverNode, mDragOverLocation);
				revalidate();
			}
			dtde.acceptDrop(DnDConstants.ACTION_MOVE);
			dtde.dropComplete(true);
		} else {
			dtde.dropComplete(false);
		}
		clearDragState();
	}

	private void clearDragState() {
		if (mDragOverNode != null) {
			repaint(getDragOverBounds());
		}
		mDragDockable = null;
		mDragOverNode = null;
		mDragOverLocation = null;
	}

	private void updateForDragOver(Point where) {
		int ex = where.x;
		int ey = where.y;
		DockLocation location = null;
		DockLayoutNode over = over(ex, ey, false);
		DockContainer container = getDockContainer(mDragDockable);
		if (over == container) {
			over = getLayout().findLayout(container);
		}
		if (over != null) {
			int x = over.getX();
			int y = over.getY();
			int width = over.getWidth();
			int height = over.getHeight();
			ex -= x;
			ey -= y;
			if (ex < width / 2) {
				location = DockLocation.WEST;
			} else {
				location = DockLocation.EAST;
				ex = width - ex;
			}
			if (ey < height / 2) {
				if (ex > ey) {
					location = DockLocation.NORTH;
				}
			} else if (ex > height - ey) {
				location = DockLocation.SOUTH;
			}
		}
		if (over != mDragOverNode || location != mDragOverLocation) {
			if (mDragOverNode != null) {
				repaint(getDragOverBounds());
			}
			mDragOverNode = over;
			mDragOverLocation = location;
			if (mDragOverNode != null) {
				repaint(getDragOverBounds());
			}
		}
	}

	private Rectangle getDragOverBounds() {
		Rectangle bounds = new Rectangle(mDragOverNode.getX(), mDragOverNode.getY(), mDragOverNode.getWidth(), mDragOverNode.getHeight());
		switch (mDragOverLocation) {
			case NORTH:
				bounds.height = Math.max(bounds.height / 2, 1);
				break;
			case SOUTH:
				int halfHeight = Math.max(bounds.height / 2, 1);
				bounds.y += bounds.height - halfHeight;
				bounds.height = halfHeight;
				break;
			case EAST:
				int halfWidth = Math.max(bounds.width / 2, 1);
				bounds.x += bounds.width - halfWidth;
				bounds.width = halfWidth;
				break;
			case WEST:
			default:
				bounds.width = Math.max(bounds.width / 2, 1);
				break;
		}
		return bounds;
	}
}
