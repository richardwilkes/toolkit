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
import com.trollworks.toolkit.ui.Colors;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.border.SelectiveLineBorder;
import com.trollworks.toolkit.ui.image.Cursors;
import com.trollworks.toolkit.ui.image.ToolkitImage;
import com.trollworks.toolkit.ui.layout.PrecisionLayout;
import com.trollworks.toolkit.ui.layout.PrecisionLayoutData;
import com.trollworks.toolkit.ui.widget.IconButton;
import com.trollworks.toolkit.utility.Localization;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/** Provides a tab that contains the {@link Dockable}'s icon, title, and close button, if any. */
public class DockTab extends JPanel implements ContainerListener, MouseListener, DragGestureListener {
	@Localize("Close")
	private static String	CLOSE_TOOLTIP;

	static {
		Localization.initialize();
	}

	private Dockable		mDockable;

	/**
	 * Creates a new {@link DockTab} for the specified {@link Dockable}.
	 *
	 * @param dockable The {@link Dockable} to work with.
	 */
	public DockTab(Dockable dockable) {
		super(new PrecisionLayout().setMargins(2, 4, 2, 4).setMiddleVerticalAlignment());
		mDockable = dockable;
		setOpaque(false);
		setBorder(new CompoundBorder(new EmptyBorder(1, 0, 0, 0), new SelectiveLineBorder(DockColors.SHADOW, 1, 1, 0, 1)));
		addContainerListener(this);
		add(new JLabel(dockable.getTitle(), dockable.getTitleIcon(), SwingConstants.LEFT), new PrecisionLayoutData().setGrabHorizontalSpace(true));
		if (dockable instanceof DockCloseable) {
			add(new IconButton(ToolkitImage.getDockClose(), CLOSE_TOOLTIP, this::attemptClose), new PrecisionLayoutData().setEndHorizontalAlignment());
		}
		addMouseListener(this);
		setCursor(Cursors.MOVE);
		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
	}

	/**
	 * @return <code>true</code> if this {@link DockTab} is the current one for the
	 *         {@link DockContainer}.
	 */
	public boolean isCurrent() {
		DockContainer dc = getDockContainer();
		return dc != null && dc.getCurrentDockable() == mDockable;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D gc = (Graphics2D) g;
		Insets insets = getInsets();
		DockContainer dc = getDockContainer();
		Color base = DockColors.BACKGROUND;
		if (dc != null) {
			if (dc.getCurrentDockable() == mDockable) {
				base = dc.isActive() ? DockColors.ACTIVE_TAB_BACKGROUND : DockColors.CURRENT_TAB_BACKGROUND;
			}
		}
		gc.setPaint(new GradientPaint(new Point(insets.left, insets.top), base, new Point(insets.left, getHeight() - (insets.top + insets.bottom)), Colors.adjustBrightness(base, -0.1f)));
		gc.fillRect(insets.left, insets.top, getWidth() - (insets.left + insets.right), getHeight() - (insets.top + insets.bottom));
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
			dc.attemptClose(mDockable);
		}
	}

	@Override
	public void dragGestureRecognized(DragGestureEvent dge) {
		if (DragSource.isDragImageSupported()) {
			Point offset = new Point(dge.getDragOrigin());
			offset.x = -offset.x;
			offset.y = -offset.y;
			dge.startDrag(Cursors.MOVE, UIUtilities.getImage(this), offset, new DockableTransferable(mDockable), null);
		}
	}

	@Override
	public void mouseEntered(MouseEvent event) {
		// Unused
	}

	@Override
	public void mousePressed(MouseEvent event) {
		DockContainer dc = getDockContainer();
		if (dc.getCurrentDockable() != mDockable) {
			dc.setCurrentDockable(mDockable);
			dc.transferFocus();
		} else if (!dc.isActive()) {
			dc.transferFocus();
		}
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		// Unused
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		// Unused
	}

	@Override
	public void mouseExited(MouseEvent event) {
		// Unused
	}
}
