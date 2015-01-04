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

package com.trollworks.toolkit.ui.layout;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;

/** A row within a {@link FlexLayout}. */
public class FlexRow extends FlexContainer {
	@Override
	protected void layoutSelf(Rectangle bounds) {
		int count = getChildCount();
		int hGap = getHorizontalGap();
		int[] gaps = new int[count > 0 ? count - 1 : 0];
		for (int i = 0; i < gaps.length; i++) {
			gaps[i] = hGap;
		}
		int width = hGap * (count > 0 ? count - 1 : 0);
		Dimension[] minSizes = getChildSizes(LayoutSize.MINIMUM);
		Dimension[] prefSizes = getChildSizes(LayoutSize.PREFERRED);
		Dimension[] maxSizes = getChildSizes(LayoutSize.MAXIMUM);
		for (int i = 0; i < count; i++) {
			width += prefSizes[i].width;
		}
		int extra = bounds.width - width;
		if (extra != 0) {
			int[] values = new int[count];
			int[] limits = new int[count];
			for (int i = 0; i < count; i++) {
				values[i] = prefSizes[i].width;
				limits[i] = extra > 0 ? maxSizes[i].width : minSizes[i].width;
			}
			extra = distribute(extra, values, limits);
			for (int i = 0; i < count; i++) {
				prefSizes[i].width = values[i];
			}
			if (extra > 0 && getFillHorizontal() && gaps.length > 0) {
				int amt = extra / gaps.length;
				for (int i = 0; i < gaps.length; i++) {
					gaps[i] += amt;
				}
				extra -= amt * gaps.length;
				for (int i = 0; i < extra; i++) {
					gaps[i]++;
				}
				extra = 0;
			}
		}
		ArrayList<FlexCell> children = getChildren();
		Rectangle[] childBounds = new Rectangle[count];
		for (int i = 0; i < count; i++) {
			childBounds[i] = new Rectangle(prefSizes[i]);
			if (getFillVertical()) {
				childBounds[i].height = Math.min(maxSizes[i].height, bounds.height);
			}
			switch (children.get(i).getVerticalAlignment()) {
				case LEFT_TOP:
				default:
					childBounds[i].y = bounds.y;
					break;
				case CENTER:
					childBounds[i].y = bounds.y + (bounds.height - childBounds[i].height) / 2;
					break;
				case RIGHT_BOTTOM:
					childBounds[i].y = bounds.y + bounds.height - childBounds[i].height;
					break;
			}
		}
		int x = bounds.x;
		Alignment hAlign = getHorizontalAlignment();
		if (hAlign == Alignment.CENTER) {
			x += extra / 2;
		} else if (hAlign == Alignment.RIGHT_BOTTOM) {
			x += extra;
		}
		for (int i = 0; i < count; i++) {
			childBounds[i].x = x;
			if (i < count - 1) {
				x += prefSizes[i].width;
				x += gaps[i];
			}
		}
		layoutChildren(childBounds);
	}

	@Override
	protected Dimension getSizeSelf(LayoutSize type) {
		Dimension[] sizes = getChildSizes(type);
		Dimension size = new Dimension(getHorizontalGap() * (sizes.length > 0 ? sizes.length - 1 : 0), 0);
		for (Dimension one : sizes) {
			size.width += one.width;
			if (one.height > size.height) {
				size.height = one.height;
			}
		}
		return size;
	}
}
