package org.springframework.webflow.engine.support;

import junit.framework.TestCase;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.webflow.core.DefaultExpressionParserFactory;
import org.springframework.webflow.execution.ViewSelection;
import org.springframework.webflow.execution.support.ApplicationView;
import org.springframework.webflow.test.engine.MockFlowExecutionContext;
import org.springframework.webflow.test.engine.MockRequestContext;

public class ApplicationViewSelectorTests extends TestCase {
	ExpressionParser parser = new DefaultExpressionParserFactory().getExpressionParser();

	public void testMakeSelection() {
		Expression exp = parser.parseExpression("${requestScope.viewVar}");
		ApplicationViewSelector selector = new ApplicationViewSelector(exp);
		MockRequestContext context = new MockRequestContext();
		context.getRequestScope().put("viewVar", "view");
		context.getRequestScope().put("foo", "bar");
		context.getFlowScope().put("foo", "bar2");
		context.getFlowScope().put("foo2", "bar");
		context.getConversationScope().put("foo", "bar3");
		context.getConversationScope().put("foo3", "bar");
		ViewSelection selection = selector.makeEntrySelection(context);
		assertTrue(selection instanceof ApplicationView);
		ApplicationView view = (ApplicationView)selection;
		assertEquals("view", view.getViewName());
		assertEquals("bar", view.getModel().get("foo"));
		assertEquals("bar", view.getModel().get("foo2"));
		assertEquals("bar", view.getModel().get("foo3"));
	}

	public void testMakeNullSelection() {
		ApplicationViewSelector selector = new ApplicationViewSelector(new StaticExpression(null));
		MockRequestContext context = new MockRequestContext();
		try {
			selector.makeEntrySelection(context);
			fail();
		}
		catch (IllegalStateException e) {
			//expected
		}
	}

	public void testMakeNullSelectionEmptyString() {
		ApplicationViewSelector selector = new ApplicationViewSelector(new StaticExpression(""));
		MockRequestContext context = new MockRequestContext();
		try {
			selector.makeEntrySelection(context);
			fail();
		}
		catch (IllegalStateException e) {
			//expected
		}
	}

	public void testIsEntrySelectionRenderable() {
		ApplicationViewSelector selector = new ApplicationViewSelector(new StaticExpression(null));
		MockRequestContext context = new MockRequestContext();
		assertTrue(selector.isEntrySelectionRenderable(context));
	}

	public void testIsEntrySelectionRenderableRedirect() {
		ApplicationViewSelector selector = new ApplicationViewSelector(new StaticExpression(null), true);
		MockRequestContext context = new MockRequestContext();
		assertFalse(selector.isEntrySelectionRenderable(context));
	}

	public void testIsEntrySelectionRenderableAlwaysRedirectOnPause() {
		ApplicationViewSelector selector = new ApplicationViewSelector(new StaticExpression(null));
		MockRequestContext requestContext = new MockRequestContext();
		MockFlowExecutionContext flowExecutionContext = new MockFlowExecutionContext();
		flowExecutionContext.putAttribute("alwaysRedirectOnPause", Boolean.TRUE);
		requestContext.setFlowExecutionContext(flowExecutionContext);
		assertFalse(selector.isEntrySelectionRenderable(requestContext));
	}
}