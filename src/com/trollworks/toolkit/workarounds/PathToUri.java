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

package com.trollworks.toolkit.workarounds;

import java.net.URI;
import java.nio.file.Path;

public class PathToUri {
    /**
     * There is a bug in the JDK creation of URI's from Paths (and maybe elsewhere) in which the
     * escaping of spaces is done incorrectly. This fixes that up.
     */
    public static URI toFixedUri(Path path) {
        URI    uri  = path.toUri();
        String text = uri.toString();
        if (text.contains("%2520")) {
            text = text.replaceAll("%2520", "%20");
            uri = URI.create(text);
        }
        return uri;
    }

}
