/*
 * Copyright (c) 1998-2015 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.io.xml;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.annotation.XmlAttr;
import com.trollworks.toolkit.annotation.XmlNoSort;
import com.trollworks.toolkit.annotation.XmlTag;
import com.trollworks.toolkit.annotation.XmlTagMinimumVersion;
import com.trollworks.toolkit.annotation.XmlTagVersion;
import com.trollworks.toolkit.utility.Introspection;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.Text;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/** Provides simple XML generation. */
public class XmlGenerator implements AutoCloseable {
	@Localize("%s has not been annotated with @%s.")
	@Localize(locale = "ru", value = "%s не имеет комментариев @%s.")
	@Localize(locale = "de", value = "%s wurde nicht mit @%s annotiert.")
	@Localize(locale = "es", value = "%s no ha sido anotado con @%s.")
	private static String		NOT_TAGGED;

	static {
		Localization.initialize();
	}

	/**
	 * The attribute that will be used for a tag's version, if {@link #add(Object)} or
	 * {@link #add(String, Object)} is called.
	 */
	public static final String	ATTR_VERSION	= "version";	//$NON-NLS-1$
	private String				mIndent			= "\t";		//$NON-NLS-1$
	private XMLStreamWriter		mWriter;
	private int					mDepth;
	private boolean				mHadText;

	/**
	 * Creates a new {@link XmlGenerator}.
	 *
	 * @param stream The {@link OutputStream} to write to.
	 */
	public XmlGenerator(OutputStream stream) throws XMLStreamException {
		mWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(stream, Text.UTF8_ENCODING);
	}

	/** @param indent The characters to use for indentation. */
	public void setIndent(String indent) {
		mIndent = indent;
	}

	/** Emits the XML document header. */
	public void startDocument() throws XMLStreamException {
		mWriter.writeStartDocument(Text.UTF8_ENCODING, "1.0");	//$NON-NLS-1$
	}

	/** Finishes the document. */
	public void endDocument() throws XMLStreamException {
		mWriter.writeEndDocument();
	}

	private void eol() throws XMLStreamException {
		mWriter.writeCharacters("\n");	//$NON-NLS-1$
		for (int i = 0; i < mDepth; i++) {
			mWriter.writeCharacters(mIndent);
		}
	}

	/**
	 * Opens a new XML tag.
	 *
	 * @param name The name of the tag.
	 */
	public void startTag(String name) throws XMLStreamException {
		eol();
		mWriter.writeStartElement(name);
		mDepth++;
	}

	/**
	 * Opens a new, empty, XML tag.
	 *
	 * @param name The name of the tag.
	 */
	public void startEmptyTag(String name) throws XMLStreamException {
		eol();
		mWriter.writeEmptyElement(name);
	}

