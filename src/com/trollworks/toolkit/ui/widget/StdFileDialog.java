/*
 * Copyright (c) 1998-2016 by Richard A. Wilkes. All rights reserved.
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
import com.trollworks.toolkit.utility.Preferences;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Provides standard file dialog handling. */
public class StdFileDialog {
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

	private static final String	MODULE		= "StdFileDialog";	//$NON-NLS-1$
	private static final String	LAST_DIR	= "LastDir";		//$NON-NLS-1$

	/**
	 * Creates a new {@link StdFileDialog}.
	 *
	 * @param comp The parent {@link Component} of the dialog. May be <code>null</code>.
	 * @param title The title to use. May be <code>null</code>.
	 * @param filters The file filters to make available. If there are none, then the
	 *            <code>showAllFilter</code> flag will be forced to <code>true</code>.
	 * @return The chosen {@link File} or <code>null</code>.
	 */
	public static File showOpenDialog(Component comp, String title, List<FileNameExtensionFilter> filters) {
		return showOpenDialog(comp, title, filters != null ? filters.toArray(new FileNameExtensionFilter[filters.size()]) : null);
	}

	/**
	 * Creates a new {@link StdFileDialog}.
	 *
	 * @param comp The parent {@link Component} of the dialog. May be <code>null</code>.
	 * @param title The title to use. May be <code>null</code>.
	 * @param filters The file filters to make available. If there are none, then the
	 *            <code>showAllFilter</code> flag will be forced to <code>true</code>.
	 * @return The chosen {@link File} or <code>null</code>.
	 */
	public static File showOpenDialog(Component comp, String title, FileNameExtensionFilter... filters) {
		Preferences prefs = Preferences.getInstance();
		String last = prefs.getStringValue(MODULE, LAST_DIR);
		if (last != null) {
			if (!new File(last).isDirectory()) {
				last = null;
			}
		}
		JFileChooser dialog = new JFileChooser(last);
		dialog.setDialogTitle(title);
		if (filters != null && filters.length > 0) {
			dialog.setAcceptAllFileFilterUsed(false);
			for (FileNameExtensionFilter filter : filters) {
				dialog.addChoosableFileFilter(filter);
			}
		} else {
			dialog.setAcceptAllFileFilterUsed(true);
		}
		int result = dialog.showOpenDialog(comp);
		if (result != JFileChooser.ERROR_OPTION) {
			File current = dialog.getCurrentDirectory();
			if (current != null) {
				prefs.setValue(MODULE, LAST_DIR, current.getAbsolutePath());
			}
		}
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = dialog.getSelectedFile();
			RecentFilesMenu.addRecent(file);
			return file;
		}
		return null;
	}

	/**
	 * Creates a new {@link StdFileDialog}.
	 *
	 * @param comp The parent {@link Component} of the dialog. May be <code>null</code>.
	 * @param title The title to use. May be <code>null</code>.
	 * @param suggestedFile The suggested file to save as. May be <code>null</code>.
	 * @param filters The file filters to make available. If there are none, then the
	 *            <code>showAllFilter</code> flag will be forced to <code>true</code>.
	 * @return The chosen {@link File} or <code>null</code>.
	 */
	public static File showSaveDialog(Component comp, String title, File suggestedFile, List<FileNameExtensionFilter> filters) {
		return showSaveDialog(comp, title, suggestedFile, filters != null ? filters.toArray(new FileNameExtensionFilter[filters.size()]) : null);
	}

	/**
	 * Creates a new {@link StdFileDialog}.
	 *
	 * @param comp The parent {@link Component} of the dialog. May be <code>null</code>.
	 * @param title The title to use. May be <code>null</code>.
	 * @param suggestedFile The suggested file to save as. May be <code>null</code>.
	 * @param filters The file filters to make available. If there are none, then the
	 *            <code>showAllFilter</code> flag will be forced to <code>true</code>.
	 * @return The chosen {@link File} or <code>null</code>.
	 */
	public static File showSaveDialog(Component comp, String title, File suggestedFile, FileNameExtensionFilter... filters) {
		Preferences prefs = Preferences.getInstance();
		String last = suggestedFile != null ? suggestedFile.getParent() : prefs.getStringValue(MODULE, LAST_DIR);
		if (last != null) {
			if (!new File(last).isDirectory()) {
				last = null;
			}
		}
		JFileChooser dialog = new JFileChooser(last);
		dialog.setDialogTitle(title);
		if (filters != null && filters.length > 0) {
			dialog.setAcceptAllFileFilterUsed(false);
			for (FileNameExtensionFilter filter : filters) {
				dialog.addChoosableFileFilter(filter);
			}
		} else {
			dialog.setAcceptAllFileFilterUsed(true);
		}
		int result = dialog.showSaveDialog(comp);
		if (result != JFileChooser.ERROR_OPTION) {
			File current = dialog.getCurrentDirectory();
			if (current != null) {
				prefs.setValue(MODULE, LAST_DIR, current.getAbsolutePath());
			}
		}
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = dialog.getSelectedFile();
			if (filters != null) {
				FileFilter fileFilter = dialog.getFileFilter();
				if (!fileFilter.accept(file)) {
					for (FileNameExtensionFilter filter : filters) {
						if (filter == fileFilter) {
							file = new File(file.getParentFile(), PathUtils.enforceExtension(file.getName(), filter.getExtensions()[0]));
							break;
						}
					}
				}
			}
			RecentFilesMenu.addRecent(file);
			return file;
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
}
