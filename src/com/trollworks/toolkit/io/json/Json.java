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

package com.trollworks.toolkit.io.json;

import com.trollworks.toolkit.annotation.JsonKey;
import com.trollworks.toolkit.io.Log;
import com.trollworks.toolkit.io.UrlUtils;
import com.trollworks.toolkit.utility.Geometry;
import com.trollworks.toolkit.utility.introspection.FieldAnnotation;
import com.trollworks.toolkit.utility.introspection.Introspection;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Json utilities. */
public class Json {
    private Reader  mReader;
    private int     mIndex;
    private int     mCharacter = 1;
    private int     mLine      = 1;
    private char    mPrevious;
    private boolean mEOF;
    private boolean mUsePrevious;

    /**
     * Load the contents of a JSON string into an object that has been marked with {@link JsonKey}
     * annotations.
     *
     * @param obj  The object to load data into.
     * @param json The JSON-formatted string.
     * @return The object that was passed in.
     */
    public static final <T> T load(T obj, String json) throws IOException {
        if (Introspection.hasDeepFieldAnnotation(obj.getClass(), JsonKey.class)) {
            return load(obj, asMap(parse(json), false));
        }
        return obj;
    }

    /**
     * Load the contents of a JSON map into an object that has been marked with {@link JsonKey}
     * annotations.
     *
     * @param obj The object to load data into.
     * @param map The {@link JsonMap}.
     * @return The object that was passed in.
     */
    public static final <T> T load(T obj, JsonMap map) {
        for (FieldAnnotation<JsonKey> fa : Introspection.getDeepFieldAnnotations(obj.getClass(), JsonKey.class)) {
            loadFieldFromMap(fa.getField(), fa.getAnnotation().value(), obj, map);
        }
        return obj;
    }

    private static void loadFieldFromMap(Field field, String name, Object obj, JsonMap map) {
        Introspection.makeFieldAccessible(field);
        Class<?> fieldType = field.getType();
        try {
            if (fieldType == boolean.class || fieldType == Boolean.class) {
                field.setBoolean(obj, map.getBoolean(name));
            } else if (fieldType == byte.class || fieldType == Byte.class) {
                field.setByte(obj, map.getByte(name));
            } else if (fieldType == char.class || fieldType == Character.class) {
                field.setChar(obj, map.getChar(name));
            } else if (fieldType == int.class || fieldType == Integer.class) {
                field.setInt(obj, map.getInt(name));
            } else if (fieldType == long.class || fieldType == Long.class) {
                field.setLong(obj, map.getLong(name));
            } else if (fieldType == float.class || fieldType == Float.class) {
                field.setFloat(obj, map.getFloat(name));
            } else if (fieldType == double.class || fieldType == Double.class) {
                field.setDouble(obj, map.getDouble(name));
            } else if (fieldType == String.class) {
                field.set(obj, map.getString(name, true));
            } else if (fieldType.isEnum()) {
                field.set(obj, extractEnum(map.getString(name, false), (Enum<?>[]) fieldType.getEnumConstants()));
            } else if (fieldType.isArray()) {
                field.set(obj, createArray(field, map.getArray(name, true)));
            } else if (List.class.isAssignableFrom(fieldType)) {
                field.set(obj, createList(field, map.getArray(name, true)));
            } else if (Map.class.isAssignableFrom(fieldType)) {
                field.set(obj, createMap(field, map.getMap(name, true)));
            } else if (Introspection.hasDeepFieldAnnotation(fieldType, JsonKey.class)) {
                try {
                    JsonMap objMap = map.getMap(name, true);
                    if (objMap != null) {
                        Constructor<?> constructor = fieldType.getDeclaredConstructor();
                        Introspection.makeConstructorAccessible(constructor);
                        field.set(obj, load(constructor.newInstance(), objMap));
                    } else {
                        field.set(obj, null);
                    }
                } catch (Exception exception) {
                    Log.error(exception);
                }
            } else {
                Log.error("Unable to restore " + fieldType.getName() + " for key " + name);
            }
        } catch (Exception exception) {
            Log.error(exception);
        }
    }

