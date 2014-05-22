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

package com.trollworks.toolkit.ui.image;

/** Convenience class to hold toolkit images. */
@SuppressWarnings("nls")
public class ToolkitImage {
	static {
		Images.addLocation(ToolkitImage.class.getResource("images/"));
	}

	/** @return The collapse icon. */
	public static final ToolkitIcon getCollapseIcon() {
		return Images.get("collapse");
	}

	/** @return The expand icon. */
	public static final ToolkitIcon getExpandIcon() {
		return Images.get("expand");
	}

	/** @return The add icon. */
	public static final ToolkitIcon getAddIcon() {
		return Images.get("add");
	}

	/** @return The down triangle icon. */
	public static final ToolkitIcon getDownTriangleIcon() {
		return Images.get("down_triangle");
	}

	/** @return The down triangle roll icon. */
	public static final ToolkitIcon getDownTriangleRollIcon() {
		return Images.get("down_triangle_roll");
	}

	/** @return The locked icon. */
	public static final ToolkitIcon getLockedIcon() {
		return Images.get("locked");
	}

	/** @return The mini warning dialog icon. */
	public static final ToolkitIcon getMiniWarningIcon() {
		return Images.get("mini_warning");
	}

	/** @return The modified marker. */
	public static final ToolkitIcon getModifiedMarker() {
		return Images.get("modified_marker");
	}

	/** @return The more icon. */
	public static final ToolkitIcon getMoreIcon() {
		return Images.get("more");
	}

	/** @return The not modified marker. */
	public static final ToolkitIcon getNotModifiedMarker() {
		return Images.get("not_modified_marker");
	}

	/** @return The file icon set. */
	public static final IconSet getFileIcons() {
		return IconSet.getOrLoad("file");
	}

	/** @return The folder icon set. */
	public static final IconSet getFolderIcons() {
		return IconSet.getOrLoad("folder");
	}

	/** @return The preferences icon set. */
	public static final IconSet getPreferencesIcons() {
		return IconSet.getOrLoad("preferences");
	}

	/** @return The remove icon. */
	public static final ToolkitIcon getRemoveIcon() {
		return Images.get("remove");
	}

	/** @return The right triangle icon. */
	public static final ToolkitIcon getRightTriangleIcon() {
		return Images.get("right_triangle");
	}

	/** @return The right triangle roll icon. */
	public static final ToolkitIcon getRightTriangleRollIcon() {
		return Images.get("right_triangle_roll");
	}

	/** @return The size-to-fit icon. */
	public static final ToolkitIcon getSizeToFitIcon() {
		return Images.get("size_to_fit");
	}

	/** @return The toggle open icon. */
	public static final ToolkitIcon getToggleOpenIcon() {
		return Images.get("toggle_open");
	}

	/** @return The unlocked icon. */
	public static final ToolkitIcon getUnlockedIcon() {
		return Images.get("unlocked");
	}

	/** @return The dock close image. */
	public static final ToolkitIcon getDockClose() {
		return Images.get("dock_close");
	}

	/** @return The dock maximize image. */
	public static final ToolkitIcon getDockMaximize() {
		return Images.get("dock_maximize");
	}

	/** @return The dock restore image. */
	public static final ToolkitIcon getDockRestore() {
		return Images.get("dock_restore");
	}
}
