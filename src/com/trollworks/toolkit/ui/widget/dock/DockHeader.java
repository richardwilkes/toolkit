package com.trollworks.toolkit.ui.widget.dock;

import com.trollworks.toolkit.ui.border.SelectiveLineBorder;

import javax.swing.JLabel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/** The header for a {@link DockContainer}. */
public class DockHeader extends JLabel {
	/**
	 * Creates a new {@link DockHeader} for the specified {@link Dockable}.
	 *
	 * @param dockable The {@link Dockable} to work with.
	 */
	public DockHeader(Dockable dockable) {
		super(dockable.getTitle(), dockable.getTitleIcon(), LEFT);
		setOpaque(true);
		setBorder(new CompoundBorder(new SelectiveLineBorder(DockColors.SHADOW, 0, 0, 1, 0), new EmptyBorder(2, 4, 2, 4)));
	}
}
