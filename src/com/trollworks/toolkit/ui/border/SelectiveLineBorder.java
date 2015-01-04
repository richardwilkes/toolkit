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

package com.trollworks.toolkit.ui.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;

/** A border that allows varying colors and thicknesses for each side. */
public class SelectiveLineBorder implements Border {
	private Color	mTopColor;
	private int		mTopThickness;
	private Color	mBottomColor;
	private int		mBottomThickness;
	private Color	mLeftColor;
	private int		mLeftThickness;
	private Color	mRightColor;
	private int		mRightThickness;

	/** Creates a black, 1 pixel border on all sides. */
	public SelectiveLineBorder() {
		this(Color.BLACK, 1);
	}

	/**
	 * Creates a border with the specified color and thickness on all sides.
	 *
	 * @param color The color to use for all sides.
	 * @param thickness The thickness to use for all sides.
	 */
	public SelectiveLineBorder(Color color, int thickness) {
		mTopColor = color;
		mTopThickness = thickness;
		mBottomColor = color;
		mBottomThickness = thickness;
		mLeftColor = color;
		mLeftThickness = thickness;
		mRightColor = color;
		mRightThickness = thickness;
	}

	/**
	 * Creates a border with the specified color on all sides and the specified thicknesses on each
	 * side.
	 *
	 * @param color The color to use for all sides.
	 * @param top The thickness to use for the top side.
	 * @param left The thickness to use for the left side.
	 * @param bottom The thickness to use for the bottom side.
	 * @param right The thickness to use for the right side.
	 */
	public SelectiveLineBorder(Color color, int top, int left, int bottom, int right) {
		mTopColor = color;
		mTopThickness = top;
		mBottomColor = color;
		mBottomThickness = bottom;
		mLeftColor = color;
		mLeftThickness = left;
		mRightColor = color;
		mRightThickness = right;
	}

	/**
	 * Creates a border.
	 *
	 * @param topColor The color to use for the top side.
	 * @param top The thickness to use for the top side.
	 * @param leftColor The color to use for the left side.
	 * @param left The thickness to use for the left side.
	 * @param bottomColor The color to use for the bottom side.
	 * @param bottom The thickness to use for the bottom side.
	 * @param rightColor The color to use for the right side.
	 * @param right The thickness to use for the right side.
	 */
	public SelectiveLineBorder(Color topColor, int top, Color leftColor, int left, Color bottomColor, int bottom, Color rightColor, int right) {
		mTopColor = topColor;
		mTopThickness = top;
		mBottomColor = bottomColor;
		mBottomThickness = bottom;
		mLeftColor = leftColor;
		mLeftThickness = left;
		mRightColor = rightColor;
		mRightThickness = right;
	}

	@Override
	public void paintBorder(Component component, Graphics gc, int x, int y, int width, int height) {
		Color color = gc.getColor();
		if (mLeftThickness > 0) {
			gc.setColor(mLeftColor);
			gc.fillRect(x, y, mLeftThickness, height);
		}
		if (mRightThickness > 0) {
			gc.setColor(mRightColor);
			gc.fillRect(x + width - mRightThickness, y, mRightThickness, height);
		}
		if (mTopThickness > 0) {
			gc.setColor(mTopColor);
			gc.fillRect(x, y, width, mTopThickness);
		}
		if (mBottomThickness > 0) {
			gc.setColor(mBottomColor);
			gc.fillRect(x, y + height - mBottomThickness, width, mBottomThickness);
		}
		gc.setColor(color);
	}

	@Override
	public Insets getBorderInsets(Component component) {
		return new Insets(mTopThickness, mLeftThickness, mBottomThickness, mRightThickness);
	}

	@Override
	public boolean isBorderOpaque() {
		return true;
	}

	/** @return The color of the top side. */
	public Color getTopColor() {
		return mTopColor;
	}

	/** @param color The color to use on the top side. */
	public void setTopColor(Color color) {
		mTopColor = color;
	}

	/** @return The thickness of the top side. */
	public int getTopThickness() {
		return mTopThickness;
	}

	/** @param thickness The thickness to use on the top side. */
	public void setTopThickness(int thickness) {
		mTopThickness = thickness;
	}

	/** @return The color of the bottom side. */
	public Color getBottomColor() {
		return mBottomColor;
	}

	/** @param color The color to use on the bottom side. */
	public void setBottomColor(Color color) {
		mBottomColor = color;
	}

	/** @return The thickness of the bottom side. */
	public int getBottomThickness() {
		return mBottomThickness;
	}

	/** @param thickness The thickness to use on the bottom side. */
	public void setBottomThickness(int thickness) {
		mBottomThickness = thickness;
	}

	/** @return The color of the left side. */
	public Color getLeftColor() {
		return mLeftColor;
	}

	/** @param color The color to use on the left side. */
	public void setLeftColor(Color color) {
		mLeftColor = color;
	}

	/** @return The thickness of the left side. */
	public int getLeftThickness() {
		return mLeftThickness;
	}

	/** @param thickness The thickness to use on the left side. */
	public void setLeftThickness(int thickness) {
		mLeftThickness = thickness;
	}

	/** @return The color of the right side. */
	public Color getRightColor() {
		return mRightColor;
	}

	/** @param color The color to use on the right side. */
	public void setRightColor(Color color) {
		mRightColor = color;
	}

	/** @return The thickness of the right side. */
	public int getRightThickness() {
		return mRightThickness;
	}

	/** @param thickness The thickness to use on the right side. */
	public void setRightThickness(int thickness) {
		mRightThickness = thickness;
	}
}
