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

import com.trollworks.toolkit.annotation.XmlDefaultShort;
import com.trollworks.toolkit.io.xml.XmlGenerator;
import com.trollworks.toolkit.io.xml.XmlParserContext;

import java.lang.reflect.Field;
import javax.xml.stream.XMLStreamException;

public class XmlShortHelper implements XmlObjectHelper {
    public static final XmlShortHelper SINGLETON = new XmlShortHelper();

    private XmlShortHelper() {
    }

    @Override
    public boolean canHandleClass(Class<?> clazz) {
        return Short.class == clazz;
    }

    @Override
    public void emitAsAttribute(XmlGenerator xml, Object obj, Field field, String name) throws XMLStreamException, ReflectiveOperationException {
        Short value = (Short) field.get(obj);
        if (value != null) {
            short           primitive = value.shortValue();
            XmlDefaultShort def       = field.getAnnotation(XmlDefaultShort.class);
            if (def != null) {
                xml.addAttributeNot(name, primitive, def.value());
            } else {
                xml.addAttribute(name, primitive);
            }
        }
    }

    @Override
    public void loadAttributeValue(XmlParserContext context, Object obj, Field field, String name) throws XMLStreamException, ReflectiveOperationException {
        XmlDefaultShort def = field.getAnnotation(XmlDefaultShort.class);
        field.set(obj, Short.valueOf((short) context.getParser().getIntegerAttribute(name, def != null ? def.value() : 0)));
    }

    @Override
    public void emitAsTag(XmlGenerator xml, String tag, Object obj) throws XMLStreamException {
        xml.startTag(tag);
        xml.addText(obj.toString());
        xml.endTag();
    }
}
