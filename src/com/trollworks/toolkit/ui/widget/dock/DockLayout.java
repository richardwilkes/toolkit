package com.trollworks.toolkit.ui.widget.dock;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

/** Provides all {@link Dock} layout management. */
public class DockLayout implements DockLayoutNode, LayoutManager {
	private DockLayout			mParent;
	private DockLayoutNode[]	mChildren			= new DockLayoutNode[2];
	private int					mX;
	private int					mY;
	private int					mWidth;
	private int					mHeight;
	private int					mDividerPosition	= -1;
	private boolean				mHorizontal;

	@Override
	public Dockable getDockable() {
		return null;
	}

	/** @param processor A processor to execute for each {@link DockContainer}. */
	public void forEachDockContainer(DockContainerProcessor processor) {
		for (DockLayoutNode child : mChildren) {
			if (child instanceof DockContainer) {
				processor.processDockContainer((DockContainer) child);
			} else if (child instanceof DockLayout) {
				((DockLayout) child).forEachDockContainer(processor);
			}
		}
	}

	/** @return The root {@link DockLayout}, which may be this object. */
	public DockLayout getRootLayout() {
		DockLayout root = this;
		while (root.mParent != null) {
			root = root.mParent;
		}
		return root;
	}

	/** @return The {@link Dock} this {@link DockLayout} is associated with. */
	public Dock getDock() {
		return getRootLayout().getDockInternal();
	}

	private Dock getDockInternal() {
		for (DockLayoutNode child : mChildren) {
			if (child instanceof DockContainer) {
				return (Dock) ((DockContainer) child).getParent();
			} else if (child instanceof DockLayout) {
				Dock dock = ((DockLayout) child).getDockInternal();
				if (dock != null) {
					return dock;
				}
			}
		}
		return null;
	}

	/**
	 * @param dockable The {@link Dockable} to search for.
	 * @return The {@link DockLayout} that contains the {@link Dockable}, or <code>null</code> if it
	 *         is not present. Note that this method will always start at the root and work its way
	 *         down, even if called on a sub-node.
	 */
	public DockLayout findLayout(Dockable dockable) {
		return getRootLayout().findLayoutInternal(dockable);
	}

	private DockLayout findLayoutInternal(Dockable dockable) {
		for (DockLayoutNode child : mChildren) {
			if (child instanceof DockContainer) {
				if (child.getDockable() == dockable) {
					return this;
				}
			} else if (child instanceof DockLayout) {
				DockLayout layout = ((DockLayout) child).findLayoutInternal(dockable);
				if (layout != null) {
					return layout;
				}
			}
		}
		return null;
	}

	/**
	 * @param dockable The {@link Dockable} to search for.
	 * @return The {@link DockContainer} that contains the {@link Dockable}, or <code>null</code> if
	 *         it is not present. Note that this method will always start at the root and work its
	 *         way down, even if called on a sub-node.
	 */
	public DockContainer findDockContainer(Dockable dockable) {
		return getRootLayout().findDockContainerInternal(dockable);
	}

	private DockContainer findDockContainerInternal(Dockable dockable) {
		for (DockLayoutNode child : mChildren) {
			if (child instanceof DockContainer) {
				if (child.getDockable() == dockable) {
					return (DockContainer) child;
				}
			} else if (child instanceof DockLayout) {
				DockContainer dc = ((DockLayout) child).findDockContainerInternal(dockable);
				if (dc != null) {
					return dc;
				}
			}
		}
		return null;
	}

	/**
	 * Docks a {@link DockContainer} within this {@link DockLayout}. If the {@link DockContainer}
	 * already exists in this {@link DockLayout}, it will be moved to the new location.
	 *
	 * @param dc The {@link DockContainer} to install into this {@link DockLayout}.
	 * @param target The target {@link DockLayoutNode}.
	 * @param locationRelativeToTarget The location relative to the target to install the
	 *            {@link DockContainer}.
	 */
	public void dock(DockContainer dc, DockLayoutNode target, DockLocation locationRelativeToTarget) {
		// Does the container already exist in our hierarchy?
		DockLayout existingLayout = findLayout(dc.getDockable());
		if (existingLayout != null) {
			// Yes. Is it the same layout?
			DockLayout targetLayout;
			if (target instanceof DockLayout) {
				targetLayout = (DockLayout) target;
			} else if (target instanceof DockContainer) {
				targetLayout = findLayout(((DockContainer) target).getDockable());
			} else {
				targetLayout = null;
			}
			if (targetLayout == existingLayout) {
				// Yes. Reposition the target within this layout.
				int[] order = locationRelativeToTarget.getOrder();
				if (targetLayout.mChildren[order[0]] != dc) {
					targetLayout.mChildren[order[1]] = targetLayout.mChildren[order[0]];
					targetLayout.mChildren[order[0]] = dc;
				}
				targetLayout.mHorizontal = locationRelativeToTarget.isHorizontal();
				return;
			}
			// Not in the same layout. Remove the container from the hierarchy so we can re-add it.
			existingLayout.remove(dc);
		}

		if (target instanceof DockLayout) {
			((DockLayout) target).dock(dc, locationRelativeToTarget);
		} else if (target instanceof DockContainer) {
			DockContainer tdc = (DockContainer) target;
			DockLayout layout = findLayout(tdc.getDockable());
			layout.dockWithContainer(dc, tdc, locationRelativeToTarget);
		}
	}

