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
package org.springframework.webflow.support;

import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.support.OgnlExpressionParser;

/**
 * Factory that creates instances of the default expression parser used by
 * Spring Web Flow when requested.
 * <p>
 * Returns {@link OgnlExpressionParser} by default. Also asserts that OGNL is in
 * the classpath when this class is loaded.
 * 
 * @author Keith Donald
 */
public class DefaultExpressionParserFactory {

	static {
		try {
			Class.forName("ognl.Ognl");
		}
		catch (ClassNotFoundException e) {
			throw new IllegalStateException(
					"Unable to access the default expression parser: OGNL could not be found in the classpath.  "
							+ "Please add OGNL to your classpath or set the default ExpressionParser instance to something that is in the classpath.  "
							+ "Details: " + e.getMessage());
		}
	}

	/**
	 * Returns the default expression parser.
	 * @return the expression parser
	 */
	public ExpressionParser getExpressionParser() {
		return new WebFlowOgnlExpressionParser();
	}
}