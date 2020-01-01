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

package com.trollworks.toolkit.ui.image;

import java.util.Objects;

public class ModuleImageLoader implements ImageLoader {
    private Module mModule;
    private String mPath;

    public ModuleImageLoader(Module module, String path) {
        mModule = module;
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        mPath = path + "/";
    }

    @Override
    public StdImage loadImage(String name) {
        return StdImage.loadImage(mModule, mPath + name);
    }

    @Override
    public int hashCode() {
        int result = 31 + (mModule == null ? 0 : mModule.hashCode());
        return 31 * result + (mPath == null ? 0 : mPath.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ModuleImageLoader) {
            ModuleImageLoader o = (ModuleImageLoader) obj;
            return Objects.equals(mPath, o.mPath) && Objects.equals(mModule, o.mModule);
        }
        return false;
    }
}
