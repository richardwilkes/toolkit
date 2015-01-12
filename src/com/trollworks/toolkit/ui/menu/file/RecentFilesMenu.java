/*
 * Copyright (c) 1998-2015 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui.menu.file;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.utility.FileType;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.PathUtils;
import com.trollworks.toolkit.utility.Platform;
import com.trollworks.toolkit.utility.Preferences;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/** The standard "Recent Files" menu. */
public class RecentFilesMenu extends JMenu implements MenuListener {
	@Localize("Recent Files")
	@Localize(locale = "ru", value = "Недавние файлы")
	@Localize(locale = "de", value = "Zuletzt verwendete Dateien")
	@Localize(locale = "es", value = "Archivos recientes")
	private static String					RECENT_FILES;

	static {
		Localization.initialize();
	}

	private static final String				PREFS_MODULE	= "RecentFiles";		//$NON-NLS-1$
	private static final int				PREFS_VERSION	= 1;
	private static final int				MAX_RECENTS		= 20;
	private static final ArrayList<File>	RECENTS			= new ArrayList<>();

	static {
		loadFromPreferences();
	}

	/** @return The current set of recents. */
	public static ArrayList<File> getRecents() {
		return new ArrayList<>(RECENTS);
	}

	/** @return The number of recents currently present. */
	public static int getRecentCount() {
		return RECENTS.size();
	}

	/** Removes all recents. */
	public static void clearRecents() {
		RECENTS.clear();
	}

	/** @param file The {@link File} to add to the recents list. */
	public static void addRecent(File file) {
		String extension = PathUtils.getExtension(file.getName());
		if (Platform.isMacintosh() || Platform.isWindows()) {
			extension = extension.toLowerCase();
		}
		for (String allowed : FileType.getOpenableExtensions()) {
			if (allowed.equals(extension)) {
				if (file.canRead()) {
					file = PathUtils.getFile(PathUtils.getFullPath(file));
					RECENTS.remove(file);
					RECENTS.add(0, file);
					if (RECENTS.size() > MAX_RECENTS) {
						RECENTS.remove(MAX_RECENTS);
					}
				}
				break;
			}
		}
	}

	/** Loads the set of recents from preferences. */
	public static void loadFromPreferences() {
		clearRecents();
		Preferences prefs = Preferences.getInstance();
		prefs.resetIfVersionMisMatch(PREFS_MODULE, PREFS_VERSION);
		for (int i = 0; i < MAX_RECENTS; i++) {
			String path = prefs.getStringValue(PREFS_MODULE, Integer.toString(i));
			if (path == null) {
				break;
			}
			addRecent(PathUtils.getFile(PathUtils.normalizeFullPath(path)));
		}
	}

	/** Saves the current set of recents to preferences. */
	public static void saveToPreferences() {
		Preferences prefs = Preferences.getInstance();
		prefs.startBatch();
		prefs.removePreferences(PREFS_MODULE);
		int count = RECENTS.size();
		for (int i = 0; i < count; i++) {
			prefs.setValue(PREFS_MODULE, Integer.toString(count - (i + 1)), RECENTS.get(i).getAbsolutePath());
		}
		prefs.endBatch();
	}

	/** Creates a new {@link RecentFilesMenu}. */
	public RecentFilesMenu() {
		super(RECENT_FILES);
		addMenuListener(this);
	}

	@Override
	public void menuCanceled(MenuEvent event) {
		// Nothing to do.
	}

	@Override
	public void menuDeselected(MenuEvent event) {
		// Nothing to do.
	}

	@Override
	public void menuSelected(MenuEvent event) {
		removeAll();
		ArrayList<File> list = new ArrayList<>();
		for (File file : RECENTS) {
			if (file.canRead()) {
				list.add(file);
				add(new JMenuItem(new OpenDataFileCommand(PathUtils.getLeafName(file.getName(), false), file)));
				if (list.size() == MAX_RECENTS) {
					break;
				}
			}
		}
		RECENTS.clear();
		RECENTS.addAll(list);

		if (getRecentCount() > 0) {
			addSeparator();
		}
		JMenuItem item = new JMenuItem(ClearRecentFilesMenuCommand.INSTANCE);
		ClearRecentFilesMenuCommand.INSTANCE.adjust();
		add(item);
	}
}
