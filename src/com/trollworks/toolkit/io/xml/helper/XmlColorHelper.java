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
import com.trollworks.toolkit.ui.Colors;

import java.awt.Color;
import java.lang.reflect.Field;
import javax.xml.stream.XMLStreamException;

public class XmlColorHelper implements XmlObjectHelper {
    public static final XmlColorHelper SINGLETON = new XmlColorHelper();

    private XmlColorHelper() {
    }

    @Override
    public boolean canHandleClass(Class<?> clazz) {
        return clazz == Color.class;
    }

    @Override
    public void emitAsAttribute(XmlGenerator xml, Object obj, Field field, String name) throws XMLStreamException, ReflectiveOperationException {
        Color color = (Color) field.get(obj);
        if (color != null) {
            String     value = Colors.encode(color);
            XmlDefault def   = field.getAnnotation(XmlDefault.class);
            if (def != null) {
                xml.addAttributeNot(name, value, def.value());
            } else {
                xml.addAttribute(name, value);
            }
        }
    }

    @Override
    public void loadAttributeValue(XmlParserContext context, Object obj, Field field, String name) throws XMLStreamException, ReflectiveOperationException {
        XmlDefault def   = field.getAnnotation(XmlDefault.class);
        String     value = context.getParser().getAttribute(name, def != null ? def.value() : null);
        field.set(obj, value != null && !value.isEmpty() ? Colors.decode(value) : null);
    }

    @Override
    public void emitAsTag(XmlGenerator xml, String tag, Object obj) throws XMLStreamException {
        xml.startTag(tag);
        xml.addText(Colors.encode((Color) obj));
        xml.endTag();
    }
}
