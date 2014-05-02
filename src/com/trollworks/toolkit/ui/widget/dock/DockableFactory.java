package com.trollworks.toolkit.ui.widget.dock;

/** A factory for creating new {@link Dockable} instances. */
public interface DockableFactory {
	/**
	 * @param descriptor A descriptor that can be used to create a {@link Dockable}.
	 * @return The newly created {@link Dockable}.
	 */
	Dockable createDockable(String descriptor);
}
