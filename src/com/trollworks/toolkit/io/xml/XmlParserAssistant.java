/*
 * Copyright (c) 1998-2015 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.io.xml;

import com.trollworks.toolkit.annotation.XmlAttr;
import com.trollworks.toolkit.annotation.XmlTag;

import java.util.Set;

import javax.xml.stream.XMLStreamException;

/** Objects that wish to have more control over the XML loading process can implement this interface. */
public interface XmlParserAssistant {
	/**
	 * Called after the XML tag attributes have been fully loaded into the object, just prior to
	 * loading any sub-tags that may be present.
	 *
	 * @param context The {@link XmlParserContext} for this object.
	 * @param unmatchedAttributes A {@link Set} of attribute names found in the XML that had no
	 *            matching {@link XmlAttr}-marked fields.
	 */
	void xmlAttributesLoaded(XmlParserContext context, Set<String> unmatchedAttributes) throws XMLStreamException;

	/**
	 * Called to create an object for an XML tag.
	 *
	 * @param context The {@link XmlParserContext} for this object.
	 * @param tag The tag to return an object for.
	 * @return The newly created object, or <code>null</code> if a new instance of the field's data
	 *         type should be created (i.e. when there is no need to use a sub-class and the default
	 *         no-args constructor can be used).
	 */
	Object createObjectForXmlTag(XmlParserContext context, String tag) throws XMLStreamException;

	/**
	 * Called to process an XML sub-tag that had no matching {@link XmlTag}-marked fields. Upon
	 * return from this method, the {@link XmlParser} should have been advanced past the current
	 * tag's contents, either by calling {@link XmlParser#skip()} or appropriate parsing of
	 * sub-tags.
	 *
	 * @param context The {@link XmlParserContext} for this object.
	 * @param tag The tag name that will be processed.
	 */
	void processUnmatchedXmlTag(XmlParserContext context, String tag) throws XMLStreamException;

	/**
	 * Called after the XML tag has been fully loaded into the object, just prior to the version
	 * being popped off the stack and control being returned to the caller.
	 *
	 * @param context The {@link XmlParserContext} for this object.
	 */
	void xmlLoaded(XmlParserContext context) throws XMLStreamException;
}
