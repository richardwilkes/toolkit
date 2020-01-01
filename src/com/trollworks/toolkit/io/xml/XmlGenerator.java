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

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/** Provides simple XML generation. */
public class XmlGenerator implements AutoCloseable {
    private String          mIndent = "\t";
    private XMLStreamWriter mWriter;
    private int             mDepth;
    private boolean         mHadText;

    /**
     * Creates a new {@link XmlGenerator}.
     *
     * @param stream The {@link OutputStream} to write to.
     */
    public XmlGenerator(OutputStream stream) throws XMLStreamException {
        mWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(new BufferedOutputStream(stream), StandardCharsets.UTF_8.name());
    }

    /** @param indent The characters to use for indentation. */
    public void setIndent(String indent) {
        mIndent = indent;
    }

    /** Emits the XML document header. */
    public void startDocument() throws XMLStreamException {
        mWriter.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
    }

    /** Finishes the document. */
    public void endDocument() throws XMLStreamException {
        mWriter.writeEndDocument();
    }

    private void eol() throws XMLStreamException {
        mWriter.writeCharacters("\n");
        for (int i = 0; i < mDepth; i++) {
            mWriter.writeCharacters(mIndent);
        }
    }

    /**
     * Opens a new XML tag.
     *
     * @param name The name of the tag.
     */
    public void startTag(String name) throws XMLStreamException {
        eol();
        mWriter.writeStartElement(name);
        mDepth++;
    }

    /**
     * Opens a new, empty, XML tag.
     *
     * @param name The name of the tag.
     */
    public void startEmptyTag(String name) throws XMLStreamException {
        eol();
        mWriter.writeEmptyElement(name);
    }

    /** Closes the current tag. */
    public void endTag() throws XMLStreamException {
        mDepth--;
        if (mHadText) {
            mHadText = false;
        } else {
            eol();
        }
        mWriter.writeEndElement();
    }

    /**
     * Adds an attribute to the currently open tag.
     *
     * @param name  The name of the attribute.
     * @param value The value of the attribute.
     */
    public void addAttribute(String name, String value) throws XMLStreamException {
        mWriter.writeAttribute(name, value == null ? "" : value);
    }

    /**
     * Adds an attribute to the currently open tag.
     *
     * @param name  The name of the attribute.
     * @param value The value of the attribute.
     */
    public void addAttribute(String name, boolean value) throws XMLStreamException {
        mWriter.writeAttribute(name, Numbers.format(value));
    }

    /**
     * Adds an attribute to the currently open tag.
     *
     * @param name  The name of the attribute.
     * @param value The value of the attribute.
     */
    public void addAttribute(String name, int value) throws XMLStreamException {
        mWriter.writeAttribute(name, Integer.toString(value));
    }

    /**
     * Adds an attribute to the currently open tag.
     *
     * @param name  The name of the attribute.
     * @param value The value of the attribute.
     */
    public void addAttribute(String name, long value) throws XMLStreamException {
        mWriter.writeAttribute(name, Long.toString(value));
    }

    /**
     * Adds an attribute to the currently open tag.
     *
     * @param name  The name of the attribute.
     * @param value The value of the attribute.
     */
    public void addAttribute(String name, double value) throws XMLStreamException {
        mWriter.writeAttribute(name, Numbers.trimTrailingZeroes(Double.toString(value), false));
    }

    /**
     * Adds an attribute to the currently open tag, but only if it isn't empty.
     *
     * @param name  The name of the attribute.
     * @param value The value of the attribute.
     */
    public void addAttributeNotEmpty(String name, String value) throws XMLStreamException {
        if (value != null && !value.isEmpty()) {
            mWriter.writeAttribute(name, value);
        }
    }

    /**
     * Adds an attribute to the currently open tag.
     *
     * @param name  The name of the attribute.
     * @param value The value of the attribute.
     * @param not   Only add the attribute if it is not equal to this value.
     */
    public void addAttributeNot(String name, String value, String not) throws XMLStreamException {
        if (!Objects.equals(not, value)) {
            addAttribute(name, value);
        }
    }

    /**
     * Adds an attribute to the currently open tag.
     *
     * @param name  The name of the attribute.
     * @param value The value of the attribute.
     * @param not   Only add the attribute if it is not equal to this value.
     */
    public void addAttributeNot(String name, boolean value, boolean not) throws XMLStreamException {
        if (value != not) {
            addAttribute(name, value);
        }
    }

    /**
     * Adds an attribute to the currently open tag.
     *
     * @param name  The name of the attribute.
     * @param value The value of the attribute.
     * @param not   Only add the attribute if it is not equal to this value.
     */
    public void addAttributeNot(String name, int value, int not) throws XMLStreamException {
        if (value != not) {
            addAttribute(name, value);
        }
    }

    /**
     * Adds an attribute to the currently open tag.
     *
     * @param name  The name of the attribute.
     * @param value The value of the attribute.
     * @param not   Only add the attribute if it is not equal to this value.
     */
    public void addAttributeNot(String name, long value, long not) throws XMLStreamException {
        if (value != not) {
            addAttribute(name, value);
        }
    }

    /**
     * Adds an attribute to the currently open tag.
     *
     * @param name  The name of the attribute.
     * @param value The value of the attribute.
     * @param not   Only add the attribute if it is not equal to this value.
     */
    public void addAttributeNot(String name, double value, double not) throws XMLStreamException {
        if (value != not) {
            addAttribute(name, value);
        }
    }

    /**
     * Adds text to the currently open tag.
     *
     * @param text The text to add.
     */
    public void addText(String text) throws XMLStreamException {
        mWriter.writeCharacters(text);
        mHadText = true;
    }

    /**
     * Adds a comment.
     *
     * @param comment The comment.
     */
    public void comment(String comment) throws XMLStreamException {
        eol();
        mWriter.writeComment(' ' + comment + ' ');
    }

    /** Closes this {@link XmlGenerator}. No further writing can be attempted with it. */
    @Override
    public void close() throws XMLStreamException {
        if (mWriter != null) {
            try {
                mWriter.close();
            } finally {
                mWriter = null;
            }
        }
    }
}
