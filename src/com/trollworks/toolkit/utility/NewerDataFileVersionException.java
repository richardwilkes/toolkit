/*
 * Copyright (c) 1998-2020 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.utility;

import java.io.IOException;
import java.text.MessageFormat;

/** An exception for data files that are too new to be loaded. */
public class NewerDataFileVersionException extends IOException {
    /** Creates a new {@link NewerDataFileVersionException}. */
    public NewerDataFileVersionException() {
        super(MessageFormat.format(I18n.Text("The data file is from a newer version of {0} and cannot be loaded."), BundleInfo.getDefault().getName()));
    }
}
