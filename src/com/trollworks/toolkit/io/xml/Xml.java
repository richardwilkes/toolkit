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
import com.trollworks.toolkit.annotation.XmlNoSort;
import com.trollworks.toolkit.annotation.XmlTag;
import com.trollworks.toolkit.annotation.XmlTagMinimumVersion;
import com.trollworks.toolkit.annotation.XmlTagVersion;
import com.trollworks.toolkit.io.xml.helper.XmlBooleanHelper;
import com.trollworks.toolkit.io.xml.helper.XmlByteHelper;
import com.trollworks.toolkit.io.xml.helper.XmlCharacterHelper;
import com.trollworks.toolkit.io.xml.helper.XmlDoubleHelper;
import com.trollworks.toolkit.io.xml.helper.XmlEnumHelper;
import com.trollworks.toolkit.io.xml.helper.XmlFloatHelper;
import com.trollworks.toolkit.io.xml.helper.XmlGenericHelper;
import com.trollworks.toolkit.io.xml.helper.XmlIntegerHelper;
import com.trollworks.toolkit.io.xml.helper.XmlLongHelper;
import com.trollworks.toolkit.io.xml.helper.XmlObjectHelper;
import com.trollworks.toolkit.io.xml.helper.XmlPrimitiveBooleanHelper;
import com.trollworks.toolkit.io.xml.helper.XmlPrimitiveByteHelper;
import com.trollworks.toolkit.io.xml.helper.XmlPrimitiveCharHelper;
import com.trollworks.toolkit.io.xml.helper.XmlPrimitiveDoubleHelper;
import com.trollworks.toolkit.io.xml.helper.XmlPrimitiveFloatHelper;
import com.trollworks.toolkit.io.xml.helper.XmlPrimitiveIntHelper;
import com.trollworks.toolkit.io.xml.helper.XmlPrimitiveLongHelper;
import com.trollworks.toolkit.io.xml.helper.XmlPrimitiveShortHelper;
import com.trollworks.toolkit.io.xml.helper.XmlShortHelper;
import com.trollworks.toolkit.io.xml.helper.XmlStringHelper;
import com.trollworks.toolkit.io.xml.helper.XmlUUIDHelper;
import com.trollworks.toolkit.utility.introspection.FieldAnnotation;
import com.trollworks.toolkit.utility.introspection.Introspection;
import com.trollworks.toolkit.workarounds.PathToUri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLStreamException;

/**
 * Provides easy loading and saving of objects that have been annotated with appropriate xml tags.
 */
public class Xml {
    private static final List<XmlObjectHelper>          HELPERS    = new ArrayList<>();
    private static final Map<Class<?>, XmlObjectHelper> HELPER_MAP = new HashMap<>();

    static {
        registerHelper(XmlPrimitiveBooleanHelper.SINGLETON);
        registerHelper(XmlPrimitiveByteHelper.SINGLETON);
        registerHelper(XmlPrimitiveCharHelper.SINGLETON);
        registerHelper(XmlPrimitiveShortHelper.SINGLETON);
        registerHelper(XmlPrimitiveIntHelper.SINGLETON);
        registerHelper(XmlPrimitiveLongHelper.SINGLETON);
        registerHelper(XmlPrimitiveFloatHelper.SINGLETON);
        registerHelper(XmlPrimitiveDoubleHelper.SINGLETON);

        registerHelper(XmlBooleanHelper.SINGLETON);
        registerHelper(XmlByteHelper.SINGLETON);
        registerHelper(XmlCharacterHelper.SINGLETON);
        registerHelper(XmlShortHelper.SINGLETON);
        registerHelper(XmlIntegerHelper.SINGLETON);
        registerHelper(XmlLongHelper.SINGLETON);
        registerHelper(XmlFloatHelper.SINGLETON);
        registerHelper(XmlDoubleHelper.SINGLETON);

        registerHelper(XmlStringHelper.SINGLETON);
        registerHelper(XmlEnumHelper.SINGLETON);
        registerHelper(XmlUUIDHelper.SINGLETON);
    }

    /** The attribute that will be used for a tag's version. */
    public static final String ATTR_VERSION = "version";

    public static final void registerHelper(XmlObjectHelper helper) {
        synchronized (HELPERS) {
            HELPERS.add(helper);
            HELPER_MAP.clear();
        }
    }

    public static final void unregisterHelper(XmlObjectHelper helper) {
        synchronized (HELPERS) {
            HELPERS.remove(helper);
            HELPER_MAP.clear();
        }
    }

