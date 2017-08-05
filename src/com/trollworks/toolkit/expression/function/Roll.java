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

package com.trollworks.toolkit.expression.function;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.expression.EvaluationException;
import com.trollworks.toolkit.expression.Evaluator;
import com.trollworks.toolkit.utility.Dice;
import com.trollworks.toolkit.utility.Localization;

public class Roll implements ExpressionFunction {
    @Localize("Invalid dice specification: %s")
    @Localize(locale = "pt-BR", value = "Especificação de dados inválida: %s")
    private static String INVALID_DICE_SPEC;

    static {
        Localization.initialize();
    }

    @Override
    public final String getName() {
        return "roll"; //$NON-NLS-1$
    }

    @Override
    public final Object execute(Evaluator evaluator, String arguments) throws EvaluationException {
        try {
            Dice dice = new Dice(arguments);
            return Double.valueOf(dice.roll());
        } catch (Exception exception) {
            throw new EvaluationException(String.format(INVALID_DICE_SPEC, arguments));
        }
    }
}
