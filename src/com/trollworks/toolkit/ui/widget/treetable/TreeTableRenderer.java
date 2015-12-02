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

import java.awt.Graphics2D;

/**
 * Objects that want to provide rendering and UI interaction services for cells within a
 * {@link TreeTable} must implement this interface.
 */
public interface TreeTableRenderer {
	/**
	 * @param table The {@link TreeTable} being rendered.
	 * @return The number of columns that will be displayed.
	 */
	int getColumnCount(TreeTable table);

	/**
	 * @param table The {@link TreeTable} being rendered.
	 * @param column The column index to check.
	 * @return The preferred width of the column.
	 */
	int getPreferredColumnWidth(TreeTable table, int column);

	/**
	 * @param table The {@link TreeTable} being rendered.
	 * @param column The column index to check.
	 * @return The width of the column.
	 */
	int getColumnWidth(TreeTable table, int column);

	/** @return The column that should contain the disclosure controls, if they are present. */
	int getDisclosureColumn();

	/**
	 * @param table The {@link TreeTable} containing the row.
	 * @param row The row object to check.
	 * @return The height of the row.
	 */
	int getRowHeight(TreeTable table, Object row);

	/**
	 * Draws the specified cell.
	 *
	 * @param table The {@link TreeTable} being rendered.
	 * @param gc The graphics context. The origin will be set to the upper-left corner of the cell.
	 * @param row The row being rendered.
	 * @param column The column index being rendered.
	 * @param width The width of the cell.
	 * @param height The height of the cell.
	 * @param selected <code>true</code> if the row is currently selected.
	 */
	void drawCell(TreeTable table, Graphics2D gc, Object row, int column, int width, int height, boolean selected);

	/**
	 * @param table The {@link TreeTable} being clicked on.
	 * @param row The row being clicked on.
	 * @param column The column index being clicked on.
	 * @param x The x-coordinate of the mouse in cell-relative coordinates.
	 * @param y The y-coordinate of the mouse in cell-relative coordinates.
	 * @param width The width of the cell.
	 * @param height The height of the cell.
	 * @param button The button that is pressed.
	 * @param clickCount The number of clicks made by this button so far.
	 * @param modifiers The key modifiers at the time of the event.
	 * @param popupTrigger <code>true</code> if this should trigger a contextual menu.
	 */
	void mousePressed(TreeTable table, Object row, int column, int x, int y, int width, int height, int button, int clickCount, int modifiers, boolean popupTrigger);
}
