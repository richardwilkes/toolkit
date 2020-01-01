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
 * Objects that wish to add additional attributes when being written to xml should implement this
 * interface.
 */
public interface TagExtraAttributes {
    /**
     * Called to allow an object to emit additional attributes that the standard processing can't
     * handle.
     *
     * @param xml The {@link XmlGenerator} for this object.
     */
    void xmlEmitExtraAttributes(XmlGenerator xml) throws XMLStreamException;
}
