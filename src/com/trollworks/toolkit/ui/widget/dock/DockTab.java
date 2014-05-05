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
import com.trollworks.toolkit.ui.image.ToolkitImage;
import com.trollworks.toolkit.ui.layout.PrecisionLayout;
import com.trollworks.toolkit.ui.layout.PrecisionLayoutData;
import com.trollworks.toolkit.ui.widget.IconButton;
import com.trollworks.toolkit.utility.Localization;

import java.awt.LayoutManager;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Provides a tab that contains the {@link Dockable}'s icon, title, and close button, if any. */
public class DockTab extends JPanel implements ContainerListener {
	@Localize("Close")
	private static String	CLOSE_TOOLTIP;

	static {
		Localization.initialize();
	}

	/**
	 * Creates a new {@link DockTab} for the specified {@link Dockable}.
	 *
	 * @param dockable The {@link Dockable} to work with.
	 */
	public DockTab(Dockable dockable) {
		super(new PrecisionLayout().setMargins(0).setMiddleVerticalAlignment());
		setOpaque(false);
		addContainerListener(this);
		add(new JLabel(dockable.getTitle(), dockable.getTitleIcon(), SwingConstants.LEFT), new PrecisionLayoutData().setGrabHorizontalSpace(true));
		if (dockable instanceof DockCloseable) {
			IconButton closeButton = new IconButton(ToolkitImage.getDockClose(), CLOSE_TOOLTIP, () -> attemptClose());
			add(closeButton, new PrecisionLayoutData().setEndHorizontalAlignment());
		}
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

	private DockContainer getDockContainer() {
		return (DockContainer) UIUtilities.getAncestorOfType(this, DockContainer.class);
	}

	public void attemptClose() {
		DockContainer dc = getDockContainer();
		if (dc != null) {
			dc.attemptClose();
		}
	}
}
