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

import java.awt.image.BufferedImage;

/** Convenience class to hold toolkit images. */
@SuppressWarnings("nls")
public class ToolkitImage {
	static {
		Images.addLocation(ToolkitImage.class.getResource("images/"));
	}

	/** @return The collapse icon. */
	public static final BufferedImage getCollapseIcon() {
		return Images.get("Collapse");
	}

	/** @return The expand icon. */
	public static final BufferedImage getExpandIcon() {
		return Images.get("Expand");
	}

	/** @return The add icon. */
	public static final BufferedImage getAddIcon() {
		return Images.get("Add");
	}

	/** @return The down triangle icon. */
	public static final BufferedImage getDownTriangleIcon() {
		return Images.get("DownTriangle");
	}

	/** @return The down triangle roll icon. */
	public static final BufferedImage getDownTriangleRollIcon() {
		return Images.get("DownTriangleRoll");
	}

	/** @return The file icon. */
	public static final BufferedImage getFileIcon() {
		return Images.get("File");
	}

	/** @return The folder icon. */
	public static final BufferedImage getFolderIcon() {
		return Images.get("Folder");
	}

	/** @return The locked icon. */
	public static final BufferedImage getLockedIcon() {
		return Images.get("Locked");
	}

	/** @return The mini warning dialog icon. */
	public static final BufferedImage getMiniWarningIcon() {
		return Images.get("MiniWarning");
	}

	/** @return The modified marker. */
	public static final BufferedImage getModifiedMarker() {
		return Images.get("ModifiedMarker");
	}

	/** @return The more icon. */
	public static final BufferedImage getMoreIcon() {
		return Images.get("More");
	}

	/** @return The not modified marker. */
	public static final BufferedImage getNotModifiedMarker() {
		return Images.get("NotModifiedMarker");
	}

	/** @return The preferences icon. */
	public static final BufferedImage getPreferencesIcon() {
		return Images.get("Preferences");
	}

	/** @return The remove icon. */
	public static final BufferedImage getRemoveIcon() {
		return Images.get("Remove");
	}

	/** @return The right triangle icon. */
	public static final BufferedImage getRightTriangleIcon() {
		return Images.get("RightTriangle");
	}

	/** @return The right triangle roll icon. */
	public static final BufferedImage getRightTriangleRollIcon() {
		return Images.get("RightTriangleRoll");
	}

	/** @return The size-to-fit icon. */
	public static final BufferedImage getSizeToFitIcon() {
		return Images.get("SizeToFit");
	}

	/** @return The toggle open icon. */
	public static final BufferedImage getToggleOpenIcon() {
		return Images.get("ToggleOpen");
	}

	/** @return The unlocked icon. */
	public static final BufferedImage getUnlockedIcon() {
		return Images.get("Unlocked");
	}
}
