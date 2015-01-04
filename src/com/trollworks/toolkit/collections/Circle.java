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

import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/** Represents a circle. */
public class Circle extends Ellipse2D {
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

	@Override
	public double getX() {
		return mX - mRadius;
	}

	@Override
	public double getY() {
		return mY - mRadius;
	}

	/** @return The horizontal center of this {@link Circle}. */
	@Override
	public double getCenterX() {
		return mX;
	}

	/** @return The vertical center of this {@link Circle}. */
	@Override
	public double getCenterY() {
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
	@Override
	public boolean contains(double x, double y) {
		x -= mX;
		y -= mY;
		return mRadius * mRadius >= x * x + y * y;
	}

	/**
	 * @param circle The {@link Circle} to test.
	 * @return <code>true</code> if the passed-in {@link Circle} is completely within the
	 *         {@link Circle}.
	 */
	public boolean contains(Circle circle) {
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

	@Override
	public Rectangle2D getBounds2D() {
		return getFrame();
	}

	@Override
	public double getWidth() {
		return mRadius * 2;
	}

	@Override
	public double getHeight() {
		return mRadius * 2;
	}

	@Override
	public boolean isEmpty() {
		return mRadius <= 0;
	}

	@Override
	public void setFrame(double x, double y, double width, double height) {
		if (width < 0) {
			width = 0;
		}
		if (height < 0) {
			height = 0;
		}
		if (width != height) {
			mRadius = Math.min(width, height) / 2;
			mX = x + mRadius + (width / 2 - mRadius);
			mY = y + mRadius + (height / 2 - mRadius);
		} else {
			mRadius = width / 2;
			mX = x + mRadius;
			mY = y + mRadius;
		}
	}

	/**
	 * Adjusts this {@link Circle} such that it encompasses both the area it occupied prior to this
	 * call and the area of the passed-in {@link Circle}. Note that it is much more expensive to do
	 * this for a {@link Circle} that does not have the same radius as this {@link Circle}.
	 *
	 * @param other The other {@link Circle}.
	 */
	public void add(Circle other) {
		if (mRadius == other.mRadius) {
			// Faster way, but only works when radius is the same
			double x1 = (mX + other.mX) / 2;
			double y1 = (mY + other.mY) / 2;
			double x = mX - other.mX;
			double y = mY - other.mY;
			double distanceSquared = x * x + y * y;
			double radiusSquared1 = mRadius * mRadius;
			double radiusSquared2 = other.mRadius * other.mRadius;
			if (radiusSquared1 < distanceSquared + radiusSquared2) {
				if (radiusSquared2 >= distanceSquared + radiusSquared1) {
					set(other);
				} else {
					set(x1, y1, (Math.sqrt(distanceSquared) + mRadius + other.mRadius) / 2);
				}
			}
		} else {
			double angle = Math.atan2(other.mY - mY, other.mX - mX);
			double x1 = other.mX + Math.cos(angle) * other.mRadius;
			double y1 = other.mY + Math.sin(angle) * other.mRadius;
			angle += Math.PI;
			double x2 = mX + Math.cos(angle) * mRadius;
			double y2 = mY + Math.sin(angle) * mRadius;
			double x = x1 - x2;
			double y = y1 - y2;
			double radius = Math.sqrt(x * x + y * y) / 2;
			if (mRadius < radius) {
				if (other.mRadius >= radius) {
					set(other);
				} else {
					set((x1 + x2) / 2, (y1 + y2) / 2, radius);
				}
			}
		}
	}
}
