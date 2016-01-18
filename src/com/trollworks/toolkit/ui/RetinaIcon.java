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

package com.trollworks.toolkit.ui;

import com.trollworks.toolkit.ui.image.StdImage;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

public class RetinaIcon implements Icon {
	private StdImage	mNormal;
	private StdImage	mRetina;
	private RetinaIcon	mOverlay;

	public RetinaIcon(String name) {
		this(StdImage.get(name), StdImage.get(name + "@2x")); //$NON-NLS-1$
	}

	public RetinaIcon(StdImage normal) {
		mNormal = normal;
		mRetina = null;
	}

	public RetinaIcon(StdImage normal, StdImage retina) {
		mNormal = normal;
		mRetina = retina;
	}

	public StdImage getNormal() {
		return mNormal;
	}

	public StdImage getRetina() {
		return mRetina;
	}

	public RetinaIcon getOverlay() {
		return mOverlay;
	}

	public void setOverlay(RetinaIcon overlay) {
		mOverlay = overlay;
	}

	@Override
	public void paintIcon(Component component, Graphics g, int x, int y) {
		if (mRetina != null && GraphicsUtilities.isRetinaDisplay(g)) {
			Graphics2D gc = (Graphics2D) g.create();
			gc.translate(x, y);
			gc.scale(0.5, 0.5);
			// Using an offset of -1,-1 as printing to PDF seems to be offset slightly otherwise
			gc.drawImage(mRetina, -1, -1, component);
			gc.dispose();
		} else {
			g.drawImage(mNormal, x, y, component);
		}
		if (mOverlay != null) {
			mOverlay.paintIcon(component, g, x, y);
		}
	}

	@Override
	public int getIconWidth() {
		return mNormal.getWidth();
	}

	@Override
	public int getIconHeight() {
		return mNormal.getHeight();
	}

	public RetinaIcon createDisabled() {
		RetinaIcon icon = new RetinaIcon(StdImage.createDisabledImage(mNormal), mRetina != null ? StdImage.createDisabledImage(mRetina) : null);
		if (mOverlay != null) {
			icon.setOverlay(mOverlay.createDisabled());
		}
		return icon;
	}
}
