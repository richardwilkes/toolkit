package com.trollworks.toolkit.ui.widget.dock;

/** Possible locations a node can be within a {@link DockLayout}. */
public enum DockLocation {
	NORTH(true, false), EAST(false, true), SOUTH(false, false), WEST(true, true);

	private static final int[]	PRIMARY_ORDER	= { 0, 1 };
	private static final int[]	SECONDARY_ORDER	= { 1, 0 };
	private boolean				mPrimary;
	private boolean				mHorizontal;

	private DockLocation(boolean primary, boolean horizontal) {
		mPrimary = primary;
		mHorizontal = horizontal;
	}

	/**
	 * @return An array with two indexes, indicating the primary (position 0) and the secondary
	 *         (position 1) indexes for the given location, where 'primary' and 'secondary' refer to
	 *         the position that the location wants to be within the {@link DockLayout} child array.
	 */
	public final int[] getOrder() {
		return mPrimary ? PRIMARY_ORDER : SECONDARY_ORDER;
	}

	/** @return <code>true</code> if this indicates a horizontal layout. */
	public final boolean isHorizontal() {
		return mHorizontal;
	}

	/** @return <code>true</code> if this indicates a vertical layout. */
	public final boolean isVertical() {
		return !mHorizontal;
	}
}
