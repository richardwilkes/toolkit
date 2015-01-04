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

import com.trollworks.toolkit.collections.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Helper utilities for introspection. */
public class Introspection {
	private static final Map<Pair<Class<?>, Class<? extends Annotation>>, Field[]>	FIELD_MAP	= new HashMap<>();

	/**
	 * Marks the specified field as accessible, even if it would normally be off-limits. Requires
	 * either no {@link SecurityManager} or one that allows making this change.
	 *
	 * @param field The {@link Field} to mark as accessible.
	 */
	public static void makeFieldAccessible(final Field field) throws SecurityException {
		if (!field.isAccessible()) {
			if (System.getSecurityManager() == null) {
				field.setAccessible(true);
			} else {
				AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
					field.setAccessible(true);
					return null;
				});
			}
		}
	}

	/**
	 * @param objClass The {@link Class} to retrieve {@link Field}s from.
	 * @param annotationClass The {@link Annotation} class to look for.
	 * @param cache Whether the result should be cached.
	 * @return The {@link Field}s in the specified {@link Class} and its super-{@link Class}es which
	 *         are marked with the specified {@link Annotation}.
	 */
	public static synchronized Field[] getFieldsWithAnnotation(Class<?> objClass, Class<? extends Annotation> annotationClass, boolean cache) {
		Pair<Class<?>, Class<? extends Annotation>> key = new Pair<>(objClass, annotationClass);
		Field[] fields = FIELD_MAP.get(key);
		if (fields == null) {
			List<Field> fieldList = new ArrayList<>();
			getFieldsWithAnnotation(objClass, annotationClass, fieldList);
			fields = fieldList.toArray(new Field[fieldList.size()]);
			if (cache) {
				FIELD_MAP.put(key, fields);
			}
		}
		return fields;
	}

	private static void getFieldsWithAnnotation(Class<?> objClass, Class<? extends Annotation> annotationClass, List<Field> fields) {
		Class<?> superClass = objClass.getSuperclass();
		if (superClass != null) {
			getFieldsWithAnnotation(superClass, annotationClass, fields);
		}
		for (Field field : objClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(annotationClass)) {
				fields.add(field);
			}
		}
	}
}
