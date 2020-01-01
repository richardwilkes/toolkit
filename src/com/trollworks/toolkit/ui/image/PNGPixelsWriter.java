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

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;

class PNGPixelsWriter implements AutoCloseable {
    private enum FilterType {
        // Do not change the order of these... and DEFAULT must be last
        NONE, SUB, UP, AVERAGE, PAETH, DEFAULT
    }

    private       OutputStream mOut;
    private final int          mBlockLen;
    private final int          mBytesRow;
    private       int          mOffset;
    private       int          mAvailable;
    private       Deflater     mDeflater;
    private       FilterType   mFilterType;
    private       FilterType   mCurrentFilterType;
    private       byte[]       mRow;
    private       byte[]       mPreviousRow;
    private       byte[]       mRowFilter;
    private       byte[]       mBuffer;

    PNGPixelsWriter(OutputStream out, int cols, int rows) {
        mOut = out;
        mBytesRow = 4 * cols;
        mBlockLen = mBytesRow + 1;
        mRow = new byte[mBlockLen];
        mRowFilter = new byte[mBlockLen];
        mPreviousRow = new byte[mBlockLen];
        mBuffer = new byte[32768];
        mAvailable = mBuffer.length;
        mDeflater = new Deflater(9);
        if (cols * (long) rows < 1024) {
            mFilterType = FilterType.NONE;
        } else if (rows == 1) {
            mFilterType = FilterType.SUB;
        } else if (cols == 1) {
            mFilterType = FilterType.UP;
        } else {
            mFilterType = FilterType.PAETH;
        }
        mCurrentFilterType = mFilterType;
        if (mCurrentFilterType != FilterType.NONE && mCurrentFilterType != FilterType.SUB) {
            mCurrentFilterType = FilterType.SUB;
        }
    }

    void writeRow(byte[] scanline) throws IOException {
        mCurrentFilterType = mFilterType;
        System.arraycopy(scanline, 0, mRow, 1, scanline.length);
        byte[] buffer = filter(mRowFilter);
        int    offset = 0;
        int    length = buffer.length;
        while (length > 0) {
            int amt = Math.min(length, mBlockLen);
            mDeflater.setInput(buffer, offset, amt);
            while (!mDeflater.needsInput()) {
                deflate();
            }
            offset += amt;
            length -= amt;
        }
        byte[] tmp = mRow;
        mRow = mPreviousRow;
        mPreviousRow = tmp;
    }

    private void deflate() throws IOException {
        int len = mDeflater.deflate(mBuffer, mOffset, mAvailable);
        if (len > 0) {
            mOffset += len;
            mAvailable -= len;
            if (mAvailable == 0) {
                flush();
            }
        }
    }

    private byte[] filter(byte[] buffer) {
        if (mCurrentFilterType == FilterType.NONE) {
            buffer = mRow;
        }
        buffer[0] = (byte) mCurrentFilterType.ordinal();
        switch (mCurrentFilterType) {
        case SUB:
            System.arraycopy(mRow, 1, buffer, 1, 4);
            for (int j = 1, i = 5; i <= mBytesRow; i++, j++) {
                buffer[i] = (byte) (mRow[i] - mRow[j]);
            }
            break;
        case UP:
            for (int i = 1; i <= mBytesRow; i++) {
                buffer[i] = (byte) (mRow[i] - mPreviousRow[i]);
            }
            break;
        case AVERAGE:
            for (int i = 1; i <= 4; i++) {
                buffer[i] = (byte) (mRow[i] - (mPreviousRow[i] & 0xFF) / 2);
            }
            for (int j = 1, i = 5; i <= mBytesRow; i++, j++) {
                buffer[i] = (byte) (mRow[i] - ((mPreviousRow[i] & 0xFF) + (mRow[j] & 0xFF)) / 2);
            }
            break;
        case PAETH:
            for (int i = 1; i <= 4; i++) {
                buffer[i] = (byte) paeth(mRow[i], 0, mPreviousRow[i] & 0xFF, 0);
            }
            for (int j = 1, i = 5; i <= mBytesRow; i++, j++) {
                buffer[i] = (byte) paeth(mRow[i], mRow[j] & 0xFF, mPreviousRow[i] & 0xFF, mPreviousRow[j] & 0xFF);
            }
            break;
        default:
            break;
        }
        return buffer;
    }

    private static int paeth(int r, int left, int up, int upleft) {
        // from http://www.libpng.org/pub/png/spec/1.2/PNG-Filters.html
        int p  = left + up - upleft;
        int pa = p >= left ? p - left : left - p;
        int pb = p >= up ? p - up : up - p;
        int pc = p >= upleft ? p - upleft : upleft - p;
        int predictor;
        if (pa <= pb && pa <= pc) {
            predictor = left;
        } else if (pb <= pc) {
            predictor = up;
        } else {
            predictor = upleft;
        }
        return r - predictor & 0xFF;
    }

    private void flush() throws IOException {
        if (mOffset > 0) {
            byte[] buffer = mBuffer;
            if (mAvailable > 0) {
                byte[] tmp = new byte[mBuffer.length - mAvailable];
                buffer = tmp;
                System.arraycopy(mBuffer, 0, tmp, 0, tmp.length);
            }
            AnnotatedImage.writeChunk(mOut, "IDAT", buffer);
            mOffset = 0;
            mAvailable = mBuffer.length;
        }
    }

    public void close() throws IOException {
        if (!mDeflater.finished()) {
            mDeflater.finish();
            while (!mDeflater.finished()) {
                deflate();
            }
        }
        flush();
        mOut.flush();
        mOffset = 0;
        mBuffer = null;
        mDeflater.end();
    }
}
