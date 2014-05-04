package com.trollworks.toolkit.ui.widget.dock;

import com.trollworks.toolkit.ui.Colors;

import java.awt.Color;

import javax.swing.UIManager;

/** Provides the colors used by the {@link Dock}. */
public class DockColors {
	public static Color	ACTIVE_HEADER_BACKGROUND	= new Color(219, 207, 171);
	public static Color	BACKGROUND					= UIManager.getColor("Panel.background");			//$NON-NLS-1$
	public static Color	HIGHLIGHT					= Colors.adjustBrightness(BACKGROUND, 0.2f);
	public static Color	SHADOW						= Colors.adjustBrightness(BACKGROUND, -0.2f);
	public static Color	DROP_AREA_OUTER_BORDER		= Color.BLUE;
	public static Color	DROP_AREA_INNER_BORDER		= Color.WHITE;
	public static Color	DROP_AREA					= Colors.getWithAlpha(DROP_AREA_OUTER_BORDER, 64);
}
