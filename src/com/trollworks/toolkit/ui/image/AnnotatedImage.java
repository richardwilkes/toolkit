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

import com.trollworks.toolkit.io.EndianUtils;
import com.trollworks.toolkit.io.Log;
import org.w3c.dom.NodeList;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

/** Read and write annotated image data. */
public class AnnotatedImage {
    private static final String   TROLLWORKS_TEXT_KEY = "twtk";
    public               StdImage mImage;
    public               String   mText;
    public               int      mDPI;

    /**
     * Reads a PNG image from the file and any metadata written by {@link #writePNG(OutputStream,
     * Image, int, String)}.
     *
     * @param file The file to read from.
     * @return The annotated image data.
     */
    public static final AnnotatedImage readPNG(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            return readPNG(in);
        }
    }

    /**
     * Reads a PNG image from the stream and any metadata written by {@link #writePNG(OutputStream,
     * Image, int, String)}.
     *
     * @param in The stream to read from.
     * @return The annotated image data.
     */
    public static final AnnotatedImage readPNG(InputStream in) throws IOException {
        ImageReader reader = null;
        try (ImageInputStream stream = ImageIO.createImageInputStream(in)) {
            AnnotatedImage result = new AnnotatedImage();
            reader = ImageIO.getImageReaders(stream).next();
            reader.setInput(stream);
            try {
                IIOMetadataNode root    = (IIOMetadataNode) reader.getImageMetadata(0).getAsTree("javax_imageio_png_1.0");
                NodeList        entries = root.getElementsByTagName("iTXtEntry");
                int             length  = entries.getLength();
                for (int i = 0; i < length; i++) {
                    IIOMetadataNode node = (IIOMetadataNode) entries.item(i);
                    if (TROLLWORKS_TEXT_KEY.equals(node.getAttribute("keyword"))) {
                        result.mText = node.getAttribute("text");
                        break;
                    }
                }
                entries = root.getElementsByTagName("pHYs");
                if (entries.getLength() > 0) {
                    IIOMetadataNode node = (IIOMetadataNode) entries.item(0);
                    if ("meter".equals(node.getAttribute("unitSpecifier"))) {
                        result.mDPI = (int) (Integer.parseInt(node.getAttribute("pixelsPerUnitXAxis")) * 0.0254f + 0.5f);
                    }
                }
            } catch (Exception exception) {
                Log.error(exception);
            }
            result.mImage = StdImage.getToolkitImage(reader.read(0));
            reader.dispose();
            return result;
        } catch (IOException exception) {
            if (reader != null) {
                reader.dispose();
            }
            throw exception;
        }
    }

    /**
     * Writes a PNG image to a file.
     *
     * @param file  The file to write to.
     * @param image The image to use.
     * @param dpi   The DPI to use. Values less than 1 are ignored.
     * @param text  Additional text to store. May pass in {@code null}.
     */
    public static final void writePNG(File file, Image image, int dpi, String text) throws IOException {
        try (FileOutputStream os = new FileOutputStream(file)) {
            writePNG(os, image, dpi, text);
        }
    }

    /**
     * Writes a PNG image to a stream.
     * <p>
     * ImageIO provides similar capabilities, but I found that its best compression was
     * significantly worse than other tools (Photoshop, GIMP) produce. This implementation comes
     * close to what I was originally expecting, although it is hard-wired to only produce color
     * 32-bit pixels with an alpha channel, which is perfect for my needs.
     * <p>
     * This does NOT close the stream.
     *
     * @param os    The stream to write to.
     * @param image The image to use.
     * @param dpi   The DPI to use. Values less than 1 are ignored.
     * @param text  Additional text to store. May pass in {@code null}.
     */
    public static final void writePNG(OutputStream os, Image image, int dpi, String text) throws IOException {
        StdImage img  = StdImage.getToolkitImage(image);
        int      cols = img.getWidth();
        int      rows = img.getHeight();

        // Write PNG signature
        os.write(new byte[]{-119, 80, 78, 71, 13, 10, 26, 10});

        // Write IHDR
        byte[] data   = new byte[13];
        int    offset = 0;
        EndianUtils.writeBEInt(cols, data, offset);
        offset += 4;
        EndianUtils.writeBEInt(rows, data, offset);
        offset += 4;
        data[offset++] = 8;
        data[offset++] = 6;
        data[offset++] = 0;
        data[offset++] = 0;
        data[offset] = 0;
        writeChunk(os, "IHDR", data);

        // Add DPI, if provided
        if (dpi > 0) {
            int ppu = (int) (dpi / 0.0254 + 0.5);
            data = new byte[9];
            EndianUtils.writeBEInt(ppu, data, 0);
            EndianUtils.writeBEInt(ppu, data, 4);
            data[8] = 1;
            writeChunk(os, "pHYs", data);
        }

        // Write text, if provided
        if (text != null) {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            ba.write(TROLLWORKS_TEXT_KEY.getBytes(StandardCharsets.ISO_8859_1));
            ba.write(new byte[]{0, 1, 0, 0, 0});
            try (OutputStream deflater = new DeflaterOutputStream(ba)) {
                try (ByteArrayInputStream in = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))) {
                    byte[] buffer = new byte[4096];
                    int    amt;
                    while ((amt = in.read(buffer)) > 0) {
                        deflater.write(buffer, 0, amt);
                    }
                }
            }
            writeChunk(os, "iTXt", ba.toByteArray());
        }

        // Write pixel data
        try (PNGPixelsWriter pw = new PNGPixelsWriter(os, cols, rows)) {
            int[]  pixels   = new int[cols];
            byte[] scanline = new byte[cols * 4];
            for (int i = 0; i < rows; i++) {
                img.getRGB(0, i, cols, 1, pixels, 0, cols);
                for (int i1 = 0, j = 0; i1 < cols; i1++) {
                    int pixel = pixels[i1];
                    scanline[j++] = (byte) (pixel >> 16 & 0xFF);
                    scanline[j++] = (byte) (pixel >> 8 & 0xFF);
                    scanline[j++] = (byte) (pixel & 0xFF);
                    scanline[j++] = (byte) (pixel >> 24 & 0xFF);
                }
                pw.writeRow(scanline);
            }
        }

        // Write IEND
        writeChunk(os, "IEND", null);
    }

    static void writeChunk(OutputStream os, String id, byte[] data) throws IOException {
        CRC32  crcengine = new CRC32();
        byte[] temp      = new byte[4];
        int    len       = data != null ? data.length : 0;
        EndianUtils.writeBEInt(len, temp, 0);
        os.write(temp);
        byte[] idBytes = id.getBytes(StandardCharsets.ISO_8859_1);
        os.write(idBytes);
        crcengine.update(idBytes, 0, 4);
        if (len > 0) {
            os.write(data, 0, len);
            crcengine.update(data, 0, len);
        }
        byte[] crcval = new byte[4];
        EndianUtils.writeBEInt((int) crcengine.getValue(), crcval, 0);
        os.write(crcval, 0, 4);
    }
}
