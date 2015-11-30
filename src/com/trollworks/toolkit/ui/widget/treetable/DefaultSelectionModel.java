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

package com.trollworks.toolkit.ui.widget.treetable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** A default implementation of {@link SelectionModel}. */
public class DefaultSelectionModel implements SelectionModel {
	private List<SelectionModelListener>	mListeners	= new ArrayList<>();
	private Set<Object>						mSelection	= new HashSet<>();

	@Override
	public boolean hasSelection() {
		return !mSelection.isEmpty();
	}

	@Override
	public Set<Object> getSelection() {
		return Collections.unmodifiableSet(mSelection);
	}

	@Override
	public boolean isSelected(Object node) {
		return mSelection.contains(node);
	}

	@Override
	public void select(Object node, boolean add) {
		boolean needNotify = false;
		if (!add && hasSelection()) {
			mSelection.clear();
			needNotify = true;
		}
		if (mSelection.add(node)) {
			needNotify = true;
		}
		if (needNotify) {
			notifyOfSelectionChange();
		}
	}

	@Override
	public void select(Collection<?> nodes, boolean add) {
		boolean needNotify = false;
		if (!add && hasSelection()) {
			mSelection.clear();
			needNotify = true;
		}
		if (mSelection.addAll(nodes)) {
			needNotify = true;
		}
		if (needNotify) {
			notifyOfSelectionChange();
		}
	}

	@Override
	public void deselect(Object node) {
		if (mSelection.remove(node)) {
			notifyOfSelectionChange();
		}
	}

	@Override
	public void deselect(Collection<?> nodes) {
		if (mSelection.removeAll(nodes)) {
			notifyOfSelectionChange();
		}
	}

	@Override
	public void clear() {
		if (!mSelection.isEmpty()) {
			mSelection.clear();
			notifyOfSelectionChange();
		}
	}

	@Override
	public synchronized void addSelectionModelListener(SelectionModelListener listener) {
		mListeners.add(listener);
	}

	@Override
	public synchronized void removeSelectionModelListener(SelectionModelListener listener) {
		mListeners.remove(listener);
	}

	private void notifyOfSelectionChange() {
		SelectionModelListener[] listeners;
		synchronized (this) {
			listeners = mListeners.toArray(new SelectionModelListener[mListeners.size()]);
		}
		for (SelectionModelListener listener : listeners) {
			listener.selectionChanged();
		}
	}
}
