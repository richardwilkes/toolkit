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

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.utility.Localization;

import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;

/** Provides a {@link QuadTree} which contains rectangular areas. */
public class QuadTree<T extends Bounds> {
	private Node<T>	mRoot;
	private Set<T>	mOutside;
	private int		mThreshold;

	/** Creates a new, empty {@link QuadTree} with a threshold of 64. */
	public QuadTree() {
		this(64);
	}

	/**
	 * Creates a new, empty {@link QuadTree} with the specified threshold.
	 *
	 * @param threshold The number of objects that may be contained within a single node before a
	 *            split occurs.
	 */
	public QuadTree(int threshold) {
		mThreshold = threshold;
		clear();
	}

	/**
	 * Adds an object to the {@link QuadTree}.
	 * <p>
	 * <b>Note</b>: Once an object is added to the {@link QuadTree}, the values it returns from
	 * calls to {@link Bounds#getX()}, {@link Bounds#getY()}, {@link Bounds#getWidth()}, and
	 * {@link Bounds#getHeight()} <b>MUST REMAIN THE SAME</b> as when the object was added. When the
	 * object is removed from the {@link QuadTree}, it is safe to once again allow those values to
	 * change.
	 *
	 * @param obj The object to add to the tree.
	 */
	public final void add(T obj) {
		if (mRoot.containsBounds(obj)) {
			mRoot.add(obj);
		} else {
			mOutside.add(obj);
			if (mOutside.size() > mThreshold) {
				reorganize();
			}
		}
	}

	/** Forces the {@link QuadTree} to reorganize itself to optimally fit its contents. */
	public final void reorganize() {
		mRoot.all(mOutside);

		// Determine the union of all contained bounds
		int x = 0;
		int y = 0;
		int width = 0;
		int height = 0;
		for (T one : mOutside) {
			int otherWidth = one.getWidth();
			if (otherWidth > 0) {
				int otherHeight = one.getHeight();
				if (otherHeight > 0) {
					int otherX = one.getX();
					int otherY = one.getY();
					if (width <= 0 || height <= 0) {
						x = otherX;
						y = otherY;
						width = otherWidth;
						height = otherHeight;
					} else {
						int x1 = Math.min(x, otherX);
						int y1 = Math.min(y, otherY);
						width = Math.max(x + width, otherX + otherWidth) - x1;
						height = Math.max(y + height, otherY + otherHeight) - y1;
						x = x1;
						y = y1;
					}
				}
			}
		}

		mRoot = new Node<>(x, y, width, height, mThreshold);
		for (T one : mOutside) {
			mRoot.add(one);
		}
		mOutside = new HashSet<>();
	}

	/** @param obj The object to remove. */
	public final void remove(T obj) {
		if (!mOutside.remove(obj)) {
			mRoot.remove(obj);
		}
	}

	/** Removes all objects from the {@link QuadTree}. */
	public final void clear() {
		mRoot = new Node<>(0, 0, 0, 0, mThreshold);
		mOutside = new HashSet<>();
	}

