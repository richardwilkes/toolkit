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

import com.trollworks.toolkit.ui.image.StdImage;
import com.trollworks.toolkit.ui.image.StdImageSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Describes a file. */
public class FileType {
    /** The PNG extension. */
    public static final  String                PNG_EXTENSION  = "png";
    /** The GIF extension. */
    public static final  String                GIF_EXTENSION  = "gif";
    /** The JPEG extension. */
    public static final  String                JPEG_EXTENSION = "jpg";
    /** The PDF extension. */
    public static final  String                PDF_EXTENSION  = "pdf";
    /** The HTML extension. */
    public static final  String                HTML_EXTENSION = "html";
    /** The XML extension. */
    public static final  String                XML_EXTENSION  = "xml";
    private static final List<FileType>        TYPES          = new ArrayList<>();
    private static       Map<String, FileType> EXTENSION_MAP  = new HashMap<>();
    private              String                mExtension;
    private              StdImageSet           mIconSet;
    private              String                mDescription;
    private              String                mReferenceURL;
    private              FileProxyCreator      mFileProxyCreator;
    private              boolean               mAllowOpen;
    private              boolean               mRegisterAppForOpening;

    /**
     * Registers a new {@link FileType}, replacing any existing entry for the specified extension.
     *
     * @param extension                   The extension of the file.
     * @param iconset                     The {@link StdImageSet} to use for the file.
     * @param description                 A short description of the file type.
     * @param referenceURL                A URL that contains a description of this file type..
     * @param fileProxyCreator            The {@link FileProxyCreator} responsible for creating a
     *                                    {@link FileProxy} with this file's contents.
     * @param allowOpen                   Whether this {@link FileType} is allowed to be opened via
     *                                    the menu command.
     * @param shouldRegisterAppForOpening Whether this {@link FileType} should be registered as a
     *                                    document type that the application can open.
     */
    public static final void register(String extension, StdImageSet iconset, String description, String referenceURL, FileProxyCreator fileProxyCreator, boolean allowOpen, boolean shouldRegisterAppForOpening) {
        extension = normalizeExtension(extension);
        for (FileType type : TYPES) {
            if (type.mExtension.equals(extension)) {
                TYPES.remove(type);
                break;
            }
        }
        FileType fileType = new FileType(extension, iconset, description, referenceURL, fileProxyCreator, allowOpen, shouldRegisterAppForOpening);
        TYPES.add(fileType);
        EXTENSION_MAP.put(extension, fileType);
    }

    public static void registerPdf(StdImageSet iconset, FileProxyCreator creator, boolean allowOpen, boolean shouldRegisterAppForOpening) {
        register(PDF_EXTENSION, iconset, I18n.Text("PDF Files"), "https://www.adobe.com/devnet/pdf/pdf_reference_archive.html", creator, allowOpen, shouldRegisterAppForOpening);
    }

    public static void registerHtml(StdImageSet iconset, FileProxyCreator creator, boolean allowOpen, boolean shouldRegisterAppForOpening) {
        register(HTML_EXTENSION, iconset, I18n.Text("HTML Files"), "http://www.w3.org/TR/html", creator, allowOpen, shouldRegisterAppForOpening);
    }

    public static void registerXml(StdImageSet iconset, FileProxyCreator creator, boolean allowOpen, boolean shouldRegisterAppForOpening) {
        register(XML_EXTENSION, iconset, I18n.Text("XML Files"), "https://www.w3.org/XML/", creator, allowOpen, shouldRegisterAppForOpening);
    }

    public static void registerPng(StdImageSet iconset, FileProxyCreator creator, boolean allowOpen, boolean shouldRegisterAppForOpening) {
        register(PNG_EXTENSION, iconset, I18n.Text("PNG Files"), "http://www.libpng.org/pub/png/pngdocs.html", creator, allowOpen, shouldRegisterAppForOpening);
    }

    public static void registerGif(StdImageSet iconset, FileProxyCreator creator, boolean allowOpen, boolean shouldRegisterAppForOpening) {
        register(GIF_EXTENSION, iconset, I18n.Text("GIF Files"), "http://www.w3.org/Graphics/GIF/spec-gif89a.txt", creator, allowOpen, shouldRegisterAppForOpening);
    }

    public static void registerJpeg(StdImageSet iconset, FileProxyCreator creator, boolean allowOpen, boolean shouldRegisterAppForOpening) {
        register(JPEG_EXTENSION, iconset, I18n.Text("JPEG Files"), "http://www.w3.org/Graphics/JPEG/jfif3.pdf", creator, allowOpen, shouldRegisterAppForOpening);
    }

    public static FileNameExtensionFilter getPdfFilter() {
        return new FileNameExtensionFilter(I18n.Text("PDF Files"), PDF_EXTENSION);
    }

    public static FileNameExtensionFilter getHtmlFilter() {
        return new FileNameExtensionFilter(I18n.Text("HTML Files"), HTML_EXTENSION, "htm");
    }

    public static FileNameExtensionFilter getXmlFilter() {
        return new FileNameExtensionFilter(I18n.Text("XML Files"), XML_EXTENSION);
    }

    public static FileNameExtensionFilter getPngFilter() {
        return new FileNameExtensionFilter(I18n.Text("PNG Files"), PNG_EXTENSION);
    }

    public static FileNameExtensionFilter getGifFilter() {
        return new FileNameExtensionFilter(I18n.Text("GIF Files"), GIF_EXTENSION);
    }

    public static FileNameExtensionFilter getJpegFilter() {
        return new FileNameExtensionFilter(I18n.Text("JPEG Files"), JPEG_EXTENSION, "jpeg");
    }

