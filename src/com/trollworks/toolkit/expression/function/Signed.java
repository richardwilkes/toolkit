package com.trollworks.toolkit.expression.function;

import com.trollworks.toolkit.expression.ArgumentTokenizer;
import com.trollworks.toolkit.expression.EvaluationException;
import com.trollworks.toolkit.expression.Evaluator;
import com.trollworks.toolkit.utility.text.Numbers;

public class Signed implements ExpressionFunction {
	@Override
	public String getName() {
		return "signed"; //$NON-NLS-1$
	}

	@Override
	public Object execute(Evaluator evaluator, String arguments) throws EvaluationException {
		return Numbers.formatWithForcedSign(ArgumentTokenizer.getDoubleArgument(new Evaluator(evaluator), arguments));
	}
}
