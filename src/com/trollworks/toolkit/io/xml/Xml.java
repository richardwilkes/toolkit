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
import com.trollworks.toolkit.annotation.XmlCollection;
import com.trollworks.toolkit.annotation.XmlEnumArrayAttr;
import com.trollworks.toolkit.annotation.XmlNoSort;
import com.trollworks.toolkit.annotation.XmlTag;
import com.trollworks.toolkit.annotation.XmlTagMinimumVersion;
import com.trollworks.toolkit.annotation.XmlTagVersion;
import com.trollworks.toolkit.io.xml.XmlParser.Context;
import com.trollworks.toolkit.utility.Introspection;
import com.trollworks.toolkit.utility.Localization;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

/** Provides easy loading and saving of objects that have been annotated with appropriate xml tags. */
public class Xml {
	@Localize("The root object has not been annotated.")
	private static String		ROOT_NOT_TAGGED;
	@Localize("The root tag \"%s\" was not present.")
	private static String		TAG_NOT_FOUND;
	@Localize("%s has not been annotated.")
	private static String		NOT_TAGGED;
	@Localize("%s is not a Collection.")
	private static String		NOT_COLLECTION;
	@Localize("%s is not an array.")
	private static String		NOT_ARRAY;
	@Localize("Unable to create object for collection tag '%s'.")
	@Localize(locale = "ru", value = "Невозможно создать объект для получения тэга '%s'.")
	@Localize(locale = "de", value = "Kann Objekt für Sammlungs-Tag '%s' nicht erstellen.")
	@Localize(locale = "es", value = "Imposible crear el objeto para la colección de etiquetas '%s'.")
	private static String		UNABLE_TO_CREATE_OBJECT_FOR_COLLECTION;
	@Localize("The tag '%s' is from an older version and cannot be loaded.")
	@Localize(locale = "ru", value = "Тег '%s' относится к более старой версии и не может быть загружен.")
	@Localize(locale = "de", value = "Das Tag '%s' ist von einer älteren Version und kann nicht geladen werden.")
	@Localize(locale = "es", value = "La etiqueta '%s' es de una versión anterior y no puede cargarse.")
	private static String		TOO_OLD;
	@Localize("The tag '%s' is from a newer version and cannot be loaded.")
	@Localize(locale = "ru", value = "Тег '%s' относится к более новой версии и не может быть загружен.")
	@Localize(locale = "de", value = "Das Tag '%s' ist von einer neueren Version und kann nicht geladen werden.")
	@Localize(locale = "es", value = "La etiqueta '%s' es de una versión demasiado nueva y no puede cargarse.")
	private static String		TOO_NEW;

	static {
		Localization.initialize();
	}

	/** The attribute that will be used for a tag's version. */
	public static final String	ATTR_VERSION	= "version";			//$NON-NLS-1$

	/**
	 * Loads the contents of an xml file into the specified object.
	 *
	 * @param file The file to load from.
	 * @param obj The object to load the xml data into.
	 * @param context Optional context for recording state while loading.
	 * @return The object that was passed in.
	 */
	public static <T> T load(File file, T obj, Context context) throws XMLStreamException {
		return load(file.toURI(), obj, context);
	}

	/**
	 * Loads the contents of an xml file into the specified object.
	 *
	 * @param uri The URI to load from.
	 * @param obj The object to load the xml data into.
	 * @param context Optional context for recording state while loading.
	 * @return The object that was passed in.
	 */
	public static <T> T load(URI uri, T obj, Context context) throws XMLStreamException {
		XmlTag xmlTag = obj.getClass().getAnnotation(XmlTag.class);
		if (xmlTag == null) {
			throw new XMLStreamException(ROOT_NOT_TAGGED);
		}
		try (XmlParser xml = new XmlParser(uri.toURL().openStream())) {
			String tag = xml.nextTag();
			if (tag != null && tag.equals(xmlTag.value())) {
				load(xml, obj, context);
				return obj;
			}
			throw new XMLStreamException(String.format(TAG_NOT_FOUND, xmlTag.value()));
		} catch (XMLStreamException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new XMLStreamException(exception);
		}
	}

