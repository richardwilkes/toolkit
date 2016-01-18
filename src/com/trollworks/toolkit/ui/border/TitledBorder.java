/*
 * Copyright (c) 1998-2016 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui.border;

import com.trollworks.toolkit.ui.GraphicsUtilities;
import com.trollworks.toolkit.ui.TextDrawing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.SwingConstants;
import javax.swing.border.Border;

/** A border consisting of a frame and optional title. */
public class TitledBorder implements Border {
	private String	mTitle;
	private Font	mFont;

	/** Creates a new border without a title. */
	public TitledBorder() {
	}

	/**
	 * Creates a new border with a title.
	 *
	 * @param font The font to use.
	 * @param title The title to use.
	 */
	public TitledBorder(Font font, String title) {
		super();
		mFont = font;
		mTitle = title;
	}

	/** @return The title. */
	public String getTitle() {
		return mTitle;
	}

	/** @param title The new title. */
	public void setTitle(String title) {
		mTitle = title;
	}

	@Override
	public Insets getBorderInsets(Component component) {
		Insets insets = new Insets(1, 1, 1, 1);
		if (mTitle != null) {
			insets.top += TextDrawing.getPreferredSize(mFont, mTitle).height;
		}
		return insets;
	}

	@Override
	public boolean isBorderOpaque() {
		return true;
	}

	@Override
	public void paintBorder(Component component, Graphics gc, int x, int y, int width, int height) {
		Color savedColor = gc.getColor();
		gc.setColor(Color.BLACK);
		gc.drawRect(x, y, width - 1, height - 1);
		if (GraphicsUtilities.isRetinaDisplay(gc)) {
			// This should not be necessary, but the top & left edges are too thin otherwise
			Graphics2D gc2 = (Graphics2D) gc.create();
			gc2.translate(x, y);
			gc2.scale(0.5, 0.5);
			gc2.drawLine(1, 0, 1, (height - 1) * 2);
			gc2.drawLine(1, 1, (width - 1) * 2, 1);
		}
		if (mTitle != null) {
			Font savedFont = gc.getFont();
			gc.setFont(mFont);
			int th = TextDrawing.getPreferredSize(mFont, mTitle).height;
			Rectangle bounds = new Rectangle(x, y, width - 1, th + 2);
			gc.fillRect(x, y, width - 1, th + 1);
			gc.setColor(Color.WHITE);
			TextDrawing.draw(gc, bounds, mTitle, SwingConstants.CENTER, SwingConstants.TOP);
			gc.setFont(savedFont);
		}
		gc.setColor(savedColor);
	}
}
