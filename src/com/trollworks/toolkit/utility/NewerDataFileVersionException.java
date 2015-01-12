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

package com.trollworks.toolkit.utility;

import com.trollworks.toolkit.annotation.Localize;

import java.io.IOException;
import java.text.MessageFormat;

/** An exception for data files that are too new to be loaded. */
public class NewerDataFileVersionException extends IOException {
	@Localize("The data file is from a newer version of {0} and cannot be loaded.")
	@Localize(locale = "ru", value = "Файл с данными относится к более поздней версии {0} и не может быть загружен.")
	@Localize(locale = "de", value = "Die Datendatei ist von einer neueren Version von {0} und kann nicht geladen werden.")
	@Localize(locale = "es", value = "El archivo se ha creado con una versión más reciente {0} y no puede abrirse")
	private static String	VERSION_NEWER;

	static {
		Localization.initialize();
	}

	/** Creates a new {@link NewerDataFileVersionException}. */
	public NewerDataFileVersionException() {
		super(MessageFormat.format(VERSION_NEWER, BundleInfo.getDefault().getName()));
	}
}
