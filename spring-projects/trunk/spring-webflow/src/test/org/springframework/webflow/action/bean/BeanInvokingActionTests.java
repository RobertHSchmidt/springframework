package org.springframework.webflow.action.bean;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.webflow.Event;
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
		action.execute(context);
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
		context.setProperty("method", new MethodKey("execute", new Argument(
				String.class, "foo")));
		context.setProperty("bean", "bean");
		Bean bean = (Bean) beanFactory.getBean("bean");
		action.execute(context);
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
		context.setProperty("method", new MethodKey("execute", new Arguments(
				new Argument[] { new Argument(String.class, "foo"),
						new Argument(Integer.class, "bar") })));
		context.setProperty("bean", "bean");
		Bean bean = (Bean) beanFactory.getBean("bean");
		action.execute(context);
		assertTrue(bean.executed);
		assertEquals("Property not set:", "a string value", bean.datum1);
		assertEquals("Property not set:", new Integer(12345), bean.datum2);
	}
	
	public void testMethodKeyConversionNoArg() {
		TextToMethodKey converter = new TextToMethodKey(new DefaultConversionService());
		MethodKey key = (MethodKey)converter.convert("execute");
		assertEquals("Method key wrong", "execute", key.getMethodName());
	}

	public void testMethodKeyConversionNoArg2() {
		TextToMethodKey converter = new TextToMethodKey(new DefaultConversionService());
		MethodKey key = (MethodKey)converter.convert("execute()");
		assertEquals("Method key wrong", "execute", key.getMethodName());
	}

	public void testMethodKeyConversionWithArgs() {
		TextToMethodKey converter = new TextToMethodKey(new DefaultConversionService());
		MethodKey key = (MethodKey)converter.convert("execute(string foo, int bar)");
		assertEquals("Method key wrong", "execute", key.getMethodName());
		assertEquals("Arguments size wrong", 2, key.getArguments().size());
		assertEquals("Argument 1 name wrong", "foo", key.getArguments().getArgument(0).getName());
		assertEquals("Argument 1 type wrong", String.class, key.getArguments().getArgument(0).getType());
		assertEquals("Argument 2 name wrong", "bar", key.getArguments().getArgument(1).getName());
		assertEquals("Argument 2 type wrong", int.class, key.getArguments().getArgument(1).getType());

	}
}