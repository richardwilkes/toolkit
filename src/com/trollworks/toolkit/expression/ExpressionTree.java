/*
 * Copyright (c) 1998-2017 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.expression;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.expression.operator.Operator;
import com.trollworks.toolkit.utility.Localization;

class ExpressionTree {
	@Localize("Expression is invalid")
	private static String INVALID_EXPRESSION;

	static {
		Localization.initialize();
	}

	private Evaluator	mEvaluator;
	private Object		mLeftOperand;
	private Object		mRightOperand;
	private Operator	mOperator;
	private Operator	mUnaryOperator;

	ExpressionTree(Evaluator evaluator, Object leftOperand, Object rightOperand, Operator operator, Operator unaryOperator) {
		mEvaluator = evaluator;
		mLeftOperand = leftOperand;
		mRightOperand = rightOperand;
		mOperator = operator;
		mUnaryOperator = unaryOperator;
	}

	final Object evaluate() throws EvaluationException {
		Object left = mEvaluator.evaluateOperand(mLeftOperand);
		Object right = mEvaluator.evaluateOperand(mRightOperand);
		if (mLeftOperand != null && mRightOperand != null) {
			Object result = mOperator.evaluate(left, right);
			return mUnaryOperator != null ? mUnaryOperator.evaluate(result) : result;
		}
		Object operand;
		if (mLeftOperand != null && mRightOperand == null) {
			operand = left;
		} else if (mLeftOperand == null && mRightOperand != null) {
			operand = right;
		} else {
			operand = null;
		}
		if (operand != null) {
			if (mUnaryOperator != null) {
				operand = mUnaryOperator.evaluate(operand);
			} else if (mOperator != null) {
				operand = mOperator.evaluate(operand);
			}
		}
		if (operand == null) {
			throw new EvaluationException(INVALID_EXPRESSION);
		}
		return operand;
	}
}
