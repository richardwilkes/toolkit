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
import java.util.ArrayList;
import java.util.HashSet;

class AreaNode implements AreaObject {
	private static final int	MAX_PER_NODE	= 4;
	private AreaNode			mParent;
	private boolean				mLeafNode;
	private Rectangle			mBounds;
	private int					mStorageCount;
	private AreaObject[]		mStorage;

	/** Creates a leaf node with no parent and a default bounds of (0,0,0,0). */
	public AreaNode() {
		this(null, true);
	}

	/**
	 * Creates a node with a default bounds of (0,0,0,0).
	 *
	 * @param parent The parent of this node.
	 * @param leafNode <code>true</code> if this should be a leaf node, <code>false</code> if not.
	 */
	protected AreaNode(AreaNode parent, boolean leafNode) {
		mParent = parent;
		mLeafNode = leafNode;
		mBounds = new Rectangle(0, 0, 0, 0);
		mStorageCount = 0;
		mStorage = new AreaObject[MAX_PER_NODE + 1];
	}

	private void addLeavesToList(ArrayList<AreaObject> list) {
		for (int i = 0; i < mStorageCount; i++) {
			if (mLeafNode) {
				list.add(mStorage[i]);
			} else {
				((AreaNode) mStorage[i]).addLeavesToList(list);
			}
			mStorage[i] = null;
		}
		mStorageCount = 0;
		mLeafNode = true;
	}

	private void adjustBounds() {
		if (mStorageCount > 0) {
			Rectangle bounds = mStorage[0].getBounds();

			mBounds.x = bounds.x;
			mBounds.y = bounds.y;
			mBounds.width = bounds.width;
			mBounds.height = bounds.height;
			for (int i = 1; i < mStorageCount; i++) {
				mBounds.add(mStorage[i].getBounds());
			}
		} else {
			mBounds.x = 0;
			mBounds.y = 0;
			mBounds.width = 0;
			mBounds.height = 0;
		}
	}

	/**
	 * @param obj The object to look for.
	 * @return <code>true</code> if the tree from this point down currently contains the object.
	 */
	protected boolean contains(AreaObject obj) {
		return findLeaf(obj) != null;
	}

	private AreaNode findLeaf(AreaObject objToFind) {
		Rectangle bounds = objToFind.getBounds();

		for (int i = 0; i < mStorageCount; i++) {
			if (bounds.intersects(mStorage[i].getBounds())) {
				if (!mLeafNode) {
					AreaNode result = ((AreaNode) mStorage[i]).findLeaf(objToFind);

					if (result != null) {
						return result;
					}
				} else if (mStorage[i] == objToFind) {
					return this;
				}
			}
		}

		return null;
	}

	@Override
	public Rectangle getBounds() {
		return mBounds;
	}

	/**
	 * Inserts an object into the correct place within the tree starting at this node.
	 *
	 * @param obj The object to add. May not be <code>null</code>.
	 * @return The root node for where the change occurred.
	 */
	protected AreaNode insert(AreaObject obj) {
		AreaNode leaf = this;
		AreaNode split = null;
		AreaNode root = this;
		Rectangle bounds = obj.getBounds();

		// Choose the correct leaf node
		while (true) {
			long growth;
			AreaNode oldNode;

			if (leaf.mLeafNode) {
				break;
			}

			growth = Long.MAX_VALUE;
			oldNode = leaf;

			for (int i = 0; i < oldNode.mStorageCount; i++) {
				AreaNode tmpNode = (AreaNode) oldNode.mStorage[i];
				Rectangle rectToGrow = tmpNode.getBounds();
				Rectangle union = rectToGrow.union(bounds);
				long growthAmt = (long) union.height * (long) union.width - (long) rectToGrow.height * (long) rectToGrow.width;

				if (growthAmt <= growth) {
					growth = growthAmt;
					leaf = tmpNode;
				}
			}
		}

		// Add the new object in
		leaf.mStorage[leaf.mStorageCount++] = obj;
		if (leaf.mStorageCount > MAX_PER_NODE) {
			split = splitNode(leaf);
		}

		// Adjust the tree
		while (leaf != null) {
			leaf.adjustBounds();
			leaf = leaf.mParent;
			if (split != null) {
				split.adjustBounds();
				if (leaf != null) {
					leaf.mStorage[leaf.mStorageCount++] = split;
					split.mParent = leaf;
					if (leaf.mStorageCount > MAX_PER_NODE) {
						split = splitNode(leaf);
					} else {
						split = null;
					}
				}
			}
		}

		if (split != null) {
			root = new AreaNode(null, false);
			mParent = root;
			split.mParent = root;
			root.mStorage[root.mStorageCount++] = this;
			root.mStorage[root.mStorageCount++] = split;
			root.adjustBounds();
		}

		return root;
	}

