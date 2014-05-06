/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui.widget;

import com.trollworks.toolkit.ui.Colors;
import com.trollworks.toolkit.ui.MouseCapture;
import com.trollworks.toolkit.ui.UIUtilities;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Icon;
import javax.swing.JComponent;

public class IconButton extends JComponent implements MouseListener, MouseMotionListener {
	private static final int	MARGIN	= 4;
	private Icon				mIcon;
	private Runnable			mClickFunction;
	private boolean				mInMouseDown;
	private boolean				mPressed;
	private boolean				mShowBorder;

	public IconButton(Icon icon, String tooltip, Runnable clickFunction) {
		setOpaque(false);
		setBackground(null);
		setToolTipText(tooltip);
		setIcon(icon);
		setClickFunction(clickFunction);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void setIcon(Icon icon) {
		mIcon = icon;
		UIUtilities.setOnlySize(this, new Dimension(icon.getIconWidth() + MARGIN * 2, icon.getIconHeight() + MARGIN * 2));
		repaint();
	}

	public void setClickFunction(Runnable clickFunction) {
		mClickFunction = clickFunction;
	}

	public void click() {
		mClickFunction.run();
	}

	@Override
	protected void paintComponent(Graphics gc) {
		Insets insets = getInsets();
		int x = insets.left;
		int y = insets.top;
		int width = getWidth() - (insets.left + insets.right);
		int height = getHeight() - (insets.top + insets.bottom);
		if (mInMouseDown && mPressed) {
			gc.setColor(Colors.adjustBrightness(getBackground(), -0.2f));
			gc.fillRect(x, y, width, height);
		}
		if (mShowBorder || mInMouseDown) {
			gc.setColor(Colors.adjustBrightness(getBackground(), -0.4f));
			gc.drawRect(x, y, width - 1, height - 1);
		}
		mIcon.paintIcon(this, gc, x + (width - mIcon.getIconWidth()) / 2, y + (height - mIcon.getIconHeight()) / 2);
	}

	private boolean isOver(int x, int y) {
		return x >= 0 && y >= 0 && x < getWidth() && y < getHeight();
	}

	@Override
	public void mouseEntered(MouseEvent event) {
		mShowBorder = true;
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		// Unused
	}

	@Override
	public void mousePressed(MouseEvent event) {
		if (!event.isPopupTrigger() && event.getButton() == 1) {
			mInMouseDown = true;
			mPressed = true;
			repaint();
			MouseCapture.start(this);
		}
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		boolean wasPressed = mPressed;
		mPressed = isOver(event.getX(), event.getY());
		if (mPressed != wasPressed) {
			repaint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		mouseDragged(event);
		MouseCapture.stop(this);
		if (mPressed) {
			mPressed = false;
			click();
		}
		mInMouseDown = false;
		mShowBorder = isOver(event.getX(), event.getY());
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		// Unused
	}

	@Override
	public void mouseExited(MouseEvent event) {
		mShowBorder = false;
		repaint();
	}
}
