/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.trollworks.toolkit.annotation.processor.localization;

class KeyValue implements Comparable<KeyValue> {
	private String	mKey;
	private String	mValue;

	KeyValue(String key, String value) {
		mKey = key;
		mValue = value;
	}

	String getKey() {
		return mKey;
	}

	String getValue() {
		return mValue;
	}

	@Override
	public int compareTo(KeyValue other) {
		return mKey.compareTo(other.mKey);
	}
}
