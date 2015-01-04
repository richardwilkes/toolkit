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

import com.trollworks.toolkit.ui.image.StdImage;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;

/**
 * Provides a {@link Cell} which toggles between two states. The {@link Row}'s
 * {@link Row#getData(Column)} method must return a {@link Boolean}.
 */
public class ToggleCell extends ImageCell {
	private StdImage	mOn;
	private StdImage	mOff;

	/** Creates a new {@link ToggleCell}. */
	public ToggleCell() {
		JCheckBox checkBox = new JCheckBox();
		checkBox.putClientProperty("JComponent.sizeVariant", "mini"); //$NON-NLS-1$ //$NON-NLS-2$
		Dimension prefSize = checkBox.getPreferredSize();
		checkBox.setSize(prefSize);
		mOff = StdImage.createTransparent(prefSize.width, prefSize.height);
		Graphics gc = mOff.getGraphics();
		checkBox.paint(gc);
		gc.dispose();
		checkBox.setSelected(true);
		mOn = StdImage.createTransparent(prefSize.width, prefSize.height);
		gc = mOn.getGraphics();
		checkBox.paint(gc);
		gc.dispose();
	}

	/**
	 * Creates a new {@link ToggleCell}.
	 *
	 * @param on The {@link StdImage} to represent the 'on' state.
	 * @param off The {@link StdImage} to represent the 'off' state.
	 */
	public ToggleCell(StdImage on, StdImage off) {
		mOn = on;
		mOff = off;
	}

	@Override
	protected StdImage getIcon(Row row, Column column, boolean selected, boolean active) {
		Object data = row.getData(column);
		return ((Boolean) data).booleanValue() ? mOn : mOff;
	}

	@Override
	public void mouseClicked(MouseEvent event, Rectangle bounds, Row row, Column column) {
		Boolean enabled = (Boolean) row.getData(column);
		row.setData(column, enabled.booleanValue() ? Boolean.FALSE : Boolean.TRUE);
	}
}
