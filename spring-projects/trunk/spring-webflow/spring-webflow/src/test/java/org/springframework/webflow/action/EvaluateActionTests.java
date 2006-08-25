package org.springframework.webflow.action;

import junit.framework.TestCase;

import org.springframework.binding.expression.ExpressionParser;
import org.springframework.webflow.TestBean;
import org.springframework.webflow.core.DefaultExpressionParserFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.ScopeType;
import org.springframework.webflow.test.engine.MockRequestContext;

public class EvaluateActionTests extends TestCase {
	private ExpressionParser parser = new DefaultExpressionParserFactory().getExpressionParser();

	private MockRequestContext context = new MockRequestContext();

	protected void setUp() throws Exception {
		context.getFlowScope().put("foo", "bar");
		context.getFlowScope().put("bean", new TestBean());
	}

	public void testEvaluateExpressionNoResult() throws Exception {
		EvaluateAction action = new EvaluateAction(parser.parseExpression("flowScope.foo"));
		Event result = action.execute(context);
		assertEquals("bar", result.getId());
		assertNull(context.getFlowScope().get("baz"));
	}

	public void testEvaluateExpressionResult() throws Exception {
		EvaluateAction action = new EvaluateAction(parser.parseExpression("flowScope.foo"), new ActionResultExposer(
				"baz", ScopeType.FLOW));
		Event result = action.execute(context);
		assertEquals("bar", result.getId());
		assertEquals("bar", context.getFlowScope().get("baz"));
	}
	
	public void testBeanResult() throws Exception {
		EvaluateAction action = new EvaluateAction(parser.parseExpression("flowScope.bean"), new ActionResultExposer(
				"baz", ScopeType.FLOW));
		Event result = action.execute(context);
		assertEquals("success", result.getId());
		assertEquals(new TestBean(), context.getFlowScope().get("baz"));
	}
}
