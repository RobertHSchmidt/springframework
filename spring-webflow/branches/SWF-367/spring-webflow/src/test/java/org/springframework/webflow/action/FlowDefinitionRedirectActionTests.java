package org.springframework.webflow.action;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.webflow.test.MockRequestContext;
import org.springframework.webflow.test.MockExternalContext.FlowDefinitionRedirectResult;

public class FlowDefinitionRedirectActionTests extends TestCase {
	private FlowDefinitionRedirectAction action;

	public void testExecute() throws Exception {
		Expression flowId = new StaticExpression("user");
		Expression[] requestElements = new Expression[] { new StaticExpression("1") };
		Map requestParameters = new HashMap();
		requestParameters.put(new StaticExpression("foo"), new StaticExpression("bar"));
		action = new FlowDefinitionRedirectAction(flowId, requestElements, requestParameters);
		MockRequestContext context = new MockRequestContext();
		action.execute(context);
		FlowDefinitionRedirectResult result = context.getMockExternalContext().getFlowDefinitionRedirectResult();
		assertEquals("user", result.getFlowId());
		assertEquals("1", result.getRequestElements()[0]);
		assertEquals("bar", result.getRequestParameters().get("foo"));
	}

	public void testExecuteWithNullRequestFields() throws Exception {
		Expression flowId = new StaticExpression("user");
		action = new FlowDefinitionRedirectAction(flowId, null, null);
		MockRequestContext context = new MockRequestContext();
		action.execute(context);
		FlowDefinitionRedirectResult result = context.getMockExternalContext().getFlowDefinitionRedirectResult();
		assertEquals("user", result.getFlowId());
		assertEquals(null, result.getRequestElements());
		assertEquals(null, result.getRequestParameters());
	}

	public void testExecuteWithNullFlowId() throws Exception {
		try {
			action = new FlowDefinitionRedirectAction(null, null, null);
			fail("Should have failed");
		} catch (IllegalArgumentException e) {

		}
	}
}
