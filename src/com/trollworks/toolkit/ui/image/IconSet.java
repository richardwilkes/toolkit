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

package com.trollworks.toolkit.ui.image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Provides a set of icons at different resolutions. */
public class IconSet {
	private static final int[]			STD_SIZES	= { 1024, 512, 256, 128, 64, 48, 32, 16 };
	private static Map<String, IconSet>	SETS		= new HashMap<>();
	private static int					SEQUENCE	= 0;
	private List<ToolkitIcon>			mIcons;
	private int							mSequence;

	/**
	 * @param name The prefix used for the icons in the set.
	 * @return A list containing the icons found for the set, starting with the largest and going to
	 *         the smallest.
	 */
	public static final IconSet get(String name) {
		IconSet iconSet = SETS.get(name);
		if (iconSet == null) {
			iconSet = new IconSet(name);
			SETS.put(name, iconSet);
		}
		return iconSet;
	}

	private IconSet(String name) {
		updateSequence();
		mIcons = new ArrayList<>();
		for (int i : STD_SIZES) {
			ToolkitIcon icon = Images.get(name + "_" + i); //$NON-NLS-1$
			if (icon != null) {
				mIcons.add(icon);
			}
		}
	}

	/**
	 * @param size The desired size of the icon. This method assumes square icons.
	 * @return The icon to use out of the set. If an exact match cannot be found, one of the
	 *         existing icons will be scaled to the desired size. <code>null</code> will be returned
	 *         if the original set contains no icons at all, however.
	 */
	public ToolkitIcon getIcon(int size) {
		int count = mIcons.size();
		if (count == 0 || size < 1) {
			return null;
		}
		for (int i = 0; i < count; i++) {
			ToolkitIcon icon = mIcons.get(i);
			int width = icon.getWidth();
			if (width == size) {
				return icon;
			}
			if (width < size) {
				icon = Images.scale(i > 0 ? mIcons.get(i - 1) : icon, size, size);
				mIcons.add(i, icon);
				updateSequence();
				return icon;
			}
		}
		ToolkitIcon icon = Images.scale(mIcons.get(count - 1), size, size);
		mIcons.add(icon);
		return icon;
	}

	/** @return A list containing the icons. */
	public List<ToolkitIcon> toList() {
		return new ArrayList<>(mIcons);
	}

	/**
	 * @return The current sequence number of this {@link IconSet}. This can be used to determine if
	 *         the {@link IconSet} is the same as the last time you used it. These are unique across
	 *         all {@link IconSet}s.
	 */
	public synchronized int getSequence() {
		return mSequence;
	}

	private synchronized void updateSequence() {
		mSequence = ++SEQUENCE;
	}
}
