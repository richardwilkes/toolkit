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

/** A column within a {@link FlexLayout}. */
public class FlexColumn extends FlexContainer {
	/** Creates a new {@link FlexColumn}. */
	public FlexColumn() {
		setFill(true);
	}

	@Override
	protected void layoutSelf(Rectangle bounds) {
		int count = getChildCount();
		int vGap = getVerticalGap();
		int[] gaps = new int[count > 0 ? count - 1 : 0];
		for (int i = 0; i < gaps.length; i++) {
			gaps[i] = vGap;
		}
		int height = vGap * (count > 0 ? count - 1 : 0);
		Dimension[] minSizes = getChildSizes(LayoutSize.MINIMUM);
		Dimension[] prefSizes = getChildSizes(LayoutSize.PREFERRED);
		Dimension[] maxSizes = getChildSizes(LayoutSize.MAXIMUM);
		for (int i = 0; i < count; i++) {
			height += prefSizes[i].height;
		}
		int extra = bounds.height - height;
		if (extra != 0) {
			int[] values = new int[count];
			int[] limits = new int[count];
			for (int i = 0; i < count; i++) {
				values[i] = prefSizes[i].height;
				limits[i] = extra > 0 ? maxSizes[i].height : minSizes[i].height;
			}
			extra = distribute(extra, values, limits);
			for (int i = 0; i < count; i++) {
				prefSizes[i].height = values[i];
			}
			if (extra > 0 && getFillVertical() && gaps.length > 0) {
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
			if (getFillHorizontal()) {
				childBounds[i].width = Math.min(maxSizes[i].width, bounds.width);
			}
			switch (children.get(i).getHorizontalAlignment()) {
				case LEFT_TOP:
				default:
					childBounds[i].x = bounds.x;
					break;
				case CENTER:
					childBounds[i].x = bounds.x + (bounds.width - childBounds[i].width) / 2;
					break;
				case RIGHT_BOTTOM:
					childBounds[i].x = bounds.x + bounds.width - childBounds[i].width;
					break;
			}
		}
		int y = bounds.y;
		Alignment vAlign = getVerticalAlignment();
		if (vAlign == Alignment.CENTER) {
			y += extra / 2;
		} else if (vAlign == Alignment.RIGHT_BOTTOM) {
			y += extra;
		}
		for (int i = 0; i < count; i++) {
			childBounds[i].y = y;
			if (i < count - 1) {
				y += prefSizes[i].height;
				y += gaps[i];
			}
		}
		layoutChildren(childBounds);
	}

	@Override
	protected Dimension getSizeSelf(LayoutSize type) {
		Dimension[] sizes = getChildSizes(type);
		Dimension size = new Dimension(0, getVerticalGap() * (sizes.length > 0 ? sizes.length - 1 : 0));
		for (Dimension one : sizes) {
			size.height += one.height;
			if (one.width > size.width) {
				size.width = one.width;
			}
		}
		return size;
	}
}
