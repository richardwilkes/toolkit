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
 * Objects that wish to be notified before they are about to be loaded should implement this
 * interface.
 */
public interface TagWillLoad {
    /**
     * Called before the XML tag will be loaded into the object.
     *
     * @param context The {@link XmlParserContext} for this object.
     */
    void xmlWillLoad(XmlParserContext context) throws XMLStreamException;
}
