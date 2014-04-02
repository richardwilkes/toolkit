/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is com.trollworks.toolkit.
 *
 * The Initial Developer of the Original Code is Richard A. Wilkes.
 * Portions created by the Initial Developer are Copyright (C) 2014,
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */

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
