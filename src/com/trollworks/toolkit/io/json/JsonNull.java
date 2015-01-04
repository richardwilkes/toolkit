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

package com.trollworks.toolkit.io.json;

/** Represents a 'null' in JSON. */
public final class JsonNull {
	/** The singleton instance. */
	public static final JsonNull	INSTANCE	= new JsonNull();

	private JsonNull() {
		// Singleton
	}

	@Override
	public final boolean equals(Object other) {
		return other == null || other == this || other instanceof JsonNull;
	}

	@Override
	public final int hashCode() {
		return super.hashCode();
	}

	@Override
	public final String toString() {
		return "null"; //$NON-NLS-1$
	}
}