	/**
	 * Removes an object from the tree starting at this node. If the object is not present, nothing
	 * occurs.
	 *
	 * @param obj The object to remove. May not be <code>null</code>.
	 * @return The root node for where the change occurred.
	 */
	protected AreaNode remove(AreaObject obj) {
		AreaNode leafNode = findLeaf(obj);
		AreaNode root = this;

		if (leafNode != null) {
			ArrayList<AreaObject> savedLeaves = new ArrayList<>();
			int size;

			leafNode.mStorageCount = removeFromArray(leafNode.mStorageCount, leafNode.mStorage, obj);

			// Condense the tree
			while (leafNode.mParent != null) {
				AreaNode parentNode = leafNode.mParent;

				if (leafNode.mStorageCount < MAX_PER_NODE / 2) {
					leafNode.addLeavesToList(savedLeaves);
					parentNode.mStorageCount = removeFromArray(parentNode.mStorageCount, parentNode.mStorage, leafNode);
					if (parentNode.mStorageCount == 0) {
						parentNode.mLeafNode = true;
					}
				} else {
					leafNode.adjustBounds();
				}
				leafNode = parentNode;
			}

			size = savedLeaves.size();
			for (int i = 0; i < size; i++) {
				root = root.insert(savedLeaves.get(i));
			}

			if (!root.mLeafNode && root.mStorageCount == 1) {
				root = (AreaNode) root.mStorage[0];
				root.mParent = null;
			}
		}

		return root;
	}

	/**
	 * Removes a list of objects from the tree starting at this node. Any objects within the list
	 * which are not in the tree are ignored.
	 *
	 * @param list The list of objects to remove. May not be <code>null</code>.
	 * @return The root node for where the change occurred.
	 */
	protected AreaNode remove(ArrayList<AreaObject> list) {
		AreaNode root = this;
		HashSet<AreaNode> leavesVisited = new HashSet<>();
		ArrayList<AreaObject> savedLeaves = new ArrayList<>();

		// Remove all the objects in the list from the tree, making a note
		// of which leaf nodes are affected by this.
		for (AreaObject bounds : list) {
			AreaNode leafNode = findLeaf(bounds);

			if (leafNode != null) {
				leavesVisited.add(leafNode);
				leafNode.mStorageCount = removeFromArray(leafNode.mStorageCount, leafNode.mStorage, bounds);
			}
		}

		// Condense the tree by visiting each leaf node that was affected, moving
		// its data into the savedLeaves list if the leaf is now too small.
		for (AreaNode leaf : leavesVisited) {
			while (leaf.mParent != null) {
				AreaNode parentNode = leaf.mParent;

				if (leaf.mStorageCount < MAX_PER_NODE / 2) {
					leaf.addLeavesToList(savedLeaves);
					parentNode.mStorageCount = removeFromArray(parentNode.mStorageCount, parentNode.mStorage, leaf);
					if (parentNode.mStorageCount == 0) {
						parentNode.mLeafNode = true;
					}
					leaf.mParent = null;
				} else {
					leaf.adjustBounds();
				}
				leaf = parentNode;
			}
		}

		// Re-insert any saved leaves
		for (AreaObject savedLeaf : savedLeaves) {
			root = root.insert(savedLeaf);
		}

		if (!root.mLeafNode && root.mStorageCount == 1) {
			root = (AreaNode) root.mStorage[0];
			root.mParent = null;
		}

		return root;
	}

	private static int removeFromArray(int count, AreaObject[] array, AreaObject obj) {
		for (int i = 0; i < count; i++) {
			if (array[i] == obj) {
				if (i != --count) {
					array[i] = array[count];
				}
				array[count] = null;
				break;
			}
		}
		return count;
	}

	/**
	 * If <code>exactMatch</code> is <code>true</code>, appends only those objects that have the
	 * exact same coordinates as <code>bounds</code> to <code>result</code>, otherwise, appends all
	 * objects that intersect with <code>bounds</code>.
	 *
	 * @param bounds The bounds to search with.
	 * @param result The list to add matches to. May not be <code>null</code>.
	 * @param exactMatch <code>true</code> to match coordinates exactly, <code>false</code> to only
	 *            require an intersection.
	 */
	protected void search(Rectangle bounds, ArrayList<AreaObject> result, boolean exactMatch) {
		for (int i = 0; i < mStorageCount; i++) {
			Rectangle nodeBounds = mStorage[i].getBounds();
			if (bounds == null || bounds.intersects(nodeBounds)) {
				if (!mLeafNode) {
					((AreaNode) mStorage[i]).search(bounds, result, exactMatch);
				} else if (bounds == null || !exactMatch || bounds.equals(nodeBounds)) {
					result.add(mStorage[i]);
				}
			}
		}
	}

