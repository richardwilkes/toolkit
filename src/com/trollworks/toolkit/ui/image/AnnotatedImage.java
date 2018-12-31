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

package com.trollworks.toolkit.ui.image;

import com.trollworks.toolkit.io.Log;
import com.trollworks.toolkit.utility.FileType;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Read and write annotated image data. */
public class AnnotatedImage {
    private static final String PNG_METADATA_FORMAT_NAME    = "javax_imageio_png_1.0"; //$NON-NLS-1$
    private static final String PHYS_TAG                    = "pHYs"; //$NON-NLS-1$
    private static final String PIXELS_PER_UNIT_X_AXIS_ATTR = "pixelsPerUnitXAxis"; //$NON-NLS-1$
    private static final String PIXELS_PER_UNIT_Y_AXIS_ATTR = "pixelsPerUnitYAxis"; //$NON-NLS-1$
    private static final String UNIT_SPECIFIER_ATTR         = "unitSpecifier"; //$NON-NLS-1$
    private static final String METER                       = "meter"; //$NON-NLS-1$
    private static final String TEXT_TAG                    = "tEXt"; //$NON-NLS-1$
    private static final String TEXT_ENTRY_TAG              = "tEXtEntry"; //$NON-NLS-1$
    private static final String KEYWORD_ATTR                = "keyword"; //$NON-NLS-1$
    private static final String VALUE_ATTR                  = "value"; //$NON-NLS-1$
    public StdImage             mImage;
    public int                  mDPI;
    public Map<String, String>  mExtraMetadata;

    /**
     * Reads a PNG image from the file and any metadata written by
     * {@link #writePNG(OutputStream, Image, int, Map)}.
     *
     * @param file The file to read from.
     * @return The annotated image data.
     */
    public static final AnnotatedImage readPNG(File file) {
        try (FileInputStream in = new FileInputStream(file)) {
            return readPNG(in);
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * Reads a PNG image from the stream and any metadata written by
     * {@link #writePNG(OutputStream, Image, int, Map)}.
     *
     * @param in The stream to read from.
     * @return The annotated image data.
     */
    public static final AnnotatedImage readPNG(InputStream in) {
        ImageReader reader = null;
        try (ImageInputStream stream = ImageIO.createImageInputStream(in)) {
            AnnotatedImage result = new AnnotatedImage();
            result.mExtraMetadata = new HashMap<>();
            reader                = ImageIO.getImageReaders(stream).next();
            reader.setInput(stream);
            try {
                IIOMetadataNode root    = (IIOMetadataNode) reader.getImageMetadata(0).getAsTree(PNG_METADATA_FORMAT_NAME);
                NodeList        entries = root.getElementsByTagName(TEXT_ENTRY_TAG);
                int             length  = entries.getLength();
                for (int i = 0; i < length; i++) {
                    IIOMetadataNode node = (IIOMetadataNode) entries.item(i);
                    result.mExtraMetadata.put(node.getAttribute(KEYWORD_ATTR), node.getAttribute(VALUE_ATTR));
                }
                entries = root.getElementsByTagName(PHYS_TAG);
                if (entries.getLength() > 0) {
                    IIOMetadataNode node = (IIOMetadataNode) entries.item(0);
                    if (node.getAttribute(UNIT_SPECIFIER_ATTR) == METER) {
                        result.mDPI = (int) (Integer.parseInt(node.getAttribute(PIXELS_PER_UNIT_X_AXIS_ATTR)) * 0.0254f + 0.5f);
                    }
                }
            } catch (Exception exception) {
                Log.error(exception);
            }
            result.mImage = StdImage.getToolkitImage(reader.read(0));
            return result;
        } catch (Exception exception) {
            if (reader != null) {
                reader.dispose();
            }
            return null;
        }
    }

    /**
     * Writes a PNG image to a file.
     *
     * @param file          The file to write to.
     * @param image         The image to use.
     * @param dpi           The DPI to use. Values less than 1 are ignored.
     * @param extraMetadata Additional metadata to store. May pass in <code>null</code>.
     * @return <code>true</code> on success.
     */
    public static final boolean writePNG(File file, Image image, int dpi, Map<String, String> extraMetadata) {
        boolean result;
        try (FileOutputStream os = new FileOutputStream(file)) {
            result = writePNG(os, image, dpi, extraMetadata);
        } catch (Exception exception) {
            result = false;
        }
        return result;
    }

    /**
     * Writes a PNG image to a stream.
     *
     * @param os            The stream to write to.
     * @param image         The image to use.
     * @param dpi           The DPI to use. Values less than 1 are ignored.
     * @param extraMetadata Additional metadata to store. May pass in <code>null</code>.
     * @return <code>true</code> on success.
     */
    public static final boolean writePNG(OutputStream os, Image image, int dpi, Map<String, String> extraMetadata) {
        ImageWriter writer = null;
        try (ImageOutputStream stream = ImageIO.createImageOutputStream(os)) {
            IIOMetadata        metaData = null;
            StdImage           img      = StdImage.getToolkitImage(image);
            ImageTypeSpecifier type     = ImageTypeSpecifier.createFromRenderedImage(img);
            writer = ImageIO.getImageWriters(type, FileType.PNG_EXTENSION).next();
            if (dpi > 0 || extraMetadata != null && !extraMetadata.isEmpty()) {
                metaData = writer.getDefaultImageMetadata(type, null);
                try {
                    Node root = metaData.getAsTree(PNG_METADATA_FORMAT_NAME);
                    if (dpi > 0) {
                        String          ppu      = Integer.toString((int) (dpi / 0.0254));
                        IIOMetadataNode physNode = new IIOMetadataNode(PHYS_TAG);
                        physNode.setAttribute(PIXELS_PER_UNIT_X_AXIS_ATTR, ppu);
                        physNode.setAttribute(PIXELS_PER_UNIT_Y_AXIS_ATTR, ppu);
                        physNode.setAttribute(UNIT_SPECIFIER_ATTR, METER);
                        root.appendChild(physNode);
                    }
                    if (extraMetadata != null && !extraMetadata.isEmpty()) {
                        IIOMetadataNode textNode = new IIOMetadataNode(TEXT_TAG);
                        for (Entry<String, String> entry : extraMetadata.entrySet()) {
                            IIOMetadataNode textEntryNode = new IIOMetadataNode(TEXT_ENTRY_TAG);
                            textEntryNode.setAttribute(KEYWORD_ATTR, entry.getKey());
                            textEntryNode.setAttribute(VALUE_ATTR, entry.getValue());
                            textNode.appendChild(textEntryNode);
                        }
                        root.appendChild(textNode);
                    }
                    metaData.setFromTree(PNG_METADATA_FORMAT_NAME, root);
                } catch (Exception exception) {
                    Log.error(exception);
                }
            }
            writer.setOutput(stream);
            writer.write(new IIOImage(img, null, metaData));
            stream.flush();
            writer.dispose();
            return true;
        } catch (Exception exception) {
            if (writer != null) {
                writer.dispose();
            }
            return false;
        }
    }
}
