/*
 * Copyright (c) 1998-2015 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui.widget;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/** Tracks and adjusts a boundary {@link Rectangle}. */
public class BoundaryTracker implements MouseListener, MouseMotionListener {
	private Component			mComponent;
	private BoundaryListener	mListener;
	private BoundaryArea		mAdjuster;
	private Point				mSnapshot;
	private Rectangle			mBoundsSnapshot;
	private Rectangle			mBoundsToAdjust;
	private Rectangle			mInterimBounds;

	/**
	 * Creates a new {@link BoundaryTracker}.
	 *
	 * @param component The {@link Component} that contains the {@link Rectangle} being tracked.
	 * @param boundsToAdjust The {@link Rectangle} to adjust.
	 * @param listener The {@link BoundaryListener} to notify when changes occur.
	 */
	public BoundaryTracker(Component component, Rectangle boundsToAdjust, BoundaryListener listener) {
		mComponent = component;
		mListener = listener;
		mBoundsToAdjust = boundsToAdjust;
		mBoundsSnapshot = new Rectangle(mBoundsToAdjust);
		mInterimBounds = new Rectangle(mBoundsToAdjust);
		mAdjuster = BoundaryArea.OUTSIDE;
		mComponent.addMouseListener(this);
		mComponent.addMouseMotionListener(this);
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		// Not used.
	}

	@Override
	public void mouseEntered(MouseEvent event) {
		updateTracking(event);
	}

	@Override
	public void mouseExited(MouseEvent event) {
		setTracking(BoundaryArea.OUTSIDE);
	}

	@Override
	public void mousePressed(MouseEvent event) {
		mSnapshot = mListener.convertToLocalCoordinates(new Point(event.getX(), event.getY()));
		mBoundsSnapshot.setBounds(mBoundsToAdjust);
		updateTracking(mSnapshot);
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		updateTracking(event);
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		Point where = mListener.convertToLocalCoordinates(new Point(event.getX(), event.getY()));
		mInterimBounds.setBounds(mBoundsToAdjust);
		mAdjuster.adjust(where.x - mSnapshot.x, where.y - mSnapshot.y, mBoundsToAdjust, mBoundsSnapshot);
		if (!mInterimBounds.equals(mBoundsToAdjust)) {
			mListener.boundaryChanged(mInterimBounds, mBoundsToAdjust);
		}
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		updateTracking(event);
	}

	private void updateTracking(MouseEvent event) {
		updateTracking(mListener.convertToLocalCoordinates(new Point(event.getX(), event.getY())));
	}

	private void updateTracking(Point where) {
		setTracking(BoundaryArea.over(where, mBoundsToAdjust));
	}

	private void setTracking(BoundaryArea tracking) {
		if (tracking != mAdjuster) {
			mAdjuster = tracking;
			Cursor cursor = mAdjuster.getCursor();
			mComponent.setCursor(cursor);
			WindowUtils.getWindowForComponent(mComponent).setCursor(cursor);
			mListener.boundaryAreaChanged(mAdjuster);
		}
	}
}
