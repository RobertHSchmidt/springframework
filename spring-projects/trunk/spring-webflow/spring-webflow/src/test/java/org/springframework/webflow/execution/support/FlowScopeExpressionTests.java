package org.springframework.webflow.execution.support;

import junit.framework.TestCase;

import org.springframework.binding.expression.Expression;
import org.springframework.webflow.core.DefaultExpressionParserFactory;
import org.springframework.webflow.test.engine.MockRequestContext;

public class FlowScopeExpressionTests extends TestCase {

	protected void setUp() throws Exception {
	}

	public void testFlowScopeExpression() {
		Expression exp = new DefaultExpressionParserFactory().getExpressionParser().parseExpression("foo");
		FlowScopeExpression flowExp = new FlowScopeExpression(exp);
		MockRequestContext context = new MockRequestContext();
		context.getFlowScope().put("foo", "bar");
		assertEquals("bar", flowExp.evaluateAgainst(context, null));
	}
}