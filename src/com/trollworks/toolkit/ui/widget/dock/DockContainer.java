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

import com.trollworks.toolkit.ui.UIUtilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/** All {@link Dockable}s are wrapped in a {@link DockContainer} when placed within a {@link Dock}. */
public class DockContainer extends JPanel implements DockLayoutNode {
	private DockHeader		mHeader;
	private List<Dockable>	mDockables	= new ArrayList<>();
	private int				mCurrent;

	/**
	 * Creates a new {@link DockContainer} for the specified {@link Dockable}.
	 *
	 * @param dockable The {@link Dockable} to wrap.
	 */
	public DockContainer(Dockable dockable) {
		super(new BorderLayout());
		setOpaque(true);
		setBackground(Color.WHITE);
		mDockables.add(dockable);
		mHeader = new DockHeader(this);
		add(mHeader, BorderLayout.NORTH);
		add(dockable.getContent(), BorderLayout.CENTER);
		setMinimumSize(new Dimension(0, 0));
	}

	/** @return The {@link Dock} this {@link DockContainer} resides in. */
	public Dock getDock() {
		return (Dock) UIUtilities.getAncestorOfType(this, Dock.class);
	}

	public List<Dockable> getDockables() {
		return mDockables;
	}

	/** @return The {@link DockHeader} for this {@link DockContainer}. */
	public DockHeader getHeader() {
		return mHeader;
	}

	/**
	 * Calls the owning {@link Dock}'s {@link Dock#maximize(DockContainer)} method with this
	 * {@link DockContainer} as the argument.
	 */
	public void maximize() {
		getDock().maximize(this);
	}

	/** Calls the owning {@link Dock}'s {@link Dock#restore()} method. */
	public void restore() {
		getDock().restore();
	}

	/** @return The current tab index. */
	public int getCurrent() {
		return mCurrent;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("Dock Container [c:");
		buffer.append(mCurrent);
		buffer.append(" x:");
		buffer.append(getX());
		buffer.append(" y:");
		buffer.append(getY());
		buffer.append(" w:");
		buffer.append(getWidth());
		buffer.append(" h:");
		buffer.append(getHeight());
		buffer.append("]");
		return buffer.toString();
	}

	/**
	 * Attempt to close a {@link Dockable} within this {@link DockContainer}. This only has an
	 * affect if the {@link Dockable} is contained by this {@link DockContainer} and implements the
	 * {@link DockCloseable} interface. If the last {@link Dockable} within this
	 * {@link DockContainer} is closed, then the {@link DockContainer} is also closed.
	 */
	public void attemptClose(Dockable dockable) {
		if (dockable instanceof DockCloseable) {
			if (mDockables.contains(dockable)) {
				if (((DockCloseable) dockable).attemptClose()) {
					close(dockable);
				}
			}
		}
	}

	/**
	 * Closes the specified {@link Dockable}. If the last {@link Dockable} within this
	 * {@link DockContainer} is closed, then this {@link DockContainer} is also removed from the
	 * {@link Dock}.
	 */
	public void close(Dockable dockable) {
		remove(dockable.getContent());
		mDockables.remove(dockable);
		if (mDockables.isEmpty()) {
			Dock dock = getDock();
			if (dock != null) {
				dock.remove(this);
				dock.revalidate();
				dock.repaint();
			}
		}
	}

	/** Called by the {@link Dock} to update the active highlight. */
	void updateActiveHighlight() {
		Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
		boolean hasFocus = focusOwner == this;
		if (!hasFocus && focusOwner != null) {
			hasFocus = UIUtilities.getAncestorOfType(focusOwner, DockContainer.class) == this;
		}
		mHeader.setActive(hasFocus);
	}
}