	private void dockWithContainer(DockContainer dc, DockLayoutNode target, DockLocation locationRelativeToTarget) {
		boolean horizontal = locationRelativeToTarget.isHorizontal();
		int[] order = locationRelativeToTarget.getOrder();
		if (mChildren[order[0]] != null) {
			if (mChildren[order[1]] == null) {
				mChildren[order[1]] = mChildren[order[0]];
				mChildren[order[0]] = dc;
				mHorizontal = horizontal;
			} else {
				DockLayout layout = new DockLayout();
				layout.mParent = this;
				layout.mChildren[order[0]] = dc;
				layout.mHorizontal = horizontal;
				layout.mDividerPosition = mDividerPosition;
				int which = target == mChildren[order[0]] ? 0 : 1;
				layout.mChildren[order[1]] = mChildren[order[which]];
				mChildren[order[which]] = layout;
			}
		} else {
			mChildren[order[0]] = dc;
			mHorizontal = horizontal;
		}
	}

	private void dock(DockContainer dc, DockLocation locationRelativeToTarget) {
		int[] order = locationRelativeToTarget.getOrder();
		if (mChildren[order[0]] != null) {
			if (mChildren[order[1]] == null) {
				mChildren[order[1]] = mChildren[order[0]];
			} else {
				mChildren[order[1]] = pushDown();
				mDividerPosition = -1;
			}
		}
		mChildren[order[0]] = dc;
		mHorizontal = locationRelativeToTarget.isHorizontal();
	}

	private DockLayout pushDown() {
		DockLayout layout = new DockLayout();
		layout.mParent = this;
		for (int i = 0; i < mChildren.length; i++) {
			if (mChildren[i] instanceof DockLayout) {
				((DockLayout) mChildren[i]).mParent = layout;
			}
			layout.mChildren[i] = mChildren[i];
		}
		layout.mHorizontal = mHorizontal;
		layout.mDividerPosition = mDividerPosition;
		return layout;
	}

	/**
	 * @param node The node to remove.
	 * @return <code>true</code> if the node was found and removed.
	 */
	public boolean remove(DockLayoutNode node) {
		if (node == mChildren[0]) {
			mChildren[0] = null;
			pullUp(mChildren[1]);
			return true;
		} else if (node == mChildren[1]) {
			mChildren[1] = null;
			pullUp(mChildren[0]);
			return true;
		}
		for (DockLayoutNode child : mChildren) {
			if (child instanceof DockLayout) {
				if (((DockLayout) child).remove(node)) {
					return true;
				}
			}
		}
		return false;
	}

	private void pullUp(DockLayoutNode node) {
		if (mParent != null) {
			if (mParent.mChildren[0] == this) {
				mParent.mChildren[0] = node;
			} else if (mParent.mChildren[1] == this) {
				mParent.mChildren[1] = node;
			}
			if (node instanceof DockLayout) {
				((DockLayout) node).mParent = mParent;
			} else if (node == null) {
				if (mParent.mChildren[0] == null && mParent.mChildren[1] == null) {
					mParent.pullUp(null);
				}
			}
		}
	}

	/** @return The parent {@link DockLayout}. */
	public DockLayout getParent() {
		return mParent;
	}

	/**
	 * @return The immediate children of this {@link DockLayout}. Note that the array may contain
	 *         <code>null</code> values.
	 */
	public DockLayoutNode[] getChildren() {
		return mChildren;
	}

	/** @return <code>true</code> if this {@link DockLayout} lays its children out horizontally. */
	public boolean isHorizontal() {
		return mHorizontal;
	}

	/** @return <code>true</code> if this {@link DockLayout} lays its children out vertically. */
	public boolean isVertical() {
		return !mHorizontal;
	}

