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

/** Handles dragging the divider of a {@link DockLayout} around. */
class DockDividerDragHandler extends DockDragHandler {
	private DockLayout	mLayout;
	private int			mInitialEventPosition;
	private int			mInitialDividerPosition;

	/** @param layout The {@link DockLayout} to be changed. */
	DockDividerDragHandler(DockLayout layout) {
		mLayout = layout;
	}

	@Override
	protected void prepare(MouseEvent event) {
		mInitialEventPosition = mLayout.isHorizontal() ? event.getX() : event.getY();
		mInitialDividerPosition = mLayout.getDividerPosition();
	}

	@Override
	protected void performDrag(MouseEvent event) {
		int pos = mInitialDividerPosition - (mInitialEventPosition - (mLayout.isHorizontal() ? event.getX() : event.getY()));
		mLayout.setDividerPosition(pos < 0 ? 0 : pos);
	}

	@Override
	protected void finishDrag(MouseEvent event) {
		drag(event);
	}
}