    private static XmlObjectHelper getHelper(Class<?> clazz) {
        synchronized (HELPERS) {
            XmlObjectHelper helper = HELPER_MAP.get(clazz);
            if (helper == null) {
                helper = XmlGenericHelper.SINGLETON;
                for (XmlObjectHelper one : HELPERS) {
                    if (one.canHandleClass(clazz)) {
                        helper = one;
                        break;
                    }
                }
                HELPER_MAP.put(clazz, helper);
            }
            return helper;
        }
    }

    /**
     * Loads the contents of an xml file into the specified object.
     *
     * @param file The file to load from.
     * @param obj  The object to load the xml data into.
     * @return The object that was passed in.
     */
    public static final <T> T load(File file, T obj) throws XMLStreamException {
        return load(file, obj, null);
    }

    /**
     * Loads the contents of an xml file into the specified object.
     *
     * @param file    The file to load from.
     * @param obj     The object to load the xml data into.
     * @param context Optional context for recording state while loading.
     * @return The object that was passed in.
     */
    public static final <T> T load(File file, T obj, XmlParserContext context) throws XMLStreamException {
        return load(file.toURI(), obj, context);
    }

    /**
     * Loads the contents of an xml file into the specified object.
     *
     * @param path The {@link Path} to load from.
     * @param obj  The object to load the xml data into.
     * @return The object that was passed in.
     */
    public static final <T> T load(Path path, T obj) throws XMLStreamException {
        return load(PathToUri.toFixedUri(path), obj, null);
    }

    /**
     * Loads the contents of an xml file into the specified object.
     *
     * @param path    The {@link Path} to load from.
     * @param obj     The object to load the xml data into.
     * @param context Optional context for recording state while loading.
     * @return The object that was passed in.
     */
    public static final <T> T load(Path path, T obj, XmlParserContext context) throws XMLStreamException {
        return load(PathToUri.toFixedUri(path), obj, context);
    }

    /**
     * Loads the contents of an xml file into the specified object.
     *
     * @param uri The URI to load from.
     * @param obj The object to load the xml data into.
     * @return The object that was passed in.
     */
    public static final <T> T load(URI uri, T obj) throws XMLStreamException {
        return load(uri, obj, null);
    }

    /**
     * Loads the contents of an xml file into the specified object.
     *
     * @param uri     The URI to load from.
     * @param obj     The object to load the xml data into.
     * @param context Optional context for recording state while loading.
     * @return The object that was passed in.
     */
    public static final <T> T load(URI uri, T obj, XmlParserContext context) throws XMLStreamException {
        try {
            return load(uri.toURL().openStream(), obj, context);
        } catch (XMLStreamException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new XMLStreamException(exception);
        }
    }

    /**
     * Loads the contents of an xml stream into the specified object.
     *
     * @param in  The stream to load from.
     * @param obj The object to load the xml data into.
     * @return The object that was passed in.
     */
    public static final <T> T load(InputStream in, T obj) throws XMLStreamException {
        return load(in, obj, null);
    }