    /** Does not support creating Arrays containing Arrays, Lists or Maps. */
    private static Object createArray(Field field, JsonArray array) {
        if (array == null) {
            return null;
        }
        int      length    = array.size();
        Class<?> fieldType = field.getType().getComponentType();
        Object   data      = Array.newInstance(fieldType, length);
        if (fieldType == boolean.class || fieldType == Boolean.class) {
            for (int i = 0; i < length; i++) {
                Array.setBoolean(data, i, array.getBoolean(i));
            }
        } else if (fieldType == byte.class || fieldType == Byte.class) {
            for (int i = 0; i < length; i++) {
                Array.setByte(data, i, array.getByte(i));
            }
        } else if (fieldType == char.class || fieldType == Character.class) {
            for (int i = 0; i < length; i++) {
                Array.setChar(data, i, array.getChar(i));
            }
        } else if (fieldType == short.class || fieldType == Short.class) {
            for (int i = 0; i < length; i++) {
                Array.setShort(data, i, array.getShort(i));
            }
        } else if (fieldType == int.class || fieldType == Integer.class) {
            for (int i = 0; i < length; i++) {
                Array.setInt(data, i, array.getInt(i));
            }
        } else if (fieldType == long.class || fieldType == Long.class) {
            for (int i = 0; i < length; i++) {
                Array.setLong(data, i, array.getLong(i));
            }
        } else if (fieldType == float.class || fieldType == Float.class) {
            for (int i = 0; i < length; i++) {
                Array.setFloat(data, i, array.getFloat(i));
            }
        } else if (fieldType == double.class || fieldType == Double.class) {
            for (int i = 0; i < length; i++) {
                Array.setDouble(data, i, array.getDouble(i));
            }
        } else if (fieldType == String.class) {
            for (int i = 0; i < length; i++) {
                Array.set(data, i, array.getString(i, true));
            }
        } else if (fieldType.isEnum()) {
            Enum<?>[] constants = (Enum<?>[]) fieldType.getEnumConstants();
            for (int i = 0; i < length; i++) {
                Array.set(data, i, extractEnum(array.getString(i, false), constants));
            }
        } else if (Introspection.hasDeepFieldAnnotation(fieldType, JsonKey.class)) {
            for (int i = 0; i < length; i++) {
                try {
                    JsonMap objMap = array.getMap(i, true);
                    if (objMap != null) {
                        Constructor<?> constructor = fieldType.getDeclaredConstructor();
                        Introspection.makeConstructorAccessible(constructor);
                        Array.set(data, i, load(constructor.newInstance(), objMap));
                    } else {
                        Array.set(data, i, null);
                    }
                } catch (Exception exception) {
                    Log.error(exception);
                }
            }
        } else {
            Log.error("Unable to restore " + fieldType.getName() + " for key " + field.getName());
        }
        return data;
    }

