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
import com.trollworks.toolkit.annotation.XmlTag;
import com.trollworks.toolkit.io.xml.XmlGenerator;
import com.trollworks.toolkit.io.xml.XmlParserContext;

import java.lang.reflect.Field;
import javax.xml.stream.XMLStreamException;

public class XmlEnumHelper implements XmlObjectHelper {
    public static final XmlEnumHelper SINGLETON = new XmlEnumHelper();

    private XmlEnumHelper() {
    }

    @Override
    public boolean canHandleClass(Class<?> clazz) {
        return clazz.isEnum();
    }

    @Override
    public void emitAsAttribute(XmlGenerator xml, Object obj, Field field, String name) throws XMLStreamException, ReflectiveOperationException {
        Enum<?> value = (Enum<?>) field.get(obj);
        if (value != null) {
            String     xmlName = getEnumXmlName(value);
            XmlDefault def     = field.getAnnotation(XmlDefault.class);
            if (def != null) {
                xml.addAttributeNot(name, xmlName, def.value());
            } else {
                xml.addAttribute(name, xmlName);
            }
        }
    }

    @Override
    public void loadAttributeValue(XmlParserContext context, Object obj, Field field, String name) throws XMLStreamException, ReflectiveOperationException {
        String    tag           = context.getParser().getAttribute(name);
        Enum<?>[] enumConstants = (Enum<?>[]) field.getType().getEnumConstants();
        for (Enum<?> one : enumConstants) {
            String xmlName = getEnumXmlName(one);
            if (xmlName.equals(tag)) {
                field.set(obj, one);
                return;
            }
        }
        XmlDefault def = field.getAnnotation(XmlDefault.class);
        if (def != null) {
            tag = def.value();
            for (Enum<?> one : enumConstants) {
                String xmlName = getEnumXmlName(one);
                if (xmlName.equals(tag)) {
                    field.set(obj, one);
                    return;
                }
            }
        }
        field.set(obj, null);
    }

    @Override
    public void emitAsTag(XmlGenerator xml, String tag, Object obj) throws XMLStreamException {
        xml.startTag(tag);
        xml.addText(getEnumXmlName((Enum<?>) obj));
        xml.endTag();
    }

    private static String getEnumXmlName(Enum<?> value) {
        String name = value.name();
        XmlTag xmlTag;
        try {
            xmlTag = value.getClass().getField(name).getAnnotation(XmlTag.class);
            if (xmlTag != null) {
                return xmlTag.value();
            }
        } catch (Exception exception) {
            // Fall back to simple case
        }
        return name.toLowerCase();
    }
}
