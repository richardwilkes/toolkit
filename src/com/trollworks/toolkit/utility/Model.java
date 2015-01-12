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

package com.trollworks.toolkit.utility;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.io.xml.XmlGenerator;
import com.trollworks.toolkit.io.xml.XmlParser;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import javax.xml.stream.XMLStreamException;

/** The abstract base model object, responsible for providing basic i/o. */
public abstract class Model implements Cloneable {
	@Localize("Expected tag \"{0}\", but found \"{1}\".")
	@Localize(locale = "ru", value = "Ожидаемый тег \"{0}\", но найден \"{1}\".")
	@Localize(locale = "de", value = "Tag \"{0}\" erwartet, aber \"{1}\" erhalten.")
	@Localize(locale = "es", value = "Se esperaba la etiqueta \"{0}\", pero se encontró \"{1}\".")
	private static String		INVALID_ROOT_TAG;
	@Localize("The tag \"{0}\" is from an older version and cannot be loaded.")
	@Localize(locale = "ru", value = "Тег \"{0}\" относится к более старой версии и не может быть загружен.")
	@Localize(locale = "de", value = "Das Tag \"{0}\" ist von einer älteren Version und kann nicht geladen werden.")
	@Localize(locale = "es", value = "La etiqueta \"{0}\" proviene de una versión anterior y no puede abrirse")
	private static String		TOO_OLD;
	@Localize("The tag \"{0}\" is from a newer version and cannot be loaded.")
	@Localize(locale = "ru", value = "Тег \"{0}\" относится к более новой версии и не может быть загружен.")
	@Localize(locale = "de", value = "Das Tag \"{0}\" ist von einer neueren Version und kann nicht geladen werden.")
	@Localize(locale = "es", value = "La etiqueta \"{0}\" proviene de una versión posterior y no puede abrirse")
	private static String		TOO_NEW;

	static {
		Localization.initialize();
	}

	private static final String	ATTR_VERSION	= "version";	//$NON-NLS-1$

	/** @return The root XML tag name. */
	public abstract String getRootTag();

	/** @return The current version of the root XML tag. */
	public abstract int getCurrentVersion();

	/** @return The minimum version of the root XML tag. */
	public abstract int getMinimumVersion();

	/**
	 * Load the model's content from the specified {@link InputStream}.
	 *
	 * @param stream The {@link InputStream} to load from.
	 * @param context The {@link ModelContext} to use.
	 */
	public final void load(InputStream stream, ModelContext context) throws XMLStreamException {
		try (XmlParser parser = new XmlParser(stream)) {
			String tag;
			while ((tag = parser.nextTag()) != null) {
				if (getRootTag().equals(tag)) {
					load(parser, context);
					break;
				}
				parser.skip();
			}
		}
	}

	/**
	 * Load the model's content from the specified {@link XmlParser}.
	 *
	 * @param parser The {@link XmlParser} to load from.
	 * @param context The {@link ModelContext} to use.
	 */
	public final void load(XmlParser parser, ModelContext context) throws XMLStreamException {
		String tag = parser.getCurrentTag();
		if (getRootTag().equals(tag)) {
			modelWillLoad(context);
			String marker = parser.getMarker();
			int version = parser.getIntegerAttribute(ATTR_VERSION);
			if (version < getMinimumVersion()) {
				throw new XMLStreamException(MessageFormat.format(TOO_OLD, parser.getCurrentTag()), parser.getLocation());
			}
			if (version > getCurrentVersion()) {
				throw new XMLStreamException(MessageFormat.format(TOO_NEW, parser.getCurrentTag()), parser.getLocation());
			}
			context.mVersionStack.push(Integer.valueOf(version));
			loadAttributes(parser, context);
			if (!loadContents(parser, context)) {
				while ((tag = parser.nextTag(marker)) != null) {
					loadChildTag(tag, parser, context);
				}
			}
			modelDidLoad(context);
			context.mVersionStack.pop();
		} else {
			throw new XMLStreamException(MessageFormat.format(INVALID_ROOT_TAG, getRootTag(), tag), parser.getLocation());
		}
	}

	/**
	 * Called to allow the loading of the text contents of the tag, rather than sub-tags.
	 *
	 * @param parser The {@link XmlParser} to load from.
	 * @param context The {@link ModelContext} to use.
	 * @return <code>true</code> if a call was made to {@link XmlParser#getText()}.
	 */
	@SuppressWarnings({ "unused", "static-method" })
	protected boolean loadContents(XmlParser parser, ModelContext context) throws XMLStreamException {
		return false;
	}

