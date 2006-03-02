package org.springframework.binding.expression.support;

import java.util.Map;

import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.Expression;
import org.springframework.core.style.ToStringCreator;

/**
 * Evaluates an array of expressions to build a concatenated string.
 * 
 * @author Keith Donald
 */
public class CompositeStringExpression implements Expression {
	private Expression[] expressions;

	public CompositeStringExpression(Expression[] expressions) {
		this.expressions = expressions;
	}

	public Object evaluateAgainst(Object target, Map evaluationContext) throws EvaluationException {
		StringBuffer buffer = new StringBuffer(128);
		for (int i = 0; i < expressions.length; i++) {
			buffer.append(expressions[i].evaluateAgainst(target, evaluationContext));
		}
		return buffer.toString();
	}

	public String toString() {
		return new ToStringCreator(this).append("expressions", expressions).toString();
	}
}