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
package org.springframework.webflow.support.convert;

import java.util.HashMap;

import org.springframework.binding.expression.Expression;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.test.MockRequestContext;

import junit.framework.TestCase;

/**
 * Test case for BaseConverter.
 * 
 * @author Erwin Vervaet
 */
public class BaseConverterTests extends TestCase {
	
	private BaseConverter converter = new BaseConverter() {
		public Class[] getSourceClasses() {
			return null;
		}
		public Class[] getTargetClasses() {
			return null;
		}
		protected Object doConvert(Object source, Class targetClass) throws Exception {
			return source;
		}
	};
	
	public void testParseExpressions() throws Exception {
		Expression[] expressions = converter.parseExpressions("");
		assertEquals(0, expressions.length);
		assertEquals("", evaluateExpressions(expressions));

		expressions = converter.parseExpressions(null);
		assertEquals(0, expressions.length);
		assertEquals("", evaluateExpressions(expressions));
		
		expressions = converter.parseExpressions("foo");
		assertEquals(1, expressions.length);
		assertEquals("foo", evaluateExpressions(expressions));

		expressions = converter.parseExpressions("${flowScope.foo}");
		assertEquals(1, expressions.length);
		assertEquals("foo", evaluateExpressions(expressions));

		expressions = converter.parseExpressions("bar${flowScope.foo}");
		assertEquals(2, expressions.length);
		assertEquals("barfoo", evaluateExpressions(expressions));

		expressions = converter.parseExpressions("${flowScope.foo}bar");
		assertEquals(2, expressions.length);
		assertEquals("foobar", evaluateExpressions(expressions));
		
		expressions = converter.parseExpressions("bar${flowScope.foo}bol");
		assertEquals(3, expressions.length);
		assertEquals("barfoobol", evaluateExpressions(expressions));

		expressions = converter.parseExpressions("bar${flowScope.foo}bol${flowScope.coo}");
		assertEquals(4, expressions.length);
		assertEquals("barfoobolcoo", evaluateExpressions(expressions));

		expressions = converter.parseExpressions("bar${flowScope.foo}${flowScope.coo}bol");
		assertEquals(4, expressions.length);
		assertEquals("barfoocoobol", evaluateExpressions(expressions));
		
		expressions = converter.parseExpressions("bar${flowScope.foo");
		assertEquals(2, expressions.length);
		assertEquals("bar${flowScope.foo", evaluateExpressions(expressions));

		expressions = converter.parseExpressions("flowScope.foo}bol");
		assertEquals(1, expressions.length);
		assertEquals("flowScope.foo}bol", evaluateExpressions(expressions));
	}

	private String evaluateExpressions(Expression[] expressions) {
		RequestContext context = new MockRequestContext();
		context.getFlowScope().setAttribute("foo", "foo");
		context.getFlowScope().setAttribute("coo", "coo");
		StringBuffer res = new StringBuffer();
		for (int i=0; i<expressions.length; i++) {
			res.append(expressions[i].evaluateAgainst(context, new HashMap()));
		}
		return res.toString();
	}
}
