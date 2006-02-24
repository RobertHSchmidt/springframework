/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.action;

import junit.framework.TestCase;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.method.MethodSignature;
import org.springframework.binding.method.Parameter;
import org.springframework.binding.method.Parameters;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.Event;
import org.springframework.webflow.support.WebFlowOgnlExpressionParser;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit test for the bean invoking actions.
 * @author Keith Donald
 */
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
		BeanFactoryBeanInvokingAction action = new BeanFactoryBeanInvokingAction();
		StaticWebApplicationContext beanFactory = new StaticWebApplicationContext();
		beanFactory.registerSingleton("bean", Bean.class);
		action.setBeanFactory(beanFactory);
		MockRequestContext context = new MockRequestContext();
		context.setAttribute("method", new MethodSignature("execute"));
		context.setAttribute("bean", "bean");
		Bean bean = (Bean)beanFactory.getBean("bean");
		action.execute(context);
		assertTrue(bean.executed);
	}

	public void testInvokeBeanWithParameters() throws Exception {
		BeanFactoryBeanInvokingAction action = new BeanFactoryBeanInvokingAction();
		StaticWebApplicationContext beanFactory = new StaticWebApplicationContext();
		beanFactory.registerSingleton("bean", Bean.class);
		action.setBeanFactory(beanFactory);
		MockRequestContext context = new MockRequestContext();
		AttributeMap attributes = new AttributeMap();
		attributes.put("foo", "a string value");
		context.setLastEvent(new Event(this, "submit", attributes));
		context.setAttribute("method", new MethodSignature("execute", new Parameter(String.class,
				expression("lastEvent.attributes.foo"))));
		context.setAttribute("bean", "bean");
		Bean bean = (Bean)beanFactory.getBean("bean");
		action.execute(context);
		assertTrue("Didn't execute:", bean.executed);
		assertEquals("Property not set:", "a string value", bean.datum1);
	}

	public void testInvokeBeanWithParametersAndTypeConversion() throws Exception {
		BeanFactoryBeanInvokingAction action = new BeanFactoryBeanInvokingAction();
		StaticWebApplicationContext beanFactory = new StaticWebApplicationContext();
		beanFactory.registerSingleton("bean", Bean.class);
		action.setBeanFactory(beanFactory);
		MockRequestContext context = new MockRequestContext();
		AttributeMap attributes = new AttributeMap();
		attributes.put("foo", "a string value");
		attributes.put("bar", "12345");
		context.setLastEvent(new Event(this, "submit", attributes));
		context.setAttribute("method", new MethodSignature("execute", new Parameters(new Parameter[] {
				new Parameter(String.class, expression("lastEvent.attributes.foo")),
				new Parameter(Integer.class, expression("lastEvent.attributes.bar")) })));
		context.setAttribute("bean", "bean");
		Bean bean = (Bean)beanFactory.getBean("bean");
		action.execute(context);
		assertTrue(bean.executed);
		assertEquals("Property not set:", "a string value", bean.datum1);
		assertEquals("Property not set:", new Integer(12345), bean.datum2);
	}

	private Expression expression(String string) {
		return new WebFlowOgnlExpressionParser().parseExpression(string);
	}
}