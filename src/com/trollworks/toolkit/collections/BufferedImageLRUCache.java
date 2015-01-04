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

import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * Provides a {@link LRUCache} for {@link BufferedImage}s, flushing their contents when removed from
 * the cache.
 */
public class BufferedImageLRUCache<K> extends LRUCache<K, BufferedImage> {
	/**
	 * Creates a new {@link BufferedImageLRUCache}.
	 *
	 * @param maxEntries The maximum number of entries to be in the cache. Older entries are removed
	 *            first.
	 */
	public BufferedImageLRUCache(int maxEntries) {
		super(maxEntries);
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, BufferedImage> eldest) {
		if (super.removeEldestEntry(eldest)) {
			eldest.getValue().flush();
			return true;
		}
		return false;
	}
}
