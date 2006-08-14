package org.springframework.webflow.action;

import junit.framework.TestCase;

import org.springframework.webflow.action.MultiAction.MethodResolver;
import org.springframework.webflow.engine.AnnotatedAction;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.engine.MockFlowSession;
import org.springframework.webflow.test.engine.MockRequestContext;
import org.springframework.webflow.util.DispatchMethodInvoker.MethodLookupException;

public class MultiActionTests extends TestCase {
	private TestMultiAction action = new TestMultiAction();

	private MockRequestContext context = new MockRequestContext();

	public void testDispatchWithMethodSignature() throws Exception {
		context.getAttributeMap().put(AnnotatedAction.METHOD_ATTRIBUTE, "increment");
		action.execute(context);
		assertEquals(1, action.counter);
	}

	public void testDispatchWithBogusMethodSignature() throws Exception {
		context.getAttributeMap().put(AnnotatedAction.METHOD_ATTRIBUTE, "bogus");
		try {
			action.execute(context);
			fail("Should've failed with no such method");
		}
		catch (MethodLookupException e) {

		}
	}

	public void testDispatchWithCurrentStateId() throws Exception {
		MockFlowSession session = context.getMockFlowExecutionContext().getMockActiveSession();
		session.setState(new ViewState(session.getDefinitionInternal(), "increment"));
		action.execute(context);
		assertEquals(1, action.counter);
	}

	public void testNoSuchMethodWithCurrentStateId() throws Exception {
		try {
			action.execute(context);
			fail("Should've failed with no such method");
		}
		catch (MethodLookupException e) {

		}
	}

	public void testCannotResolveMethod() throws Exception {
		try {
			context.getMockFlowExecutionContext().getMockActiveSession().setState(null);
			action.execute(context);
			fail("Should've failed with illegal state");
		}
		catch (IllegalStateException e) {

		}
	}

	public void testCustomMethodResolver() throws Exception {
		MethodResolver methodResolver = new MethodResolver() {
			public String resolveMethod(RequestContext context) {
				return "increment";
			}
		};
		action.setMethodResolver(methodResolver);
		action.execute(context);
		assertEquals(1, action.counter);
	}
}