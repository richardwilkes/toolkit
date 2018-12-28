/*
 * Copyright (c) 1998-2018 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.io.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class JsonStreamWriter extends OutputStreamWriter {
    private boolean mNeedComma;

    public JsonStreamWriter(OutputStream out) {
        super(out, StandardCharsets.UTF_8);
    }

    public void key(String key) throws IOException {
        if (mNeedComma) {
            append(',');
            mNeedComma = false;
        }
        append(Json.quote(key));
        append(':');
    }

    public void startObject() throws IOException {
        if (mNeedComma) {
            append(',');
            mNeedComma = false;
        }
        append('{');
    }

    public void endObject() throws IOException {
        append('}');
        mNeedComma = true;
    }

    public void startArray() throws IOException {
        if (mNeedComma) {
            append(',');
            mNeedComma = false;
        }
        append('[');
    }

    public void endArray() throws IOException {
        append(']');
        mNeedComma = true;
    }

    public void value(String value) throws IOException {
        commaIfNeeded();
        append(Json.quote(value));
    }

    public void value(Number value) throws IOException {
        commaIfNeeded();
        append(Json.toString(value));
    }

    public void value(boolean value) throws IOException {
        commaIfNeeded();
        append(value ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void value(short value) throws IOException {
        commaIfNeeded();
        append(Short.toString(value));
    }

    public void value(int value) throws IOException {
        commaIfNeeded();
        append(Integer.toString(value));
    }

    public void value(long value) throws IOException {
        commaIfNeeded();
        append(Long.toString(value));
    }

    public void value(float value) throws IOException {
        value(Float.valueOf(value));
    }

    public void value(double value) throws IOException {
        value(Double.valueOf(value));
    }

    public void keyValue(String key, String value) throws IOException {
        key(key);
        append(Json.quote(value));
        mNeedComma = true;
    }

    public void keyValueNot(String key, String value, String not) throws IOException {
        if (value == null ? not != null : !value.equals(not)) {
            key(key);
            append(Json.quote(value));
            mNeedComma = true;
        }
    }

    public void keyValue(String key, Number value) throws IOException {
        key(key);
        append(Json.toString(value));
        mNeedComma = true;
    }

    public void keyValueNot(String key, Number value, Number not) throws IOException {
        if (value == null ? not != null : !value.equals(not)) {
            key(key);
            append(Json.toString(value));
            mNeedComma = true;
        }
    }

    public void keyValue(String key, boolean value) throws IOException {
        key(key);
        append(value ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
        mNeedComma = true;
    }

    public void keyValueNot(String key, boolean value, boolean not) throws IOException {
        if (value != not) {
            key(key);
            append(value ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
            mNeedComma = true;
        }
    }

    public void keyValue(String key, short value) throws IOException {
        key(key);
        append(Short.toString(value));
        mNeedComma = true;
    }

    public void keyValueNot(String key, short value, short not) throws IOException {
        if (value != not) {
            key(key);
            append(Short.toString(value));
            mNeedComma = true;
        }
    }

    public void keyValue(String key, int value) throws IOException {
        key(key);
        append(Integer.toString(value));
        mNeedComma = true;
    }

    public void keyValueNot(String key, int value, int not) throws IOException {
        if (value != not) {
            key(key);
            append(Integer.toString(value));
            mNeedComma = true;
        }
    }

    public void keyValue(String key, long value) throws IOException {
        key(key);
        append(Long.toString(value));
        mNeedComma = true;
    }

    public void keyValueNot(String key, long value, long not) throws IOException {
        if (value != not) {
            key(key);
            append(Long.toString(value));
            mNeedComma = true;
        }
    }

    public void keyValue(String key, float value) throws IOException {
        keyValue(key, Float.valueOf(value));
    }

    public void keyValueNot(String key, float value, float not) throws IOException {
        if (value != not) {
            keyValue(key, Float.valueOf(value));
        }
    }

    public void keyValue(String key, double value) throws IOException {
        keyValue(key, Double.valueOf(value));
    }

    public void keyValueNot(String key, double value, double not) throws IOException {
        if (value != not) {
            keyValue(key, Double.valueOf(value));
        }
    }

    private void commaIfNeeded() throws IOException {
        if (mNeedComma) {
            append(',');
        } else {
            mNeedComma = true;
        }
    }
}
