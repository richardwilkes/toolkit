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

import com.trollworks.toolkit.annotation.XmlAttr;

import java.util.Set;
import javax.xml.stream.XMLStreamException;

/**
 * Objects that wish to be notified when their attributes have been loaded should implement this
 * interface.
 */
public interface TagAttributesLoaded {
    /**
     * Called after the XML tag attributes have been fully loaded into the object, just prior to
     * loading any sub-tags that may be present.
     *
     * @param context             The {@link XmlParserContext} for this object.
     * @param unmatchedAttributes A {@link Set} of attribute names found in the XML that had no
     *                            matching {@link XmlAttr}-marked fields.
     */
    void xmlAttributesLoaded(XmlParserContext context, Set<String> unmatchedAttributes) throws XMLStreamException;
}
