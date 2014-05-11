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

package com.trollworks.toolkit.ui.menu.file;

import com.trollworks.toolkit.ui.image.IconSet;
import com.trollworks.toolkit.ui.image.ToolkitImage;
import com.trollworks.toolkit.ui.widget.AppWindow;
import com.trollworks.toolkit.utility.PathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/** Describes a file. */
public class FileType {
	private static final String					DOT			= ".";					//$NON-NLS-1$
	private static final ArrayList<FileType>	TYPES		= new ArrayList<>();
	private static HashMap<String, IconSet>		ICONSET_MAP	= new HashMap<>();
	private String								mExtension;
	private IconSet								mIconSet;
	private Class<? extends AppWindow>			mWindowClass;
	private boolean								mAllowOpen;

	/**
	 * Registers a new {@link FileType}, replacing any existing entry for the specified extension.
	 *
	 * @param extension The extension of the file.
	 * @param iconset The {@link IconSet} to use for the file.
	 * @param windowClass The {@link Class} responsible for creating a window with this file's
	 *            contents.
	 * @param allowOpen Whether this {@link FileType} is allowed to be opened via the menu command.
	 */
	public static final void register(String extension, IconSet iconset, Class<? extends AppWindow> windowClass, boolean allowOpen) {
		if (!extension.startsWith(DOT)) {
			extension = DOT + extension;
		}
		for (FileType type : TYPES) {
			if (type.mExtension.equals(extension)) {
				TYPES.remove(type);
				break;
			}
		}
		TYPES.add(new FileType(extension, iconset, windowClass, allowOpen));
		ICONSET_MAP.put(extension, iconset);
	}

	/** @return All of the registered {@link FileType}s. */
	public static final FileType[] getAll() {
		return TYPES.toArray(new FileType[TYPES.size()]);
	}

	/** @return All of the registered {@link FileType}s that can be opened. */
	public static final FileType[] getOpenable() {
		ArrayList<FileType> openable = new ArrayList<>();
		for (FileType type : TYPES) {
			if (type.allowOpen()) {
				openable.add(type);
			}
		}
		return openable.toArray(new FileType[openable.size()]);
	}

	/** @return All of the registered {@link FileType} extensions that can be opened. */
	public static final String[] getOpenableExtensions() {
		ArrayList<String> openable = new ArrayList<>();
		for (FileType type : TYPES) {
			if (type.mAllowOpen) {
				openable.add(type.getExtension());
			}
		}
		return openable.toArray(new String[openable.size()]);
	}

	/**
	 * @param path The path to return an icon for.
	 * @return The icon for the specified file.
	 */
	public static IconSet getIconsForFile(String path) {
		return getIconsForFileExtension(PathUtils.getExtension(path));
	}

	/**
	 * @param file The file to return an icon for.
	 * @return The icon for the specified file.
	 */
	public static IconSet getIconsForFile(File file) {
		return getIconsForFile(file != null && file.isFile() ? file.getName() : null);
	}

	/**
	 * @param extension The extension to return an icon for.
	 * @return The icon for the specified file extension.
	 */
	public static IconSet getIconsForFileExtension(String extension) {
		if (extension != null) {
			if (!extension.startsWith(DOT)) {
				extension = DOT + extension;
			}
			IconSet icons = ICONSET_MAP.get(extension);
			if (icons != null) {
				return icons;
			}
			return ToolkitImage.getFileIcons();
		}
		return ToolkitImage.getFolderIcons();
	}

	private FileType(String extension, IconSet iconset, Class<? extends AppWindow> windowClass, boolean allowOpen) {
		mExtension = extension;
		mIconSet = iconset;
		mWindowClass = windowClass;
		mAllowOpen = allowOpen;
	}

	/** @return The extension of the file. */
	public String getExtension() {
		return mExtension;
	}

	/** @return The {@link IconSet} representing the file. */
	public IconSet getIcons() {
		return mIconSet;
	}

	/** @return The {@link Class} responsible for creating a window with this file's contents. */
	public Class<? extends AppWindow> getWindowClass() {
		return mWindowClass;
	}

	/** @return Whether this {@link FileType} is allowed to be opened via the menu command. */
	public boolean allowOpen() {
		return mAllowOpen;
	}
}
