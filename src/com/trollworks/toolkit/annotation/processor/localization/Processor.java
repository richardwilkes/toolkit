/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.annotation.processor.localization;

import com.trollworks.toolkit.annotation.Localize;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

@SupportedAnnotationTypes("com.trollworks.toolkit.annotation.Localize")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class Processor extends AbstractProcessor {
	private static final String	LOCALIZE_NAME	= Localize.class.getCanonicalName();

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
		Map<String, MessageBundle> bundles = new HashMap<>();
		for (Element element : env.getElementsAnnotatedWith(Localize.class)) {
			Name name = element.getSimpleName();
			if (element.getKind().isField()) {
				MessageBundle bundle = new MessageBundle(element, processingEnv);
				String fullName = bundle.getFullName();
				MessageBundle existing = bundles.get(fullName);
				if (existing == null) {
					bundles.put(fullName, bundle);
				} else {
					bundle = existing;
				}
				bundle.addField(element);
			} else {
				logError(element, name + " is not a field or enum"); //$NON-NLS-1$
			}
		}
		for (MessageBundle bundle : bundles.values()) {
			bundle.createPropertiesFile();
		}
		return true;
	}

	private String logError(Element element, String msg) {
		for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
			if (mirror.getAnnotationType().toString().equals(LOCALIZE_NAME)) {
				processingEnv.getMessager().printMessage(Kind.ERROR, msg, element, mirror);
				return msg;
			}
		}
		processingEnv.getMessager().printMessage(Kind.ERROR, msg, element);
		return msg;
	}
}
