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

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

/** The possible boundary areas. */
public enum BoundaryArea {
	/** The left edge. */
	LEFT {
		@Override
		public void adjust(int dx, int dy, Rectangle adjustee, Rectangle original) {
			adjustee.x = original.x + dx;
			adjustee.width = original.width - dx;
			if (adjustee.width < 1) {
				adjustee.x = original.x + original.width - 1;
				adjustee.width = 1;
			}
		}

		@Override
		public Cursor getCursor() {
			return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
		}
	},
	/** The right edge. */
	RIGHT {
		@Override
		public void adjust(int dx, int dy, Rectangle adjustee, Rectangle original) {
			adjustee.width = original.width + dx;
			if (adjustee.width < 1) {
				adjustee.width = 1;
			}
		}

		@Override
		public Cursor getCursor() {
			return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
		}
	},
	/** The top edge. */
	TOP {
		@Override
		public void adjust(int dx, int dy, Rectangle adjustee, Rectangle original) {
			adjustee.y = original.y + dy;
			adjustee.height = original.height - dy;
			if (adjustee.height < 1) {
				adjustee.y = original.y + original.height - 1;
				adjustee.height = 1;
			}
		}

		@Override
		public Cursor getCursor() {
			return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
		}
	},
	/** The bottom edge. */
	BOTTOM {
		@Override
		public void adjust(int dx, int dy, Rectangle adjustee, Rectangle original) {
			adjustee.height = original.height + dy;
			if (adjustee.height < 1) {
				adjustee.height = 1;
			}
		}

		@Override
		public Cursor getCursor() {
			return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
		}
	},
	/** The top, left edge. */
	TOP_LEFT {
		@Override
		public void adjust(int dx, int dy, Rectangle adjustee, Rectangle original) {
			TOP.adjust(dx, dy, adjustee, original);
			LEFT.adjust(dx, dy, adjustee, original);
		}

		@Override
		public Cursor getCursor() {
			return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
		}
	},
	/** The top, right edge. */
	TOP_RIGHT {
		@Override
		public void adjust(int dx, int dy, Rectangle adjustee, Rectangle original) {
			TOP.adjust(dx, dy, adjustee, original);
			RIGHT.adjust(dx, dy, adjustee, original);
		}

		@Override
		public Cursor getCursor() {
			return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
		}
	},
	/** The bottom, left edge. */
	BOTTOM_LEFT {
		@Override
		public void adjust(int dx, int dy, Rectangle adjustee, Rectangle original) {
			BOTTOM.adjust(dx, dy, adjustee, original);
			LEFT.adjust(dx, dy, adjustee, original);
		}

		@Override
		public Cursor getCursor() {
			return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
		}
	},
	/** The bottom, right edge. */
	BOTTOM_RIGHT {
		@Override
		public void adjust(int dx, int dy, Rectangle adjustee, Rectangle original) {
			BOTTOM.adjust(dx, dy, adjustee, original);
			RIGHT.adjust(dx, dy, adjustee, original);
		}

		@Override
		public Cursor getCursor() {
			return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
		}
	},
	/** The interior. */
	INTERIOR {
		@Override
		public void adjust(int dx, int dy, Rectangle adjustee, Rectangle original) {
			adjustee.x = original.x + dx;
			adjustee.y = original.y + dy;
		}

		@Override
		public Cursor getCursor() {
			return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
		}
	},
	/** Outside the rectangle. */
	OUTSIDE {
		@Override
		public void adjust(int dx, int dy, Rectangle adjustee, Rectangle original) {
			// Nothing to do.
		}

		@Override
		public Cursor getCursor() {
			return Cursor.getDefaultCursor();
		}
	};

	/**
	 * Adjusts a {@link Rectangle}.
	 *
	 * @param dx The delta from the original x-coordinate.
	 * @param dy The delta from the original y-coordinate.
	 * @param bounds The {@link Rectangle} to be adjusted.
	 * @param original The original {@link Rectangle}.
	 */
	public abstract void adjust(int dx, int dy, Rectangle bounds, Rectangle original);

	/** @return The {@link Cursor} corresponding to this {@link BoundaryArea}. */
	public abstract Cursor getCursor();

	private static final int	SLOP		= 5;
	private static final int	TWICE_SLOP	= SLOP * 2;

	/**
	 * @param where The coordinates to check.
	 * @param bounds The {@link Rectangle} being adjusted.
	 * @return The {@link BoundaryArea} corresponding to the coordinates.
	 */
	public static final BoundaryArea over(Point where, Rectangle bounds) {
		return over(where.x, where.y, bounds);
	}

	/**
	 * @param x The x-coordinate to check.
	 * @param y The y-coordinate to check.
	 * @param bounds The {@link Rectangle} being adjusted.
	 * @return The {@link BoundaryArea} corresponding to the coordinates.
	 */
	public static final BoundaryArea over(int x, int y, Rectangle bounds) {
		Rectangle expansion = new Rectangle(bounds.x - SLOP, bounds.y - SLOP, bounds.width + TWICE_SLOP, bounds.height + TWICE_SLOP);
		if (expansion.contains(x, y)) {
			boolean overTop = over(y, bounds.y);
			boolean overBottom = over(y, bounds.y + bounds.height);
			if (over(x, bounds.x)) {
				if (overTop) {
					return TOP_LEFT;
				} else if (overBottom) {
					return BOTTOM_LEFT;
				} else {
					return LEFT;
				}
			} else if (over(x, bounds.x + bounds.width)) {
				if (overTop) {
					return TOP_RIGHT;
				} else if (overBottom) {
					return BOTTOM_RIGHT;
				} else {
					return RIGHT;
				}
			} else if (overTop) {
				return TOP;
			} else if (overBottom) {
				return BOTTOM;
			} else if (bounds.contains(x, y)) {
				return INTERIOR;
			}
		}
		return OUTSIDE;
	}

	private static final boolean over(int value, int target) {
		return value > target - SLOP && value < target + SLOP;
	}

	private static final int	HANDLE		= 7;
	private static final int	HALF_HANDLE	= HANDLE / 2;

	/**
	 * Draws the boundary handles.
	 *
	 * @param gc The {@link Graphics} to use.
	 * @param bounds The {@link Rectangle} to draw handles on.
	 */
	public static final void drawHandles(Graphics gc, Rectangle bounds) {
		int yt = bounds.y - HALF_HANDLE;
		int ym = bounds.y + bounds.height / 2 - HALF_HANDLE;
		int yb = bounds.y + bounds.height - HALF_HANDLE;
		int xl = bounds.x - HALF_HANDLE;
		int xm = bounds.x + bounds.width / 2 - HALF_HANDLE;
		int xr = bounds.x + bounds.width - HALF_HANDLE;
		gc.fillRect(xl, yt, HANDLE, HANDLE);
		gc.fillRect(xm, yt, HANDLE, HANDLE);
		gc.fillRect(xr, yt, HANDLE, HANDLE);
		gc.fillRect(xl, ym, HANDLE, HANDLE);
		gc.fillRect(xr, ym, HANDLE, HANDLE);
		gc.fillRect(xl, yb, HANDLE, HANDLE);
		gc.fillRect(xm, yb, HANDLE, HANDLE);
		gc.fillRect(xr, yb, HANDLE, HANDLE);
		gc.fillRect(xm, ym, HANDLE, HANDLE);
	}
}
