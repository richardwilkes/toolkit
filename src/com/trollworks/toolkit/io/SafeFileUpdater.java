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

package com.trollworks.toolkit.io;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.utility.Localization;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides transactional file writing. By using this class to wrap all file updates, you can
 * guarantee that all files will be updated as a logical unit, or, on failure, none will be
 * modified.
 */
public class SafeFileUpdater {
	@Localize("No transaction in progress.")
	@Localize(locale = "ru", value = "Нет действий в обработке")
	@Localize(locale = "de", value = "Keine Transaktion wird ausgeführt.")
	@Localize(locale = "es", value = "No hay transaciones en progreso.")
	private static String		NO_TRANSACTION_IN_PROGRESS;
	@Localize("Unable to swap files.")
	@Localize(locale = "ru", value = "Невозможно поменять файлы.")
	@Localize(locale = "de", value = "Kann Dateien nicht auswechseln.")
	@Localize(locale = "es", value = "Imposible crear ficheros de intercambio.")
	private static String		FILE_SWAP_FAILED;
	@Localize("\"file\" may not be null.")
	@Localize(locale = "ru", value = "\"файл\" не может быть пустым.")
	@Localize(locale = "de", value = "\"file\" darf nicht null sein.")
	@Localize(locale = "es", value = "\"archivo\" no puede ser nulo.")
	private static String		MAY_NOT_BE_NULL;
	@Localize("\"file\" may not refer to a directory.")
	@Localize(locale = "ru", value = "\"файл\" не может ссылаться на папку.")
	@Localize(locale = "de", value = "\"file\" darf kein Verzeichnis sein.")
	@Localize(locale = "es", value = "\"archivo\" no puede ser un directorio.")
	private static String		MAY_NOT_BE_DIRECTORY;

	static {
		Localization.initialize();
	}

	private HashMap<File, File>	mFiles;
	private int					mStarted;

	/** Creates a new transaction. */
	public SafeFileUpdater() {
		mStarted = 0;
		mFiles = new HashMap<>();
	}

	/**
	 * Aborts the transaction, leaving the files existing before the transaction was started as they
	 * were.
	 */
	public void abort() {
		mStarted = 0;
		for (File file : mFiles.values()) {
			file.delete();
		}
		mFiles.clear();
	}

	/** Call to begin a transaction. */
	public void begin() {
		mStarted++;
	}

	/**
	 * Commits the transaction. If a transactional file was removed, the corresponding real file
	 * will be removed as well.
	 *
	 * @throws IOException if a failure occurs. In this case, no files will be altered, as if
	 *             <code>abort()</code> had been called instead.
	 */
	public void commit() throws IOException {
		if (mStarted == 0) {
			throw new IllegalStateException(NO_TRANSACTION_IN_PROGRESS);
		}

		if (--mStarted == 0) {
			HashMap<File, File> renameMap = new HashMap<>();
			File destFile;
			File tmpFile;

			// Attempt to swap all the necessary files
			try {
				for (Map.Entry<File, File> entry1 : mFiles.entrySet()) {
					destFile = entry1.getKey();
					tmpFile = entry1.getValue();

					if (destFile.exists()) {
						File tmpRenameFile = File.createTempFile("ren", null, destFile.getParentFile()); //$NON-NLS-1$

						if (tmpRenameFile.delete() && destFile.renameTo(tmpRenameFile)) {
							renameMap.put(destFile, tmpRenameFile);
						} else {
							throw new IOException(FILE_SWAP_FAILED);
						}
					}

					if (tmpFile.exists() && !tmpFile.renameTo(destFile)) {
						throw new IOException(FILE_SWAP_FAILED);
					}
				}
			} catch (IOException ioe) {
				for (Map.Entry<File, File> entry2 : renameMap.entrySet()) {
					destFile = entry2.getKey();
					tmpFile = entry2.getValue();

					destFile.delete();
					tmpFile.renameTo(destFile);
				}
				abort();
				throw ioe;
			}

			// Clean up our temporary files
			for (File delFile1 : renameMap.values()) {
				delFile1.delete();
			}
			for (File delFile2 : mFiles.values()) {
				delFile2.delete();
			}
			mFiles.clear();
		}
	}

	/**
	 * When <code>commit</code> is called, the transactional file obtained from this call will be
	 * swapped with the original.
	 *
	 * @param file The file to be created/modified.
	 * @return A <code>File</code> object that can be written to as if it were the file specified.
	 *         It initially points to an empty, zero-byte file.
	 * @throws IOException if the transactional file cannot be created.
	 */
	public File getTransactionFile(File file) throws IOException {
		if (mStarted == 0) {
			throw new IllegalStateException(NO_TRANSACTION_IN_PROGRESS);
		} else if (file == null) {
			throw new IllegalArgumentException(MAY_NOT_BE_NULL);
		} else if (file.isDirectory()) {
			throw new IllegalArgumentException(MAY_NOT_BE_DIRECTORY);
		}

		File transFile = mFiles.get(file);
		if (transFile == null) {
			transFile = File.createTempFile(".trn", null, file.getParentFile()); //$NON-NLS-1$
			mFiles.put(file, transFile);
		}
		return transFile;
	}
}
