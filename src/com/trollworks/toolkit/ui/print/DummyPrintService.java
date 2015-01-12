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

package com.trollworks.toolkit.ui.print;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.utility.Localization;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.ServiceUIFactory;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.event.PrintServiceAttributeListener;

class DummyPrintService implements PrintService {
	@Localize("No printer is available!")
	@Localize(locale = "ru", value = "Нет доступного принтера!")
	@Localize(locale = "de", value = "Kein Drucker verfügbar!")
	@Localize(locale = "es", value = "¡No hay impresora disponible!")
	private static String	NO_PRINTER_AVAILABLE;

	static {
		Localization.initialize();
	}

	@Override
	public void addPrintServiceAttributeListener(PrintServiceAttributeListener listener) {
		// Not used.
	}

	@Override
	public DocPrintJob createPrintJob() {
		return null;
	}

	@Override
	public <T extends PrintServiceAttribute> T getAttribute(Class<T> category) {
		return null;
	}

	@Override
	public PrintServiceAttributeSet getAttributes() {
		return null;
	}

	@Override
	public Object getDefaultAttributeValue(Class<? extends Attribute> category) {
		if (category == Media.class) {
			return MediaSizeName.NA_LETTER;
		}
		if (category == OrientationRequested.class) {
			return OrientationRequested.PORTRAIT;
		}
		return null;
	}

	@Override
	public String getName() {
		return NO_PRINTER_AVAILABLE;
	}

	@Override
	public ServiceUIFactory getServiceUIFactory() {
		return null;
	}

	@Override
	public Class<?>[] getSupportedAttributeCategories() {
		return null;
	}

	@Override
	public Object getSupportedAttributeValues(Class<? extends Attribute> category, DocFlavor flavor, AttributeSet attributes) {
		if (category == Media.class) {
			return new Media[] { MediaSizeName.NA_LETTER, MediaSizeName.NA_LEGAL, MediaSizeName.ISO_A4 };
		}
		if (category == OrientationRequested.class) {
			return new OrientationRequested[] { OrientationRequested.PORTRAIT, OrientationRequested.LANDSCAPE };
		}
		return null;
	}

	@Override
	public DocFlavor[] getSupportedDocFlavors() {
		return null;
	}

	@Override
	public AttributeSet getUnsupportedAttributes(DocFlavor flavor, AttributeSet attributes) {
		return null;
	}

	@Override
	public boolean isAttributeCategorySupported(Class<? extends Attribute> category) {
		return false;
	}

	@Override
	public boolean isAttributeValueSupported(Attribute attrval, DocFlavor flavor, AttributeSet attributes) {
		return false;
	}

	@Override
	public boolean isDocFlavorSupported(DocFlavor flavor) {
		return false;
	}

	@Override
	public void removePrintServiceAttributeListener(PrintServiceAttributeListener listener) {
		// Not used.
	}
}
