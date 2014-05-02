package com.trollworks.toolkit.ui.widget.dock;

import java.awt.event.MouseEvent;

/** Handles dragging the divider of a {@link DockLayout} around. */
class DividerDragHandler extends DockDragHandler {
	private DockLayout	mLayout;
	private int			mInitialEventPosition;
	private int			mInitialDividerPosition;

	/** @param layout The {@link DockLayout} to be changed. */
	DividerDragHandler(DockLayout layout) {
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
