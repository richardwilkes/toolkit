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

package com.trollworks.toolkit.ui.widget.outline;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Provides an iterator that will iterate over all rows (disclosed or not) in an outline model.
 *
 * @param <T> The type of row being iterated over.
 */
public class RowIterator<T extends Row> implements Iterator<T>, Iterable<T> {
	private List<Row>		mList;
	private int				mIndex;
	private RowIterator<T>	mIterator;

	/**
	 * Creates an iterator that will iterate over all rows (disclosed or not) in the specified
	 * outline model.
	 *
	 * @param model The model to iterator over.
	 */
	public RowIterator(OutlineModel model) {
		this(model.getTopLevelRows());
	}

	private RowIterator(List<Row> rows) {
		mList = rows;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasNext() {
		boolean hasNext = mIterator != null && mIterator.hasNext();

		if (!hasNext) {
			mIterator = null;
			hasNext = mIndex < mList.size();
		}
		return hasNext;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T next() {
		if (hasNext()) {
			if (mIterator == null) {
				Row row = mList.get(mIndex++);

				if (row.hasChildren()) {
					mIterator = new RowIterator<>(row.getChildren());
				}
				return (T) row;
			}
			return mIterator.next();
		}
		throw new NoSuchElementException();
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}
}
