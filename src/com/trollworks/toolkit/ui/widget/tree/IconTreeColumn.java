/*
 * Copyright (c) 1998-2018 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */
package com.trollworks.toolkit.ui.widget.tree;

import com.trollworks.toolkit.ui.image.StdImage;

import java.awt.Graphics2D;
import java.awt.Point;

public class IconTreeColumn extends TreeColumn {
    public static final int HMARGIN = 2;
    public static final int VMARGIN = 1;
    private IconInteractor  mIconInteractor;

    public IconTreeColumn(String name, IconInteractor iconInteractor) {
        super(name);
        mIconInteractor = iconInteractor;
    }

    protected StdImage getIcon(TreeRow row) {
        return mIconInteractor != null ? mIconInteractor.getIcon(row) : null;
    }

    @Override
    public int compare(TreeRow o1, TreeRow o2) {
        return 0;
    }

    @Override
    public int calculatePreferredWidth(TreeRow row) {
        StdImage icon = getIcon(row);
        return HMARGIN + (icon != null ? icon.getWidth() : 0) + HMARGIN;
    }

    @Override
    public int calculatePreferredHeight(TreeRow row, int width) {
        StdImage icon = getIcon(row);
        return VMARGIN + (icon != null ? icon.getHeight() : 0) + VMARGIN;
    }

    @Override
    public void draw(Graphics2D gc, TreePanel panel, TreeRow row, int position, int top, int left, int width, boolean selected, boolean active) {
        StdImage icon = getIcon(row);
        if (icon != null) {
            gc.drawImage(icon, left + (width - icon.getWidth()) / 2, top + (panel.getRowHeight(row) - icon.getHeight()) / 2, null);
        }
    }

    @Override
    public boolean mousePress(TreeRow row, Point where) {
        if (mIconInteractor != null) {
            return mIconInteractor.mousePress(row, this, where);
        }
        return false;
    }
}
