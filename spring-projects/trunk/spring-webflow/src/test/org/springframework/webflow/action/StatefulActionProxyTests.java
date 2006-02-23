/*
 * Copyright 2002-2005 the original author or authors.
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

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.binding.method.MethodSignature;
import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit test for the StatefulActionWrapper class.
 * 
 * @author Erwin Vervaet
 */
public class StatefulActionProxyTests extends TestCase {
	
	public void testStatefulAction() throws Exception {
		MockRequestContext context = new MockRequestContext();
		context.setAttribute("actionId", "test");
		StatefulActionProxy proxy = new StatefulActionProxy();
		proxy.setBeanFactory(new XmlBeanFactory(new ClassPathResource("context.xml", getClass())));
		
		assertTrue(context.getFlowScope().size() == 0);
		
		context.setAttribute("method", new MethodSignature("increment"));
		proxy.execute(context);
		
		assertEquals(1, context.getFlowScope().size());
		TestAction testAction = (TestAction)context.getFlowScope().get("test");
		assertEquals(1, testAction.getCounter());
		proxy.execute(context);
		assertSame(testAction, context.getFlowScope().get("test"));
		assertEquals(2, testAction.getCounter());

		context.setAttribute("method", new MethodSignature("decrement"));
		proxy.execute(context);
		proxy.execute(context);
		proxy.execute(context);

		assertEquals(1, context.getFlowScope().size());
		assertSame(testAction, context.getFlowScope().get("test"));
		assertEquals(-1, testAction.getCounter());
	}
}
