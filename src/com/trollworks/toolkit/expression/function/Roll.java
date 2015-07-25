package com.trollworks.toolkit.expression.function;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.expression.EvaluationException;
import com.trollworks.toolkit.expression.Evaluator;
import com.trollworks.toolkit.utility.Dice;
import com.trollworks.toolkit.utility.Localization;

public class Roll implements ExpressionFunction {
	@Localize("Invalid dice specification: %s")
	private static String	INVALID_DICE_SPEC;

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