    /**
     * Loads the contents of an xml stream into the specified object.
     *
     * @param in      The stream to load from.
     * @param obj     The object to load the xml data into.
     * @param context Optional context for recording state while loading.
     * @return The object that was passed in.
     */
    public static final <T> T load(InputStream in, T obj, XmlParserContext context) throws XMLStreamException {
        XmlTag xmlTag = obj.getClass().getAnnotation(XmlTag.class);
        if (xmlTag == null) {
            throw new XMLStreamException("The root object has not been annotated.");
        }
        try (XmlParser xml = new XmlParser(in)) {
            String tag = xml.nextTag();
            if (tag != null && tag.equals(xmlTag.value())) {
                load(xml, obj, context);
                return obj;
            }
            throw new XMLStreamException(String.format("The root tag \"%s\" was not present.", xmlTag.value()));
        } catch (XMLStreamException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new XMLStreamException(exception);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void load(XmlParser xml, Object obj, XmlParserContext context) throws XMLStreamException {
        try {
            if (context == null) {
                context = new XmlParserContext(xml);
            }
            String marker = xml.getMarker();
            if (obj instanceof TagWillLoad) {
                ((TagWillLoad) obj).xmlWillLoad(context);
            }
            Class<?> tagClass = obj.getClass();
            int      version  = xml.getIntegerAttribute(ATTR_VERSION, 0);
            if (version > getVersionOfTag(tagClass)) {
                throw new XMLStreamException(String.format("The tag '%s' is from a newer version and cannot be loaded.", xml.getCurrentTag()), xml.getLocation());
            }
            if (version < getMinimumLoadableVersionOfTag(tagClass)) {
                throw new XMLStreamException(String.format("The tag '%s' is from an older version and cannot be loaded.", xml.getCurrentTag()), xml.getLocation());
            }
            if (version != 0) {
                context.pushVersion(version);
            }
            Set<String> unmatchedAttributes = new HashSet<>();
            for (int i = xml.getAttributeCount(); --i > 0; ) {
                unmatchedAttributes.add(xml.getAttributeName(i));
            }
            unmatchedAttributes.remove(ATTR_VERSION);
            for (FieldAnnotation<XmlAttr> fa : Introspection.getDeepFieldAnnotations(tagClass, XmlAttr.class)) {
                Field field = fa.getField();
                Introspection.makeFieldAccessible(field);
                String name = fa.getAnnotation().value();
                unmatchedAttributes.remove(name);
                getHelper(field.getType()).loadAttributeValue(context, obj, field, name);
            }
            if (obj instanceof TagAttributesLoaded) {
                ((TagAttributesLoaded) obj).xmlAttributesLoaded(context, unmatchedAttributes);
            }
            Map<String, Field> subTags = new HashMap<>();
            for (FieldAnnotation<XmlTag> fa : Introspection.getDeepFieldAnnotations(tagClass, XmlTag.class)) {
                subTags.put(fa.getAnnotation().value(), fa.getField());
            }
            String tag;
            while ((tag = xml.nextTag(marker)) != null) {
                Field field = subTags.get(tag);
                if (field != null) {
                    Introspection.makeFieldAccessible(field);
                    Class<?> type = field.getType();
                    if (String.class == type) {
                        field.set(obj, xml.getText());
                    } else if (Collection.class.isAssignableFrom(type)) {
                        Type genericType = field.getGenericType();
                        if (genericType instanceof ParameterizedType) {
                            genericType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
                        } else {
                            throw new XMLStreamException(String.format("Unable to create object for collection tag '%s'.", tag), xml.getLocation());
                        }
                        Object   fieldObj;
                        Class<?> cls = Class.forName(genericType.getTypeName());
                        if (cls == String.class) {
                            fieldObj = xml.getText();
                        } else {
                            fieldObj = cls.getDeclaredConstructor().newInstance();
                            load(xml, fieldObj, context);
                        }
                        ((Collection) field.get(obj)).add(fieldObj);
                    } else {
                        Object fieldObj = null;
                        if (obj instanceof TagObjectCreator) {
                            fieldObj = ((TagObjectCreator) obj).xmlCreateObject(context, tag);
                        }
                        if (fieldObj == null) {
                            fieldObj = type.getDeclaredConstructor().newInstance();
                        }
                        load(xml, fieldObj, context);
                        field.set(obj, fieldObj);
                    }
                } else if (obj instanceof TagUnmatched) {
                    ((TagUnmatched) obj).xmlUnmatchedTag(context, tag);
                } else {
                    xml.skip();
                }
            }
            if (obj instanceof TagLoaded) {
                ((TagLoaded) obj).xmlLoaded(context);
            }
            if (version != 0) {
                context.popVersion();
            }
        } catch (XMLStreamException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new XMLStreamException(exception);
        }
    }

    /**
     * Saves the contents of an object into an xml file.
     *
     * @param file The file to save to.
     * @param obj  The object to save the xml data from.
     */
    public static final void save(File file, Object obj) throws XMLStreamException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            save(out, obj);
        } catch (XMLStreamException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new XMLStreamException(exception);
        }
    }

    /**
     * Saves the contents of an object into an xml file.
     *
     * @param path The {@link Path} to save to.
     * @param obj  The object to save the xml data from.
     */
    public static final void save(Path path, Object obj) throws XMLStreamException {
        try {
            URLConnection connection = PathToUri.toFixedUri(path).toURL().openConnection();
            connection.setDoInput(false);
            connection.setDoOutput(true);
            try (OutputStream out = connection.getOutputStream()) {
                save(out, obj);
            }
        } catch (XMLStreamException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new XMLStreamException(exception);
        }
    }

    /**
     * Saves the contents of an object into an xml file.
     *
     * @param out The {@link OutputStream} to save to.
     * @param obj The object to save the xml data from.
     */
    public static final void save(OutputStream out, Object obj) throws XMLStreamException {
        try (XmlGenerator xml = new XmlGenerator(out)) {
            xml.startDocument();
            add(xml, obj);
            xml.endDocument();
        } catch (XMLStreamException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new XMLStreamException(exception);
        }
    }

    /**
     * Adds the specified object to the current xml stream.
     *
     * @param xml The {@link XmlGenerator} to use.
     * @param obj The object to add. This object must have been annotated with the {@link XmlTag}
     *            annotation.
     */
    public static final void add(XmlGenerator xml, Object obj) throws XMLStreamException {
        Class<?> objClass = obj.getClass();
        XmlTag   tag      = objClass.getAnnotation(XmlTag.class);
        if (tag != null) {
            add(xml, tag.value(), obj);
        } else {
            throw new XMLStreamException(String.format("%s has not been annotated.", objClass.getName()));
        }
    }