	@Override
	public Dimension getPreferredSize() {
		int width = 0;
		int height = 0;
		if (mChildren[0] != null) {
			Dimension size = mChildren[0].getPreferredSize();
			width = size.width;
			height = size.height;
		}
		if (mChildren[1] != null) {
			Dimension size = mChildren[1].getPreferredSize();
			if (width < size.width) {
				width = size.width;
			}
			if (height < size.height) {
				height = size.height;
			}
			if (mHorizontal) {
				width *= 2;
				width += Dock.DIVIDER_SIZE;
			} else {
				height *= 2;
				height += Dock.DIVIDER_SIZE;
			}
		}
		return new Dimension(width, height);
	}

	@Override
	public int getX() {
		return mX;
	}

	@Override
	public int getY() {
		return mY;
	}

	@Override
	public int getWidth() {
		return mWidth;
	}

	@Override
	public int getHeight() {
		return mHeight;
	}

	/** @return <code>true</code> if this {@link DockLayout} has no children. */
	public boolean isEmpty() {
		return mChildren[0] == null && mChildren[1] == null;
	}

	/** @return <code>true</code> if both child nodes of this {@link DockLayout} are occupied. */
	public boolean isFull() {
		return mChildren[0] != null && mChildren[1] != null;
	}

	/**
	 * @return The maximum value the divider can be set to. Will always return 0 if
	 *         {@link #isFull()} returns <code>false</code>.
	 */
	public int getDividerMaximum() {
		if (isFull()) {
			return Math.max((mHorizontal ? mWidth : mHeight) - Dock.DIVIDER_SIZE, 0);
		}
		return 0;
	}

	/** @return The current divider position. */
	public int getDividerPosition() {
		if (isFull()) {
			if (mDividerPosition == -1) {
				if (mHorizontal) {
					return mChildren[0].getWidth();
				}
				return mChildren[0].getHeight();
			}
			return mDividerPosition;
		}
		return 0;
	}

	/** @return <code>true</code> if the divider is currently set and not in its default mode. */
	public boolean isDividerPositionSet() {
		return mDividerPosition != -1;
	}

	/**
	 * @param position The new divider position to set. Use a value less than 0 to reset the divider
	 *            to its default mode, which splits the available space evenly between the children.
	 */
	public void setDividerPosition(int position) {
		int old = mDividerPosition;
		int max = getDividerMaximum();
		mDividerPosition = position < 0 ? -1 : Math.min(position, max);
		if (mDividerPosition != old && isFull()) {
			setBounds(mX, mY, mWidth, mHeight);
			revalidate();
			getDock().repaint(mX - 1, mY - 1, mWidth + 2, mHeight + 2);
		}
	}

	@Override
	public void revalidate() {
		for (DockLayoutNode child : mChildren) {
			if (child != null) {
				child.revalidate();
			}
		}
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		mX = x;
		mY = y;
		mWidth = width;
		mHeight = height;
		if (isFull()) {
			int available = Math.max((mHorizontal ? width : height) - Dock.DIVIDER_SIZE, 0);
			int primary;
			if (mDividerPosition == -1) {
				primary = available / 2;
			} else {
				if (mDividerPosition > available) {
					mDividerPosition = available;
				}
				primary = mDividerPosition;
			}
			if (mHorizontal) {
				mChildren[0].setBounds(x, y, primary, height);
				mChildren[1].setBounds(x + primary + Dock.DIVIDER_SIZE, y, available - primary, height);
			} else {
				mChildren[0].setBounds(x, y, width, primary);
				mChildren[1].setBounds(x, y + primary + Dock.DIVIDER_SIZE, width, available - primary);
			}
		} else {
			DockLayoutNode node = mChildren[0] != null ? mChildren[0] : mChildren[1];
			if (node != null) {
				node.setBounds(x, y, width, height);
			}
		}
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
		// Unused
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		if (comp instanceof DockLayoutNode) {
			remove((DockLayoutNode) comp);
		}
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return getPreferredSize();
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension(Dock.DIVIDER_SIZE, Dock.DIVIDER_SIZE);
	}

	@Override
	public void layoutContainer(Container parent) {
		setBounds(0, 0, parent.getWidth(), parent.getHeight());
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append('[');
		buffer.append(Integer.toHexString(hashCode()));
		buffer.append(' ');
		buffer.append(mHorizontal ? 'H' : 'V');
		buffer.append(" -");
		if (mDividerPosition != -1) {
			buffer.append(" d:");
			buffer.append(mDividerPosition);
		}
		buffer.append(" x:");
		buffer.append(mX);
		buffer.append(" y:");
		buffer.append(mY);
		buffer.append(" w:");
		buffer.append(mWidth);
		buffer.append(" h:");
		buffer.append(mHeight);
		if (mParent != null) {
			buffer.append(" p:");
			buffer.append(Integer.toHexString(mParent.hashCode()));
		}
		buffer.append(']');
		return buffer.toString();
	}
}