	/**
	 * @param x The horizontal coordinate to check.
	 * @param y The vertical coordinate to check.
	 * @return <code>true</code> if this {@link QuadTree} has at least one object that contains the
	 *         specified coordinates.
	 */
	public final boolean contains(int x, int y) {
		if (mRoot.contains(x, y)) {
			return true;
		}
		for (T one : mOutside) {
			if (one.containsLocation(x, y)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param x The horizontal coordinate to check.
	 * @param y The vertical coordinate to check.
	 * @param matcher A {@link Matcher} to use to verify any potential matches.
	 * @return <code>true</code> if this {@link QuadTree} has at least one object that contains the
	 *         specified coordinates and passes the {@link Matcher}'s test.
	 */
	public final boolean contains(int x, int y, Matcher<T> matcher) {
		if (mRoot.contains(x, y, matcher)) {
			return true;
		}
		for (T one : mOutside) {
			if (one.containsLocation(x, y) && matcher.matches(one)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param bounds The bounds to check.
	 * @return <code>true</code> if this {@link QuadTree} has at least one object that intersects
	 *         with the specified bounds.
	 */
	public final boolean intersects(Rectangle bounds) {
		return intersects(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	/**
	 * @param bounds The bounds to check.
	 * @return <code>true</code> if this {@link QuadTree} has at least one object that intersects
	 *         with the specified bounds.
	 */
	public final boolean intersects(Bounds bounds) {
		return intersects(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
	}

	/**
	 * @param x The horizontal coordinate to check.
	 * @param y The vertical coordinate to check.
	 * @param width The width of the space to check.
	 * @param height The height of the space to check.
	 * @return <code>true</code> if this {@link QuadTree} has at least one object that intersects
	 *         with the specified bounds.
	 */
	public final boolean intersects(int x, int y, int width, int height) {
		if (mRoot.intersects(x, y, width, height)) {
			return true;
		}
		for (T one : mOutside) {
			if (one.intersectsBounds(x, y, width, height)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param bounds The bounds to check.
	 * @param matcher A {@link Matcher} to use to verify any potential matches.
	 * @return <code>true</code> if this {@link QuadTree} has at least one object that intersects
	 *         with the specified bounds and passes the {@link Matcher}'s test.
	 */
	public final boolean intersects(Rectangle bounds, Matcher<T> matcher) {
		return intersects(bounds.x, bounds.y, bounds.width, bounds.height, matcher);
	}

	/**
	 * @param bounds The bounds to check.
	 * @param matcher A {@link Matcher} to use to verify any potential matches.
	 * @return <code>true</code> if this {@link QuadTree} has at least one object that intersects
	 *         with the specified bounds and passes the {@link Matcher}'s test.
	 */
	public final boolean intersects(Bounds bounds, Matcher<T> matcher) {
		return intersects(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), matcher);
	}

	/**
	 * @param x The horizontal coordinate to check.
	 * @param y The vertical coordinate to check.
	 * @param width The width of the space to check.
	 * @param height The height of the space to check.
	 * @param matcher A {@link Matcher} to use to verify any potential matches.
	 * @return <code>true</code> if this {@link QuadTree} has at least one object that intersects
	 *         with the specified bounds and passes the {@link Matcher}'s test.
	 */
	public final boolean intersects(int x, int y, int width, int height, Matcher<T> matcher) {
		if (mRoot.intersects(x, y, width, height, matcher)) {
			return true;
		}
		for (T one : mOutside) {
			if (one.intersectsBounds(x, y, width, height) && matcher.matches(one)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param bounds The bounds to check.
	 * @return <code>true</code> if this {@link QuadTree} has at least one object that would be
	 *         contained by the specified bounds.
	 */
	public final boolean containedBy(Rectangle bounds) {
		return containedBy(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	/**
	 * @param bounds The bounds to check.
	 * @return <code>true</code> if this {@link QuadTree} has at least one object that would be
	 *         contained by the specified bounds.
	 */
	public final boolean containedBy(Bounds bounds) {
		return containedBy(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
	}

	/**
	 * @param x The horizontal coordinate to check.
	 * @param y The vertical coordinate to check.
	 * @param width The width of the space to check.
	 * @param height The height of the space to check.
	 * @return <code>true</code> if this {@link QuadTree} has at least one object that would be
	 *         contained by the specified bounds.
	 */
	public final boolean containedBy(int x, int y, int width, int height) {
		if (mRoot.inside(x, y, width, height)) {
			return true;
		}
		for (T one : mOutside) {
			if (one.containedBy(x, y, width, height)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param bounds The bounds to check.
	 * @param matcher A {@link Matcher} to use to verify any potential matches.
	 * @return <code>true</code> if this {@link QuadTree} has at least one object that would be
	 *         contained by the specified bounds and passes the {@link Matcher}'s test.
	 */
	public final boolean containedBy(Rectangle bounds, Matcher<T> matcher) {
		return containedBy(bounds.x, bounds.y, bounds.width, bounds.height, matcher);
	}

	/**
	 * @param bounds The bounds to check.
	 * @param matcher A {@link Matcher} to use to verify any potential matches.
	 * @return <code>true</code> if this {@link QuadTree} has at least one object that would be
	 *         contained by the specified bounds and passes the {@link Matcher}'s test.
	 */
	public final boolean containedBy(Bounds bounds, Matcher<T> matcher) {
		return containedBy(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), matcher);
	}

	/**
	 * @param x The horizontal coordinate to check.
	 * @param y The vertical coordinate to check.
	 * @param width The width of the space to check.
	 * @param height The height of the space to check.
	 * @param matcher A {@link Matcher} to use to verify any potential matches.
	 * @return <code>true</code> if this {@link QuadTree} has at least one object that would be
	 *         contained by the specified bounds and passes the {@link Matcher}'s test.
	 */
	public final boolean containedBy(int x, int y, int width, int height, Matcher<T> matcher) {
		if (mRoot.inside(x, y, width, height, matcher)) {
			return true;
		}
		for (T one : mOutside) {
			if (one.containedBy(x, y, width, height) && matcher.matches(one)) {
				return true;
			}
		}
		return false;
	}

	/** @return All objects that have been added to this {@link QuadTree}. */
	public final Set<T> all() {
		Set<T> result = new HashSet<>();
		mRoot.all(result);
		result.addAll(mOutside);
		return result;
	}

	/**
	 * @param matcher A {@link Matcher} to use to verify any potential matches.
	 * @return All objects that have been added to this {@link QuadTree} and pass the
	 *         {@link Matcher}'s test.
	 */
	public final Set<T> all(Matcher<T> matcher) {
		Set<T> result = new HashSet<>();
		mRoot.all(result, matcher);
		for (T one : mOutside) {
			if (matcher.matches(one)) {
				result.add(one);
			}
		}
		return result;
	}

	/**
	 * @param x The horizontal coordinate to check.
	 * @param y The vertical coordinate to check.
	 * @return All objects in this {@link QuadTree} that contain the specified coordinates.
	 */
	public final Set<T> findContains(int x, int y) {
		Set<T> result = new HashSet<>();
		mRoot.findContains(x, y, result);
		for (T one : mOutside) {
			if (one.containsLocation(x, y)) {
				result.add(one);
			}
		}
		return result;
	}

	/**
	 * @param x The horizontal coordinate to check.
	 * @param y The vertical coordinate to check.
	 * @param matcher A {@link Matcher} to use to verify any potential matches.
	 * @return All objects in this {@link QuadTree} that contain the specified coordinates and pass
	 *         the {@link Matcher}'s test.
	 */
	public final Set<T> findContains(int x, int y, Matcher<T> matcher) {
		Set<T> result = new HashSet<>();
		mRoot.findContains(x, y, result, matcher);
		for (T one : mOutside) {
			if (one.containsLocation(x, y) && matcher.matches(one)) {
				result.add(one);
			}
		}
		return result;
	}

	/**
	 * @param bounds The bounds to check.
	 * @return All objects in this {@link QuadTree} that intersect with the specified bounds.
	 */
	public final Set<T> findIntersects(Rectangle bounds) {
		return findIntersects(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	/**
	 * @param bounds The bounds to check.
	 * @return All objects in this {@link QuadTree} that intersect with the specified bounds.
	 */
	public final Set<T> findIntersects(Bounds bounds) {
		return findIntersects(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
	}

	/**
	 * @param x The horizontal coordinate to check.
	 * @param y The vertical coordinate to check.
	 * @param width The width of the space to check.
	 * @param height The height of the space to check.
	 * @return All objects in this {@link QuadTree} that intersect with the specified bounds.
	 */
	public final Set<T> findIntersects(int x, int y, int width, int height) {
		Set<T> result = new HashSet<>();
		mRoot.findIntersects(x, y, width, height, result);
		for (T one : mOutside) {
			if (one.intersectsBounds(x, y, width, height)) {
				result.add(one);
			}
		}
		return result;
	}

	/**
	 * @param bounds The bounds to check.
	 * @param matcher A {@link Matcher} to use to verify any potential matches.
	 * @return All objects in this {@link QuadTree} that intersect with the specified bounds and
	 *         pass the {@link Matcher}'s test.
	 */
	public final Set<T> findIntersects(Rectangle bounds, Matcher<T> matcher) {
		return findIntersects(bounds.x, bounds.y, bounds.width, bounds.height, matcher);
	}

	/**
	 * @param bounds The bounds to check.
	 * @param matcher A {@link Matcher} to use to verify any potential matches.
	 * @return All objects in this {@link QuadTree} that intersect with the specified bounds and
	 *         pass the {@link Matcher}'s test.
	 */
	public final Set<T> findIntersects(Bounds bounds, Matcher<T> matcher) {
		return findIntersects(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), matcher);
	}

	/**
	 * @param x The horizontal coordinate to check.
	 * @param y The vertical coordinate to check.
	 * @param width The width of the space to check.
	 * @param height The height of the space to check.
	 * @param matcher A {@link Matcher} to use to verify any potential matches.
	 * @return All objects in this {@link QuadTree} that intersect with the specified bounds and
	 *         pass the {@link Matcher}'s test.
	 */
	public final Set<T> findIntersects(int x, int y, int width, int height, Matcher<T> matcher) {
		Set<T> result = new HashSet<>();
		mRoot.findIntersects(x, y, width, height, result, matcher);
		for (T one : mOutside) {
			if (one.intersectsBounds(x, y, width, height) && matcher.matches(one)) {
				result.add(one);
			}
		}
		return result;
	}

	/**
	 * @param bounds The bounds to check.
	 * @return All objects in this {@link QuadTree} that would be contained by the specified bounds.
	 */
	public final Set<T> findContainedBy(Rectangle bounds) {
		return findContainedBy(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	/**
	 * @param bounds The bounds to check.
	 * @return All objects in this {@link QuadTree} that would be contained by the specified bounds.
	 */
	public final Set<T> findContainedBy(Bounds bounds) {
		return findContainedBy(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
	}

	/**
	 * @param x The horizontal coordinate to check.
	 * @param y The vertical coordinate to check.
	 * @param width The width of the space to check.
	 * @param height The height of the space to check.
	 * @return All objects in this {@link QuadTree} that would be contained by the specified bounds.
	 */
	public final Set<T> findContainedBy(int x, int y, int width, int height) {
		Set<T> result = new HashSet<>();
		mRoot.findContainedBy(x, y, width, height, result);
		for (T one : mOutside) {
			if (one.containedBy(x, y, width, height)) {
				result.add(one);
			}
		}
		return result;
	}

	/**
	 * @param bounds The bounds to check.
	 * @param matcher A {@link Matcher} to use to verify any potential matches.
	 * @return All objects in this {@link QuadTree} that would be contained by the specified bounds
	 *         and pass the {@link Matcher}'s test.
	 */
	public final Set<T> findContainedBy(Rectangle bounds, Matcher<T> matcher) {
		return findContainedBy(bounds.x, bounds.y, bounds.width, bounds.height, matcher);
	}

	/**
	 * @param bounds The bounds to check.
	 * @param matcher A {@link Matcher} to use to verify any potential matches.
	 * @return All objects in this {@link QuadTree} that would be contained by the specified bounds
	 *         and pass the {@link Matcher}'s test.
	 */
	public final Set<T> findContainedBy(Bounds bounds, Matcher<T> matcher) {
		return findContainedBy(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), matcher);
	}

	/**
	 * @param x The horizontal coordinate to check.
	 * @param y The vertical coordinate to check.
	 * @param width The width of the space to check.
	 * @param height The height of the space to check.
	 * @param matcher A {@link Matcher} to use to verify any potential matches.
	 * @return All objects in this {@link QuadTree} that would be contained by the specified bounds
	 *         and pass the {@link Matcher}'s test.
	 */
	public final Set<T> findContainedBy(int x, int y, int width, int height, Matcher<T> matcher) {
		Set<T> result = new HashSet<>();
		mRoot.findContainedBy(x, y, width, height, result, matcher);
		for (T one : mOutside) {
			if (one.containedBy(x, y, width, height) && matcher.matches(one)) {
				result.add(one);
			}
		}
		return result;
	}

	static class Node<T extends Bounds> implements Bounds {
		@Localize("Objects must have a width and height greater than zero.")
		@Localize(locale = "ru", value = "Объекты должны иметь ширину и высоту больше нуля.")
		@Localize(locale = "de", value = "Objekte müssen eine Höhe und Breite größer als Null haben.")
		@Localize(locale = "es", value = "El objeto debe tener anchura y altura mayor que cero.")
		private static String	MUST_HAVE_SIZE_GREATER_THAN_ZERO;
		private int				mX;
		private int				mY;
		private int				mWidth;
		private int				mHeight;
		protected Set<T>		mContents;
		private int				mMaxCapacity;
		private Node<T>			mNorthEast;
		private Node<T>			mNorthWest;
		private Node<T>			mSouthEast;
		private Node<T>			mSouthWest;

		static {
			Localization.initialize();
		}

		Node(int x, int y, int width, int height, int maxCapacity) {
			mX = x;
			mY = y;
			mWidth = width;
			mHeight = height;
			mMaxCapacity = maxCapacity;
			mContents = new HashSet<>();
		}

		final void zeroBounds() {
			mX = 0;
			mY = 0;
			mWidth = 0;
			mHeight = 0;
		}

		@Override
		public final int getX() {
			return mX;
		}

		@Override
		public final int getY() {
			return mY;
		}

		@Override
		public final int getWidth() {
			return mWidth;
		}

		@Override
		public final int getHeight() {
			return mHeight;
		}

		private final boolean isLeaf() {
			return mNorthEast == null;
		}

		final void remove(T obj) {
			if (!mContents.remove(obj) && !isLeaf() && intersectsBounds(obj)) {
				mNorthEast.remove(obj);
				mNorthWest.remove(obj);
				mSouthEast.remove(obj);
				mSouthWest.remove(obj);
			}
		}

		final void add(T obj) {
			if (obj.getWidth() < 1 || obj.getHeight() < 1) {
				throw new IllegalArgumentException(MUST_HAVE_SIZE_GREATER_THAN_ZERO);
			}
			// Do we have to split?
			if (isLeaf() && mContents.size() >= mMaxCapacity && mWidth > 1 && mHeight > 1) {
				split();
			}
			if (isLeaf() || obj.containsBounds(this)) {
				mContents.add(obj);
			} else {
				if (mNorthEast.intersectsBounds(obj)) {
					mNorthEast.add(obj);
				}
				if (mNorthWest.intersectsBounds(obj)) {
					mNorthWest.add(obj);
				}
				if (mSouthEast.intersectsBounds(obj)) {
					mSouthEast.add(obj);
				}
				if (mSouthWest.intersectsBounds(obj)) {
					mSouthWest.add(obj);
				}
			}
		}

		final void all(Set<T> result) {
			result.addAll(mContents);
			if (!isLeaf()) {
				mNorthEast.all(result);
				mNorthWest.all(result);
				mSouthEast.all(result);
				mSouthWest.all(result);
			}
		}

		final void all(Set<T> result, Matcher<T> matcher) {
			for (T one : mContents) {
				if (matcher.matches(one)) {
					result.add(one);
				}
			}
			if (!isLeaf()) {
				mNorthEast.all(result, matcher);
				mNorthWest.all(result, matcher);
				mSouthEast.all(result, matcher);
				mSouthWest.all(result, matcher);
			}
		}

		private final void split() {
			if (isLeaf()) {
				int hw = mWidth / 2;
				int hh = mHeight / 2;
				mNorthWest = new Node<>(mX, mY, hw, hh, mMaxCapacity);
				mNorthEast = new Node<>(mX + hw, mY, mWidth - hw, hh, mMaxCapacity);
				mSouthWest = new Node<>(mX, mY + hh, hw, mHeight - hh, mMaxCapacity);
				mSouthEast = new Node<>(mX + hw, mY + hh, mWidth - hw, mHeight - hh, mMaxCapacity);
				Set<T> temp = mContents;
				mContents = new HashSet<>();
				for (T one : temp) {
					add(one);
				}
			}
		}

		final boolean contains(int x, int y) {
			if (containsLocation(x, y)) {
				for (T one : mContents) {
					if (one.containsLocation(x, y)) {
						return true;
					}
				}
				if (!isLeaf()) {
					return mNorthWest.contains(x, y) || mNorthEast.contains(x, y) || mSouthWest.contains(x, y) || mSouthEast.contains(x, y);
				}
			}
			return false;
		}

		final boolean contains(int x, int y, Matcher<T> matcher) {
			if (containsLocation(x, y)) {
				for (T one : mContents) {
					if (one.containsLocation(x, y) && matcher.matches(one)) {
						return true;
					}
				}
				if (!isLeaf()) {
					return mNorthWest.contains(x, y, matcher) || mNorthEast.contains(x, y, matcher) || mSouthWest.contains(x, y, matcher) || mSouthEast.contains(x, y, matcher);
				}
			}
			return false;
		}

		final boolean intersects(int x, int y, int width, int height) {
			if (intersectsBounds(x, y, width, height)) {
				for (T one : mContents) {
					if (one.intersectsBounds(x, y, width, height)) {
						return true;
					}
				}
				if (!isLeaf()) {
					return mNorthWest.intersects(x, y, width, height) || mNorthEast.intersects(x, y, width, height) || mSouthWest.intersects(x, y, width, height) || mSouthEast.intersects(x, y, width, height);
				}
			}
			return false;
		}

		final boolean intersects(int x, int y, int width, int height, Matcher<T> matcher) {
			if (intersectsBounds(x, y, width, height)) {
				for (T one : mContents) {
					if (one.intersectsBounds(x, y, width, height) && matcher.matches(one)) {
						return true;
					}
				}
				if (!isLeaf()) {
					return mNorthWest.intersects(x, y, width, height, matcher) || mNorthEast.intersects(x, y, width, height, matcher) || mSouthWest.intersects(x, y, width, height, matcher) || mSouthEast.intersects(x, y, width, height, matcher);
				}
			}
			return false;
		}

		final boolean inside(int x, int y, int width, int height) {
			if (intersectsBounds(x, y, width, height)) {
				for (T one : mContents) {
					if (one.containedBy(x, y, width, height)) {
						return true;
					}
				}
				if (!isLeaf()) {
					return mNorthWest.inside(x, y, width, height) || mNorthEast.inside(x, y, width, height) || mSouthWest.inside(x, y, width, height) || mSouthEast.inside(x, y, width, height);
				}
			}
			return false;
		}

		final boolean inside(int x, int y, int width, int height, Matcher<T> matcher) {
			if (intersectsBounds(x, y, width, height)) {
				for (T one : mContents) {
					if (one.containedBy(x, y, width, height) && matcher.matches(one)) {
						return true;
					}
				}
				if (!isLeaf()) {
					return mNorthWest.inside(x, y, width, height, matcher) || mNorthEast.inside(x, y, width, height, matcher) || mSouthWest.inside(x, y, width, height, matcher) || mSouthEast.inside(x, y, width, height, matcher);
				}
			}
			return false;
		}

		final void findContains(int x, int y, Set<T> result) {
			if (containsLocation(x, y)) {
				for (T one : mContents) {
					if (one.containsLocation(x, y)) {
						result.add(one);
					}
				}
				if (!isLeaf()) {
					mNorthWest.findContains(x, y, result);
					mNorthEast.findContains(x, y, result);
					mSouthWest.findContains(x, y, result);
					mSouthEast.findContains(x, y, result);
				}
			}
		}

		final void findContains(int x, int y, Set<T> result, Matcher<T> matcher) {
			if (containsLocation(x, y)) {
				for (T one : mContents) {
					if (one.containsLocation(x, y) && matcher.matches(one)) {
						result.add(one);
					}
				}
				if (!isLeaf()) {
					mNorthWest.findContains(x, y, result, matcher);
					mNorthEast.findContains(x, y, result, matcher);
					mSouthWest.findContains(x, y, result, matcher);
					mSouthEast.findContains(x, y, result, matcher);
				}
			}
		}

		final void findIntersects(int x, int y, int width, int height, Set<T> result) {
			if (intersectsBounds(x, y, width, height)) {
				for (T one : mContents) {
					if (one.intersectsBounds(x, y, width, height)) {
						result.add(one);
					}
				}
				if (!isLeaf()) {
					mNorthWest.findIntersects(x, y, width, height, result);
					mNorthEast.findIntersects(x, y, width, height, result);
					mSouthWest.findIntersects(x, y, width, height, result);
					mSouthEast.findIntersects(x, y, width, height, result);
				}
			}
		}

		final void findIntersects(int x, int y, int width, int height, Set<T> result, Matcher<T> matcher) {
			if (intersectsBounds(x, y, width, height)) {
				for (T one : mContents) {
					if (one.intersectsBounds(x, y, width, height) && matcher.matches(one)) {
						result.add(one);
					}
				}
				if (!isLeaf()) {
					mNorthWest.findIntersects(x, y, width, height, result, matcher);
					mNorthEast.findIntersects(x, y, width, height, result, matcher);
					mSouthWest.findIntersects(x, y, width, height, result, matcher);
					mSouthEast.findIntersects(x, y, width, height, result, matcher);
				}
			}
		}

		final void findContainedBy(int x, int y, int width, int height, Set<T> result) {
			if (intersectsBounds(x, y, width, height)) {
				for (T one : mContents) {
					if (one.containedBy(x, y, width, height)) {
						result.add(one);
					}
				}
				if (!isLeaf()) {
					mNorthWest.findContainedBy(x, y, width, height, result);
					mNorthEast.findContainedBy(x, y, width, height, result);
					mSouthWest.findContainedBy(x, y, width, height, result);
					mSouthEast.findContainedBy(x, y, width, height, result);
				}
			}
		}

		final void findContainedBy(int x, int y, int width, int height, Set<T> result, Matcher<T> matcher) {
			if (intersectsBounds(x, y, width, height)) {
				for (T one : mContents) {
					if (one.containedBy(x, y, width, height) && matcher.matches(one)) {
						result.add(one);
					}
				}
				if (!isLeaf()) {
					mNorthWest.findContainedBy(x, y, width, height, result, matcher);
					mNorthEast.findContainedBy(x, y, width, height, result, matcher);
					mSouthWest.findContainedBy(x, y, width, height, result, matcher);
					mSouthEast.findContainedBy(x, y, width, height, result, matcher);
				}
			}
		}

		@SuppressWarnings("nls")
		@Override
		public String toString() {
			return mX + "," + mY + "," + mWidth + "," + mHeight;
		}
	}
}
