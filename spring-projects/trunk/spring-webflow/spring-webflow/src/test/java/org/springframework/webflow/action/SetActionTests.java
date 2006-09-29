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
package org.springframework.webflow.action;

import junit.framework.TestCase;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.PropertyExpression;
import org.springframework.webflow.TestBean;
import org.springframework.webflow.core.DefaultExpressionParserFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.ScopeType;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit tests for {@link SetAction}.
 */
public class SetActionTests extends TestCase {
	
	private ExpressionParser parser = new DefaultExpressionParserFactory().getExpressionParser();

	private MockRequestContext context = new MockRequestContext();

	protected void setUp() throws Exception {
		context.getFlowScope().put("foo", "bar");
		context.getFlowScope().put("bean", new TestBean());
	}

	public void testExecuteSetAction() throws Exception {
		PropertyExpression attr = parser.parsePropertyExpression("bean.executed");
		Expression value = parser.parseExpression("true");
		SetAction action = new SetAction(attr, ScopeType.FLOW, value);
		Event outcome = action.execute(context);
		assertEquals("success", outcome.getId());
		assertEquals(true, ((TestBean)context.getFlowScope().get("bean")).executed);
	}
}
