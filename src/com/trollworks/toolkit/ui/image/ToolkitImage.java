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
		return Images.get("Collapse");
	}

	/** @return The expand icon. */
	public static final ToolkitIcon getExpandIcon() {
		return Images.get("Expand");
	}

	/** @return The add icon. */
	public static final ToolkitIcon getAddIcon() {
		return Images.get("Add");
	}

	/** @return The down triangle icon. */
	public static final ToolkitIcon getDownTriangleIcon() {
		return Images.get("DownTriangle");
	}

	/** @return The down triangle roll icon. */
	public static final ToolkitIcon getDownTriangleRollIcon() {
		return Images.get("DownTriangleRoll");
	}

	/** @return The locked icon. */
	public static final ToolkitIcon getLockedIcon() {
		return Images.get("Locked");
	}

	/** @return The mini warning dialog icon. */
	public static final ToolkitIcon getMiniWarningIcon() {
		return Images.get("MiniWarning");
	}

	/** @return The modified marker. */
	public static final ToolkitIcon getModifiedMarker() {
		return Images.get("ModifiedMarker");
	}

	/** @return The more icon. */
	public static final ToolkitIcon getMoreIcon() {
		return Images.get("More");
	}

	/** @return The not modified marker. */
	public static final ToolkitIcon getNotModifiedMarker() {
		return Images.get("NotModifiedMarker");
	}

	/** @return The file icon set. */
	public static final IconSet getFileIcons() {
		return IconSet.getOrLoad("File");
	}

	/** @return The folder icon set. */
	public static final IconSet getFolderIcons() {
		return IconSet.getOrLoad("Folder");
	}

	/** @return The preferences icon set. */
	public static final IconSet getPreferencesIcons() {
		return IconSet.getOrLoad("preferences");
	}

	/** @return The remove icon. */
	public static final ToolkitIcon getRemoveIcon() {
		return Images.get("Remove");
	}

	/** @return The right triangle icon. */
	public static final ToolkitIcon getRightTriangleIcon() {
		return Images.get("RightTriangle");
	}

	/** @return The right triangle roll icon. */
	public static final ToolkitIcon getRightTriangleRollIcon() {
		return Images.get("RightTriangleRoll");
	}

	/** @return The size-to-fit icon. */
	public static final ToolkitIcon getSizeToFitIcon() {
		return Images.get("SizeToFit");
	}

	/** @return The toggle open icon. */
	public static final ToolkitIcon getToggleOpenIcon() {
		return Images.get("ToggleOpen");
	}

	/** @return The unlocked icon. */
	public static final ToolkitIcon getUnlockedIcon() {
		return Images.get("Unlocked");
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
