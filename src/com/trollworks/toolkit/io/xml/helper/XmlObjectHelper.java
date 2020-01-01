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

package com.trollworks.toolkit.io.xml.helper;

import com.trollworks.toolkit.io.xml.XmlGenerator;
import com.trollworks.toolkit.io.xml.XmlParserContext;

import java.lang.reflect.Field;
import javax.xml.stream.XMLStreamException;

/**
 * An interface that allows custom handling of specific classes of object during xml
 * loading/saving.
 */
public interface XmlObjectHelper {
    /**
     * @param clazz The class that will be processed.
     * @return {@code true} if this instance wants to handle loading and saving of the specified
     *         class.
     */
    boolean canHandleClass(Class<?> clazz);

    /**
     * Called to emit an xml attribute for an object whose class {@link #canHandleClass(Class)}
     * returned {@code true} for.
     *
     * @param xml   The {@link XmlGenerator} to use.
     * @param obj   The object to emit the attribute for.
     * @param field The {@link Field} to access within the object.
     * @param name  The attribute name to use for the object.
     */
    void emitAsAttribute(XmlGenerator xml, Object obj, Field field, String name) throws XMLStreamException, ReflectiveOperationException;

    void loadAttributeValue(XmlParserContext context, Object obj, Field field, String name) throws XMLStreamException, ReflectiveOperationException;

    /**
     * Called to emit a fully-formed xml tag for an object whose class {@link
     * #canHandleClass(Class)} returned {@code true} for.
     *
     * @param xml The {@link XmlGenerator} to use.
     * @param tag The xml tag to use for the object.
     * @param obj The object to emit the tag for.
     */
    void emitAsTag(XmlGenerator xml, String tag, Object obj) throws XMLStreamException;
}