    /** Does not support creating Lists containing Arrays, Lists or Maps. */
    private static List<?> createList(Field field, JsonArray array) {
        if (array != null) {
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                try {
                    ParameterizedType pType = (ParameterizedType) genericType;
                    Type[]            args  = pType.getActualTypeArguments();
                    if (args.length == 1) {
                        Class<?>     type   = Class.forName(args[0].getTypeName());
                        int          length = array.size();
                        List<Object> result = new ArrayList<>(length);
                        for (int i = 0; i < length; i++) {
                            result.add(createObject(type, array.get(i)));
                        }
                        return result;
                    }
                    Log.error("Must have one type argument for a list");
                } catch (ClassNotFoundException exception) {
                    Log.error(exception);
                }
            } else {
                Log.error("Unable to determine generic type");
            }
        }
        return null;
    }

    /** Does not support creating Maps containing Arrays, Lists or Maps. */
    private static Map<String, ?> createMap(Field field, JsonMap map) {
        if (map != null) {
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                try {
                    ParameterizedType pType = (ParameterizedType) genericType;
                    Type[]            args  = pType.getActualTypeArguments();
                    if (args.length == 2) {
                        Class<?> keyClass = Class.forName(args[0].getTypeName());
                        if (keyClass == String.class) {
                            Class<?>            type   = Class.forName(args[1].getTypeName());
                            Map<String, Object> result = new HashMap<>();
                            for (String key : map.keySet()) {
                                result.put(key, createObject(type, map.get(key)));
                            }
                            return result;
                        }
                        Log.error("Only maps with Strings for their keys are permitted");
                    } else {
                        Log.error("Must have two type arguments for a map");
                    }
                } catch (ClassNotFoundException exception) {
                    Log.error(exception);
                }
            } else {
                Log.error("Unable to determine generic type");
            }
        }
        return null;
    }

    /** Does not support creating Arrays, Lists or Maps. */
    private static Object createObject(Class<?> type, Object jsonData) {
        if (type == boolean.class || type == Boolean.class) {
            return asBooleanObject(jsonData);
        }
        if (type == byte.class || type == Byte.class) {
            return asByteObject(jsonData);
        }
        if (type == char.class || type == Character.class) {
            return asCharObject(jsonData);
        }
        if (type == int.class || type == Integer.class) {
            return asIntObject(jsonData);
        }
        if (type == long.class || type == Long.class) {
            return asLongObject(jsonData);
        }
        if (type == float.class || type == Float.class) {
            return asFloatObject(jsonData);
        }
        if (type == double.class || type == Double.class) {
            return asDoubleObject(jsonData);
        }
        if (type == String.class) {
            return asString(jsonData, true);
        }
        if (type.isEnum()) {
            return extractEnum(asString(jsonData, false), (Enum<?>[]) type.getEnumConstants());
        }
        if (Introspection.hasDeepFieldAnnotation(type, JsonKey.class)) {
            try {
                JsonMap objMap = asMap(jsonData, true);
                if (objMap != null) {
                    Constructor<?> constructor = type.getDeclaredConstructor();
                    Introspection.makeConstructorAccessible(constructor);
                    return load(constructor.newInstance(), objMap);
                }
                return null;
            } catch (Exception exception) {
                Log.error(exception);
            }
        }
        Log.error("Unable to create type: " + type.getSimpleName());
        return null;
    }

    private static Enum<?> extractEnum(String value, Enum<?>[] constants) {
        for (Enum<?> one : constants) {
            if (one.toString().equals(value)) {
                return one;
            }
        }
        return null;
    }

    /**
     * @param reader A {@link Reader} to load JSON data from.
     * @return The result of loading the data.
     */
    public static final Object parse(Reader reader) throws IOException {
        return new Json(reader).nextValue();
    }

    /**
     * @param url A {@link URL} to load JSON data from.
     * @return The result of loading the data.
     */
    public static final Object parse(URL url) throws IOException {
        try (InputStream in = UrlUtils.setupConnection(url).getInputStream()) {
            return parse(in);
        }
    }

    /**
     * @param stream An {@link InputStream} to load JSON data from. {@link StandardCharsets#UTF_8}
     *               will be used as the encoding when reading from the stream.
     * @return The result of loading the data.
     */
    public static final Object parse(InputStream stream) throws IOException {
        return parse(stream, StandardCharsets.UTF_8);
    }

    /**
     * @param stream   An {@link InputStream} to load JSON data from.
     * @param encoding The character encoding to use when reading from the stream.
     * @return The result of loading the data.
     */
    public static final Object parse(InputStream stream, Charset encoding) throws IOException {
        return parse(new InputStreamReader(stream, encoding));
    }

    /**
     * @param string A {@link String} to load JSON data from.
     * @return The result of loading the data.
     */
    public static final Object parse(String string) throws IOException {
        return parse(new StringReader(string));
    }

    /**
     * @param obj An object to process.
     * @return The value associated with the object or {@code false} if the object is {@code null}
     *         or the value cannot be converted to a boolean.
     */
    public static final boolean asBoolean(Object obj) {
        return Boolean.TRUE.equals(obj) || obj instanceof String && Boolean.TRUE.toString().equalsIgnoreCase((String) obj);
    }

    /**
     * @param obj An object to process.
     * @return The value associated with the object or {@code false} if the object is {@code null}
     *         or the value cannot be converted to a {@link Boolean}.
     */
    public static final Boolean asBooleanObject(Object obj) {
        return asBoolean(obj) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * @param obj An object to process.
     * @return The value associated with the object or {@code 0} if the object is {@code null} or
     *         the value cannot be converted to a byte.
     */
    public static final byte asByte(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).byteValue();
        }
        if (obj instanceof String) {
            try {
                return Byte.parseByte((String) obj);
            } catch (Exception exception) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * @param obj An object to process.
     * @return The value associated with the object or {@code 0} if the object is {@code null} or
     *         the value cannot be converted to a {@link Byte}.
     */
    public static final Byte asByteObject(Object obj) {
        return Byte.valueOf(asByte(obj));
    }

    /**
     * @param obj An object to process.
     * @return The value associated with the object or {@code 0} if the object is {@code null} or
     *         the value cannot be converted to a char.
     */
    public static final char asChar(Object obj) {
        if (obj instanceof Number) {
            return (char) ((Number) obj).intValue();
        }
        if (obj instanceof String) {
            String str = (String) obj;
            if (!str.isEmpty()) {
                return str.charAt(0);
            }
        }
        return 0;
    }

    /**
     * @param obj An object to process.
     * @return The value associated with the object or {@code 0} if the object is {@code null} or
     *         the value cannot be converted to a {@link Character}.
     */
    public static final Character asCharObject(Object obj) {
        return Character.valueOf(asChar(obj));
    }

    /**
     * @param obj An object to process.
     * @return The value associated with the object or {@code 0} if the object is {@code null} or
     *         the value cannot be converted to a short.
     */
    public static final short asShort(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).shortValue();
        }
        if (obj instanceof String) {
            try {
                return Short.parseShort((String) obj);
            } catch (Exception exception) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * @param obj An object to process.
     * @return The value associated with the object or {@code 0} if the object is {@code null} or
     *         the value cannot be converted to a {@link Short}.
     */
    public static final Short asShortObject(Object obj) {
        return Short.valueOf(asShort(obj));
    }

    /**
     * @param obj An object to process.
     * @return The value associated with the object or {@code 0} if the object is {@code null} or
     *         the value cannot be converted to an int.
     */
    public static final int asInt(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (Exception exception) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * @param obj An object to process.
     * @return The value associated with the object or {@code 0} if the object is {@code null} or
     *         the value cannot be converted to an {@link Integer}.
     */
    public static final Integer asIntObject(Object obj) {
        return Integer.valueOf(asInt(obj));
    }

    /**
     * @param obj An object to process.
     * @return The value associated with the object or {@code 0} if the object is {@code null} or
     *         the value cannot be converted to a long.
     */
    public static final long asLong(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (Exception exception) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * @param obj An object to process.
     * @return The value associated with the object or {@code 0} if the object is {@code null} or
     *         the value cannot be converted to a {@link Long}.
     */
    public static final Long asLongObject(Object obj) {
        return Long.valueOf(asLong(obj));
    }

    /**
     * @param obj An object to process.
     * @return The value associated with the object or {@code 0} if the object is {@code null} or
     *         the value cannot be converted to a float.
     */
    public static final float asFloat(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).floatValue();
        }
        if (obj instanceof String) {
            try {
                return Float.parseFloat((String) obj);
            } catch (Exception exception) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * @param obj An object to process.
     * @return The value associated with the object or {@code 0} if the object is {@code null} or
     *         the value cannot be converted to a {@link Float}.
     */
    public static final Float asFloatObject(Object obj) {
        return Float.valueOf(asFloat(obj));
    }

    /**
     * @param obj An object to process.
     * @return The value associated with the object or {@code 0} if the object is {@code null} or
     *         the value cannot be converted to a double.
     */
    public static final double asDouble(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (Exception exception) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * @param obj An object to process.
     * @return The value associated with the object or {@code 0} if the object is {@code null} or
     *         the value cannot be converted to a {@link Double}.
     */
    public static final Double asDoubleObject(Object obj) {
        return Double.valueOf(asDouble(obj));
    }

    /**
     * @param obj       An object to process.
     * @param allowNull {@code false} to return an empty string if the result would be {@code
     *                  null}.
     * @return The value associated with the object.
     */
    public static final String asString(Object obj, boolean allowNull) {
        if (JsonNull.INSTANCE.equals(obj)) {
            return allowNull ? null : "";
        }
        return obj.toString();
    }

    /**
     * @param obj       An object to process.
     * @param allowNull {@code false} to return an empty array if the object is {@code null} or the
     *                  value is not a {@link JsonArray}.
     * @return The {@link JsonArray}.
     */
    public static final JsonArray asArray(Object obj, boolean allowNull) {
        if (obj instanceof JsonArray) {
            return (JsonArray) obj;
        }
        return allowNull ? null : new JsonArray();
    }

    /**
     * @param obj       An object to process.
     * @param allowNull {@code false} to return an empty map if the object is {@code null} or the
     *                  value is not a {@link JsonMap}.
     * @return The {@link JsonMap}.
     */
    public static final JsonMap asMap(Object obj, boolean allowNull) {
        if (obj instanceof JsonMap) {
            return (JsonMap) obj;
        }
        return allowNull ? null : new JsonMap();
    }

    /**
     * @param obj       An object to process.
     * @param allowNull {@code false} to return an empty point if the object is {@code null} or the
     *                  value cannot be converted to a {@link Point}.
     * @return The value associated with the object.
     */
    public static final Point asPoint(Object obj, boolean allowNull) {
        if (obj instanceof Point) {
            return (Point) obj;
        }
        if (!JsonNull.INSTANCE.equals(obj)) {
            try {
                return Geometry.toPoint(obj.toString());
            } catch (Exception exception) {
                // Fall through
            }
        }
        return allowNull ? null : new Point();
    }

    /**
     * @param obj       An object to process.
     * @param allowNull {@code false} to return an empty point if the object is {@code null} or the
     *                  value cannot be converted to a {@link Rectangle}.
     * @return The value associated with the object.
     */
    public static final Rectangle asRectangle(Object obj, boolean allowNull) {
        if (obj instanceof Rectangle) {
            return (Rectangle) obj;
        }
        if (!JsonNull.INSTANCE.equals(obj)) {
            try {
                return Geometry.toRectangle(obj.toString());
            } catch (Exception exception) {
                // Fall through
            }
        }
        return allowNull ? null : new Rectangle();
    }

    /**
     * @param value The value to encode as a JSON string.
     * @return The encoded {@link String}.
     */
    public static final String toString(Object value) {
        if (JsonNull.INSTANCE.equals(value)) {
            return JsonNull.INSTANCE.toString();
        }
        if (value instanceof Number) {
            String str = value.toString();
            if (str.indexOf('.') > 0 && str.indexOf('e') < 0 && str.indexOf('E') < 0) {
                while (str.endsWith("0")) {
                    str = str.substring(0, str.length() - 1);
                }
                if (str.endsWith(".")) {
                    str = str.substring(0, str.length() - 1);
                }
            }
            return str;
        }
        if (value instanceof Boolean || value instanceof JsonCollection) {
            return value.toString();
        }
        if (value instanceof Map || value instanceof List || value.getClass().isArray()) {
            return wrap(value).toString();
        }
        if (value instanceof Point) {
            return quote(Geometry.toString((Point) value));
        }
        if (value instanceof Rectangle) {
            return quote(Geometry.toString((Rectangle) value));
        }
        return quote(value.toString());
    }

    /**
     * @param object The object to wrap for storage inside a {@link JsonCollection}.
     * @return The wrapped version of the object, which may be the original object passed in.
     */
    public static final Object wrap(Object object) {
        if (JsonNull.INSTANCE.equals(object)) {
            return JsonNull.INSTANCE;
        }
        if (object instanceof JsonCollection || object instanceof Boolean || object instanceof Byte || object instanceof Character || object instanceof Short || object instanceof Integer || object instanceof Long || object instanceof Float || object instanceof Double || object instanceof String) {
            return object;
        }
        if (object instanceof List) {
            JsonArray array = new JsonArray();
            for (Object one : (List<?>) object) {
                array.put(wrap(one));
            }
            return array;
        }
        Class<?> type = object.getClass();
        if (type.isArray()) {
            JsonArray array = new JsonArray();
            if (object instanceof boolean[]) {
                for (boolean value : (boolean[]) object) {
                    array.put(value);
                }
            } else if (object instanceof byte[]) {
                for (byte value : (byte[]) object) {
                    array.put(value);
                }
            } else if (object instanceof char[]) {
                for (char value : (char[]) object) {
                    array.put(value);
                }
            } else if (object instanceof short[]) {
                for (short value : (short[]) object) {
                    array.put(value);
                }
            } else if (object instanceof int[]) {
                for (int value : (int[]) object) {
                    array.put(value);
                }
            } else if (object instanceof long[]) {
                for (long value : (long[]) object) {
                    array.put(value);
                }
            } else if (object instanceof float[]) {
                for (float value : (float[]) object) {
                    array.put(value);
                }
            } else if (object instanceof double[]) {
                for (double value : (double[]) object) {
                    array.put(value);
                }
            } else {
                for (Object obj : (Object[]) object) {
                    array.put(wrap(obj));
                }
            }
            return array;
        }
        if (object instanceof Map) {
            JsonMap map = new JsonMap();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                map.put(entry.getKey().toString(), entry.getValue());
            }
            return map;
        }
        if (object instanceof Point) {
            return Geometry.toString((Point) object);
        }
        if (object instanceof Rectangle) {
            return Geometry.toString((Rectangle) object);
        }
        if (Introspection.hasDeepFieldAnnotation(type, JsonKey.class)) {
            JsonMap map = new JsonMap();
            for (FieldAnnotation<JsonKey> fa : Introspection.getDeepFieldAnnotations(type, JsonKey.class)) {
                Field field = fa.getField();
                Introspection.makeFieldAccessible(field);
                try {
                    map.put(fa.getAnnotation().value(), field.get(object));
                } catch (IllegalAccessException exception) {
                    Log.error(exception);
                }
            }
            return map;
        }
        return object.toString();
    }

    /**
     * @param string The string to quote.
     * @return The quoted {@link String}, suitable for storage inside a JSON object.
     */
    public static final String quote(String string) {
        int len;
        if (string == null || (len = string.length()) == 0) {
            return "\"\"";
        }
        StringBuilder buffer = new StringBuilder(len + 4);
        buffer.append('"');
        char ch = 0;
        for (int i = 0; i < len; i++) {
            char last = ch;
            ch = string.charAt(i);
            switch (ch) {
            case '\\':
            case '"':
                buffer.append('\\');
                buffer.append(ch);
                break;
            case '/':
                if (last == '<') {
                    buffer.append('\\');
                }
                buffer.append(ch);
                break;
            case '\b':
                buffer.append("\\b");
                break;
            case '\t':
                buffer.append("\\t");
                break;
            case '\n':
                buffer.append("\\n");
                break;
            case '\f':
                buffer.append("\\f");
                break;
            case '\r':
                buffer.append("\\r");
                break;
            default:
                if (ch < ' ' || ch >= '\u0080' && ch < '\u00a0' || ch >= '\u2000' && ch < '\u2100') {
                    String hex = "000" + Integer.toHexString(ch);
                    buffer.append("\\u").append(hex.substring(hex.length() - 4));
                } else {
                    buffer.append(ch);
                }
                break;
            }
        }
        buffer.append('"');
        return buffer.toString();
    }

    private Json(Reader reader) {
        mReader = reader;
    }

    private char next() throws IOException {
        int c;
        if (mUsePrevious) {
            mUsePrevious = false;
            c = mPrevious;
        } else {
            c = mReader.read();
            if (c <= 0) { // End of stream
                mEOF = true;
                c = 0;
            }
        }
        mIndex++;
        if (mPrevious == '\r') {
            mLine++;
            mCharacter = c == '\n' ? 0 : 1;
        } else if (c == '\n') {
            mLine++;
            mCharacter = 0;
        } else {
            mCharacter++;
        }
        mPrevious = (char) c;
        return mPrevious;
    }

    private char nextSkippingWhitespace() throws IOException {
        for (; ; ) {
            char c = next();
            if (c == 0 || c > ' ') {
                return c;
            }
        }
    }

    private Object nextValue() throws IOException {
        char   c = nextSkippingWhitespace();
        String s;

        switch (c) {
        case '"':
        case '\'':
            return nextString(c);
        case '{':
            back();
            return nextMap();
        case '[':
        case '(':
            back();
            return nextArray();
        default:
            break;
        }

        StringBuilder sb = new StringBuilder();
        while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
            sb.append(c);
            c = next();
        }
        back();

        s = sb.toString().trim();
        if (s.isEmpty()) {
            throw syntaxError("Missing value");
        }
        if ("true".equalsIgnoreCase(s)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(s)) {
            return Boolean.FALSE;
        }
        if ("null".equalsIgnoreCase(s)) {
            return JsonNull.INSTANCE;
        }

        char b = s.charAt(0);
        if (b >= '0' && b <= '9' || b == '.' || b == '-' || b == '+') {
            if (b == '0' && s.length() > 2 && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
                try {
                    return Integer.valueOf(Integer.parseInt(s.substring(2), 16));
                } catch (Exception ignore) {
                    Log.error(ignore);
                }
            }
            try {
                if (s.indexOf('.') > -1 || s.indexOf('e') > -1 || s.indexOf('E') > -1) {
                    return Double.valueOf(s);
                }
                Long myLong = Long.valueOf(s);
                if (myLong.longValue() == myLong.intValue()) {
                    return Integer.valueOf(myLong.intValue());
                }
                return myLong;
            } catch (Exception ignore) {
                Log.error(ignore);
            }
        }
        return s;
    }

    private JsonArray nextArray() throws IOException {
        char c = nextSkippingWhitespace();
        char q;
        if (c == '[') {
            q = ']';
        } else if (c == '(') {
            q = ')';
        } else {
            throw syntaxError("A JSONArray text must start with '['");
        }
        JsonArray array = new JsonArray();
        if (nextSkippingWhitespace() == ']') {
            return array;
        }
        back();
        for (; ; ) {
            if (nextSkippingWhitespace() == ',') {
                back();
                array.put(null);
            } else {
                back();
                array.put(nextValue());
            }
            c = nextSkippingWhitespace();
            switch (c) {
            case ';':
            case ',':
                if (nextSkippingWhitespace() == ']') {
                    return array;
                }
                back();
                break;
            case ']':
            case ')':
                if (q != c) {
                    throw syntaxError("Expected a '" + Character.toString(q) + "'");
                }
                return array;
            default:
                throw syntaxError("Expected a ',' or ']'");
            }
        }
    }

    private JsonMap nextMap() throws IOException {
        char   c;
        String key;

        if (nextSkippingWhitespace() != '{') {
            throw syntaxError("JSON object text must begin with '{'");
        }
        JsonMap map = new JsonMap();
        while (true) {
            c = nextSkippingWhitespace();
            switch (c) {
            case 0:
                throw syntaxError("JSON object text must end with '}'");
            case '}':
                return map;
            default:
                back();
                key = nextValue().toString();
            }

            c = nextSkippingWhitespace();
            if (c == '=') {
                if (next() != '>') {
                    back();
                }
            } else if (c != ':') {
                throw syntaxError("Expected a ':' after a key");
            }
            if (map.has(key)) {
                throw new IOException("Duplicate key \"" + key + "\"");
            }
            map.put(key, nextValue());

            switch (nextSkippingWhitespace()) {
            case ';':
            case ',':
                if (nextSkippingWhitespace() == '}') {
                    return map;
                }
                back();
                break;
            case '}':
                return map;
            default:
                throw syntaxError("Expected a ',' or '}'");
            }
        }
    }

    private String nextString(char quote) throws IOException {
        char          c;
        StringBuilder buffer = new StringBuilder();
        for (; ; ) {
            c = next();
            switch (c) {
            case 0:
            case '\n':
            case '\r':
                throw syntaxError("Unterminated string");
            case '\\':
                c = next();
                switch (c) {
                case 'b':
                    buffer.append('\b');
                    break;
                case 't':
                    buffer.append('\t');
                    break;
                case 'n':
                    buffer.append('\n');
                    break;
                case 'f':
                    buffer.append('\f');
                    break;
                case 'r':
                    buffer.append('\r');
                    break;
                case 'u':
                    buffer.append((char) Integer.parseInt(next(4), 16));
                    break;
                case '"':
                case '\'':
                case '\\':
                case '/':
                    buffer.append(c);
                    break;
                default:
                    throw syntaxError("Illegal escape.");
                }
                break;
            default:
                if (c == quote) {
                    return buffer.toString();
                }
                buffer.append(c);
            }
        }
    }

    private void back() {
        if (mUsePrevious || mIndex <= 0) {
            throw new IllegalStateException("Stepping back two steps is not supported");
        }
        mIndex--;
        mCharacter--;
        mUsePrevious = true;
        mEOF = false;
    }

    private String next(int n) throws IOException {
        if (n == 0) {
            return "";
        }

        char[] buffer = new char[n];
        int    pos    = 0;

        while (pos < n) {
            buffer[pos] = next();
            if (mEOF && !mUsePrevious) {
                throw syntaxError("Substring bounds error");
            }
            pos++;
        }
        return new String(buffer);
    }

    private IOException syntaxError(String message) {
        return new IOException(message + toString());
    }

    @Override
    public String toString() {
        return " at " + mIndex + " [character " + mCharacter + " line " + mLine + "]";
    }
}
