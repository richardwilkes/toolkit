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

import com.trollworks.toolkit.annotation.XmlDefault;
import com.trollworks.toolkit.io.xml.XmlGenerator;
import com.trollworks.toolkit.io.xml.XmlParserContext;

import java.lang.reflect.Field;
import java.util.UUID;
import javax.xml.stream.XMLStreamException;

public class XmlUUIDHelper implements XmlObjectHelper {
    public static final XmlUUIDHelper SINGLETON = new XmlUUIDHelper();

    private XmlUUIDHelper() {
    }

    @Override
    public boolean canHandleClass(Class<?> clazz) {
        return clazz == UUID.class;
    }

    @Override
    public void emitAsAttribute(XmlGenerator xml, Object obj, Field field, String name) throws XMLStreamException, ReflectiveOperationException {
        UUID value = (UUID) field.get(obj);
        if (value != null) {
            XmlDefault def = field.getAnnotation(XmlDefault.class);
            if (def != null) {
                xml.addAttributeNot(name, value.toString(), def.value());
            } else {
                xml.addAttribute(name, value.toString());
            }
        }
    }

    @Override
    public void loadAttributeValue(XmlParserContext context, Object obj, Field field, String name) throws XMLStreamException, ReflectiveOperationException {
        XmlDefault def   = field.getAnnotation(XmlDefault.class);
        String     value = context.getParser().getAttribute(name, def != null ? def.value() : null);
        field.set(obj, value != null && !value.isEmpty() ? UUID.fromString(value) : null);
    }

    @Override
    public void emitAsTag(XmlGenerator xml, String tag, Object obj) throws XMLStreamException {
        xml.startTag(tag);
        xml.addText(obj.toString());
        xml.endTag();
    }
}