	/** Closes the current tag. */
	public void endTag() throws XMLStreamException {
		mDepth--;
		if (!mHadText) {
			eol();
		} else {
			mHadText = false;
		}
		mWriter.writeEndElement();
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 */
	public void addAttribute(String name, String value) throws XMLStreamException {
		mWriter.writeAttribute(name, value == null ? "" : value);	//$NON-NLS-1$
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 */
	public void addAttribute(String name, boolean value) throws XMLStreamException {
		mWriter.writeAttribute(name, value ? "yes" : "no");	//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 */
	public void addAttribute(String name, int value) throws XMLStreamException {
		mWriter.writeAttribute(name, Integer.toString(value));
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 */
	public void addAttribute(String name, long value) throws XMLStreamException {
		mWriter.writeAttribute(name, Long.toString(value));
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 */
	public void addAttribute(String name, double value) throws XMLStreamException {
		mWriter.writeAttribute(name, Double.toString(value));
	}

	/**
	 * Adds an attribute to the currently open tag, but only if it isn't empty.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 */
	public void addAttributeNotEmpty(String name, String value) throws XMLStreamException {
		if (value != null && value.length() > 0) {
			mWriter.writeAttribute(name, value);
		}
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 * @param not Only add the attribute if it is not equal to this value.
	 */
	public void addAttributeNot(String name, String value, String not) throws XMLStreamException {
		if (not != null ? !not.equals(value) : value != null) {
			addAttribute(name, value);
		}
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 * @param not Only add the attribute if it is not equal to this value.
	 */
	public void addAttributeNot(String name, boolean value, boolean not) throws XMLStreamException {
		if (value != not) {
			addAttribute(name, value);
		}
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 * @param not Only add the attribute if it is not equal to this value.
	 */
	public void addAttributeNot(String name, int value, int not) throws XMLStreamException {
		if (value != not) {
			addAttribute(name, value);
		}
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 * @param not Only add the attribute if it is not equal to this value.
	 */
	public void addAttributeNot(String name, long value, long not) throws XMLStreamException {
		if (value != not) {
			addAttribute(name, value);
		}
	}

	/**
	 * Adds an attribute to the currently open tag.
	 *
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 * @param not Only add the attribute if it is not equal to this value.
	 */
	public void addAttributeNot(String name, double value, double not) throws XMLStreamException {
		if (value != not) {
			addAttribute(name, value);
		}
	}

	/**
	 * Adds text to the currently open tag.
	 *
	 * @param text The text to add.
	 */
	public void addText(String text) throws XMLStreamException {
		mWriter.writeCharacters(text);
		mHadText = true;
	}

	/**
	 * Adds a comment.
	 *
	 * @param comment The comment.
	 */
	public void comment(String comment) throws XMLStreamException {
		eol();
		mWriter.writeComment(' ' + comment + ' ');
	}

	/** Closes this {@link XmlGenerator}. No further writing can be attempted with it. */
	@Override
	public void close() throws XMLStreamException {
		if (mWriter != null) {
			try {
				mWriter.close();
			} finally {
				mWriter = null;
			}
		}
	}

	/**
	 * Adds an object to the XML.
	 *
	 * @param obj The object to add. Must be annotated with {@link XmlTag}.
	 */
	public void add(Object obj) throws XMLStreamException {
		add(null, obj);
	}

	/**
	 * Adds an object to the XML.
	 *
	 * @param tag The tag to use for this object. May be <code>null</code>.
	 * @param obj The object to add. If <code>tag</code> is <code>null</code> or empty, then the
	 *            object must be annotated with {@link XmlTag}.
	 */
	public void add(String tag, Object obj) throws XMLStreamException {
		Class<?> objClass = obj.getClass();
		if (tag == null || tag.isEmpty()) {
			XmlTag xmlTag = objClass.getAnnotation(XmlTag.class);
			if (xmlTag == null) {
				throw new XMLStreamException(String.format(NOT_TAGGED, obj.getClass().getName(), XmlTag.class.getSimpleName()));
			}
			tag = xmlTag.value();
		}
		if (hasSubTags(obj, objClass)) {
			startTag(tag);
			emitAttributes(obj, objClass);
			emitSubTags(obj, objClass);
			endTag();
		} else {
			startEmptyTag(tag);
			emitAttributes(obj, objClass);
		}
	}

	private static boolean hasSubTags(Object obj, Class<?> objClass) throws XMLStreamException {
		for (Field field : Introspection.getFieldsWithAnnotation(objClass, XmlTag.class, true)) {
			try {
				Introspection.makeFieldAccessible(field);
				Object content = field.get(obj);
				if (content != null) {
					if (Collection.class.isAssignableFrom(field.getType())) {
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

	private void emitSubTags(Object obj, Class<?> objClass) throws XMLStreamException {
		for (Field field : Introspection.getFieldsWithAnnotation(objClass, XmlTag.class, true)) {
			try {
				Introspection.makeFieldAccessible(field);
				Object content = field.get(obj);
				if (content != null) {
					XmlTag subTag = field.getAnnotation(XmlTag.class);
					Class<?> type = field.getType();
					if (Collection.class.isAssignableFrom(type)) {
						Collection<?> collection = (Collection<?>) content;
						if (!field.isAnnotationPresent(XmlNoSort.class)) {
							Object[] data = collection.toArray();
							Arrays.sort(data);
							collection = Arrays.asList(data);
						}
						if (!collection.isEmpty()) {
							String wrapName = subTag.value();
							if (!wrapName.isEmpty()) {
								startTag(wrapName);
							}
							for (Object one : collection) {
								add(one);
							}
							if (!wrapName.isEmpty()) {
								endTag();
							}
						}
					} else {
						add(subTag.value(), content);
					}
				}
			} catch (XMLStreamException exception) {
				throw exception;
			} catch (Exception exception) {
				throw new XMLStreamException(exception);
			}
		}
	}

	private void emitAttributes(Object obj, Class<?> objClass) throws XMLStreamException {
		addAttributeNot(ATTR_VERSION, getVersionOfTag(objClass), 0);
		for (Field field : Introspection.getFieldsWithAnnotation(objClass, XmlAttr.class, true)) {
			String name = field.getAnnotation(XmlAttr.class).value();
			try {
				Introspection.makeFieldAccessible(field);
				Class<?> type = field.getType();
				if (type == boolean.class) {
					addAttributeNot(name, field.getBoolean(obj), false);
				} else if (type == int.class || type == short.class) {
					addAttributeNot(name, field.getInt(obj), 0);
				} else if (type == long.class) {
					addAttributeNot(name, field.getLong(obj), 0);
				} else if (type == double.class || type == float.class) {
					addAttributeNot(name, field.getDouble(obj), 0.0);
				} else {
					Object content = field.get(obj);
					if (content != null) {
						addAttributeNotEmpty(name, content.toString());
					}
				}
			} catch (Exception exception) {
				throw new XMLStreamException(exception);
			}
		}
	}

	/**
	 * @param objClass The {@link Class} to retrieve the information for.
	 * @return The version of the XML tag that would be emitted for the specified {@link Class}.
	 */
	public static int getVersionOfTag(Class<?> objClass) {
		XmlTagVersion tagVersion = objClass.getAnnotation(XmlTagVersion.class);
		return tagVersion != null ? tagVersion.value() : 0;
	}

	/**
	 * @param objClass The {@link Class} to retrieve the information for.
	 * @return The minimum version of the XML tag that can be loaded for the specified {@link Class}
	 *         .
	 */
	public static int getMinimumLoadableVersionOfTag(Class<?> objClass) {
		XmlTagMinimumVersion tagVersion = objClass.getAnnotation(XmlTagMinimumVersion.class);
		return tagVersion != null ? tagVersion.value() : 0;
	}
}
