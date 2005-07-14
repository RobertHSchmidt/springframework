package org.springframework.webflow.support;

import java.util.Map;

import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionFactory;
import org.springframework.binding.support.Assert;
import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.RequestContext;

/**
 * Expression evaluator that evaluates an expression in flow scope.
 * 
 * @author Keith Donald
 */
public class FlowScopeExpression implements Expression {
	
	private Expression expression;

	/**
	 * Create a new expression evaluator that executes given evaluator
	 * 'in flow scope'.
	 * @param expressionString the expression string
	 */
	public FlowScopeExpression(String expressionString) {
		this(ExpressionFactory.parseExpression(expressionString));
	}

	/**
	 * Create a new expression evaluator that executes given evaluator
	 * 'in flow scope'.
	 * @param expression the nested evaluator to execute
	 */
	public FlowScopeExpression(Expression expression) {
		this.expression = expression;
	}
	
	protected Expression getExpression() {
		return expression;
	}
	
	public Object evaluateAgainst(Object target, Map context) throws EvaluationException {
		return expression.evaluateAgainst(requestContext(target).getFlowScope(), context);
	}
	
	protected RequestContext requestContext(Object target) {
		Assert.isInstanceOf(RequestContext.class, target,
			"In the web flow system all source (from) mapping expressions must be evaluated against the request context: ");
		return (RequestContext)target;
	}
	
	public String toString() {
		return new ToStringCreator(this).append("expression", expression).toString();
	}
}