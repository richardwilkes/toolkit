/*
 * Copyright (c) 2019 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.utility;

import com.trollworks.toolkit.workarounds.AppHome;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LinuxDesktop {
    public static void createDesktopFile(BundleInfo bi, List<String> execArgs, List<String> categories, List<String> keywords) throws IOException {
        String exeName = bi.getExecutableName().replaceAll(" ", "\\ ");
        if (exeName != "") {
            String home = System.getenv("HOME");
            if (home == null) {
                home = ".";
            }
            home = home.replaceAll(" ", "\\ ");
            String appHome  = AppHome.get().toString().replaceAll(" ", "\\ ");
            Path   filePath = Paths.get(home, ".local", "share", "applications");
            Files.createDirectories(filePath);
            filePath = filePath.resolve(exeName + ".desktop");
            try (PrintStream out = new PrintStream(filePath.toString())) {
                out.println("[Desktop Entry]");
                out.println("Version=1.0");
                out.println("Type=Application");
                out.println("Name=" + bi.getName());
                out.println("Icon=" + appHome + "/support/app.png");
                out.println("Path=" + appHome);
                out.print("Exec=" + appHome + "/" + exeName);
                for (String arg : execArgs) {
                    out.print(" ");
                    out.print(arg);
                }
                out.println();
                out.print("Categories=");
                boolean first = true;
                for (String category : categories) {
                    if (first) {
                        first = false;
                    } else {
                        out.print(";");
                    }
                    out.print(category);
                }
                out.println();
                out.print("Keywords=");
                first = true;
                for (String keyword : keywords) {
                    if (first) {
                        first = false;
                    } else {
                        out.print(";");
                    }
                    out.print(keyword);
                }
                out.println();
                out.println("Terminal=false");
            }
            Set<PosixFilePermission> attrs = new HashSet<>();
            attrs.add(PosixFilePermission.OWNER_READ);
            attrs.add(PosixFilePermission.OWNER_WRITE);
            attrs.add(PosixFilePermission.OWNER_EXECUTE);
            attrs.add(PosixFilePermission.GROUP_READ);
            attrs.add(PosixFilePermission.GROUP_WRITE);
            attrs.add(PosixFilePermission.GROUP_EXECUTE);
            attrs.add(PosixFilePermission.OTHERS_READ);
            attrs.add(PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(filePath, attrs);
        }
    }
}
