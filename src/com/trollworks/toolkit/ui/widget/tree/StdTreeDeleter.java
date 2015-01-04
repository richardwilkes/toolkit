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

package com.trollworks.toolkit.ui.widget.tree;

import com.trollworks.toolkit.ui.menu.edit.Deletable;

import java.util.ArrayList;
import java.util.HashMap;

/** Provides a standard implementation of {@link Deletable} for the {@link TreePanel}. */
public class StdTreeDeleter implements Deletable {
	private TreePanel	mPanel;

	/**
	 * Creates a new {@link StdTreeDeleter}.
	 *
	 * @param panel The {@link TreePanel} to work with.
	 */
	public StdTreeDeleter(TreePanel panel) {
		mPanel = panel;
	}

	@Override
	public boolean canDeleteSelection() {
		return true;
	}

	@Override
	public void deleteSelection() {
		HashMap<TreeContainerRow, ArrayList<TreeRow>> map = new HashMap<>();
		ArrayList<TreeRow> rows = mPanel.getSelectedRows();
		for (TreeRow row : rows) {
			TreeContainerRow parent = row.getParent();
			ArrayList<TreeRow> list = map.get(parent);
			if (list == null) {
				list = new ArrayList<>();
				map.put(parent, list);
			}
			list.add(row);
		}
		for (TreeContainerRow row : map.keySet()) {
			row.removeRow(map.get(row));
		}
		mPanel.pack();
	}
}
