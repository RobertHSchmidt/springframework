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
package org.springframework.webflow.core;

import junit.framework.TestCase;

import org.springframework.binding.expression.ExpressionParser;

/**
 * Unit tests for {@link DefaultExpressionParserFactory}.
 */
public class DefaultExpressionParserFactoryTests extends TestCase {
	
	public void testGetDefaultExpressionParser() {
		DefaultExpressionParserFactory f = new DefaultExpressionParserFactory();
		ExpressionParser parser = f.getExpressionParser();
		assertNotNull(parser);
		assertTrue(parser instanceof WebFlowOgnlExpressionParser);
		assertSame(parser, f.getExpressionParser());
	}
}
