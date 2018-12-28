package com.trollworks.toolkit.io;

import java.io.IOException;
import java.io.OutputStream;

public class NonClosingPassThroughOutputStream extends OutputStream {
    private OutputStream mOut;

    public NonClosingPassThroughOutputStream(OutputStream out) {
        mOut = out;
    }

    @Override
    public void write(int b) throws IOException {
        mOut.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        mOut.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        mOut.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        mOut.flush();
    }

    @Override
    public void close() throws IOException {
        mOut.flush();
    }
}
