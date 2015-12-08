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

package com.trollworks.toolkit.ui.widget;

import com.trollworks.toolkit.ui.UIUtilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

public class ColorWell extends JPanel implements MouseListener {
	private Color					mColor;
	private ColorChangedListener	mListener;

	public ColorWell(Color color, ColorChangedListener listener) {
		mColor = color;
		setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new LineBorder(Color.WHITE)));
		UIUtilities.setOnlySize(this, new Dimension(20, 20));
		addMouseListener(this);
		mListener = listener;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D gc = (Graphics2D) g.create();
		Insets insets = getInsets();
		Rectangle bounds = getBounds();
		bounds.x = insets.left;
		bounds.y = insets.top;
		bounds.width -= insets.left + insets.right;
		bounds.height -= insets.top + insets.bottom;
		gc.setColor(Color.WHITE);
		gc.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
		gc.setColor(Color.LIGHT_GRAY);
		for (int y = bounds.y; y < bounds.y + bounds.height; y += 4) {
			for (int x = bounds.x + ((y - bounds.y) / 4 & 1) * 4; x < bounds.x + bounds.height; x += 8) {
				gc.fillRect(x, y, 4, 4);
			}
		}
		gc.setColor(mColor);
		gc.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		Color color = JColorChooser.showDialog(this, null, mColor);
		if (color != null) {
			if (!mColor.equals(color)) {
				mColor = color;
				repaint();
				mListener.colorChanged(mColor);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent event) {
		// Unused
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		// Unused
	}

	@Override
	public void mouseEntered(MouseEvent event) {
		// Unused
	}

	@Override
	public void mouseExited(MouseEvent event) {
		// Unused
	}

	public interface ColorChangedListener {
		void colorChanged(Color color);
	}
}
