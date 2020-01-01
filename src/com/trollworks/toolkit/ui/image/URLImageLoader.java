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

import java.net.MalformedURLException;
import java.net.URL;

public class URLImageLoader implements ImageLoader {
    private URL mURL;

    public URLImageLoader(URL url) {
        mURL = url;
    }

    @Override
    public StdImage loadImage(String name) {
        try {
            return StdImage.loadImage(new URL(mURL, name));
        } catch (MalformedURLException exception) {
            return null;
        }
    }

    @Override
    public int hashCode() {
        return 31 + (mURL == null ? 0 : mURL.toString().hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof URLImageLoader) {
            return mURL.toString().equals(((URLImageLoader) obj).mURL.toString());
        }
        return false;
    }
}
