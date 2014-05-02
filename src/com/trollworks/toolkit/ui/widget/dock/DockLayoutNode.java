package com.trollworks.toolkit.ui.widget.dock;

import java.awt.Dimension;

/** A node within a {@link DockLayout}. */
public interface DockLayoutNode {
	/**
	 * @return The {@link Dockable} this node represents. May be <code>null</code> if this node
	 *         isn't a leaf node.
	 */
	Dockable getDockable();

	/** @return The preferred size of this node. */
	Dimension getPreferredSize();

	/** @return The node's horizontal starting coordinate. */
	int getX();

	/** @return The node's vertical starting coordinate. */
	int getY();

	/** @return The node's width. */
	int getWidth();

	/** @return The node's height. */
	int getHeight();

	/**
	 * Sets the position and size of this node, which may alter any contained sub-nodes.
	 *
	 * @param x The horizontal starting coordinate.
	 * @param y The vertical starting coordinate.
	 * @param width The width;
	 * @param height The height;
	 */
	void setBounds(int x, int y, int width, int height);

	/** Invalidate then validate the layout of this node. */
	void revalidate();
}
