/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.engine.support;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.springframework.webflow.execution.ScopeType;
import org.springframework.webflow.test.MockRequestContext;

public class SimpleFlowVariableTests extends TestCase {
	private MockRequestContext context = new MockRequestContext();

	public void testCreateValidFlowVariableCustomScope() {
		SimpleFlowVariable variable = new SimpleFlowVariable("var", ArrayList.class, ScopeType.REQUEST);
		variable.create(context);
		assertTrue(context.getRequestScope().contains("var"));
		context.getRequestScope().getRequired("var", ArrayList.class);
	}
	
	public void testCreateVariableNoDefaultConstructor() {
		SimpleFlowVariable variable = new SimpleFlowVariable("var", Integer.class, ScopeType.FLOW);
		try {
			variable.create(context);
			fail("should have failed");
		} catch (Exception e) {
			
		}
	}
}
