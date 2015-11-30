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

import java.util.List;

/** Objects that want to provide data to a {@link TreeTable} must implement this interface. */
public interface TreeTableModel {
	/** @return The {@link SelectionModel} to use. */
	SelectionModel getSelectionModel();

	/** @return A {@link List} containing the root row objects. May be empty. */
	List<Object> getRootRows();

	/**
	 * @param row The row object to check.
	 * @return <code>true</code> if the row is not capable of having child rows.
	 */
	boolean isLeafRow(Object row);

	/**
	 * @param row The row object to check.
	 * @return <code>true</code> if the row is in the disclosed (open) state.
	 */
	boolean isRowDisclosed(Object row);

	/**
	 * @param row The row object to modify.
	 * @param disclosed The disclosure state to set.
	 */
	void setRowDisclosed(Object row, boolean disclosed);

	/**
	 * @param row The row object to work on.
	 * @return The number of direct children the row contains.
	 */
	int getRowChildCount(Object row);

	/**
	 * @param row The row object to work on.
	 * @param index The index specifying which child to return.
	 * @return The child at the specified index.
	 */
	Object getRowChild(Object row, int index);

	/**
	 * @param row The row object to work on.
	 * @param child The child row object.
	 * @return The child's index within the row.
	 */
	int getIndexOfRowChild(Object row, Object child);

	/**
	 * @param row The row object to work on.
	 * @return The row object's parent row object, or <code>null</code> if the passed in row is a
	 *         root.
	 */
	Object getRowParent(Object row);

	/** @param listener The {@link TreeTableModelListener} to add. */
	void addTreeTableModelListener(TreeTableModelListener listener);

	/** @param listener The {@link TreeTableModelListener} to remove. */
	void removeTreeTableModelListener(TreeTableModelListener listener);
}
