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
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.utility.Localization;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.Collection;

import javax.swing.JOptionPane;

/** Provides the "Save" command. */
public class SaveCommand extends Command {
	@Localize("Save")
	@Localize(locale = "ru", value = "Сохранить")
	@Localize(locale = "de", value = "Speichern")
	@Localize(locale = "es", value = "Salvar")
	private static String			SAVE;
	@Localize("Save changes to \"{0}\"?")
	@Localize(locale = "ru", value = "Сохранить изменения в \"{0}\"?")
	@Localize(locale = "de", value = "Änderungen in \"{0}\" speichern?")
	@Localize(locale = "es", value = "Salvar cambios en \"{0}\"?")
	private static String			SAVE_CHANGES;

	static {
		Localization.initialize();
	}

	/** The action command this command will issue. */
	public static final String		CMD_SAVE	= "Save";				//$NON-NLS-1$

	/** The singleton {@link SaveCommand}. */
	public static final SaveCommand	INSTANCE	= new SaveCommand();

	private SaveCommand() {
		super(SAVE, CMD_SAVE, KeyEvent.VK_S);
	}

	@Override
	public void adjust() {
		Saveable saveable = getTarget(Saveable.class);
		setEnabled(saveable != null ? saveable.isModified() : false);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		save(getTarget(Saveable.class));
	}

	/**
	 * Makes an attempt to save the specified {@link Saveable}s if any have been modified.
	 *
	 * @param saveables The {@link Saveable}s to work on.
	 * @return <code>false</code> if a save was cancelled or failed.
	 */
	public static boolean attemptSave(Collection<Saveable> saveables) {
		UIUtilities.forceFocusToAccept();
		for (Saveable saveable : saveables) {
			if (!attemptSaveInternal(saveable)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Makes an attempt to save the specified {@link Saveable} if it has been modified.
	 *
	 * @param saveable The {@link Saveable} to work on.
	 * @return <code>false</code> if the save was cancelled or failed.
	 */
	public static boolean attemptSave(Saveable saveable) {
		if (saveable != null) {
			UIUtilities.forceFocusToAccept();
			return attemptSaveInternal(saveable);
		}
		return true;
	}

	private static boolean attemptSaveInternal(Saveable saveable) {
		if (saveable.isModified()) {
			saveable.toFrontAndFocus();
			int answer = JOptionPane.showConfirmDialog(UIUtilities.getComponentForDialog(saveable), MessageFormat.format(SAVE_CHANGES, saveable.getSaveTitle()), SAVE, JOptionPane.YES_NO_CANCEL_OPTION);
			if (answer == JOptionPane.CANCEL_OPTION || answer == JOptionPane.CLOSED_OPTION) {
				return false;
			}
			if (answer == JOptionPane.YES_OPTION) {
				SaveCommand.save(saveable);
				if (saveable.isModified()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Allows the user to save the file.
	 *
	 * @param saveable The {@link Saveable} to work on.
	 * @return The file(s) actually written to. May be empty.
	 */
	public static File[] save(Saveable saveable) {
		if (saveable == null) {
			return new File[0];
		}
		File file = saveable.getBackingFile();
		if (file != null) {
			File[] files = saveable.saveTo(file);
			for (File one : files) {
				RecentFilesMenu.addRecent(one);
			}
			return files;

		}
		return SaveAsCommand.saveAs(saveable);
	}
}