	private static void load(XmlParser xml, Object obj, Context context) throws XMLStreamException {
		try {
			if (context == null) {
				context = new Context(xml);
			}
			String marker = xml.getMarker();
			if (obj instanceof TagWillLoad) {
				((TagWillLoad) obj).xmlWillLoad(context);
			}
			Class<?> tagClass = obj.getClass();
			int version = xml.getIntegerAttribute(ATTR_VERSION, 0);
			if (version > getVersionOfTag(tagClass)) {
				throw new XMLStreamException(String.format(TOO_NEW, xml.getCurrentTag()), xml.getLocation());
			}
			if (version < getMinimumLoadableVersionOfTag(tagClass)) {
				throw new XMLStreamException(String.format(TOO_OLD, xml.getCurrentTag()), xml.getLocation());
			}
			if (version != 0) {
				context.pushVersion(version);
			}
			Set<String> unmatchedAttributes = new HashSet<>();
			for (int i = xml.getAttributeCount(); --i > 0;) {
				unmatchedAttributes.add(xml.getAttributeName(i));
			}
			unmatchedAttributes.remove(ATTR_VERSION);
			for (Field field : Introspection.getFieldsWithAnnotation(tagClass, true, XmlAttr.class, XmlEnumArrayAttr.class)) {
				Introspection.makeFieldAccessible(field);
				XmlAttr attr = field.getAnnotation(XmlAttr.class);
				if (attr != null) {
					String name = attr.value();
					unmatchedAttributes.remove(name);
					Class<?> type = field.getType();
					if (type == boolean.class) {
						field.setBoolean(obj, xml.isAttributeSet(name, false));
					} else if (type == int.class) {
						field.setInt(obj, xml.getIntegerAttribute(name, 0));
					} else if (type == long.class) {
						field.setLong(obj, xml.getLongAttribute(name, 0));
					} else if (type == short.class) {
						field.setShort(obj, (short) xml.getIntegerAttribute(name, 0));
					} else if (type == double.class) {
						field.setDouble(obj, xml.getDoubleAttribute(name, 0.0));
					} else if (type == float.class) {
						field.setFloat(obj, (float) xml.getDoubleAttribute(name, 0.0));
					} else if (type == char.class) {
						String charStr = xml.getAttribute(name);
						field.setChar(obj, charStr == null || charStr.isEmpty() ? 0 : charStr.charAt(0));
					} else if (type == String.class) {
						field.set(obj, xml.getAttribute(name, "")); //$NON-NLS-1$
					} else if (type == UUID.class) {
						field.set(obj, UUID.fromString(xml.getAttribute(name, ""))); //$NON-NLS-1$
					} else if (type.isEnum()) {
						field.set(obj, getMatchingEnum(type, xml.getAttribute(name, ""))); //$NON-NLS-1$
					} else {
						field.set(obj, type.getConstructor(String.class).newInstance(xml.getAttribute(name, ""))); //$NON-NLS-1$
					}
				} else {
					XmlEnumArrayAttr enumAttr = field.getAnnotation(XmlEnumArrayAttr.class);
					if (enumAttr != null) {
						Object arrayObj = field.get(obj);
						Class<? extends Object> arrayClass = arrayObj.getClass();
						Class<?> type = arrayClass.getComponentType();
						if (type == null) {
							throw new XMLStreamException(String.format(NOT_ARRAY, field.getName()));
						}
						int index = 0;
						for (Enum<?> one : enumAttr.value().getEnumConstants()) {
							XmlTag xmlTag = one.getClass().getField(((Enum<?>) one).name()).getAnnotation(XmlTag.class);
							if (xmlTag != null) {
								String name = xmlTag.value();
								unmatchedAttributes.remove(name);
								if (type == boolean.class) {
									((boolean[]) arrayObj)[index] = xml.isAttributeSet(name, false);
								} else if (type == int.class) {
									((int[]) arrayObj)[index] = xml.getIntegerAttribute(name, 0);
								} else if (type == long.class) {
									((long[]) arrayObj)[index] = xml.getLongAttribute(name, 0);
								} else if (type == short.class) {
									((short[]) arrayObj)[index] = (short) xml.getIntegerAttribute(name, 0);
								} else if (type == double.class) {
									((double[]) arrayObj)[index] = xml.getDoubleAttribute(name, 0.0);
								} else if (type == float.class) {
									((float[]) arrayObj)[index] = (float) xml.getDoubleAttribute(name, 0.0);
								} else if (type == char.class) {
									String charStr = xml.getAttribute(name);
									((char[]) arrayObj)[index] = charStr == null || charStr.isEmpty() ? 0 : charStr.charAt(0);
								} else if (type == String.class) {
									((String[]) arrayObj)[index] = xml.getAttribute(name, ""); //$NON-NLS-1$
								} else if (type == UUID.class) {
									((UUID[]) arrayObj)[index] = UUID.fromString(xml.getAttribute(name, "")); //$NON-NLS-1$
								} else if (type.isEnum()) {
									((Object[]) arrayObj)[index] = getMatchingEnum(type, xml.getAttribute(name, "")); //$NON-NLS-1$
								} else {
									((Object[]) arrayObj)[index] = type.getConstructor(String.class).newInstance(xml.getAttribute(name, "")); //$NON-NLS-1$
								}
							}
							index++;
						}
					}
				}
			}
			if (obj instanceof TagAttributesLoaded) {
				((TagAttributesLoaded) obj).xmlAttributesLoaded(context, unmatchedAttributes);
			}
			Map<String, Field> subTags = new HashMap<>();
			for (Field field : Introspection.getFieldsWithAnnotation(tagClass, true, XmlTag.class)) {
				subTags.put(field.getAnnotation(XmlTag.class).value(), field);
			}
			Map<String, Field> collectionSubTags = new HashMap<>();
			for (Field field : Introspection.getFieldsWithAnnotation(tagClass, true, XmlCollection.class)) {
				collectionSubTags.put(field.getAnnotation(XmlCollection.class).value(), field);
			}
			String tag;
			while ((tag = xml.nextTag(marker)) != null) {
				Field field = subTags.get(tag);
				if (field != null) {
					Introspection.makeFieldAccessible(field);
					Class<?> type = field.getType();
					if (String.class == type) {
						field.set(obj, xml.getText());
					} else {
						Object fieldObj = null;
						if (obj instanceof TagObjectCreator) {
							fieldObj = ((TagObjectCreator) obj).xmlCreateObject(context, tag);
						}
						if (fieldObj == null) {
							fieldObj = type.newInstance();
						}
						load(xml, fieldObj, context);
						field.set(obj, fieldObj);
					}
				} else {
					field = collectionSubTags.get(tag);
					if (field != null) {
						Introspection.makeFieldAccessible(field);
						Type genericType = field.getGenericType();
						if (genericType instanceof ParameterizedType) {
							genericType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
						} else {
							throw new XMLStreamException(String.format(UNABLE_TO_CREATE_OBJECT_FOR_COLLECTION, tag), xml.getLocation());
						}
						Object fieldObj = null;
						Class<?> cls = Class.forName(genericType.getTypeName());
						if (cls == String.class) {
							fieldObj = xml.getText();
						} else {
							fieldObj = cls.newInstance();
							load(xml, fieldObj, context);
						}
						ensureCollectionIsAllocated(obj, field, null).add(fieldObj);
					} else if (obj instanceof TagUnmatched) {
						((TagUnmatched) obj).xmlUnmatchedTag(context, tag);
					} else {
						xml.skip();
					}
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

	private static Object getMatchingEnum(Class<?> type, String tag) throws NoSuchFieldException, SecurityException {
		for (Object one : type.getEnumConstants()) {
			XmlTag xmlTag = one.getClass().getField(((Enum<?>) one).name()).getAnnotation(XmlTag.class);
			if (xmlTag != null && xmlTag.value().equals(tag)) {
				return one;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static Collection<Object> ensureCollectionIsAllocated(Object obj, Field field, Object collection) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		if (collection == null) {
			collection = field.get(obj);
			if (collection == null) {
				Class<?> type = field.getType();
				if (type == List.class) {
					collection = new ArrayList<>();
				} else if (type == Set.class) {
					collection = new HashSet<>();
				} else {
					collection = type.newInstance();
				}
				field.set(obj, collection);
			}
		}
		return (Collection<Object>) collection;
	}

	/**
	 * Saves the contents of an object into an xml file.
	 *
	 * @param file The file to save to.
	 * @param obj The object to save the xml data from.
	 */
	public static void save(File file, Object obj) throws XMLStreamException {
		try (XmlGenerator xml = new XmlGenerator(new FileOutputStream(file))) {
			xml.startDocument();
			add(xml, obj);
			xml.endDocument();
		} catch (XMLStreamException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new XMLStreamException(exception);
		}
	}

	private static void add(XmlGenerator xml, Object obj) throws XMLStreamException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Class<?> objClass = obj.getClass();
		XmlTag tag = objClass.getAnnotation(XmlTag.class);
		if (tag != null) {
			add(xml, tag.value(), obj);
		} else {
			throw new XMLStreamException(String.format(NOT_TAGGED, objClass.getName()));
		}
	}

	private static void add(XmlGenerator xml, String tag, Object obj) throws XMLStreamException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		if (obj != null) {
			Class<?> objClass = obj.getClass();
			if (tag == null || tag.isEmpty()) {
				throw new XMLStreamException(String.format(NOT_TAGGED, objClass.getName()));
			}
			if (obj instanceof TagWillSave) {
				((TagWillSave) obj).xmlWillSave(xml);
			}
			if (obj instanceof String) {
				String str = (String) obj;
				if (!str.isEmpty()) {
					xml.startTag(tag);
					xml.addText(str);
					xml.endTag();
				}
			} else if (hasSubTags(obj, objClass)) {
				xml.startTag(tag);
				emitAttributes(xml, obj, objClass);
				emitSubTags(xml, obj, objClass);
				xml.endTag();
			} else {
				xml.startEmptyTag(tag);
				emitAttributes(xml, obj, objClass);
			}
		}
	}

	private static boolean hasSubTags(Object obj, Class<?> objClass) throws XMLStreamException {
		for (Field field : Introspection.getFieldsWithAnnotation(objClass, true, XmlTag.class, XmlCollection.class)) {
			try {
				Introspection.makeFieldAccessible(field);
				Object content = field.get(obj);
				if (content != null && (!(content instanceof String) || !((String) content).isEmpty())) {
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

	private static void emitAttributes(XmlGenerator xml, Object obj, Class<?> objClass) throws XMLStreamException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		xml.addAttributeNot(ATTR_VERSION, getVersionOfTag(objClass), 0);
		for (Field field : Introspection.getFieldsWithAnnotation(objClass, true, XmlAttr.class, XmlEnumArrayAttr.class)) {
			Introspection.makeFieldAccessible(field);
			XmlAttr xmlAttr = field.getAnnotation(XmlAttr.class);
			if (xmlAttr != null) {
				String name = xmlAttr.value();
				try {
					Class<?> type = field.getType();
					if (type == boolean.class) {
						xml.addAttributeNot(name, field.getBoolean(obj), false);
					} else if (type == int.class || type == short.class) {
						xml.addAttributeNot(name, field.getInt(obj), 0);
					} else if (type == long.class) {
						xml.addAttributeNot(name, field.getLong(obj), 0);
					} else if (type == double.class || type == float.class) {
						xml.addAttributeNot(name, field.getDouble(obj), 0.0);
					} else if (type.isEnum()) {
						Object content = field.get(obj);
						if (content != null) {
							XmlTag xmlTag = content.getClass().getField(((Enum<?>) content).name()).getAnnotation(XmlTag.class);
							if (xmlTag != null) {
								xml.addAttribute(name, xmlTag.value());
							}
						}
					} else {
						Object content = field.get(obj);
						if (content != null) {
							xml.addAttributeNotEmpty(name, content.toString());
						}
					}
				} catch (Exception exception) {
					throw new XMLStreamException(exception);
				}
			} else {
				XmlEnumArrayAttr enumAttr = field.getAnnotation(XmlEnumArrayAttr.class);
				if (enumAttr != null) {
					Object arrayObj = field.get(obj);
					Class<? extends Object> arrayClass = arrayObj.getClass();
					Class<?> type = arrayClass.getComponentType();
					if (type == null) {
						throw new XMLStreamException(String.format(NOT_ARRAY, field.getName()));
					}
					int index = 0;
					for (Enum<?> one : enumAttr.value().getEnumConstants()) {
						XmlTag xmlTag = one.getClass().getField(((Enum<?>) one).name()).getAnnotation(XmlTag.class);
						if (xmlTag != null) {
							String name = xmlTag.value();
							if (type == boolean.class) {
								xml.addAttributeNot(name, ((boolean[]) arrayObj)[index], false);
							} else if (type == int.class) {
								xml.addAttributeNot(name, ((int[]) arrayObj)[index], 0);
							} else if (type == long.class) {
								xml.addAttributeNot(name, ((long[]) arrayObj)[index], 0);
							} else if (type == short.class) {
								xml.addAttributeNot(name, ((short[]) arrayObj)[index], 0);
							} else if (type == double.class) {
								xml.addAttributeNot(name, ((double[]) arrayObj)[index], 0);
							} else if (type == float.class) {
								xml.addAttributeNot(name, ((float[]) arrayObj)[index], 0);
							} else if (type.isEnum()) {
								Object content = ((Object[]) arrayObj)[index];
								if (content != null) {
									XmlTag contentTag = content.getClass().getField(((Enum<?>) content).name()).getAnnotation(XmlTag.class);
									if (contentTag != null) {
										xml.addAttribute(name, contentTag.value());
									}
								}
							} else {
								Object content = ((Object[]) arrayObj)[index];
								if (content != null) {
									xml.addAttributeNotEmpty(name, content.toString());
								}
							}
						}
						index++;
					}
				}
			}
		}
		if (obj instanceof TagExtraAttributes) {
			((TagExtraAttributes) obj).xmlEmitExtraAttributes(xml);
		}
	}

	private static void emitSubTags(XmlGenerator xml, Object obj, Class<?> objClass) throws XMLStreamException {
		for (Field field : Introspection.getFieldsWithAnnotation(objClass, true, XmlTag.class, XmlCollection.class)) {
			try {
				Introspection.makeFieldAccessible(field);
				Object content = field.get(obj);
				if (content != null && (!(content instanceof String) || !((String) content).isEmpty())) {
					XmlTag subTag = field.getAnnotation(XmlTag.class);
					if (subTag != null) {
						add(xml, subTag.value(), content);
					} else {
						XmlCollection collectionTag = field.getAnnotation(XmlCollection.class);
						if (collectionTag != null) {
							Class<?> type = field.getType();
							if (Collection.class.isAssignableFrom(type)) {
								Collection<?> collection = (Collection<?>) content;
								if (!collection.isEmpty()) {
									if (!field.isAnnotationPresent(XmlNoSort.class)) {
										Object[] data = collection.toArray();
										Arrays.sort(data);
										collection = Arrays.asList(data);
									}
									String tag = collectionTag.value();
									for (Object one : collection) {
										add(xml, tag, one);
									}
								}
							} else {
								throw new XMLStreamException(String.format(NOT_COLLECTION, field.getName()));
							}
						}
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

	/**
	 * Objects that wish to be notified before they are about to be loaded should implement this
	 * interface.
	 */
	public interface TagWillLoad {
		/**
		 * Called before the XML tag will be loaded into the object.
		 *
		 * @param context The {@link Context} for this object.
		 */
		void xmlWillLoad(Context context) throws XMLStreamException;
	}

	/**
	 * Objects that wish to be notified when their attributes have been loaded should implement this
	 * interface.
	 */
	public interface TagAttributesLoaded {
		/**
		 * Called after the XML tag attributes have been fully loaded into the object, just prior to
		 * loading any sub-tags that may be present.
		 *
		 * @param context The {@link Context} for this object.
		 * @param unmatchedAttributes A {@link Set} of attribute names found in the XML that had no
		 *            matching {@link XmlAttr}-marked fields.
		 */
		void xmlAttributesLoaded(Context context, Set<String> unmatchedAttributes) throws XMLStreamException;
	}

	/**
	 * Objects that wish to be notified when they have been loaded should implement this interface.
	 */
	public interface TagLoaded {
		/**
		 * Called after the XML tag has been fully loaded into the object, just prior to the version
		 * being popped off the stack and control being returned to the caller.
		 *
		 * @param context The {@link Context} for this object.
		 */
		void xmlLoaded(Context context) throws XMLStreamException;
	}

	/**
	 * Objects that wish to control the object creation process for their fields should implement
	 * this interface.
	 */
	public interface TagObjectCreator {
		/**
		 * Called to create an object for an XML tag.
		 *
		 * @param context The {@link Context} for this object.
		 * @param tag The tag to return an object for.
		 * @return The newly created object, or <code>null</code> if a new instance of the field's
		 *         data type should be created (i.e. when there is no need to use a sub-class and
		 *         the default no-args constructor can be used).
		 */
		Object xmlCreateObject(Context context, String tag) throws XMLStreamException;
	}

	/**
	 * Objects that wish to control how unmatched tags are handled should implement this interface.
	 */
	public interface TagUnmatched {
		/**
		 * Called to process an XML sub-tag that had no matching fields marked with {@link XmlTag}
		 * or {@link XmlCollection}. Upon return from this method, the {@link XmlParser} should have
		 * been advanced past the current tag's contents, either by calling {@link XmlParser#skip()}
		 * or appropriate parsing of sub-tags.
		 *
		 * @param context The {@link Context} for this object.
		 * @param tag The tag name that will be processed.
		 */
		void xmlUnmatchedTag(Context context, String tag) throws XMLStreamException;
	}

	/**
	 * Objects that wish to be notified before they are about to be written to xml should implement
	 * this interface.
	 */
	public interface TagWillSave {
		/**
		 * Called before the XML tag will be written.
		 *
		 * @param xml The {@link XmlGenerator} for this object.
		 */
		void xmlWillSave(XmlGenerator xml) throws XMLStreamException;
	}

	/**
	 * Objects that wish to add additional attributes when being written to xml should implement
	 * this interface.
	 */
	public interface TagExtraAttributes {
		/**
		 * Called to allow an object to emit additional attributes that the standard processing
		 * can't handle.
		 *
		 * @param xml The {@link XmlGenerator} for this object.
		 */
		void xmlEmitExtraAttributes(XmlGenerator xml) throws XMLStreamException;
	}
}
