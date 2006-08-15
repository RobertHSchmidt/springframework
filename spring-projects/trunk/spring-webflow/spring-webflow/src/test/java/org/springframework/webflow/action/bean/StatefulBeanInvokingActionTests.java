package org.springframework.webflow.action.bean;

import junit.framework.TestCase;

import org.springframework.binding.method.MethodSignature;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.webflow.TestBean;
import org.springframework.webflow.action.bean.StatefulBeanInvokingAction;
import org.springframework.webflow.execution.ScopeType;
import org.springframework.webflow.test.engine.MockRequestContext;

public class StatefulBeanInvokingActionTests extends TestCase {
	private StatefulBeanInvokingAction action;

	private StaticWebApplicationContext beanFactory = new StaticWebApplicationContext();

	private MockRequestContext context = new MockRequestContext();

	public void setUp() {
		action = new StatefulBeanInvokingAction(new MethodSignature("execute"), "bean", beanFactory);
	}

	public void testInvokeBean() throws Exception {
		beanFactory.registerPrototype("bean", TestBean.class);
		action.execute(context);
		assertNotNull(context.getFlowScope().get("bean"));
		TestBean bean = (TestBean)context.getFlowScope().get("bean");
		assertTrue(bean.executed);
		bean.datum1 = "3";
		action.execute(context);
		bean = (TestBean)context.getFlowScope().get("bean");
		assertEquals("3", bean.datum1);
	}

	public void testInvokeBeanNotPrototype() throws Exception {
		beanFactory.registerSingleton("bean", TestBean.class);
		try {
			action.execute(context);
			fail("should've failed iae");
		}
		catch (IllegalArgumentException e) {

		}
	}

	public void testInvokeBeanCustomScope() throws Exception {
		action.setBeanScope(ScopeType.CONVERSATION);
		beanFactory.registerPrototype("bean", TestBean.class);
		action.execute(context);
		assertNotNull(context.getConversationScope().get("bean"));
		TestBean bean = (TestBean)context.getConversationScope().get("bean");
		assertTrue(bean.executed);
	}
}