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

public final class ClassAnnotation<T extends Annotation> {
    private Class<?> mClass;
    private Class<T> mAnnotationClass;

    public ClassAnnotation(Class<?> cls, Class<T> annotationCls) {
        mClass = cls;
        mAnnotationClass = annotationCls;
    }

    public Class<?> getClassWithAnnotation() {
        return mClass;
    }

    public Class<T> getAnnotationClass() {
        return mAnnotationClass;
    }

    @Override
    public int hashCode() {
        return 31 * (31 + mAnnotationClass.hashCode()) + mClass.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ClassAnnotation) {
            ClassAnnotation<?> other = (ClassAnnotation<?>) obj;
            return mClass == other.mClass && mAnnotationClass == other.mAnnotationClass;
        }
        return false;
    }
}
