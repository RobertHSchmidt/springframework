package org.springframework.binding.expression;

import org.springframework.binding.expression.support.ExpressionParserUtils;
import org.springframework.util.Assert;

/**
 * A static factory for producing configured expression evaluators.
 * @author Keith
 */
public class ExpressionFactory {
	
	/**
	 * Return the evaluator for the specified expression string.
	 * @param expressionString the expression string
	 * @return the evaluator
	 */
	public static Expression evaluatorFor(String expressionString) {
		return ExpressionParserUtils.getDefaultExpressionParser().parseExpression(expressionString);
	}
	
	/**
	 * Retrun the evaluator/setter for the specified expression string.
	 * @param expressionString the expression string
	 * @return the evaluator setter
	 */
	public static PropertyExpression propertyEvaluatorFor(String expressionString) {
		Expression evaluator = ExpressionParserUtils.getDefaultExpressionParser().parseExpression(expressionString);
		Assert.isInstanceOf(PropertyExpression.class, evaluator, "The expression evaluator is not a PropertyExpressionEvaluator");
		return (PropertyExpression)evaluator;
	}
}