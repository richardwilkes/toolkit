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

import com.trollworks.toolkit.expression.EvaluationException;
import com.trollworks.toolkit.expression.Evaluator;
import com.trollworks.toolkit.utility.Dice;
import com.trollworks.toolkit.utility.I18n;

public class Roll implements ExpressionFunction {
    @Override
    public final String getName() {
        return "roll";
    }

    @Override
    public final Object execute(Evaluator evaluator, String arguments) throws EvaluationException {
        try {
            Dice dice = new Dice(arguments);
            return Double.valueOf(dice.roll());
        } catch (Exception exception) {
            throw new EvaluationException(String.format(I18n.Text("Invalid dice specification: %s"), arguments));
        }
    }
}
