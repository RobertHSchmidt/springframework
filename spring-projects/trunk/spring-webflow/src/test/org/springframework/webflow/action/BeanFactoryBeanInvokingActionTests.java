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

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.binding.method.MethodSignature;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit test for the bean factory bean invoking action.
 * @author Keith Donald
 */
public class BeanFactoryBeanInvokingActionTests extends TestCase {

	private BeanFactoryBeanInvokingAction action = new BeanFactoryBeanInvokingAction();
	
	private StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();

	private MockRequestContext context = new MockRequestContext();
	
	public void setUp() {
		action.setBeanFactory(beanFactory);
		context.setAttribute("method", new MethodSignature("execute"));
		context.setAttribute("bean", "bean");
	}
	
	public void testInvokeBean() throws Exception {
		beanFactory.addBean("bean", new Bean());
		action.execute(context);
		Bean bean = (Bean)beanFactory.getBean("bean");
		assertTrue(bean.executed);
	}
	
	public void testInvokeNoSuchBean() throws Exception {
		try {
			action.execute(context);
		} catch (NoSuchBeanDefinitionException e) {
			
		}
	}

	public void testInvokeBeanCustomBeanName() throws Exception {
		action.setBeanName("bean");
		context.removeAttribute("bean");
		beanFactory.addBean("bean", new Bean());
		action.execute(context);
		Bean bean = (Bean)beanFactory.getBean("bean");
		assertTrue(bean.executed);
	}

	public void testInvalidBeanNameConfiguration() throws Exception {
		context.setAttributes(null);
		try {
			action.execute(context);
		} catch (IllegalArgumentException e) {
			
		}
	}

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
}