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
import com.trollworks.toolkit.ui.widget.IconButton;
import com.trollworks.toolkit.utility.Localization;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/** The header for a {@link DockContainer}. */
public class DockHeader extends JPanel implements LayoutManager {
	@Localize("Maximize")
	private static String		MAXIMIZE_TOOLTIP;
	@Localize("Restore")
	private static String		RESTORE_TOOLTIP;

	static {
		Localization.initialize();
	}

	private static final int	MINIMUM_TAB_WIDTH	= 28;
	private static final int	GAP					= 4;
	private IconButton			mMaximizeRestoreButton;

	/**
	 * Creates a new {@link DockHeader} for the specified {@link DockContainer}.
	 *
	 * @param dc The {@link DockContainer} to work with.
	 */
	public DockHeader(DockContainer dc) {
		super.setLayout(this);
		setOpaque(true);
		setBackground(DockColors.BACKGROUND);
		setBorder(new CompoundBorder(new SelectiveLineBorder(DockColors.SHADOW, 0, 0, 1, 0), new EmptyBorder(0, 4, 0, 4)));
		for (Dockable dockable : dc.getDockables()) {
			add(new DockTab(dockable));
		}
		mMaximizeRestoreButton = new IconButton(ToolkitImage.getDockMaximize(), MAXIMIZE_TOOLTIP, this::maximize);
		add(mMaximizeRestoreButton);
	}

	void addTab(Dockable dockable) {
		int count = getComponentCount();
		int i;
		for (i = 0; i < count; i++) {
			if (!(getComponent(i) instanceof DockTab)) {
				break;
			}
		}
		add(new DockTab(dockable), i);
		revalidate();
		repaint();
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
	public void setLayout(LayoutManager mgr) {
		// Don't allow overrides
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
		// Unused
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		// Unused
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		Insets insets = getInsets();
		int count = getComponentCount();
		int width = count > 0 ? (count - 1) * GAP : 0;
		int height = 0;
		for (int i = 0; i < count; i++) {
			Dimension size = getComponent(i).getPreferredSize();
			width += size.width;
			if (height < size.height) {
				height = size.height;
			}
		}
		return new Dimension(insets.left + width + insets.right, insets.top + height + insets.bottom);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		Insets insets = getInsets();
		int count = getComponentCount();
		int width = count > 0 ? (count - 1) * GAP : 0;
		int height = 0;
		for (int i = 0; i < count; i++) {
			Component component = getComponent(i);
			Dimension size = component.getPreferredSize();
			width += component instanceof DockTab ? MINIMUM_TAB_WIDTH : size.width;
			if (height < size.height) {
				height = size.height;
			}
		}
		return new Dimension(insets.left + width + insets.right, insets.top + height + insets.bottom);
	}

	@Override
	public void layoutContainer(Container parent) {
		int extra = getWidth() - preferredLayoutSize(parent).width;
		Insets insets = getInsets();
		int count = getComponentCount();
		Component[] comps = getComponents();
		int[] widths = new int[count];
		int[] heights = new int[count];
		for (int i = 0; i < count; i++) {
			Dimension size = comps[i].getPreferredSize();
			widths[i] = size.width;
			heights[i] = size.height;
		}
		if (extra < 0) {
			int current = getDockContainer().getCurrentTabIndex();
			int remaining = -extra;
			boolean found = true;
			// Shrink the non-current tabs down
			while (found && remaining > 0) {
				int tabs = 0;
				found = false;
				for (int i = 0; i < count; i++) {
					if (i != current && comps[i] instanceof DockTab && widths[i] > MINIMUM_TAB_WIDTH) {
						tabs++;
					}
				}
				if (tabs > 0) {
					int perTab = Math.max(remaining / tabs, 1);
					for (int i = 0; i < count && remaining > 0; i++) {
						if (i != current && comps[i] instanceof DockTab && widths[i] > MINIMUM_TAB_WIDTH) {
							found = true;
							remaining -= perTab;
							widths[i] -= perTab;
							if (widths[i] <= MINIMUM_TAB_WIDTH) {
								remaining += MINIMUM_TAB_WIDTH - widths[i];
								widths[i] = MINIMUM_TAB_WIDTH;
							}
						}
					}
				}
			}
			if (remaining > 0) {
				// Still not small enough... shrink the current tab down, too
				widths[current] -= remaining;
				if (widths[current] < MINIMUM_TAB_WIDTH) {
					widths[current] = MINIMUM_TAB_WIDTH;
				}
			}
			extra = 0;
		}
		int x = insets.left;
		int height = getHeight();
		boolean insertExtra = true;
		for (int i = 0; i < count; i++) {
			if (insertExtra && !(comps[i] instanceof DockTab)) {
				insertExtra = false;
				x += extra;
			}
			comps[i].setBounds(x, insets.top + (height - (insets.top + heights[i] + insets.bottom)) / 2, widths[i], heights[i]);
			x += widths[i] + GAP;
		}
	}
}
