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

import javax.xml.stream.XMLStreamException;

/**
 * Objects that wish to control the object creation process for their fields should implement this
 * interface.
 */
public interface TagObjectCreator {
    /**
     * Called to create an object for an XML tag.
     *
     * @param context The {@link XmlParserContext} for this object.
     * @param tag     The tag to return an object for.
     * @return The newly created object, or {@code null} if a new instance of the field's data type
     *         should be created (i.e. when there is no need to use a sub-class and the default
     *         no-args constructor can be used).
     */
    Object xmlCreateObject(XmlParserContext context, String tag) throws XMLStreamException;
}
