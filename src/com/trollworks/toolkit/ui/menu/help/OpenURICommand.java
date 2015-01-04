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

package com.trollworks.toolkit.ui.menu.help;

import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.ui.widget.WindowUtils;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/** A command that will open a specific URI in the preferred browser. */
public class OpenURICommand extends Command {
	private URI	mURI;

	/**
	 * Creates a new {@link OpenURICommand}.
	 *
	 * @param title The title to use.
	 * @param uri The URI to open.
	 */
	public OpenURICommand(String title, URI uri) {
		super(title, "OpenURL[" + uri + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		mURI = uri;
	}

	/**
	 * Creates a new {@link OpenURICommand}.
	 *
	 * @param title The title to use.
	 * @param uri The URI to open.
	 */
	public OpenURICommand(String title, String uri) {
		super(title, "OpenURL[" + uri + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			mURI = new URI(uri);
		} catch (URISyntaxException exception) {
			exception.printStackTrace();
		}
	}

	@Override
	public void adjust() {
		setEnabled(mURI != null && Desktop.isDesktopSupported());
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (mURI != null) {
			try {
				Desktop.getDesktop().browse(mURI);
			} catch (IOException exception) {
				WindowUtils.showError(null, exception.getMessage());
			}
		}
	}
}
