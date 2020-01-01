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

import java.nio.ByteBuffer;

public final class Buffers {
    public static byte[] toBytes(char[] data) {
        ByteBuffer bytes = ByteBuffer.allocate(data.length * Character.BYTES);
        bytes.asCharBuffer().put(data);
        return bytes.array();
    }

    public static byte[] toBytes(short[] data) {
        ByteBuffer bytes = ByteBuffer.allocate(data.length * Short.BYTES);
        bytes.asShortBuffer().put(data);
        return bytes.array();
    }

    public static byte[] toBytes(int[] data) {
        ByteBuffer bytes = ByteBuffer.allocate(data.length * Integer.BYTES);
        bytes.asIntBuffer().put(data);
        return bytes.array();
    }

    public static byte[] toBytes(long[] data) {
        ByteBuffer bytes = ByteBuffer.allocate(data.length * Long.BYTES);
        bytes.asLongBuffer().put(data);
        return bytes.array();
    }

    public static byte[] toBytes(float[] data) {
        ByteBuffer bytes = ByteBuffer.allocate(data.length * Float.BYTES);
        bytes.asFloatBuffer().put(data);
        return bytes.array();
    }

    public static byte[] toBytes(double[] data) {
        ByteBuffer bytes = ByteBuffer.allocate(data.length * Double.BYTES);
        bytes.asDoubleBuffer().put(data);
        return bytes.array();
    }
}
