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

import com.trollworks.toolkit.annotation.XmlTag;

import javax.xml.stream.XMLStreamException;

/**
 * Objects that wish to control how unmatched tags are handled should implement this interface.
 */
public interface TagUnmatched {
    /**
     * Called to process an XML sub-tag that had no matching fields marked with {@link XmlTag}. Upon
     * return from this method, the {@link XmlParser} should have been advanced past the current
     * tag's contents, either by calling {@link XmlParser#skip()} or appropriate parsing of
     * sub-tags.
     *
     * @param context The {@link XmlParserContext} for this object.
     * @param tag     The tag name that will be processed.
     */
    void xmlUnmatchedTag(XmlParserContext context, String tag) throws XMLStreamException;
}
