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

package com.trollworks.toolkit.collections;

import java.awt.Point;
import java.awt.Rectangle;

/** Defines a rectangular boundary of an object. */
public interface Bounds {
	/** @return The horizontal coordinate of the object. */
	int getX();

	/** @return The vertical coordinate of the object. */
	int getY();

	/** @return The width of the object. */
	int getWidth();

	/** @return The height of the object. */
	int getHeight();

	default boolean containsLocation(Point pt) {
		return containsLocation(pt.x, pt.y);
	}

	default boolean containsLocation(int x, int y) {
		int width = getWidth();
		if (width > 0) {
			int height = getHeight();
			if (height > 0) {
				int cx = getX();
				if (x >= cx && x < cx + width) {
					int cy = getY();
					return y >= cy && y < cy + height;
				}
			}
		}
		return false;
	}

	default boolean containsBounds(Rectangle other) {
		return containsBounds(other.x, other.y, other.width, other.height);
	}

	default boolean containsBounds(Bounds other) {
		return containsBounds(other.getX(), other.getY(), other.getWidth(), other.getHeight());
	}

	default boolean containsBounds(int otherX, int otherY, int otherWidth, int otherHeight) {
		int width = getWidth();
		if (width > 0) {
			int height = getHeight();
			if (height > 0) {
				int startX = getX();
				if (startX <= otherX) {
					int startY = getY();
					if (startY <= otherY) {
						int endX = startX + width;
						if (otherX < endX) {
							int endY = startY + height;
							if (otherY < endY) {
								int otherEndX = otherX + otherWidth;
								if (startX < otherEndX && endX >= otherEndX) {
									int otherEndY = otherY + otherHeight;
									return startY < otherEndY && endY >= otherEndY;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	default boolean containedBy(Rectangle other) {
		return containedBy(other.x, other.y, other.width, other.height);
	}

	default boolean containedBy(Bounds other) {
		return containedBy(other.getX(), other.getY(), other.getWidth(), other.getHeight());
	}

	default boolean containedBy(int otherX, int otherY, int otherWidth, int otherHeight) {
		if (otherWidth > 0 && otherHeight > 0) {
			int x = getX();
			if (otherX <= x) {
				int y = getY();
				if (otherY <= y) {
					int endX = otherX + otherWidth;
					if (x < endX) {
						int endY = otherY + otherHeight;
						if (y < endY) {
							int otherEndX = x + getWidth();
							if (otherX < otherEndX && endX >= otherEndX) {
								int otherEndY = y + getHeight();
								return otherY < otherEndY && endY >= otherEndY;
							}
						}
					}
				}
			}
		}
		return false;
	}

	default boolean intersectsBounds(Rectangle other) {
		return intersectsBounds(other.x, other.y, other.width, other.height);
	}

	default boolean intersectsBounds(Bounds other) {
		return intersectsBounds(other.getX(), other.getY(), other.getWidth(), other.getHeight());
	}

	default boolean intersectsBounds(int otherX, int otherY, int otherWidth, int otherHeight) {
		if (otherWidth > 0 && otherHeight > 0) {
			int width = getWidth();
			if (width > 0) {
				int height = getHeight();
				if (height > 0) {
					int x = getX();
					if (Math.min(x + width, otherX + otherWidth) > Math.max(x, otherX)) {
						int y = getY();
						return Math.min(y + height, otherY + otherHeight) > Math.max(y, otherY);
					}
				}
			}
		}
		return false;
	}
}
