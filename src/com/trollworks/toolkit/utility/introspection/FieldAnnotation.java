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
import java.lang.reflect.Field;

public final class FieldAnnotation<T extends Annotation> {
    private Field mField;
    private T     mAnnotation;

    public FieldAnnotation(Field field, T annotation) {
        mField = field;
        mAnnotation = annotation;
    }

    public Field getField() {
        return mField;
    }

    public T getAnnotation() {
        return mAnnotation;
    }
}
