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

package com.trollworks.toolkit.ui.widget.dock;

import com.trollworks.toolkit.ui.UIUtilities;

import java.awt.Component;

import javax.swing.Icon;

/** Represents dockable items. */
public interface Dockable {
	/**
	 * @return A unique descriptor that can be used by a {@link DockableFactory} to recreate the
	 *         contents of the {@link Dockable}.
	 */
	String getDescriptor();

	/** @return An {@link Icon} to represent this {@link Dockable}. */
	Icon getTitleIcon();

	/** @return The title of this {@link Dockable}. */
	String getTitle();

	/** @return The title tooltip of this {@link Dockable}. */
	String getTitleTooltip();

	/**
	 * @return The content of this {@link Dockable}. Note that this content should only be created
	 *         once and the same object returned for all subsequent calls to this method.
	 */
	Component getContent();

	default DockContainer getDockContainer() {
		return (DockContainer) UIUtilities.getAncestorOfType(getContent(), DockContainer.class);
	}
}
