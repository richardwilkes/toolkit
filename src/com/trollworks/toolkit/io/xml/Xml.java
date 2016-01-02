/*
 * Copyright (c) 1998-2016 by Richard A. Wilkes. All rights reserved.
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
import com.trollworks.toolkit.annotation.XmlDefault;
import com.trollworks.toolkit.annotation.XmlDefaultBoolean;
import com.trollworks.toolkit.annotation.XmlDefaultByte;
import com.trollworks.toolkit.annotation.XmlDefaultChar;
import com.trollworks.toolkit.annotation.XmlDefaultDouble;
import com.trollworks.toolkit.annotation.XmlDefaultFloat;
import com.trollworks.toolkit.annotation.XmlDefaultInteger;
import com.trollworks.toolkit.annotation.XmlDefaultLong;
import com.trollworks.toolkit.annotation.XmlDefaultShort;
import com.trollworks.toolkit.annotation.XmlEnumArrayAttr;
import com.trollworks.toolkit.annotation.XmlNoSort;
import com.trollworks.toolkit.annotation.XmlTag;
import com.trollworks.toolkit.annotation.XmlTagMinimumVersion;
import com.trollworks.toolkit.annotation.XmlTagVersion;
import com.trollworks.toolkit.io.xml.XmlParser.Context;
import com.trollworks.toolkit.utility.Introspection;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.workarounds.PathToUri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
	 * @return The object that was passed in.
	 */
	public static <T> T load(File file, T obj) throws XMLStreamException {
		return load(file, obj, null);
	}

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
	 * @param path The {@link Path} to load from.
	 * @param obj The object to load the xml data into.
	 * @return The object that was passed in.
	 */
	public static <T> T load(Path path, T obj) throws XMLStreamException {
		return load(PathToUri.toFixedUri(path), obj, null);
	}

	/**
	 * Loads the contents of an xml file into the specified object.
	 *
	 * @param path The {@link Path} to load from.
	 * @param obj The object to load the xml data into.
	 * @param context Optional context for recording state while loading.
	 * @return The object that was passed in.
	 */
	public static <T> T load(Path path, T obj, Context context) throws XMLStreamException {
		return load(PathToUri.toFixedUri(path), obj, context);
	}

	/**
	 * Loads the contents of an xml file into the specified object.
	 *
	 * @param uri The URI to load from.
	 * @param obj The object to load the xml data into.
	 * @return The object that was passed in.
	 */
	public static <T> T load(URI uri, T obj) throws XMLStreamException {
		return load(uri, obj, null);
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
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
						field.setBoolean(obj, loadBooleanAttribute(xml, field, name));
					} else if (type == byte.class) {
						field.setByte(obj, loadByteAttribute(xml, field, name));
					} else if (type == char.class) {
						field.setChar(obj, loadCharAttribute(xml, field, name));
					} else if (type == short.class) {
						field.setShort(obj, loadShortAttribute(xml, field, name));
					} else if (type == int.class) {
						field.setInt(obj, loadIntegerAttribute(xml, field, name));
					} else if (type == long.class) {
						field.setLong(obj, loadLongAttribute(xml, field, name));
					} else if (type == float.class) {
						field.setFloat(obj, loadFloatAttribute(xml, field, name));
					} else if (type == double.class) {
						field.setDouble(obj, loadDoubleAttribute(xml, field, name));
					} else if (type == String.class) {
						field.set(obj, loadStringAttribute(xml, field, name));
					} else if (type == UUID.class) {
						field.set(obj, loadUUIDAttribute(xml, field, name));
					} else if (type.isEnum()) {
						field.set(obj, loadEnumAttribute(xml, field, name));
					} else {
						field.set(obj, loadObjectAttribute(xml, field, name, context));
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
									((boolean[]) arrayObj)[index] = loadBooleanAttribute(xml, field, name);
								} else if (type == byte.class) {
									((byte[]) arrayObj)[index] = loadByteAttribute(xml, field, name);
								} else if (type == char.class) {
									((char[]) arrayObj)[index] = loadCharAttribute(xml, field, name);
								} else if (type == short.class) {
									((short[]) arrayObj)[index] = loadShortAttribute(xml, field, name);
								} else if (type == int.class) {
									((int[]) arrayObj)[index] = loadIntegerAttribute(xml, field, name);
								} else if (type == long.class) {
									((long[]) arrayObj)[index] = loadLongAttribute(xml, field, name);
								} else if (type == float.class) {
									((float[]) arrayObj)[index] = loadFloatAttribute(xml, field, name);
								} else if (type == double.class) {
									((double[]) arrayObj)[index] = loadDoubleAttribute(xml, field, name);
								} else if (type == String.class) {
									((String[]) arrayObj)[index] = loadStringAttribute(xml, field, name);
								} else if (type == UUID.class) {
									((UUID[]) arrayObj)[index] = loadUUIDAttribute(xml, field, name);
								} else if (type.isEnum()) {
									((Object[]) arrayObj)[index] = loadEnumAttribute(xml, field, name);
								} else {
									((Object[]) arrayObj)[index] = loadObjectAttribute(xml, field, name, context);
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
						((Collection) field.get(obj)).add(fieldObj);
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
	 * @param obj The object to save the xml data from.
	 */
	public static void save(File file, Object obj) throws XMLStreamException {
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
	 * @param obj The object to save the xml data from.
	 */
	public static void save(Path path, Object obj) throws XMLStreamException {
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
	public static void save(OutputStream out, Object obj) throws XMLStreamException {
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
			if (obj instanceof TagSaved) {
				((TagSaved) obj).xmlSaved(xml);
			}
		}
	}

	private static boolean hasSubTags(Object obj, Class<?> objClass) throws XMLStreamException {
		for (Field field : Introspection.getFieldsWithAnnotation(objClass, true, XmlTag.class)) {
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
						emitBooleanAttribute(xml, field, name, field.getBoolean(obj));
					} else if (type == byte.class) {
						emitByteAttribute(xml, field, name, field.getByte(obj));
					} else if (type == char.class) {
						emitCharAttribute(xml, field, name, field.getChar(obj));
					} else if (type == short.class) {
						emitShortAttribute(xml, field, name, field.getShort(obj));
					} else if (type == int.class) {
						emitIntegerAttribute(xml, field, name, field.getInt(obj));
					} else if (type == long.class) {
						emitLongAttribute(xml, field, name, field.getLong(obj));
					} else if (type == float.class) {
						emitFloatAttribute(xml, field, name, field.getFloat(obj));
					} else if (type == double.class) {
						emitDoubleAttribute(xml, field, name, field.getDouble(obj));
					} else if (type.isEnum()) {
						emitEnumAttribute(xml, field, name, (Enum<?>) field.get(obj));
					} else {
						emitObjectAttribute(xml, field, name, field.get(obj));
					}
				} catch (Exception exception) {
					exception.printStackTrace();
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
								emitBooleanAttribute(xml, field, name, ((boolean[]) arrayObj)[index]);
							} else if (type == byte.class) {
								emitByteAttribute(xml, field, name, ((byte[]) arrayObj)[index]);
							} else if (type == char.class) {
								emitCharAttribute(xml, field, name, ((char[]) arrayObj)[index]);
							} else if (type == short.class) {
								emitShortAttribute(xml, field, name, ((short[]) arrayObj)[index]);
							} else if (type == int.class) {
								emitIntegerAttribute(xml, field, name, ((int[]) arrayObj)[index]);
							} else if (type == long.class) {
								emitLongAttribute(xml, field, name, ((long[]) arrayObj)[index]);
							} else if (type == float.class) {
								emitFloatAttribute(xml, field, name, ((float[]) arrayObj)[index]);
							} else if (type == double.class) {
								emitDoubleAttribute(xml, field, name, ((double[]) arrayObj)[index]);
							} else if (type.isEnum()) {
								emitEnumAttribute(xml, field, name, ((Enum<?>[]) arrayObj)[index]);
							} else {
								emitObjectAttribute(xml, field, name, ((Object[]) arrayObj)[index]);
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

	private static final void emitBooleanAttribute(XmlGenerator xml, Field field, String name, boolean value) throws XMLStreamException {
		XmlDefaultBoolean def = field.getAnnotation(XmlDefaultBoolean.class);
		if (def != null) {
			xml.addAttributeNot(name, value, def.value());
		} else {
			xml.addAttribute(name, value);
		}
	}

	private static final boolean loadBooleanAttribute(XmlParser xml, Field field, String name) {
		XmlDefaultBoolean def = field.getAnnotation(XmlDefaultBoolean.class);
		return xml.isAttributeSet(name, def != null ? def.value() : false);
	}

	private static final void emitByteAttribute(XmlGenerator xml, Field field, String name, byte value) throws XMLStreamException {
		XmlDefaultByte def = field.getAnnotation(XmlDefaultByte.class);
		if (def != null) {
			xml.addAttributeNot(name, value, def.value());
		} else {
			xml.addAttribute(name, value);
		}
	}

	private static final byte loadByteAttribute(XmlParser xml, Field field, String name) {
		XmlDefaultByte def = field.getAnnotation(XmlDefaultByte.class);
		return (byte) xml.getIntegerAttribute(name, def != null ? def.value() : 0);
	}

	private static final void emitCharAttribute(XmlGenerator xml, Field field, String name, char value) throws XMLStreamException {
		XmlDefaultChar def = field.getAnnotation(XmlDefaultChar.class);
		String strValue = String.valueOf(value);
		if (def != null) {
			xml.addAttributeNot(name, strValue, String.valueOf(def.value()));
		} else {
			xml.addAttribute(name, strValue);
		}
	}

	private static final char loadCharAttribute(XmlParser xml, Field field, String name) {
		XmlDefaultChar def = field.getAnnotation(XmlDefaultChar.class);
		String charStr = xml.getAttribute(name);
		return charStr == null || charStr.isEmpty() ? def != null ? def.value() : 0 : charStr.charAt(0);
	}

	private static final void emitShortAttribute(XmlGenerator xml, Field field, String name, short value) throws XMLStreamException {
		XmlDefaultShort def = field.getAnnotation(XmlDefaultShort.class);
		if (def != null) {
			xml.addAttributeNot(name, value, def.value());
		} else {
			xml.addAttribute(name, value);
		}
	}

	private static final short loadShortAttribute(XmlParser xml, Field field, String name) {
		XmlDefaultShort def = field.getAnnotation(XmlDefaultShort.class);
		return (short) xml.getIntegerAttribute(name, def != null ? def.value() : 0);
	}

	private static final void emitIntegerAttribute(XmlGenerator xml, Field field, String name, int value) throws XMLStreamException {
		XmlDefaultInteger def = field.getAnnotation(XmlDefaultInteger.class);
		if (def != null) {
			xml.addAttributeNot(name, value, def.value());
		} else {
			xml.addAttribute(name, value);
		}
	}

	private static final int loadIntegerAttribute(XmlParser xml, Field field, String name) {
		XmlDefaultInteger def = field.getAnnotation(XmlDefaultInteger.class);
		return xml.getIntegerAttribute(name, def != null ? def.value() : 0);
	}

	private static final void emitLongAttribute(XmlGenerator xml, Field field, String name, long value) throws XMLStreamException {
		XmlDefaultLong def = field.getAnnotation(XmlDefaultLong.class);
		if (def != null) {
			xml.addAttributeNot(name, value, def.value());
		} else {
			xml.addAttribute(name, value);
		}
	}

	private static final long loadLongAttribute(XmlParser xml, Field field, String name) {
		XmlDefaultLong def = field.getAnnotation(XmlDefaultLong.class);
		return xml.getLongAttribute(name, def != null ? def.value() : 0);
	}

	private static final void emitFloatAttribute(XmlGenerator xml, Field field, String name, float value) throws XMLStreamException {
		XmlDefaultFloat def = field.getAnnotation(XmlDefaultFloat.class);
		if (def != null) {
			xml.addAttributeNot(name, value, def.value());
		} else {
			xml.addAttribute(name, value);
		}
	}

	private static final float loadFloatAttribute(XmlParser xml, Field field, String name) {
		XmlDefaultFloat def = field.getAnnotation(XmlDefaultFloat.class);
		return (float) xml.getDoubleAttribute(name, def != null ? def.value() : 0);
	}

	private static final void emitDoubleAttribute(XmlGenerator xml, Field field, String name, double value) throws XMLStreamException {
		XmlDefaultDouble def = field.getAnnotation(XmlDefaultDouble.class);
		if (def != null) {
			xml.addAttributeNot(name, value, def.value());
		} else {
			xml.addAttribute(name, value);
		}
	}

	private static final double loadDoubleAttribute(XmlParser xml, Field field, String name) {
		XmlDefaultDouble def = field.getAnnotation(XmlDefaultDouble.class);
		return xml.getDoubleAttribute(name, def != null ? def.value() : 0);
	}

	private static final void emitEnumAttribute(XmlGenerator xml, Field field, String name, Enum<?> value) throws XMLStreamException {
		if (value != null) {
			String xmlName = getEnumXmlName(value);
			XmlDefault def = field.getAnnotation(XmlDefault.class);
			if (def != null) {
				xml.addAttributeNot(name, xmlName, def.value());
			} else {
				xml.addAttribute(name, xmlName);
			}
		}
	}

	private static final Object loadEnumAttribute(XmlParser xml, Field field, String name) {
		String tag = xml.getAttribute(name);
		Enum<?>[] enumConstants = (Enum<?>[]) field.getType().getEnumConstants();
		for (Enum<?> one : enumConstants) {
			String xmlName = getEnumXmlName(one);
			if (xmlName.equals(tag)) {
				return one;
			}
		}
		XmlDefault def = field.getAnnotation(XmlDefault.class);
		if (def != null) {
			tag = def.value();
			for (Enum<?> one : enumConstants) {
				String xmlName = getEnumXmlName(one);
				if (xmlName.equals(tag)) {
					return one;
				}
			}
		}
		return null;
	}

	private static final String getEnumXmlName(Enum<?> value) {
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

	private static final String loadStringAttribute(XmlParser xml, Field field, String name) {
		XmlDefault def = field.getAnnotation(XmlDefault.class);
		return xml.getAttribute(name, def != null ? def.value() : null);
	}

	private static final UUID loadUUIDAttribute(XmlParser xml, Field field, String name) {
		XmlDefault def = field.getAnnotation(XmlDefault.class);
		String attribute = xml.getAttribute(name, def != null ? def.value() : null);
		return attribute != null && !attribute.isEmpty() ? UUID.fromString(attribute) : null;
	}

	private static final void emitObjectAttribute(XmlGenerator xml, Field field, String name, Object value) throws XMLStreamException {
		if (value != null) {
			String stringValue = value.toString();
			XmlDefault def = field.getAnnotation(XmlDefault.class);
			if (def != null) {
				xml.addAttributeNot(name, stringValue, def.value());
			} else {
				xml.addAttribute(name, stringValue);
			}
		}
	}

	private static final Object loadObjectAttribute(XmlParser xml, Field field, String name, Context context) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		XmlDefault def = field.getAnnotation(XmlDefault.class);
		String attribute = xml.getAttribute(name, def != null ? def.value() : null);
		if (attribute != null && !attribute.isEmpty()) {
			try {
				return field.getType().getConstructor(String.class, Context.class).newInstance(attribute, context);
			} catch (NoSuchMethodException exception) {
				return field.getType().getConstructor(String.class).newInstance(attribute);
			}
		}
		return null;
	}

	private static final void emitSubTags(XmlGenerator xml, Object obj, Class<?> objClass) throws XMLStreamException {
		for (Field field : Introspection.getFieldsWithAnnotation(objClass, true, XmlTag.class)) {
			try {
				Introspection.makeFieldAccessible(field);
				Object content = field.get(obj);
				if (content != null && (!(content instanceof String) || !((String) content).isEmpty())) {
					XmlTag subTag = field.getAnnotation(XmlTag.class);
					if (subTag != null) {
						Class<?> type = field.getType();
						if (Collection.class.isAssignableFrom(type)) {
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
		 * Called to process an XML sub-tag that had no matching fields marked with {@link XmlTag}.
		 * Upon return from this method, the {@link XmlParser} should have been advanced past the
		 * current tag's contents, either by calling {@link XmlParser#skip()} or appropriate parsing
		 * of sub-tags.
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
	 * Objects that wish to be notified when they have been saved should implement this interface.
	 */
	public interface TagSaved {
		/**
		 * Called after the XML tag has been fully written to xml.
		 *
		 * @param xml The {@link XmlGenerator} for this object.
		 */
		void xmlSaved(XmlGenerator xml) throws XMLStreamException;
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
