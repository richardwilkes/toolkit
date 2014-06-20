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

package com.trollworks.toolkit.collections;

import java.awt.geom.Rectangle2D;

/** Represents a circle. */
public class Circle {
	private double	mX;
	private double	mY;
	private double	mRadius;

	/** Creates a new {@link Circle} with a center point of 0,0 and a radius of 0. */
	public Circle() {
	}

	/**
	 * Creates a new {@link Circle}.
	 *
	 * @param x The horizontal center.
	 * @param y The vertical center.
	 * @param radius The radius to use. Will be forced to a minimum of 0.
	 */
	public Circle(double x, double y, double radius) {
		set(x, y, radius);
	}

	/**
	 * Creates a new {@link Circle} the encompasses the two passed-in {@link Circle}s. Note that it
	 * is much more expensive to do this for {@link Circle}s that do not have the same radius.
	 *
	 * @param c1 The first {@link Circle}.
	 * @param c2 The second {@link Circle}.
	 */
	public Circle(Circle c1, Circle c2) {
		if (c1.mRadius == c2.mRadius) {
			// Faster way, but only works when radius is the same
			double x1 = (c1.mX + c2.mX) / 2;
			double y1 = (c1.mY + c2.mY) / 2;
			double x = c1.mX - c2.mX;
			double y = c1.mY - c2.mY;
			double distanceSquared = x * x + y * y;
			double radiusSquared1 = c1.mRadius * c1.mRadius;
			double radiusSquared2 = c2.mRadius * c2.mRadius;
			if (radiusSquared1 >= distanceSquared + radiusSquared2) {
				set(c1);
			} else if (radiusSquared2 >= distanceSquared + radiusSquared1) {
				set(c2);
			} else {
				set(x1, y1, (Math.sqrt(distanceSquared) + c1.mRadius + c2.mRadius) / 2);
			}
		} else {
			double angle = Math.atan2(c2.mY - c1.mY, c2.mX - c1.mX);
			double x1 = c2.mX + Math.cos(angle) * c2.mRadius;
			double y1 = c2.mY + Math.sin(angle) * c2.mRadius;
			angle += Math.PI;
			double x2 = c1.mX + Math.cos(angle) * c1.mRadius;
			double y2 = c1.mY + Math.sin(angle) * c1.mRadius;
			double x = x1 - x2;
			double y = y1 - y2;
			double radius = Math.sqrt(x * x + y * y) / 2;
			if (radius < c1.mRadius) {
				set(c1);
			} else if (radius < c2.mRadius) {
				set(c2);
			} else {
				set((x1 + x2) / 2, (y1 + y2) / 2, radius);
			}
		}
	}

	/** @return The horizontal center of this {@link Circle}. */
	public double getX() {
		return mX;
	}

	/** @return The vertical center of this {@link Circle}. */
	public double getY() {
		return mY;
	}

	/** @return The radius of this {@link Circle}. */
	public double getRadius() {
		return mRadius;
	}

	/**
	 * @param x The horizontal center.
	 * @param y The vertical center.
	 */
	public void setCenter(double x, double y) {
		mX = x;
		mY = y;
	}

	/** @param radius The radius to use. Will be forced to a minimum of 0. */
	public void setRadius(double radius) {
		mRadius = Math.max(radius, 0);
	}

	/**
	 * @param x The horizontal center.
	 * @param y The vertical center.
	 * @param radius The radius to use. Will be forced to a minimum of 0.
	 */
	public void set(double x, double y, double radius) {
		mX = x;
		mY = y;
		mRadius = Math.max(radius, 0);
	}

	/** @param other Another circle to copy the state from. */
	public void set(Circle other) {
		mX = other.mX;
		mY = other.mY;
		mRadius = other.mRadius;
	}

	/**
	 * @param x The horizontal coordinate to test.
	 * @param y The vertical coordinate to test.
	 * @return <code>true</code> if the coordinates are within the {@link Circle}.
	 */
	public boolean containsPoint(double x, double y) {
		x -= mX;
		y -= mY;
		return mRadius * mRadius >= x * x + y * y;
	}

	/**
	 * @param circle The {@link Circle} to test.
	 * @return <code>true</code> if the passed-in {@link Circle} is completely within the
	 *         {@link Circle}.
	 */
	public boolean containsCircle(Circle circle) {
		double x = mX - circle.mX;
		double y = mY - circle.mY;
		return mRadius * mRadius >= x * x + y * y + circle.mRadius * circle.mRadius;
	}

	/**
	 * @param circle The {@link Circle} to test.
	 * @return <code>true</code> if the passed-in {@link Circle} overlaps the {@link Circle}.
	 */
	public boolean intersects(Circle circle) {
		double x = mX - circle.mX;
		double y = mY - circle.mY;
		double radius = mRadius + circle.mRadius;
		return radius * radius >= x * x + y * y;
	}

	/** @return The rectangular area containing the {@link Circle}. */
	public Rectangle2D getBoundingBox() {
		double size = mRadius * 2;
		return new Rectangle2D.Double(mX - mRadius, mY - mRadius, size, size);
	}
}
