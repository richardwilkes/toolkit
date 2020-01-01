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

import com.trollworks.toolkit.annotation.XmlDefaultChar;
import com.trollworks.toolkit.io.xml.XmlGenerator;
import com.trollworks.toolkit.io.xml.XmlParserContext;

import java.lang.reflect.Field;
import javax.xml.stream.XMLStreamException;

public class XmlCharacterHelper implements XmlObjectHelper {
    public static final XmlCharacterHelper SINGLETON = new XmlCharacterHelper();

    private XmlCharacterHelper() {
    }

    @Override
    public boolean canHandleClass(Class<?> clazz) {
        return Character.class == clazz;
    }

    @Override
    public void emitAsAttribute(XmlGenerator xml, Object obj, Field field, String name) throws XMLStreamException, ReflectiveOperationException {
        Character value = (Character) field.get(obj);
        if (value != null) {
            String         str = value.toString();
            XmlDefaultChar def = field.getAnnotation(XmlDefaultChar.class);
            if (def != null) {
                xml.addAttributeNot(name, str, String.valueOf(def.value()));
            } else {
                xml.addAttribute(name, str);
            }
        }
    }

    @Override
    public void loadAttributeValue(XmlParserContext context, Object obj, Field field, String name) throws XMLStreamException, ReflectiveOperationException {
        String charStr = context.getParser().getAttribute(name);
        if (charStr == null || charStr.isEmpty()) {
            XmlDefaultChar def = field.getAnnotation(XmlDefaultChar.class);
            field.set(obj, Character.valueOf(def != null ? def.value() : 0));
        } else {
            field.set(obj, Character.valueOf(charStr.charAt(0)));
        }
    }

    @Override
    public void emitAsTag(XmlGenerator xml, String tag, Object obj) throws XMLStreamException {
        xml.startTag(tag);
        xml.addText(obj.toString());
        xml.endTag();
    }
}