	/**
	 * Appends all objects that intersect with <code>bounds</code> and are instances of the target
	 * class.
	 *
	 * @param bounds The bounds to search with.
	 * @param result The list to add matches to. May not be <code>null</code>.
	 * @param targetClass The class of object for which we're looking
	 */
	protected void search(Rectangle bounds, ArrayList<AreaObject> result, Class<? extends AreaObject> targetClass) {
		for (int i = 0; i < mStorageCount; i++) {
			Rectangle nodeBounds = mStorage[i].getBounds();
			if (bounds == null || bounds.intersects(nodeBounds)) {
				if (!mLeafNode) {
					((AreaNode) mStorage[i]).search(bounds, result, targetClass);
				} else if (targetClass.isInstance(mStorage[i])) {
					result.add(mStorage[i]);
				}
			}
		}
	}

	/**
	 * Appends all objects that intersect with the <code>location</code> to <code>result</code>.
	 *
	 * @param location The location to search with. May not be <code>null</code>.
	 * @param result The list to add matches to. May not be <code>null</code>.
	 */
	protected void search(Point location, ArrayList<AreaObject> result) {
		for (int i = 0; i < mStorageCount; i++) {
			if (mStorage[i].getBounds().contains(location)) {
				if (!mLeafNode) {
					((AreaNode) mStorage[i]).search(location, result);
				} else {
					result.add(mStorage[i]);
				}
			}
		}
	}

	/**
	 * Appends all objects that intersect with the <code>location</code> and are instances of the
	 * target class to <code>result</code>.
	 *
	 * @param location The location to search with. May not be <code>null</code>.
	 * @param result The list to add matches to. May not be <code>null</code>.
	 * @param targetClass The class of object for which we're looking
	 */
	protected void search(Point location, ArrayList<AreaObject> result, Class<? extends AreaObject> targetClass) {
		for (int i = 0; i < mStorageCount; i++) {
			if (mStorage[i].getBounds().contains(location)) {
				if (!mLeafNode) {
					((AreaNode) mStorage[i]).search(location, result);
				} else if (targetClass.isInstance(mStorage[i])) {
					result.add(mStorage[i]);
				}
			}
		}
	}

