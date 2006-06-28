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
package org.springframework.webflow.action.annotation;

import java.util.Map;

import junit.framework.TestCase;

import org.springframework.binding.method.MethodSignature;
import org.springframework.webflow.action.LocalBeanInvokingAction;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit tests testing bean state persisters.
 * 
 * @author Keith Donald
 */
public class StateManagementTests extends TestCase {

	@Stateful(name = "bean")
	public static class Bean {
		private String datum1 = "initial value";

		private Integer datum2;

		@Transient
		private NotSerializable datum3;

		public void execute() {
		}
	}

	public static class NotSerializable extends Object {
	}

	public void testStateSavingAndRestoring() throws Exception {
		Bean bean = new Bean();
		LocalBeanInvokingAction action = new LocalBeanInvokingAction(new MethodSignature("execute"), bean);
		action.setBeanStatePersister(new AnnotationBeanStatePersister());
		MockRequestContext context = new MockRequestContext();
		action.execute(context);
		assertNotNull("Bean memento not saved", context.getFlowScope().get("bean"));
		@SuppressWarnings("unchecked")
		Map<String, Object> memento = (Map<String, Object>)context.getFlowScope().get("bean");
		assertEquals("initial value", memento.get("datum1"));
		assertFalse(memento.containsKey("datum3"));
		memento.put("datum1", "new value");
		memento.put("datum2", new Integer(12345));
		action.execute(context);
		assertEquals("Wrong value", "new value", bean.datum1);
		assertEquals("Wrong value", new Integer(12345), bean.datum2);
		assertNull("Wrong value", bean.datum3);
	}
}