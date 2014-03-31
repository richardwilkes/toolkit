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

import com.trollworks.toolkit.utility.Numbers;

import java.io.InputStream;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/** Provides simple XML parsing. */
public class XmlParser implements AutoCloseable {
	private static final String	SEPARATOR	= "\u0000"; //$NON-NLS-1$
	private XMLStreamReader		mReader;
	private int					mDepth;
	private String				mMarker;

	/**
	 * Creates a new {@link XmlParser}.
	 *
	 * @param stream The {@link InputStream} to read from.
	 */
	public XmlParser(InputStream stream) throws XMLStreamException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		mReader = factory.createXMLStreamReader(stream);
	}

	/** @return The current line:column position. */
	public String getLocation() {
		Location location = mReader.getLocation();
		return location.getLineNumber() + ":" + location.getColumnNumber(); //$NON-NLS-1$
	}

	/** @return A marker for determining if you've come to the end of a specific tag. */
	public String getMarker() {
		return mMarker;
	}

	/** @return The current tag's name, or <code>null</code>. */
	public String getCurrentTag() {
		return mReader.getLocalName();
	}

	/**
	 * Advances to the next position.
	 *
	 * @return The next tag's name, or <code>null</code>.
	 */
	public String nextTag() throws XMLStreamException {
		return nextTag(null);
	}

	/**
	 * Advances to the next position.
	 *
	 * @param marker If this is not <code>null</code>, when an end tag matches this marker return
	 *            <code>null</code>.
	 * @return The next tag's name, or <code>null</code>.
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
					if (marker != null && marker.equals(mMarker)) {
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
	 * @return The attribute value, or <code>null</code>.
	 */
	public String getAttribute(String name) {
		return mReader.getAttributeValue(null, name);
	}

	/**
	 * @param name The name of the attribute to retrieve.
	 * @param def The default value to use if the attribute value isn't present.
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
	 * @param def The default value to use if the attribute value isn't present or cannot be
	 *            converted.
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
	 * @param def The default value to use if the attribute value isn't present or cannot be
	 *            converted.
	 * @return The value of the attribute.
	 */
	public int getIntegerAttribute(String name, int def) {
		return Numbers.extractInteger(getAttribute(name), def, false);
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
	 * @param def The default value to use if the attribute value isn't present or cannot be
	 *            converted.
	 * @return The value of the attribute.
	 */
	public long getLongAttribute(String name, long def) {
		return Numbers.extractLong(getAttribute(name), def, false);
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
	 * @param def The default value to use if the attribute value isn't present or cannot be
	 *            converted.
	 * @return The value of the attribute.
	 */
	public double getDoubleAttribute(String name, double def) {
		return Numbers.extractDouble(getAttribute(name), def, false);
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
