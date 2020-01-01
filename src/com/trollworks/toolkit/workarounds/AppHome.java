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
import java.nio.file.Paths;

public final class AppHome {
    private static Path APP_HOME_PATH;

    public static synchronized void setup(Class<?> theClass) {
        if (APP_HOME_PATH == null) {
            // Fix the current working directory, as bundled apps break the normal logic.
            // Sadly, this still doesn't fix stuff references from the "default" filesystem
            // class, as it is already initialized to the wrong value and won't pick this
            // change up.
            String pwd = System.getenv("PWD");
            if (pwd != null && !pwd.isEmpty()) {
                System.setProperty("user.dir", pwd);
            }

            Path path;
            try {
                path = Paths.get(System.getProperty("java.home"));
                if (path.endsWith("Contents/runtime/Contents/Home")) {
                    // Running inside a macOS package
                    path = path.getParent().getParent().getParent().getParent().getParent();
                } else if (path.endsWith("runtime")) {
                    // Running inside a linux package
                    path = path.getParent();
                } else if (path.endsWith("support")) {
                    // Running inside module-ized package
                    path = path.getParent();
                } else {
                    URI uri = theClass.getProtectionDomain().getCodeSource().getLocation().toURI();
                    path = Paths.get(uri).normalize().getParent().toAbsolutePath();
                }
            } catch (Throwable throwable) {
                path = Paths.get(".");
            }
            APP_HOME_PATH = path.normalize().toAbsolutePath();
        }
    }

    /** @return The application's 'home' directory. */
    public static synchronized Path get() {
        return APP_HOME_PATH;
    }
}
