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
import com.trollworks.toolkit.utility.text.Numbers;

public class Signed implements ExpressionFunction {
    @Override
    public String getName() {
        return "signed";
    }

    @Override
    public Object execute(Evaluator evaluator, String arguments) throws EvaluationException {
        return Numbers.formatWithForcedSign(ArgumentTokenizer.getDoubleArgument(new Evaluator(evaluator), arguments));
    }
}
