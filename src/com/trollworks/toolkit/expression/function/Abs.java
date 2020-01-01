/*
 * Copyright (c) 1998-2020 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.expression.function;

import com.trollworks.toolkit.expression.ArgumentTokenizer;
import com.trollworks.toolkit.expression.EvaluationException;
import com.trollworks.toolkit.expression.Evaluator;

public class Abs implements ExpressionFunction {
    @Override
    public final String getName() {
        return "abs";
    }

    @Override
    public final Object execute(Evaluator evaluator, String arguments) throws EvaluationException {
        return Double.valueOf(Math.abs(ArgumentTokenizer.getDoubleArgument(evaluator, arguments)));
    }
}
