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
import com.trollworks.toolkit.utility.Dice;
import com.trollworks.toolkit.utility.I18n;

import java.util.ArrayList;
import java.util.List;

public class DisplayDice implements ExpressionFunction {
    @Override
    public String getName() {
        return "dice";
    }

    @Override
    public Object execute(Evaluator evaluator, String arguments) throws EvaluationException {
        try {
            Evaluator         ev        = new Evaluator(evaluator);
            ArgumentTokenizer tokenizer = new ArgumentTokenizer(arguments);
            List<Integer>     args      = new ArrayList<>();
            while (tokenizer.hasMoreTokens()) {
                args.add(Integer.valueOf((int) ArgumentTokenizer.getDouble(ev.evaluate(tokenizer.nextToken()))));
            }
            Dice dice;
            switch (args.size()) {
            case 1: // sides
                dice = new Dice(1, args.get(0).intValue(), 0, 1);
                break;
            case 2: // count, sides
                dice = new Dice(args.get(0).intValue(), args.get(1).intValue(), 0, 1);
                break;
            case 3: // count, sides, modifier
                dice = new Dice(args.get(0).intValue(), args.get(1).intValue(), args.get(2).intValue(), 1);
                break;
            case 4: // count, sides, modifier, multiplier
                dice = new Dice(args.get(0).intValue(), args.get(1).intValue(), args.get(2).intValue(), args.get(3).intValue());
                break;
            default:
                throw new Exception();
            }
            return dice.toString();
        } catch (EvaluationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new EvaluationException(String.format(I18n.Text("Invalid dice specification: %s"), arguments));
        }
    }
}
