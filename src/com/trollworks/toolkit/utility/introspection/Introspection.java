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

package com.trollworks.toolkit.utility.introspection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Helper utilities for introspection. */
public class Introspection {
    private static Map<Class<?>, List<Class<?>>>                     CLASS_TREE_MAP                  = new HashMap<>();
    private static Map<ClassAnnotation<?>, List<FieldAnnotation<?>>> CLASS_FIELD_ANNOTATION_MAP      = new HashMap<>();
    private static Map<ClassAnnotation<?>, List<FieldAnnotation<?>>> CLASS_DEEP_FIELD_ANNOTATION_MAP = new HashMap<>();

    /**
     * Marks the specified field as accessible, even if it would normally be off-limits. Requires
     * either no {@link SecurityManager} or one that allows making this change.
     *
     * @param field The {@link Field} to mark as accessible.
     */
    public static void makeFieldAccessible(Field field) throws SecurityException {
        boolean canAccess = false;
        try {
            canAccess = field.canAccess(field.getDeclaringClass());
        } catch (Exception ex) {
            // ignore
        }
        if (!canAccess) {
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
     * Marks the specified constructor as accessible, even if it would normally be off-limits.
     * Requires either no {@link SecurityManager} or one that allows making this change.
     *
     * @param constructor The {@link Constructor} to mark as accessible.
     */
    public static void makeConstructorAccessible(Constructor<?> constructor) {
        boolean canAccess = false;
        try {
            canAccess = constructor.canAccess(constructor.getDeclaringClass());
        } catch (Exception ex) {
            // ignore
        }
        if (!canAccess) {
            if (System.getSecurityManager() == null) {
                constructor.setAccessible(true);
            } else {
                AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                    constructor.setAccessible(true);
                    return null;
                });
            }
        }
    }

    public static List<Class<?>> getClassTree(Class<?> cls) {
        synchronized (CLASS_TREE_MAP) {
            List<Class<?>> classes = CLASS_TREE_MAP.get(cls);
            if (classes == null) {
                Set<Class<?>> set = new LinkedHashSet<>();
                collectClassTree(cls, set);
                classes = new ArrayList<>(set);
                CLASS_TREE_MAP.put(cls, classes);
            }
            return classes;
        }
    }

    private static void collectClassTree(Class<?> cls, Set<Class<?>> set) {
        if (cls != null && !Object.class.equals(cls) && !set.contains(cls)) {
            set.add(cls);
            collectClassTree(cls.getSuperclass(), set);
            for (Class<?> intf : cls.getInterfaces()) {
                collectClassTree(intf, set);
            }
        }
    }

    public static final boolean hasDeepFieldAnnotation(Class<?> cls, Class<? extends Annotation> annotationCls) {
        return !getDeepFieldAnnotations(cls, annotationCls).isEmpty();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> List<FieldAnnotation<T>> getDeepFieldAnnotations(Class<?> cls, Class<T> annotationCls) {
        synchronized (CLASS_FIELD_ANNOTATION_MAP) {
            ClassAnnotation<T>       ca          = new ClassAnnotation<>(cls, annotationCls);
            List<FieldAnnotation<?>> annotations = CLASS_DEEP_FIELD_ANNOTATION_MAP.get(ca);
            if (annotations == null) {
                annotations = new ArrayList<>();
                for (Class<?> one : getClassTree(cls)) {
                    annotations.addAll(getFieldAnnotations(one, annotationCls));
                }
                CLASS_DEEP_FIELD_ANNOTATION_MAP.put(ca, annotations);
            }
            return (List<FieldAnnotation<T>>) (List<?>) annotations;
        }
    }

    public static final boolean hasFieldAnnotation(Class<?> cls, Class<? extends Annotation> annotationCls) {
        return !getFieldAnnotations(cls, annotationCls).isEmpty();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> List<FieldAnnotation<T>> getFieldAnnotations(Class<?> cls, Class<T> annotationCls) {
        synchronized (CLASS_FIELD_ANNOTATION_MAP) {
            ClassAnnotation<T>       ca          = new ClassAnnotation<>(cls, annotationCls);
            List<FieldAnnotation<?>> annotations = CLASS_FIELD_ANNOTATION_MAP.get(ca);
            if (annotations == null) {
                annotations = new ArrayList<>();
                for (Field field : cls.getDeclaredFields()) {
                    T annotation = field.getAnnotation(annotationCls);
                    if (annotation != null) {
                        annotations.add(new FieldAnnotation<>(field, annotation));
                    }
                }
                CLASS_FIELD_ANNOTATION_MAP.put(ca, annotations);
            }
            return (List<FieldAnnotation<T>>) (List<?>) annotations;
        }
    }
}
