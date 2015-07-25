package com.trollworks.toolkit.expression.function;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.expression.ArgumentTokenizer;
import com.trollworks.toolkit.expression.EvaluationException;
import com.trollworks.toolkit.expression.Evaluator;
import com.trollworks.toolkit.utility.Dice;
import com.trollworks.toolkit.utility.Localization;

import java.util.ArrayList;
import java.util.List;

public class DisplayDice implements ExpressionFunction {
	@Localize("Invalid dice specification: %s")
	private static String	INVALID_DICE_SPEC;

	static {
		Localization.initialize();
	}

	@Override
	public String getName() {
		return "dice"; //$NON-NLS-1$
	}

	@Override
	public Object execute(Evaluator evaluator, String arguments) throws EvaluationException {
		try {
			Evaluator ev = new Evaluator(evaluator);
			ArgumentTokenizer tokenizer = new ArgumentTokenizer(arguments);
			List<Integer> args = new ArrayList<>();
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
			throw new EvaluationException(String.format(INVALID_DICE_SPEC, arguments));
		}
	}
}