    public static FileNameExtensionFilter getImageFilter() {
        List<String> extensions = new ArrayList<>();
        extensions.addAll(Arrays.asList(getPngFilter().getExtensions()));
        extensions.addAll(Arrays.asList(getJpegFilter().getExtensions()));
        extensions.addAll(Arrays.asList(getGifFilter().getExtensions()));
        return new FileNameExtensionFilter(I18n.Text("Image Files"), extensions.toArray(new String[0]));
    }

    public static final FileType getByExtension(String extension) {
        return extension != null ? EXTENSION_MAP.get(normalizeExtension(extension)) : null;
    }

    /**
     * @param extension The file extension to normalize.
     * @return The extension, minus the leading '.', if it was present.
     */
    public static String normalizeExtension(String extension) {
        if (extension == null) {
            return "";
        }
        if (extension.startsWith(".")) {
            extension = extension.substring(1);
        }
        return extension;
    }

    /** @return All of the registered {@link FileType}s. */
    public static final FileType[] getAll() {
        return TYPES.toArray(new FileType[0]);
    }

    /** @return All of the registered {@link FileType}s that can be opened. */
    public static final FileType[] getOpenable() {
        ArrayList<FileType> openable = new ArrayList<>();
        for (FileType type : TYPES) {
            if (type.allowOpen()) {
                openable.add(type);
            }
        }
        return openable.toArray(new FileType[0]);
    }

    /**
     * @param nameForAggregate The name for a {@link FileNameExtensionFilter} that will open them
     *                         all, or {@code null}.
     * @return {@link FileNameExtensionFilter}s for all of the registered {@link FileType}s that can
     *         be opened.
     */
    public static final FileNameExtensionFilter[] getOpenableFileFilters(String nameForAggregate) {
        return getFileFilters(nameForAggregate, getOpenable());
    }

    /** @return All of the registered {@link FileType} extensions that can be opened. */
    public static final String[] getOpenableExtensions() {
        List<String> openable = new ArrayList<>();
        for (FileType type : TYPES) {
            if (type.allowOpen()) {
                openable.add(type.getExtension());
            }
        }
        return openable.toArray(new String[0]);
    }

    /**
     * @param nameForAggregate The name for a {@link FileNameExtensionFilter} that includes all
     *                         types, or {@code null}.
     * @return {@link FileNameExtensionFilter}s for all of the specified {@link FileType}s.
     */
    public static final FileNameExtensionFilter[] getFileFilters(String nameForAggregate, FileType... fileType) {
        List<FileNameExtensionFilter> filters = new ArrayList<>();
        if (fileType != null) {
            int length = fileType.length;
            if (length > 0) {
                if (nameForAggregate != null && !nameForAggregate.isEmpty()) {
                    String[] extensions = new String[length];
                    for (int i = 0; i < length; i++) {
                        extensions[i] = fileType[i].getExtension();
                    }
                    filters.add(new FileNameExtensionFilter(nameForAggregate, extensions));
                }
                for (FileType one : fileType) {
                    filters.add(new FileNameExtensionFilter(one.getDescription(), one.getExtension()));
                }
            }
        }
        return filters.toArray(new FileNameExtensionFilter[0]);
    }

    /**
     * @param path The path to return an icon for.
     * @return The icon for the specified file.
     */
    public static StdImageSet getIconsForFile(String path) {
        return getIconsForFileExtension(PathUtils.getExtension(path));
    }

    /**
     * @param file The file to return an icon for.
     * @return The icon for the specified file.
     */
    public static StdImageSet getIconsForFile(File file) {
        return getIconsForFile(file != null && file.isFile() ? file.getName() : null);
    }

    /**
     * @param extension The extension to return an icon for.
     * @return The icon for the specified file extension.
     */
    public static StdImageSet getIconsForFileExtension(String extension) {
        if (extension != null) {
            FileType fileType = getByExtension(extension);
            if (fileType != null) {
                StdImageSet icons = fileType.getIcons();
                if (icons != null) {
                    return icons;
                }
            }
            return StdImage.FILE;
        }
        return StdImage.FOLDER;
    }

    private FileType(String extension, StdImageSet iconset, String description, String referenceURL, FileProxyCreator fileProxyCreator, boolean allowOpen, boolean shouldRegisterAppForOpening) {
        mExtension = extension;
        mIconSet = iconset;
        mDescription = description;
        mReferenceURL = referenceURL;
        mFileProxyCreator = fileProxyCreator;
        mAllowOpen = allowOpen;
        mRegisterAppForOpening = shouldRegisterAppForOpening;
    }

    /** @return The extension of the file. */
    public String getExtension() {
        return mExtension;
    }

    /** @return The {@link StdImageSet} representing the file. */
    public StdImageSet getIcons() {
        return mIconSet;
    }

    /** @return A short description for the file type. */
    public String getDescription() {
        return mDescription;
    }

    /** @return A URL that contains a description of this file type. */
    public String getReferenceURL() {
        return mReferenceURL;
    }

    /**
     * @return The {@link FileProxyCreator} responsible for creating a {@link FileProxy} with this
     *         file's contents.
     */
    public FileProxyCreator getFileProxyCreator() {
        return mFileProxyCreator;
    }

    /** @return Whether this {@link FileType} is allowed to be opened via the menu command. */
    public boolean allowOpen() {
        return mAllowOpen;
    }

    /**
     * @return Whether this {@link FileType} should be registered as a document type that the
     *         application can open.
     */
    public boolean shouldRegisterAppForOpening() {
        return mAllowOpen && mRegisterAppForOpening;
    }
}
