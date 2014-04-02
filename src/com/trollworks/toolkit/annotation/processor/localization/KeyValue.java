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
