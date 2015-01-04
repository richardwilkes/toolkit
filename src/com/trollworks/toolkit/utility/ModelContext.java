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

package com.trollworks.toolkit.utility;

import com.trollworks.toolkit.collections.Stack;

import java.util.HashMap;

/** An object for holding state during model loads and saves. */
public class ModelContext extends HashMap<String, Object> {
	/** The stack for the versions of the data being loaded. */
	public Stack<Integer>	mVersionStack	= new Stack<>();
}
