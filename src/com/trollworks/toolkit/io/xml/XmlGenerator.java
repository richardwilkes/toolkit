/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is com.trollworks.toolkit.
 *
 * The Initial Developer of the Original Code is Richard A. Wilkes.
 * Portions created by the Initial Developer are Copyright (C) 1998-2014,
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */

package com.trollworks.toolkit.io.xml;

import com.trollworks.toolkit.utility.Text;

import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/** Provides simple XML generation. */
public class XmlGenerator implements AutoCloseable {
	private String			mIndent	= "\t";	//$NON-NLS-1$
	private XMLStreamWriter	mWriter;
	private int				mDepth;
	private boolean			mHadText;

	/**
	 * Creates a new {@link XmlGenerator}.
	 *
	 * @param stream The {@link OutputStream} to write to.
	 */
	public XmlGenerator(OutputStream stream) throws XMLStreamException {
		mWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(stream, Text.UTF8_ENCODING);
	}

	/** @param indent The characters to use for indentation. */
	public void setIndent(String indent) {
		mIndent = indent;
	}

	/** Emits the XML document header. */
	public void startDocument() throws XMLStreamException {
		mWriter.writeStartDocument(Text.UTF8_ENCODING, "1.0");	//$NON-NLS-1$
	}

	/** Finishes the document. */
	public void endDocument() throws XMLStreamException {
		mWriter.writeEndDocument();
	}

	private void eol() throws XMLStreamException {
		mWriter.writeCharacters("\n");	//$NON-NLS-1$
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
		if (!mHadText) {
			eol();
		} else {
			mHadText = false;
		}
		mWriter.writeEndElement();
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 */
	public void addAttribute(String name, String value) throws XMLStreamException {
		mWriter.writeAttribute(name, value == null ? "" : value);	//$NON-NLS-1$
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 */
	public void addAttribute(String name, boolean value) throws XMLStreamException {
		mWriter.writeAttribute(name, value ? "yes" : "no");	//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 */
	public void addAttribute(String name, int value) throws XMLStreamException {
		mWriter.writeAttribute(name, Integer.toString(value));
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 */
	public void addAttribute(String name, long value) throws XMLStreamException {
		mWriter.writeAttribute(name, Long.toString(value));
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 */
	public void addAttribute(String name, double value) throws XMLStreamException {
		mWriter.writeAttribute(name, Double.toString(value));
	}

	/**
	 * Adds an attribute to the currently open tag, but only if it isn't empty.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 */
	public void addAttributeNotEmpty(String name, String value) throws XMLStreamException {
		if (value != null && value.length() > 0) {
			mWriter.writeAttribute(name, value);
		}
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 * @param not Only add the attribute if it is not equal to this value.
	 */
	public void addAttributeNot(String name, String value, String not) throws XMLStreamException {
		if (not != null ? !not.equals(value) : value != null) {
			addAttribute(name, value);
		}
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 * @param not Only add the attribute if it is not equal to this value.
	 */
	public void addAttributeNot(String name, boolean value, boolean not) throws XMLStreamException {
		if (value != not) {
			addAttribute(name, value);
		}
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 * @param not Only add the attribute if it is not equal to this value.
	 */
	public void addAttributeNot(String name, int value, int not) throws XMLStreamException {
		if (value != not) {
			addAttribute(name, value);
		}
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 * @param not Only add the attribute if it is not equal to this value.
	 */
	public void addAttributeNot(String name, long value, long not) throws XMLStreamException {
		if (value != not) {
			addAttribute(name, value);
		}
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 * @param not Only add the attribute if it is not equal to this value.
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
