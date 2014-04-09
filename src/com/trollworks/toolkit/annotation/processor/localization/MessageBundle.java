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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

class MessageBundle {
	private ProcessingEnvironment	mProcessingEnv;
	private Element					mClassElement;
	private String					mPackageName;
	private String					mClassName;
	private List<KeyValue>			mKeyValues	= new ArrayList<>();

	MessageBundle(Element fieldElement, ProcessingEnvironment env) {
		mProcessingEnv = env;
		mClassElement = fieldElement.getEnclosingElement();
		mPackageName = env.getElementUtils().getPackageOf(mClassElement).getQualifiedName().toString();
		mClassName = mClassElement.getSimpleName().toString();
		Element parent = mClassElement.getEnclosingElement();
		if (parent != null && parent.getKind().isClass()) {
			mClassName = parent.getSimpleName().toString() + "$" + mClassName; //$NON-NLS-1$
		}
	}

	String getFullName() {
		return mPackageName + "." + mClassName; //$NON-NLS-1$
	}

	void addField(Element fieldElement) {
		mKeyValues.add(new KeyValue(fieldElement.getSimpleName().toString(), fieldElement.getAnnotation(Localize.class).value()));
	}

	@SuppressWarnings("nls")
	void createPropertiesFile() {
		try {
			FileObject resource = mProcessingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, mPackageName, mClassName + ".properties", mClassElement);
			try (PrintWriter out = new PrintWriter(resource.openWriter())) {
				out.println("###################################");
				out.println("#          AUTO-GENERATED         #");
				out.println("# Any changes will be overwritten #");
				out.println("###################################");
				StringBuilder buffer = new StringBuilder();
				Collections.sort(mKeyValues);
				for (KeyValue kv : mKeyValues) {
					String value = kv.getValue();
					int count = value.length();
					buffer.setLength(0);
					for (int i = 0; i < count; i++) {
						char ch = value.charAt(i);
						if (ch > ' ' && ch <= '~') {
							buffer.append(ch);
						} else if (ch == ' ') {
							if (buffer.length() > 0) {
								buffer.append(' ');
							} else {
								buffer.append("\\ ");
							}
						} else if (ch == '\n') {
							buffer.append("\\n");
						} else if (ch == '\t') {
							buffer.append("\\t");
						} else {
							buffer.append("\\u");
							String num = Integer.toHexString(ch);
							int zeros = 4 - num.length();
							for (int j = 0; j < zeros; j++) {
								buffer.append('0');
							}
							buffer.append(num);
						}
					}
					count = buffer.length();
					if (count > 2) {
						if (buffer.charAt(count - 1) == ' ') {
							buffer.setLength(count - 1);
							buffer.append("\\ ");
						}
					}
					out.println(kv.getKey() + " = " + buffer.toString());
				}
			}
		} catch (IOException exception) {
			mProcessingEnv.getMessager().printMessage(Kind.ERROR, exception.getMessage(), mClassElement);
		}
	}
}
