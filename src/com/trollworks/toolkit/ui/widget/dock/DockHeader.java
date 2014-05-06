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

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.border.SelectiveLineBorder;
import com.trollworks.toolkit.ui.image.ToolkitImage;
import com.trollworks.toolkit.ui.layout.PrecisionLayout;
import com.trollworks.toolkit.ui.layout.PrecisionLayoutData;
import com.trollworks.toolkit.ui.widget.IconButton;
import com.trollworks.toolkit.utility.Localization;

import java.awt.LayoutManager;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/** The header for a {@link DockContainer}. */
public class DockHeader extends JPanel implements ContainerListener {
	@Localize("Maximize")
	private static String	MAXIMIZE_TOOLTIP;
	@Localize("Restore")
	private static String	RESTORE_TOOLTIP;
	private IconButton		mMaximizeRestoreButton;

	static {
		Localization.initialize();
	}

	/**
	 * Creates a new {@link DockHeader} for the specified {@link DockContainer}.
	 *
	 * @param dc The {@link DockContainer} to work with.
	 */
	public DockHeader(DockContainer dc) {
		super(new PrecisionLayout().setMargins(0).setMiddleVerticalAlignment());
		setOpaque(true);
		setBackground(DockColors.BACKGROUND);
		setBorder(new CompoundBorder(new SelectiveLineBorder(DockColors.SHADOW, 0, 0, 1, 0), new EmptyBorder(2, 4, 2, 4)));
		addContainerListener(this);
		for (Dockable dockable : dc.getDockables()) {
			add(new DockTab(dockable), new PrecisionLayoutData().setGrabHorizontalSpace(true));
		}
		mMaximizeRestoreButton = new IconButton(ToolkitImage.getDockMaximize(), MAXIMIZE_TOOLTIP, this::maximize);
		add(mMaximizeRestoreButton, new PrecisionLayoutData().setEndHorizontalAlignment());
	}

	private DockContainer getDockContainer() {
		return (DockContainer) UIUtilities.getAncestorOfType(this, DockContainer.class);
	}

	private void maximize() {
		getDockContainer().maximize();
	}

	private void restore() {
		getDockContainer().restore();
	}

	/** Called when the owning {@link DockContainer} is set to the maximized state. */
	void adjustToMaximizedState() {
		mMaximizeRestoreButton.setClickFunction(this::restore);
		mMaximizeRestoreButton.setIcon(ToolkitImage.getDockRestore());
		mMaximizeRestoreButton.setToolTipText(RESTORE_TOOLTIP);
	}

	/** Called when the owning {@link DockContainer} is restored from the maximized state. */
	void adjustToRestoredState() {
		mMaximizeRestoreButton.setClickFunction(this::maximize);
		mMaximizeRestoreButton.setIcon(ToolkitImage.getDockMaximize());
		mMaximizeRestoreButton.setToolTipText(MAXIMIZE_TOOLTIP);
	}

	@Override
	public PrecisionLayout getLayout() {
		return (PrecisionLayout) super.getLayout();
	}

	@Override
	public void setLayout(LayoutManager mgr) {
		if (mgr instanceof PrecisionLayout) {
			super.setLayout(mgr);
		} else {
			throw new IllegalArgumentException("Must use a PrecisionLayout."); //$NON-NLS-1$
		}
	}

	@Override
	public void componentAdded(ContainerEvent event) {
		getLayout().setColumns(getComponentCount());
	}

	@Override
	public void componentRemoved(ContainerEvent event) {
		getLayout().setColumns(getComponentCount());
	}

	/**
	 * Called when the 'active' state changes.
	 *
	 * @param active Whether the header should be drawn in its active state or not.
	 */
	void setActive(boolean active) {
		setBackground(active ? DockColors.ACTIVE_DOCK_HEADER_BACKGROUND : DockColors.BACKGROUND);
	}
}
