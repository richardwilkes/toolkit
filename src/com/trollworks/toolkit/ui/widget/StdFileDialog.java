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

package com.trollworks.toolkit.ui.widget;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.io.Log;
import com.trollworks.toolkit.ui.menu.file.RecentFilesMenu;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.NewerDataFileVersionException;
import com.trollworks.toolkit.utility.PathUtils;
import com.trollworks.toolkit.utility.Platform;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.FilenameFilter;
import java.text.MessageFormat;
import java.util.HashSet;

/** Provides standard file dialog handling. */
public class StdFileDialog implements FilenameFilter {
	@Localize("Unable to open \"{0}\".")
	@Localize(locale = "ru", value = "Невозможно открыть \"{0}\".")
	@Localize(locale = "de", value = "Kann Datei \"{0}\" nicht öffnen.")
	@Localize(locale = "es", value = "Imposible abrir \"{0}\".")
	private static String	UNABLE_TO_OPEN;
	@Localize("Unable to open \"{0}\"\n{1}")
	@Localize(locale = "ru", value = "Невозможно открыть \"{0}\"\n{1}")
	@Localize(locale = "de", value = "Kann Datei \"{0}\" nicht öffnen.\n{1}")
	@Localize(locale = "es", value = "Imposible abrir \"{0}\".")
	private static String	UNABLE_TO_OPEN_WITH_EXCEPTION;

	static {
		Localization.initialize();
	}

	private HashSet<String>	mFileNameMatchers	= new HashSet<>();

	/**
	 * Creates a new {@link StdFileDialog}.
	 *
	 * @param comp The {@link Component} to use for determining the parent {@link Frame} or
	 *            {@link Dialog}.
	 * @param open Whether an 'open' or a 'save' dialog is presented.
	 * @param title The title to use.
	 * @param dir The initial directory to start with. May be <code>null</code>.
	 * @param name The initial file name to start with. May be <code>null</code>.
	 * @param extension One or more file name extensions that should be allowed to be opened or
	 *            saved. If this is a save dialog, the first extension will be forced if none match
	 *            what the user has supplied.
	 * @return The chosen {@link File} or <code>null</code>.
	 */
	public static File choose(Component comp, boolean open, String title, String dir, String name, String... extension) {
		StdFileDialog filter = new StdFileDialog(extension);
		FileDialog dialog;
		Window window = WindowUtils.getWindowForComponent(comp);
		int mode = open ? FileDialog.LOAD : FileDialog.SAVE;
		if (window instanceof Frame) {
			dialog = new FileDialog((Frame) window, title, mode);
		} else {
			dialog = new FileDialog((Dialog) window, title, mode);
		}
		dialog.setFilenameFilter(filter);
		dialog.setDirectory(dir);
		dialog.setFile(name);
		dialog.setVisible(true);
		String result = dialog.getFile();
		if (result != null) {
			if (filter.accept(null, result)) {
				File file = new File(dialog.getDirectory(), result);
				RecentFilesMenu.addRecent(file);
				return file;
			} else if (!open) {
				File file = new File(dialog.getDirectory(), PathUtils.enforceExtension(result, extension[0]));
				RecentFilesMenu.addRecent(file);
				return file;
			}
			showCannotOpenMsg(comp, result, null);
		}
		return null;
	}

	/**
	 * @param comp The {@link Component} to use for determining the parent {@link Frame} or
	 *            {@link Dialog}.
	 * @param name The name of the file that cannot be opened.
	 * @param throwable The {@link Throwable}, if any, that caused the failure.
	 */
	public static void showCannotOpenMsg(Component comp, String name, Throwable throwable) {
		if (throwable instanceof NewerDataFileVersionException) {
			WindowUtils.showError(comp, MessageFormat.format(UNABLE_TO_OPEN_WITH_EXCEPTION, name, throwable.getMessage()));
		} else {
			if (throwable != null) {
				Log.error(throwable);
			}
			WindowUtils.showError(comp, MessageFormat.format(UNABLE_TO_OPEN, name));
		}
	}

	/**
	 * Convenience for creating a regular expression that will match a file extension. This takes
	 * care of turning on case-insensitivity for those platforms that need it.
	 *
	 * @param extension A file name extension.
	 * @return The regular expression that will match the specified file name extension.
	 */
	public static String createExtensionMatcher(String extension) {
		StringBuilder builder = new StringBuilder();

		if (Platform.isMacintosh() || Platform.isWindows()) {
			builder.append("(?i)"); //$NON-NLS-1$
		}
		builder.append("^.*\\"); //$NON-NLS-1$
		if (!extension.startsWith(".")) { //$NON-NLS-1$
			builder.append('.');
		}
		builder.append(extension);
		builder.append('$');
		return builder.toString();
	}

	private StdFileDialog(String... extension) {
		for (String one : extension) {
			mFileNameMatchers.add(createExtensionMatcher(one));
		}
	}

	@Override
	public boolean accept(File dir, String name) {
		if (mFileNameMatchers.isEmpty()) {
			return true;
		}
		for (String one : mFileNameMatchers) {
			if (name.matches(one)) {
				return true;
			}
		}
		return false;
	}
}
