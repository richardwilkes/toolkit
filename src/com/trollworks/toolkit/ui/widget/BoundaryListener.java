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
import java.awt.Point;
import java.awt.Rectangle;

/**
 * Objects that want to respond to notifications from a {@link BoundaryTracker} must implement this
 * interface.
 */
public interface BoundaryListener {
	/**
	 * Called to perform any necessary adjustments on the mouse coordinates before they are used for
	 * tracking.
	 *
	 * @param where The coordinates, relative to the {@link Component} containing the
	 *            {@link Rectangle} being tracked.
	 * @return The object that was passed in, adjusted as necessary.
	 */
	Point convertToLocalCoordinates(Point where);

	/**
	 * Called when the {@link BoundaryArea} the mouse is over has changed.
	 *
	 * @param area The new {@link BoundaryArea}.
	 */
	void boundaryAreaChanged(BoundaryArea area);

	/**
	 * Called when the {@link Rectangle} representing the boundary is resized or repositioned.
	 *
	 * @param oldBounds The old {@link Rectangle}.
	 * @param newBounds The new {@link Rectangle}.
	 */
	void boundaryChanged(Rectangle oldBounds, Rectangle newBounds);
}
