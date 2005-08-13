package org.springframework.webflow.action;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.webflow.Event;
import org.springframework.webflow.action.bean.Argument;
import org.springframework.webflow.action.bean.Arguments;
import org.springframework.webflow.action.bean.BeanInvokingAction;
import org.springframework.webflow.action.bean.MethodKey;
import org.springframework.webflow.test.MockRequestContext;

public class BeanInvokingActionTests extends TestCase {
	public static class Bean {
		private String datum1;

		private Integer datum2;

		private boolean executed;

		public void execute() {
			this.executed = true;
		}

		public void execute(String parameter) {
			this.executed = true;
			this.datum1 = parameter;
		}

		public void execute(String parameter, Integer parameter2) {
			this.executed = true;
			this.datum1 = parameter;
			this.datum2 = parameter2;
		}
	}

	public void testInvokeBeanNoParameters() throws Exception {
		BeanInvokingAction action = new BeanInvokingAction();
		StaticWebApplicationContext beanFactory = new StaticWebApplicationContext();
		beanFactory.registerSingleton("bean", Bean.class);
		action.setBeanFactory(beanFactory);
		MockRequestContext context = new MockRequestContext();
		context.setProperty("method", new MethodKey("execute"));
		context.setProperty("bean", "bean");
		Bean bean = (Bean) beanFactory.getBean("bean");
		Event result = action.execute(context);
		assertTrue(bean.executed);
	}

	public void testInvokeBeanWithParameters() throws Exception {
		BeanInvokingAction action = new BeanInvokingAction();
		StaticWebApplicationContext beanFactory = new StaticWebApplicationContext();
		beanFactory.registerSingleton("bean", Bean.class);
		action.setBeanFactory(beanFactory);
		MockRequestContext context = new MockRequestContext();
		Map parameters = new HashMap();
		parameters.put("foo", "a string value");
		context.setLastEvent(new Event(this, "submit", parameters));
		context.setProperty("method", new MethodKey("execute", new Argument("foo", String.class)));
		context.setProperty("bean", "bean");
		Bean bean = (Bean) beanFactory.getBean("bean");
		Event result = action.execute(context);
		assertTrue("Didn't execute:", bean.executed);
		assertEquals("Property not set:", "a string value", bean.datum1);
	}

	public void testInvokeBeanWithParametersAndTypeConversion()
			throws Exception {
		BeanInvokingAction action = new BeanInvokingAction();
		StaticWebApplicationContext beanFactory = new StaticWebApplicationContext();
		beanFactory.registerSingleton("bean", Bean.class);
		action.setBeanFactory(beanFactory);
		MockRequestContext context = new MockRequestContext();
		Map parameters = new HashMap();
		parameters.put("foo", "a string value");
		parameters.put("bar", "12345");
		context.setLastEvent(new Event(this, "submit", parameters));
		Arguments arguments = new Arguments(new Argument[] {
				new Argument("foo", String.class),
				new Argument("bar", Integer.class) });
		context.setProperty("method", new MethodKey("execute", arguments));
		context.setProperty("arguments", arguments);
		context.setProperty("bean", "bean");
		Bean bean = (Bean) beanFactory.getBean("bean");
		Event result = action.execute(context);
		assertTrue(bean.executed);
		assertEquals("Property not set:", new Integer(12345), bean.datum2);
	}
}