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
 * Objects that wish to be notified when they have been loaded should implement this interface.
 */
public interface TagLoaded {
    /**
     * Called after the XML tag has been fully loaded into the object, just prior to the version
     * being popped off the stack and control being returned to the caller.
     *
     * @param context The {@link XmlParserContext} for this object.
     */
    void xmlLoaded(XmlParserContext context) throws XMLStreamException;
}
