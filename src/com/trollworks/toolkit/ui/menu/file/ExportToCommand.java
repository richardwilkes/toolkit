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
import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.ui.widget.StdFileDialog;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.PathUtils;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

/** Provides the "Save As..." command. */
public class ExportToCommand extends Command {
	@Localize("Export To HTML\u2026")
	@Localize(locale = "ru", value = "Экспорт в HTML\u2026")
	@Localize(locale = "de", value = "Exportiere als HTML\u2026")
	@Localize(locale = "es", value = "Exportar a HTML\u2026")
	private static String				HTML;
	@Localize("Export To PDF\u2026")
	@Localize(locale = "ru", value = "Экспорт в PDF\u2026")
	@Localize(locale = "de", value = "Exportiere als PDF\u2026")
	@Localize(locale = "es", value = "Exportar a PDF\u2026")
	private static String				PDF;
	@Localize("Export to PNG\u2026")
	@Localize(locale = "ru", value = "Экспорт в PNG\u2026")
	@Localize(locale = "de", value = "Exportiere als PNG\u2026")
	@Localize(locale = "es", value = "Exportar a PNG\u2026")
	private static String				PNG;

	static {
		Localization.initialize();
	}

	/** The action command the HTML form of this command will issue. */
	public static final String			CMD_EXPORT_TO_HTML	= "ExportToHTML";													//$NON-NLS-1$
	/** The action command the PDF form of this command will issue. */
	public static final String			CMD_EXPORT_TO_PDF	= "ExportToPDF";													//$NON-NLS-1$
	/** The action command the PNG form of this command will issue. */
	public static final String			CMD_EXPORT_TO_PNG	= "ExportToPNG";													//$NON-NLS-1$
	/** The PNG extension. */
	public static final String			PNG_EXTENSION		= "png";															//$NON-NLS-1$
	/** The PDF extension. */
	public static final String			PDF_EXTENSION		= "pdf";															//$NON-NLS-1$
	/** The HTML extension. */
	public static final String			HTML_EXTENSION		= "html";															//$NON-NLS-1$
	/** The "Export To HTML...". */
	public static final ExportToCommand	EXPORT_TO_HTML		= new ExportToCommand(HTML, CMD_EXPORT_TO_HTML, HTML_EXTENSION);
	/** The "Export To PDF...". */
	public static final ExportToCommand	EXPORT_TO_PDF		= new ExportToCommand(PDF, CMD_EXPORT_TO_PDF, PDF_EXTENSION);
	/** The "Export To PNG...". */
	public static final ExportToCommand	EXPORT_TO_PNG		= new ExportToCommand(PNG, CMD_EXPORT_TO_PNG, PNG_EXTENSION);
	private String						mExtension;

	private ExportToCommand(String title, String cmd, String extension) {
		super(title, cmd);
		mExtension = extension;
	}

	@Override
	public void adjust() {
		setEnabled(getTarget() != null);
	}

	private Saveable getTarget() {
		Saveable saveable = getTarget(Saveable.class);
		if (saveable != null) {
			for (String extension : saveable.getAllowedExtensions()) {
				if (mExtension.equals(extension)) {
					return saveable;
				}
			}
		}
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		saveAs(getTarget());
	}

	/**
	 * Allows the user to save the file under another name.
	 *
	 * @param saveable The {@link Saveable} to work on.
	 * @return The file(s) actually written to.
	 */
	public File[] saveAs(Saveable saveable) {
		if (saveable == null) {
			return new File[0];
		}
		String path = saveable.getPreferredSavePath();
		File result = StdFileDialog.choose(saveable instanceof Component ? (Component) saveable : null, false, (String) getValue(NAME), PathUtils.getParent(path), PathUtils.getLeafName(path), mExtension);
		return result != null ? saveable.saveTo(result) : new File[0];
	}
}
