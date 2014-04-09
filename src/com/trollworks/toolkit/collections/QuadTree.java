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
		if (mRoot.intersects(bounds)) {
			return true;
		}
		for (T one : mOutside) {
			if (one.intersectsBounds(bounds)) {
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
		if (mRoot.intersects(bounds, matcher)) {
			return true;
		}
		for (T one : mOutside) {
			if (one.intersectsBounds(bounds) && matcher.matches(one)) {
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
		if (mRoot.inside(bounds)) {
			return true;
		}
		for (T one : mOutside) {
			if (one.containedBy(bounds)) {
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
		if (mRoot.inside(bounds, matcher)) {
			return true;
		}
		for (T one : mOutside) {
			if (one.containedBy(bounds) && matcher.matches(one)) {
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
		Set<T> result = new HashSet<>();
		mRoot.findIntersects(bounds, result);
		for (T one : mOutside) {
			if (one.intersectsBounds(bounds)) {
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
		Set<T> result = new HashSet<>();
		mRoot.findIntersects(bounds, result, matcher);
		for (T one : mOutside) {
			if (one.intersectsBounds(bounds) && matcher.matches(one)) {
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
		Set<T> result = new HashSet<>();
		mRoot.findContainedBy(bounds, result);
		for (T one : mOutside) {
			if (one.containedBy(bounds)) {
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
		Set<T> result = new HashSet<>();
		mRoot.findContainedBy(bounds, result, matcher);
		for (T one : mOutside) {
			if (one.containedBy(bounds) && matcher.matches(one)) {
				result.add(one);
			}
		}
		return result;
	}

	static class Node<T extends Bounds> implements Bounds {
		private int			mX;
		private int			mY;
		private int			mWidth;
		private int			mHeight;
		protected Set<T>	mContents;
		private int			mMaxCapacity;
		private Node<T>		mNorthEast;
		private Node<T>		mNorthWest;
		private Node<T>		mSouthEast;
		private Node<T>		mSouthWest;

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

		final boolean intersects(Rectangle bounds) {
			if (intersectsBounds(bounds)) {
				for (T one : mContents) {
					if (one.intersectsBounds(bounds)) {
						return true;
					}
				}
				if (!isLeaf()) {
					return mNorthWest.intersects(bounds) || mNorthEast.intersects(bounds) || mSouthWest.intersects(bounds) || mSouthEast.intersects(bounds);
				}
			}
			return false;
		}

		final boolean intersects(Rectangle bounds, Matcher<T> matcher) {
			if (intersectsBounds(bounds)) {
				for (T one : mContents) {
					if (one.intersectsBounds(bounds) && matcher.matches(one)) {
						return true;
					}
				}
				if (!isLeaf()) {
					return mNorthWest.intersects(bounds, matcher) || mNorthEast.intersects(bounds, matcher) || mSouthWest.intersects(bounds, matcher) || mSouthEast.intersects(bounds, matcher);
				}
			}
			return false;
		}

		final boolean inside(Rectangle bounds) {
			if (intersectsBounds(bounds)) {
				for (T one : mContents) {
					if (one.containedBy(bounds)) {
						return true;
					}
				}
				if (!isLeaf()) {
					return mNorthWest.inside(bounds) || mNorthEast.inside(bounds) || mSouthWest.inside(bounds) || mSouthEast.inside(bounds);
				}
			}
			return false;
		}

		final boolean inside(Rectangle bounds, Matcher<T> matcher) {
			if (intersectsBounds(bounds)) {
				for (T one : mContents) {
					if (one.containedBy(bounds) && matcher.matches(one)) {
						return true;
					}
				}
				if (!isLeaf()) {
					return mNorthWest.inside(bounds, matcher) || mNorthEast.inside(bounds, matcher) || mSouthWest.inside(bounds, matcher) || mSouthEast.inside(bounds, matcher);
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

		final void findIntersects(Rectangle bounds, Set<T> result) {
			if (intersectsBounds(bounds)) {
				for (T one : mContents) {
					if (one.intersectsBounds(bounds)) {
						result.add(one);
					}
				}
				if (!isLeaf()) {
					mNorthWest.findIntersects(bounds, result);
					mNorthEast.findIntersects(bounds, result);
					mSouthWest.findIntersects(bounds, result);
					mSouthEast.findIntersects(bounds, result);
				}
			}
		}

		final void findIntersects(Rectangle bounds, Set<T> result, Matcher<T> matcher) {
			if (intersectsBounds(bounds)) {
				for (T one : mContents) {
					if (one.intersectsBounds(bounds) && matcher.matches(one)) {
						result.add(one);
					}
				}
				if (!isLeaf()) {
					mNorthWest.findIntersects(bounds, result, matcher);
					mNorthEast.findIntersects(bounds, result, matcher);
					mSouthWest.findIntersects(bounds, result, matcher);
					mSouthEast.findIntersects(bounds, result, matcher);
				}
			}
		}

		final void findContainedBy(Rectangle bounds, Set<T> result) {
			if (intersectsBounds(bounds)) {
				for (T one : mContents) {
					if (one.containedBy(bounds)) {
						result.add(one);
					}
				}
				if (!isLeaf()) {
					mNorthWest.findContainedBy(bounds, result);
					mNorthEast.findContainedBy(bounds, result);
					mSouthWest.findContainedBy(bounds, result);
					mSouthEast.findContainedBy(bounds, result);
				}
			}
		}

		final void findContainedBy(Rectangle bounds, Set<T> result, Matcher<T> matcher) {
			if (intersectsBounds(bounds)) {
				for (T one : mContents) {
					if (one.containedBy(bounds) && matcher.matches(one)) {
						result.add(one);
					}
				}
				if (!isLeaf()) {
					mNorthWest.findContainedBy(bounds, result, matcher);
					mNorthEast.findContainedBy(bounds, result, matcher);
					mSouthWest.findContainedBy(bounds, result, matcher);
					mSouthEast.findContainedBy(bounds, result, matcher);
				}
			}
		}
	}
}