    /**
     * Adds the specified object to the current xml stream.
     *
     * @param xml The {@link XmlGenerator} to use.
     * @param tag The xml tag to use for the object.
     * @param obj The object to add.
     */
    public static final void add(XmlGenerator xml, String tag, Object obj) throws XMLStreamException {
        try {
            if (obj != null) {
                Class<?> objClass = obj.getClass();
                if (tag == null || tag.isEmpty()) {
                    throw new XMLStreamException(String.format("%s has not been annotated.", objClass.getName()));
                }
                if (obj instanceof TagWillSave) {
                    ((TagWillSave) obj).xmlWillSave(xml);
                }
                XmlObjectHelper helper = getHelper(objClass);
                if (helper != XmlGenericHelper.SINGLETON) {
                    helper.emitAsTag(xml, tag, obj);
                } else if (obj instanceof TagExtraSubTags || hasSubTags(obj, objClass)) {
                    xml.startTag(tag);
                    emitAttributes(xml, obj, objClass);
                    emitSubTags(xml, obj, objClass);
                    if (obj instanceof TagExtraSubTags) {
                        ((TagExtraSubTags) obj).xmlEmitExtraSubTags(xml);
                    }
                    xml.endTag();
                } else {
                    xml.startEmptyTag(tag);
                    emitAttributes(xml, obj, objClass);
                }
                if (obj instanceof TagSaved) {
                    ((TagSaved) obj).xmlSaved(xml);
                }
            }
        } catch (XMLStreamException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new XMLStreamException(exception);
        }
    }

    private static boolean hasSubTags(Object obj, Class<?> objClass) throws XMLStreamException {
        for (FieldAnnotation<XmlTag> fa : Introspection.getDeepFieldAnnotations(objClass, XmlTag.class)) {
            try {
                Field field = fa.getField();
                Introspection.makeFieldAccessible(field);
                Object content = field.get(obj);
                if (content != null && (!(content instanceof String) || !((String) content).isEmpty())) {
                    if (Collection.class.isAssignableFrom(field.getType())) {
                        //noinspection CastConflictsWithInstanceof,ConstantConditions
                        if (!((Collection<?>) content).isEmpty()) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            } catch (Exception exception) {
                throw new XMLStreamException(exception);
            }
        }
        return false;
    }

    private static void emitAttributes(XmlGenerator xml, Object obj, Class<?> objClass) throws XMLStreamException, ReflectiveOperationException {
        xml.addAttributeNot(ATTR_VERSION, getVersionOfTag(objClass), 0);
        for (FieldAnnotation<XmlAttr> fa : Introspection.getDeepFieldAnnotations(objClass, XmlAttr.class)) {
            Field field = fa.getField();
            Introspection.makeFieldAccessible(field);
            getHelper(field.getType()).emitAsAttribute(xml, obj, field, fa.getAnnotation().value());
        }
        if (obj instanceof TagExtraAttributes) {
            ((TagExtraAttributes) obj).xmlEmitExtraAttributes(xml);
        }
    }

    private static void emitSubTags(XmlGenerator xml, Object obj, Class<?> objClass) throws XMLStreamException {
        for (FieldAnnotation<XmlTag> fa : Introspection.getDeepFieldAnnotations(objClass, XmlTag.class)) {
            try {
                Field field = fa.getField();
                Introspection.makeFieldAccessible(field);
                Object content = field.get(obj);
                if (content != null && (!(content instanceof String) || !((String) content).isEmpty())) {
                    XmlTag   subTag = fa.getAnnotation();
                    Class<?> type   = field.getType();
                    if (Collection.class.isAssignableFrom(type)) {
                        //noinspection CastConflictsWithInstanceof,ConstantConditions
                        Collection<?> collection = (Collection<?>) content;
                        if (!collection.isEmpty()) {
                            if (!field.isAnnotationPresent(XmlNoSort.class)) {
                                Object[] data = collection.toArray();
                                Arrays.sort(data);
                                collection = Arrays.asList(data);
                            }
                            String tag = subTag.value();
                            for (Object one : collection) {
                                add(xml, tag, one);
                            }
                        }
                    } else {
                        add(xml, subTag.value(), content);
                    }
                }
            } catch (XMLStreamException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new XMLStreamException(exception);
            }
        }
    }

    private static int getVersionOfTag(Class<?> objClass) {
        XmlTagVersion tagVersion = objClass.getAnnotation(XmlTagVersion.class);
        return tagVersion != null ? tagVersion.value() : 0;
    }

    private static int getMinimumLoadableVersionOfTag(Class<?> objClass) {
        XmlTagMinimumVersion tagVersion = objClass.getAnnotation(XmlTagMinimumVersion.class);
        return tagVersion != null ? tagVersion.value() : 0;
    }
}
