package com.trollworks.toolkit.ui.widget.dock;

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

	/**
	 * @return The content of this {@link Dockable}. Note that this content should only be created
	 *         once and the same object returned for all subsequent calls to this method.
	 */
	Component getContent();
}