	/**
	 * Called just prior to the model being loaded. The default implementation does nothing.
	 *
	 * @param context The {@link ModelContext}.
	 */
	@SuppressWarnings("unused")
	protected void modelWillLoad(ModelContext context) throws XMLStreamException {
		// Does nothing by default.
	}

	/**
	 * Called just after the model is loaded. The default implementation does nothing.
	 *
	 * @param context The {@link ModelContext}.
	 */
	@SuppressWarnings("unused")
	protected void modelDidLoad(ModelContext context) throws XMLStreamException {
		// Does nothing by default.
	}

	/**
	 * Called to load attributes from the root tag. The default implementation does nothing.
	 *
	 * @param parser The {@link XmlParser}.
	 * @param context The {@link ModelContext}.
	 */
	@SuppressWarnings("unused")
	protected void loadAttributes(XmlParser parser, ModelContext context) throws XMLStreamException {
		// Does nothing by default.
	}

	/**
	 * Called for each top-level child tag in the model. The default implementation skips all tags.
	 *
	 * @param tag The current tag to load.
	 * @param parser The {@link XmlParser}.
	 * @param context The {@link ModelContext}.
	 */
	@SuppressWarnings("static-method")
	protected void loadChildTag(String tag, XmlParser parser, ModelContext context) throws XMLStreamException {
		parser.skip();
	}

	/**
	 * Saves the model's content to the specified {@link OutputStream}.
	 *
	 * @param stream The {@link OutputStream} to save to.
	 * @param context The {@link ModelContext} to use.
	 */
	public final void save(OutputStream stream, ModelContext context) throws XMLStreamException {
		try (XmlGenerator generator = new XmlGenerator(stream)) {
			generator.startDocument();
			save(generator, context);
			generator.endDocument();
		}
	}

	/**
	 * Save the model's content to the specified {@link XmlGenerator}.
	 *
	 * @param generator The {@link XmlGenerator} to load from.
	 * @param context The {@link ModelContext} to use.
	 */
	public final void save(XmlGenerator generator, ModelContext context) throws XMLStreamException {
		modelWillSave(context);
		String tag = getRootTag();
		if (isEmptyTag()) {
			generator.startEmptyTag(tag);
			generator.addAttribute(ATTR_VERSION, getCurrentVersion());
			saveAttributes(generator, context);
		} else {
			generator.startTag(tag);
			generator.addAttribute(ATTR_VERSION, getCurrentVersion());
			saveAttributes(generator, context);
			saveChildTags(generator, context);
			generator.endTag();
		}
		modelDidSave(context);
	}

	/** @return Whether or not this is an empty tag (i.e. just attributes). */
	protected abstract boolean isEmptyTag();

	/**
	 * Called just prior to the model being saved. The default implementation does nothing.
	 *
	 * @param context The {@link ModelContext}.
	 */
	@SuppressWarnings("unused")
	protected void modelWillSave(ModelContext context) throws XMLStreamException {
		// Does nothing by default.
	}

	/**
	 * Called just after the model is saved. The default implementation does nothing.
	 *
	 * @param context The {@link ModelContext}.
	 */
	@SuppressWarnings("unused")
	protected void modelDidSave(ModelContext context) throws XMLStreamException {
		// Does nothing by default.
	}

	/**
	 * Called to save the root tag attributes. The default implementation does nothing.
	 *
	 * @param generator The {@link XmlGenerator} to load from.
	 * @param context The {@link ModelContext} to use.
	 */
	@SuppressWarnings("unused")
	protected void saveAttributes(XmlGenerator generator, ModelContext context) throws XMLStreamException {
		// Does nothing by default.
	}

	/**
	 * Called to save the top-level tags in the model. The default implementation does nothing.
	 *
	 * @param generator The {@link XmlGenerator}.
	 * @param context The {@link ModelContext}.
	 */
	@SuppressWarnings("unused")
	protected void saveChildTags(XmlGenerator generator, ModelContext context) throws XMLStreamException {
		// Does nothing by default.
	}

	@Override
	public Model clone() {
		try {
			return (Model) super.clone();
		} catch (CloneNotSupportedException exception) {
			return null; // Not possible
		}
	}
}
