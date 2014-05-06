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

import java.awt.event.MouseEvent;

/** Handles dragging a {@link DockContainer} around. */
class DockContainerDragHandler extends DockDragHandler {
	private DockContainer	mContainer;
	private DockLayoutNode	mOver;
	private DockLocation	mLocation;

	/** @param container The {@link DockContainer} to be moved. */
	DockContainerDragHandler(DockContainer container) {
		mContainer = container;
	}

	@Override
	protected void performDrag(MouseEvent event) {
		Dock dock = (Dock) mContainer.getParent();
		DockLayoutNode oldOver = mOver;
		DockLocation oldLocation = mLocation;
		int ex = event.getX();
		int ey = event.getY();
		DockLayoutNode over = dock.over(ex, ey, false);
		if (over == mContainer) {
			over = dock.getLayout().findLayout(mContainer.getDockable());
		}
		if (over != null) {
			mOver = over;
			int x = mOver.getX();
			int y = mOver.getY();
			int width = mOver.getWidth();
			int height = mOver.getHeight();
			ex -= x;
			ey -= y;
			if (ex < width / 2) {
				mLocation = DockLocation.WEST;
			} else {
				mLocation = DockLocation.EAST;
				ex = width - ex;
			}
			if (ey < height / 2) {
				if (ex > ey) {
					mLocation = DockLocation.NORTH;
				}
			} else if (ex > height - ey) {
				mLocation = DockLocation.SOUTH;
			}
		} else {
			mOver = null;
			mLocation = null;
		}
		if (oldOver != mOver || oldLocation != mLocation) {
			dock.setDragOver(mOver, mLocation);
		}
	}

	@Override
	protected void finishDrag(MouseEvent event) {
		drag(event);
		Dock dock = (Dock) mContainer.getParent();
		dock.setDragOver(null, null);
		if (mOver != null) {
			dock.getLayout().dock(mContainer, mOver, mLocation);
			dock.revalidate();
		}
	}
}
