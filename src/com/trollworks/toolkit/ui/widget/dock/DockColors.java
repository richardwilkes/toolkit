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

import com.trollworks.toolkit.ui.Colors;

import java.awt.Color;

import javax.swing.UIManager;

/** Provides the colors used by the {@link Dock}. */
public class DockColors {
	public static Color	ACTIVE_DOCK_HEADER_BACKGROUND	= new Color(219, 207, 171);
	public static Color	BACKGROUND						= UIManager.getColor("Panel.background");				//$NON-NLS-1$
	public static Color	HIGHLIGHT						= Colors.adjustBrightness(BACKGROUND, 0.2f);
	public static Color	SHADOW							= Colors.adjustBrightness(BACKGROUND, -0.2f);
	public static Color	DOCK_DROP_AREA_OUTER_BORDER		= Color.BLUE;
	public static Color	DOCK_DROP_AREA_INNER_BORDER		= Color.WHITE;
	public static Color	DOCK_DROP_AREA					= Colors.getWithAlpha(DOCK_DROP_AREA_OUTER_BORDER, 64);
}
