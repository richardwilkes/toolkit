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

package com.trollworks.toolkit.io.xml;

import com.trollworks.toolkit.utility.text.Numbers;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/** Provides simple XML parsing. */
public class XmlParser implements AutoCloseable {
    private static final String          SEPARATOR = "\u0000";
    private              XMLStreamReader mReader;
    private              int             mDepth;
    private              String          mMarker;

    /**
     * Creates a new {@link XmlParser}.
     *
     * @param stream The {@link InputStream} to read from.
     */
    public XmlParser(InputStream stream) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        mReader = factory.createXMLStreamReader(new BufferedInputStream(stream));
    }

    /** @return The current line:column position. */
    public Location getLocation() {
        return mReader.getLocation();
    }

    /** @return A marker for determining if you've come to the end of a specific tag. */
    public String getMarker() {
        return mMarker;
    }

    /** @return The current tag's name, or {@code null}. */
    public String getCurrentTag() {
        return mReader.getLocalName();
    }

    /**
     * Advances to the next position.
     *
     * @return The next tag's name, or {@code null}.
     */
    public String nextTag() throws XMLStreamException {
        return nextTag(null);
    }

    /**
     * Advances to the next position.
     *
     * @param marker If this is not {@code null}, when an end tag matches this marker return {@code
     *               null}.
     * @return The next tag's name, or {@code null}.
     */
    public String nextTag(String marker) throws XMLStreamException {
        while (mReader.hasNext()) {
            switch (mReader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                String name = mReader.getLocalName();
                mMarker = mDepth++ + SEPARATOR + name;
                return name;
            case XMLStreamConstants.END_ELEMENT:
                mMarker = --mDepth + SEPARATOR + mReader.getLocalName();
                if (mMarker.equals(marker)) {
                    return null;
                }
                break;
            case XMLStreamConstants.START_DOCUMENT:
                mMarker = null;
                if (marker != null) {
                    return null;
                }
                break;
            case XMLStreamConstants.END_DOCUMENT:
                mMarker = null;
                return null;
            default:
                break;
            }
        }
        return null;
    }

    /** Skips the end of the current tag, bypassing its children. */
    public void skip() throws XMLStreamException {
        skip(getMarker());
    }

    /** @param marker Up to the end of the tag this marker came from will be skipped. */
    public void skip(String marker) throws XMLStreamException {
        while (nextTag(marker) != null) {
            // Intentionally empty
        }
    }

    /**
     * @param name The name of the attribute to check.
     * @return Whether the attribute is present.
     */
    public boolean hasAttribute(String name) {
        return getAttribute(name) != null;
    }

    /**
     * @param name The name of the attribute to retrieve.
     * @return The attribute value, or {@code null}.
     */
    public String getAttribute(String name) {
        return mReader.getAttributeValue(null, name);
    }

    /**
     * @param name The name of the attribute to retrieve.
     * @param def  The default value to use if the attribute value isn't present.
     * @return The value of the attribute.
     */
    public String getAttribute(String name, String def) {
        String value = getAttribute(name);
        return value != null ? value : def;
    }

    /**
     * @param name The name of the attribute to retrieve.
     * @return Whether or not the attribute is present and set to a 'true' value.
     */
    public boolean isAttributeSet(String name) {
        return Numbers.extractBoolean(getAttribute(name));
    }

    /**
     * @param name The name of the attribute to retrieve.
     * @param def  The default value to use if the attribute value isn't present or cannot be
     *             converted.
     * @return Whether or not the attribute is present and set to a 'true' value.
     */
    public boolean isAttributeSet(String name, boolean def) {
        return Numbers.extractBoolean(getAttribute(name, Boolean.toString(def)));
    }

    /**
     * @param name The name of the attribute to retrieve.
     * @return The value of the attribute.
     */
    public int getIntegerAttribute(String name) {
        return Numbers.extractInteger(getAttribute(name), 0, false);
    }

    /**
     * @param name The name of the attribute to retrieve.
     * @param def  The default value to use if the attribute value isn't present or cannot be
     *             converted.
     * @return The value of the attribute.
     */
    public int getIntegerAttribute(String name, int def) {
        return Numbers.extractInteger(getAttribute(name), def, false);
    }

    /**
     * @param name The name of the attribute to retrieve.
     * @param def  The default value to use if the attribute value isn't present or cannot be
     *             converted.
     * @param min  The minimum value to return.
     * @param max  The maximum value to return.
     * @return The value of the attribute.
     */
    public int getIntegerAttribute(String name, int def, int min, int max) {
        return Numbers.extractInteger(getAttribute(name), def, min, max, false);
    }

    /**
     * @param name The name of the attribute to retrieve.
     * @return The value of the attribute.
     */
    public long getLongAttribute(String name) {
        return Numbers.extractLong(getAttribute(name), 0, false);
    }

    /**
     * @param name The name of the attribute to retrieve.
     * @param def  The default value to use if the attribute value isn't present or cannot be
     *             converted.
     * @return The value of the attribute.
     */
    public long getLongAttribute(String name, long def) {
        return Numbers.extractLong(getAttribute(name), def, false);
    }

    /**
     * @param name The name of the attribute to retrieve.
     * @param def  The default value to use if the attribute value isn't present or cannot be
     *             converted.
     * @param min  The minimum value to return.
     * @param max  The maximum value to return.
     * @return The value of the attribute.
     */
    public long getLongAttribute(String name, long def, long min, long max) {
        return Numbers.extractLong(getAttribute(name), def, min, max, false);
    }

    /**
     * @param name The name of the attribute to retrieve.
     * @return The value of the attribute.
     */
    public double getDoubleAttribute(String name) {
        return Numbers.extractDouble(getAttribute(name), 0, false);
    }

    /**
     * @param name The name of the attribute to retrieve.
     * @param def  The default value to use if the attribute value isn't present or cannot be
     *             converted.
     * @return The value of the attribute.
     */
    public double getDoubleAttribute(String name, double def) {
        return Numbers.extractDouble(getAttribute(name), def, false);
    }

    /**
     * @param name The name of the attribute to retrieve.
     * @param def  The default value to use if the attribute value isn't present or cannot be
     *             converted.
     * @param min  The minimum value to return.
     * @param max  The maximum value to return.
     * @return The value of the attribute.
     */
    public double getDoubleAttribute(String name, double def, double min, double max) {
        return Numbers.extractDouble(getAttribute(name), def, min, max, false);
    }

    /** @return The number of attributes. */
    public int getAttributeCount() {
        return mReader.getAttributeCount();
    }

    /**
     * @param index The index of the attribute.
     * @return The attribute value.
     */
    public String getAttributeName(int index) {
        return mReader.getAttributeLocalName(index);
    }

    /**
     * @param index The index of the attribute.
     * @return The attribute value.
     */
    public String getAttributeValue(int index) {
        return mReader.getAttributeValue(index);
    }

    /** @return The text of the current element. */
    public String getText() throws XMLStreamException {
        String text = mReader.getElementText();
        mMarker = --mDepth + SEPARATOR + mReader.getLocalName();
        return text;
    }

    /** Closes this {@link XmlParser}. No further reading can be attempted with it. */
    @Override
    public void close() throws XMLStreamException {
        if (mReader != null) {
            try {
                mReader.close();
            } finally {
                mReader = null;
            }
        }
    }
}
