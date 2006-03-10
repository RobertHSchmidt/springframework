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
import org.springframework.binding.method.MethodSignature;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit test for the bean factory bean invoking action.
 * @author Keith Donald
 */
public class LocalBeanInvokingActionTests extends TestCase {

	private TestBean bean = new TestBean();
	
	private LocalBeanInvokingAction action = new LocalBeanInvokingAction(bean);
	
	private MockRequestContext context = new MockRequestContext();
	
	public void setUp() {
		context.setAttribute("method", new MethodSignature("execute"));
		context.setAttribute("bean", "bean");
	}
	
	public void testInvokeBean() throws Exception {
		action.execute(context);
		assertTrue(bean.executed);
	}

	public void testNullTargetBean() throws Exception {
		try {
			action = new LocalBeanInvokingAction(null);
			fail("Should've failed with iae");
		} catch (IllegalArgumentException e) {
			
		}
	}

	public void testInvokeNoSuchBean() throws Exception {
		try {
			action.execute(context);
		} catch (NoSuchBeanDefinitionException e) {
			
		}
	}
}