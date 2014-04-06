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

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.image.ToolkitImage;
import com.trollworks.toolkit.utility.Localization;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JRootPane;

/** A toolbar marker that tracks the modified state. */
public class ModifiedMarker extends JLabel implements DataModifiedListener {
	@Localize("Changes have been made")
	private static String		MODIFIED;
	@Localize("No changes have been made")
	private static String		NOT_MODIFIED;

	static {
		Localization.initialize();
	}

	private static ImageIcon	ICON_NOT_MODIFIED	= new ImageIcon(ToolkitImage.getNotModifiedMarker());
	private static ImageIcon	ICON_MODIFIED		= new ImageIcon(ToolkitImage.getModifiedMarker());

	/** Creates a new {@link ModifiedMarker}. */
	public ModifiedMarker() {
		super(ICON_NOT_MODIFIED);
		setToolTipText(NOT_MODIFIED);
	}

	@Override
	public void dataModificationStateChanged(Object obj, boolean modified) {
		if (modified) {
			setIcon(ICON_MODIFIED);
			setToolTipText(MODIFIED);
		} else {
			setIcon(ICON_NOT_MODIFIED);
			setToolTipText(NOT_MODIFIED);
		}
		repaint();
		JRootPane rootPane = getRootPane();
		if (rootPane != null) {
			rootPane.putClientProperty("Window.documentModified", modified ? Boolean.TRUE : Boolean.FALSE); //$NON-NLS-1$
		}
	}
}
