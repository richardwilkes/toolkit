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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Hash {
    public static byte[] md5(byte[] data) {
        return hash("MD5", data);
    }

    public static byte[] sha1(byte[] data) {
        return hash("SHA-1", data);
    }

    public static byte[] hash(String algorithm, byte[] data) {
        try {
            return MessageDigest.getInstance(algorithm).digest(data);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalArgumentException(exception);
        }
    }
}
