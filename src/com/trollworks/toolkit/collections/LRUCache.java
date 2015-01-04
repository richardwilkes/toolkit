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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of a least recently used cache. As new entries are added older entries are
 * removed. Calling get() or put() will refresh the entry. Calling containsKey() will not.
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
	private final int	mMaxEntries;

	/**
	 * Creates a new {@link LRUCache}.
	 *
	 * @param maxEntries The maximum number of entries to be in the cache. Older entries are removed
	 *            first.
	 */
	public LRUCache(int maxEntries) {
		super(16, 0.75f, true);
		mMaxEntries = maxEntries;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > mMaxEntries;
	}
}
