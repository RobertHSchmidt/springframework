package org.springframework.webflow.support;

import java.util.Map;

import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.ExpressionFactory;
import org.springframework.binding.expression.PropertyExpression;

/**
 * Expression evaluator that evaluates an expression in flow scope.
 * 
 * @author Keith Donald
 */
public class FlowScopePropertyExpression extends FlowScopeExpression implements PropertyExpression {
	
	/**
	 * Create a new property expression evaluator that executes given evaluator
	 * 'in flow scope'.
	 * @param expressionString the expression string
	 */
	public FlowScopePropertyExpression(String expressionString) {
		this(ExpressionFactory.parsePropertyExpression(expressionString));
	}

	/**
	 * Create a new expression evaluator that executes given evaluator
	 * 'in flow scope'.
	 * @param expression the nested evaluator to execute
	 */
	public FlowScopePropertyExpression(PropertyExpression expression) {
		super(expression);
	}

	protected PropertyExpression getPropertyExpression() {
		return (PropertyExpression)getExpression();
	}
	
	public void setValue(Object target, Object value, Map context) throws EvaluationException {
		getPropertyExpression().setValue(requestContext(target).getFlowScope(), value, context);
	}
}