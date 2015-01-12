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

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.image.StdImage;
import com.trollworks.toolkit.utility.Localization;

import javax.swing.JLabel;
import javax.swing.JRootPane;

/** A toolbar marker that tracks the modified state. */
public class ModifiedMarker extends JLabel implements DataModifiedListener {
	@Localize("Changes have been made")
	@Localize(locale = "ru", value = "Изменения были внесены")
	@Localize(locale = "de", value = "Nicht gespeicherte Änderungen")
	@Localize(locale = "es", value = "Se han realizado los cambios")
	private static String	MODIFIED;
	@Localize("No changes have been made")
	@Localize(locale = "ru", value = "Изменений не было сделано")
	@Localize(locale = "de", value = "Unverändert")
	@Localize(locale = "es", value = "No se han hecho cambios")
	private static String	NOT_MODIFIED;

	static {
		Localization.initialize();
	}

	/** Creates a new {@link ModifiedMarker}. */
	public ModifiedMarker() {
		super(StdImage.NOT_MODIFIED_MARKER);
		setToolTipText(NOT_MODIFIED);
	}

	@Override
	public void dataModificationStateChanged(Object obj, boolean modified) {
		if (modified) {
			setIcon(StdImage.MODIFIED_MARKER);
			setToolTipText(MODIFIED);
		} else {
			setIcon(StdImage.NOT_MODIFIED_MARKER);
			setToolTipText(NOT_MODIFIED);
		}
		repaint();
		JRootPane rootPane = getRootPane();
		if (rootPane != null) {
			rootPane.putClientProperty("Window.documentModified", modified ? Boolean.TRUE : Boolean.FALSE); //$NON-NLS-1$
		}
	}
}
