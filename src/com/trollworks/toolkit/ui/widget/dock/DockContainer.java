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

	/** @return The {@link Dock} this {@link DockContainer} resides in. */
	public Dock getDock() {
		return (Dock) UIUtilities.getAncestorOfType(this, Dock.class);
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

	/**
	 * Attempt to close this {@link DockContainer}. This only has an affect if the contained
	 * {@link Dockable} implements the {@link DockCloseable} interface.
	 */
	public void attemptClose() {
		if (mDockable instanceof DockCloseable) {
			if (((DockCloseable) mDockable).attemptClose()) {
				close();
			}
		}
	}

	/** Closes this {@link DockContainer} and removes it from the {@link Dock}. */
	public void close() {
		Dock dock = getDock();
		if (dock != null) {
			dock.remove(this);
			dock.revalidate();
			dock.repaint();
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
