package org.springframework.webflow.support;

import java.util.Map;

import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.Expression;
import org.springframework.webflow.RequestContext;

/**
 * Expression evaluator that evaluates an expression in flow scope.
 */
public class FlowScopeExpression implements Expression {
	
	private Expression expression;
	
	/**
	 * Create a new expression evaluator that executes given evaluator
	 * 'in flow scope'.
	 * @param evaluator the nested evaluator to execute
	 */
	public FlowScopeExpression(Expression evaluator) {
		this.expression = evaluator;
	}
	
	public Object evaluateAgainst(Object target, Map context) throws EvaluationException {
		RequestContext requestContext = (RequestContext)target;
		return expression.evaluateAgainst(requestContext.getFlowScope(), context);
	}
}