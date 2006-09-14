package org.springframework.webflow.engine.support;

import junit.framework.TestCase;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.webflow.core.DefaultExpressionParserFactory;
import org.springframework.webflow.engine.support.FlowDefinitionRedirectSelector;
import org.springframework.webflow.execution.ViewSelection;
import org.springframework.webflow.execution.support.FlowDefinitionRedirect;
import org.springframework.webflow.test.engine.MockRequestContext;

public class FlowRedirectSelectorTests extends TestCase {
	ExpressionParser parser = new DefaultExpressionParserFactory().getExpressionParser();

	public void testMakeSelection() {
		Expression exp = parser.parseExpression("${requestScope.flowIdVar}?a=b&c=${requestScope.bar}");
		FlowDefinitionRedirectSelector selector = new FlowDefinitionRedirectSelector(exp);
		MockRequestContext context = new MockRequestContext();
		context.getRequestScope().put("flowIdVar", "foo");
		context.getRequestScope().put("bar", "baz");
		ViewSelection selection = selector.makeEntrySelection(context);
		assertTrue(selection instanceof FlowDefinitionRedirect);
		FlowDefinitionRedirect redirect = (FlowDefinitionRedirect)selection;
		assertEquals("foo", redirect.getFlowDefinitionId());
		assertEquals("b", redirect.getExecutionInput().get("a"));
		assertEquals("baz", redirect.getExecutionInput().get("c"));
	}
	
	public void testMakeSelectionInvalidVariable() {
		Expression exp = parser.parseExpression("${flowScope.flowId}");
		FlowDefinitionRedirectSelector selector = new FlowDefinitionRedirectSelector(exp);
		MockRequestContext context = new MockRequestContext();
		try {
			ViewSelection selection = selector.makeEntrySelection(context);
			assertTrue(selection instanceof FlowDefinitionRedirect);
		} catch (IllegalStateException e) {
			
		}
	}
}