	/**
	 * @param location The location to search with. May not be <code>null</code>.
	 * @return <code>true</code> if there are any objects that intersect with <code>location</code>.
	 */
	protected boolean searchHit(Point location) {
		for (int i = 0; i < mStorageCount; i++) {
			if (mStorage[i].getBounds().contains(location)) {
				if (mLeafNode || ((AreaNode) mStorage[i]).searchHit(location)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param bounds The bounds to search with.
	 * @return <code>true</code> if there are any objects that intersect with <code>bounds</code>.
	 */
	protected boolean searchHit(Rectangle bounds) {
		for (int i = 0; i < mStorageCount; i++) {
			if (bounds.intersects(mStorage[i].getBounds())) {
				if (mLeafNode || ((AreaNode) mStorage[i]).searchHit(bounds)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param bounds The bounds to search with.
	 * @param targetClass The class of object for which we're looking
	 * @return <code>true</code> if there are any objects of the target class that intersect with
	 *         <code>bounds</code>.
	 */
	protected boolean searchHit(Rectangle bounds, Class<? extends AreaObject> targetClass) {
		for (int i = 0; i < mStorageCount; i++) {
			if (bounds.intersects(mStorage[i].getBounds())) {
				if (mLeafNode) {
					if (targetClass.isInstance(mStorage[i])) {
						return true;
					}
				} else if (((AreaNode) mStorage[i]).searchHit(bounds, targetClass)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param bounds The bounds to search with.
	 * @param exactMatch <code>true</code> to match coordinates exactly, <code>false</code> to only
	 *            require an intersection.
	 * @return The number of objects that intersect with <code>bounds</code>. If
	 *         <code>exactMatch</code> is <code>true</code>, then only those objects that have the
	 *         exact same coordinates as <code>bounds</code> are counted.
	 */
	protected int searchCount(Rectangle bounds, boolean exactMatch) {
		int count = 0;
		for (int i = 0; i < mStorageCount; i++) {
			Rectangle nodeBounds = mStorage[i].getBounds();
			if (bounds == null || bounds.intersects(nodeBounds)) {
				if (!mLeafNode) {
					count += ((AreaNode) mStorage[i]).searchCount(bounds, exactMatch);
				} else if (bounds == null || !exactMatch || bounds.equals(nodeBounds)) {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * @param location The location to search with. May not be <code>null</code>.
	 * @return The number of objects that intersect with the <code>location</code>.
	 */
	protected int searchCount(Point location) {
		int count = 0;

		for (int i = 0; i < mStorageCount; i++) {
			if (mStorage[i].getBounds().contains(location)) {
				if (!mLeafNode) {
					count += ((AreaNode) mStorage[i]).searchCount(location);
				} else {
					count++;
				}
			}
		}
		return count;
	}

	@SuppressWarnings("null")
	private static AreaNode splitNode(AreaNode node) {
		AreaObject oldStorage[] = new AreaObject[MAX_PER_NODE + 1];
		int oldStorageCount = node.mStorageCount;
		int largestWasted = -1;
		AreaObject firstShape = null;
		AreaObject secondShape = null;
		boolean addToFirst = true;
		AreaNode split;
		int i;
		Rectangle bounds;

		if (oldStorageCount > 0) {
			System.arraycopy(node.mStorage, 0, oldStorage, 0, node.mStorageCount);
		}

		split = new AreaNode(node.mParent, node.mLeafNode);

		// Find the two rectangles that are the most wasteful when paired together
		for (i = 0; i < oldStorageCount - 1; i++) {
			for (int j = i + 1; j < oldStorageCount; j++) {
				int wasted = areaWasted(oldStorage[i].getBounds(), oldStorage[j].getBounds());
				if (wasted >= largestWasted) {
					largestWasted = wasted;
					firstShape = oldStorage[i];
					secondShape = oldStorage[j];
				}
			}
		}

		while (node.mStorageCount > 0) {
			node.mStorage[--node.mStorageCount] = null;
		}
		node.mStorage[node.mStorageCount++] = firstShape;
		bounds = firstShape.getBounds();
		node.mBounds.x = bounds.x;
		node.mBounds.y = bounds.y;
		node.mBounds.width = bounds.width;
		node.mBounds.height = bounds.height;

		oldStorageCount = removeFromArray(oldStorageCount, oldStorage, firstShape);

		if (!node.mLeafNode) {
			((AreaNode) firstShape).mParent = node;
		}

		split.mStorage[split.mStorageCount++] = secondShape;

		oldStorageCount = removeFromArray(oldStorageCount, oldStorage, secondShape);
		bounds = secondShape.getBounds();
		split.mBounds.x = bounds.x;
		split.mBounds.y = bounds.y;
		split.mBounds.width = bounds.width;
		split.mBounds.height = bounds.height;
		if (!split.mLeafNode) {
			((AreaNode) secondShape).mParent = split;
		}

		while (oldStorageCount > 0) {
			if (addToFirst) {
				oldStorageCount = node.transferFromLargestOverlap(oldStorageCount, oldStorage);
				addToFirst = false;
			} else {
				oldStorageCount = split.transferFromLargestOverlap(oldStorageCount, oldStorage);
				addToFirst = true;
			}
		}

		return split;
	}

	private int transferFromLargestOverlap(int count, AreaObject[] array) {
		int smallestWasted = Integer.MAX_VALUE;
		int bestPick = -1;

		for (int i = 0; i < count; i++) {
			int wasted = areaWasted(mBounds, array[i].getBounds());
			if (wasted <= smallestWasted) {
				smallestWasted = wasted;
				bestPick = i;
			}
		}

		mBounds.union(array[bestPick].getBounds());
		mStorage[mStorageCount++] = array[bestPick];
		if (!mLeafNode) {
			((AreaNode) array[bestPick]).mParent = this;
		}

		if (bestPick != --count) {
			array[bestPick] = array[count];
		}
		array[count] = null;
		return count;
	}

	/**
	 * Returns a value measuring the area that would be wasted if the two bounds were merged into
	 * one.
	 *
	 * @param first The first bounds.
	 * @param second The second bounds.
	 * @return A value representing the percentage area wasted.
	 */
	private static int areaWasted(Rectangle first, Rectangle second) {
		if (first.width > 0 && first.height > 0 && second.width > 0 && second.height > 0) {
			long combinedArea = (long) first.width * (long) first.height + (long) second.width * (long) second.height;
			Rectangle union = first.union(second);
			long unionArea = (long) union.height * (long) union.width;
			Rectangle overlap = first.intersection(second);
			long overlapArea = (long) overlap.height * (long) overlap.width;
			return (int) ((unionArea - (combinedArea - overlapArea)) * 100L / combinedArea);
		}
		return Integer.MAX_VALUE / 2;
	}
}
