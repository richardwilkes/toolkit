package com.trollworks.toolkit.ui.widget.dock;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

/** All {@link Dockable}s are wrapped in a {@link DockContainer} when placed within a {@link Dock}. */
public class DockContainer extends JPanel implements DockLayoutNode {
	private DockHeader	mHeader;
	private Dockable	mDockable;

	/**
	 * Creates a new {@link DockContainer} for the specified {@link Dockable}.
	 *
	 * @param dockable The {@link Dockable} to wrap.
	 */
	public DockContainer(Dockable dockable) {
		super(new BorderLayout());
		setOpaque(true);
		setBackground(Color.WHITE);
		mDockable = dockable;
		mHeader = new DockHeader(mDockable);
		add(mHeader, BorderLayout.NORTH);
		add(mDockable.getContent(), BorderLayout.CENTER);
		setMinimumSize(new Dimension(0, 0));
	}

	@Override
	public Dockable getDockable() {
		return mDockable;
	}

	/** @return The {@link DockHeader} for this {@link DockContainer}. */
	public DockHeader getHeader() {
		return mHeader;
	}

	@Override
	public String toString() {
		return mDockable.getTitle();
	}
}
