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

package com.trollworks.toolkit.expression;

import com.trollworks.toolkit.expression.operator.Operator;

class ExpressionOperator {
	Operator	mOperator;
	Operator	mUnaryOperator;

	ExpressionOperator(Operator operator, Operator unaryOperator) {
		mOperator = operator;
		mUnaryOperator = unaryOperator;
	}
}
