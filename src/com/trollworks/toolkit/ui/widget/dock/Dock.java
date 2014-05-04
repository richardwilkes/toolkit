package com.trollworks.toolkit.ui.widget.dock;

import com.trollworks.toolkit.ui.image.Cursors;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.IllegalComponentStateException;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

/** Provides an area where {@link Dockable} components can be displayed and rearranged. */
public class Dock extends JPanel implements MouseListener, MouseMotionListener, PropertyChangeListener {
	private static final String	PERMANENT_FOCUS_OWNER_KEY	= "permanentFocusOwner";			//$NON-NLS-1$
	private static final int	GRIP_GAP					= 1;
	private static final int	GRIP_WIDTH					= 4;
	private static final int	GRIP_HEIGHT					= 2;
	private static final int	GRIP_LENGTH					= GRIP_HEIGHT * 5 + GRIP_GAP * 4;
	public static final int		DIVIDER_SIZE				= GRIP_WIDTH + 4;
	private DockDragHandler		mDragHandler;
	private DockLayoutNode		mDragOverNode;
	private DockLocation		mDragOverLocation;

	/** Creates a new, empty {@link Dock}. */
	public Dock() {
		super(new DockLayout(), true);
		setBorder(null);
		addMouseListener(this);
		addMouseMotionListener(this);
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
	 *            {@link Dockable}.
	 */
	public void dock(Dockable dockable, Dockable target, DockLocation locationRelativeToTarget) {
		dock(dockable, (DockContainer) target.getContent().getParent(), locationRelativeToTarget);
	}

	/**
	 * Docks a {@link Dockable} within this {@link Dock}. If the {@link Dockable} already exists in
	 * this {@link Dock}, it will be moved to the new location.
	 *
	 * @param dockable The {@link Dockable} to install into this {@link Dock}.
	 * @param target The target {@link DockLayoutNode}.
	 * @param locationRelativeToTarget The location relative to the target to install the
	 *            {@link Dockable}.
	 */
	public void dock(Dockable dockable, DockLayoutNode target, DockLocation locationRelativeToTarget) {
		DockLayout layout = getLayout();
		DockContainer dc = layout.findDockContainer(dockable);
		if (dc == null) {
			dc = new DockContainer(dockable);
			layout.dock(dc, target, locationRelativeToTarget);
			addImpl(dc, null, -1);
		} else {
			layout.dock(dc, target, locationRelativeToTarget);
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
	public void mouseClicked(MouseEvent event) {
		// Unused
	}

	@Override
	public void mousePressed(MouseEvent event) {
		DockLayoutNode over = over(event.getX(), event.getY(), true);
		if (over instanceof DockLayout) {
			mDragHandler = new DividerDragHandler((DockLayout) over);
		} else if (over instanceof DockContainer) {
			mDragHandler = new DockContainerDragHandler((DockContainer) over);
		}
		if (mDragHandler != null) {
			mDragHandler.start(event);
		}
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		if (mDragHandler != null) {
			mDragHandler.drag(event);
		}
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		if (mDragHandler != null) {
			mDragHandler.finish(event);
			mDragHandler = null;
		}
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		updateCursor(event);
	}

	private void updateCursor(MouseEvent event) {
		DockLayoutNode over = over(event.getX(), event.getY(), true);
		Cursor cursor;
		if (over instanceof DockLayout) {
			cursor = ((DockLayout) over).isHorizontal() ? Cursors.HORIZONTAL_RESIZE : Cursors.VERTICAL_RESIZE;
		} else if (over instanceof DockContainer) {
			cursor = Cursors.MOVE;
		} else {
			cursor = null;
		}
		setCursor(cursor);
	}

	@Override
	public void mouseExited(MouseEvent event) {
		setCursor(null);
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

	/**
	 * Causes the drop area to be displayed or cleared.
	 *
	 * @param node The {@link DockLayoutNode} the drag is currently over. Pass in <code>null</code>
	 *            to clear the drop area display.
	 * @param location The location for the drop, should it occur.
	 */
	void setDragOver(DockLayoutNode node, DockLocation location) {
		if (mDragOverNode != null) {
			repaint(getDragOverBounds());
		}
		mDragOverNode = node;
		mDragOverLocation = location;
		if (mDragOverNode != null) {
			repaint(getDragOverBounds());
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
			buffer.append(((DockContainer) node).getDockable().getTitle());
			buffer.append(" [w:");
			buffer.append(node.getWidth());
			buffer.append(" h:");
			buffer.append(node.getHeight());
			buffer.append("]");
			buffer.append('\n');
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
}
