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
package org.springframework.binding.expression.support;

import org.springframework.binding.expression.ExpressionParser;

/**
 * Static utilities dealing with <code>ExpressionParser</code>s.
 * 
 * @see org.springframework.binding.expression.ExpressionParser
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class ExpressionParserUtils {

	private static ExpressionParser defaultExpressionParser;

	public static void load(ExpressionParser defaultInstance) {
		defaultExpressionParser = defaultInstance;
	}
	
	/**
	 * Utility method that checks which expression parsers are available
	 * on the classpath and returns the appropriate default one.
	 */
	public static synchronized ExpressionParser getDefaultExpressionParser() {
		if (defaultExpressionParser == null) {
			try {
				Class.forName("ognl.Ognl");
				defaultExpressionParser = new OgnlExpressionParser();
			}
			catch (ClassNotFoundException e) {
				IllegalStateException ise =
					new IllegalStateException("Unable to access the default expression parser: OGNL could not be found in the classpath.  " + 
							"Please add OGNL to your classpath or set the default ExpressionParser instance to something that is in the classpath.  " + 
							"Details: " + e.getMessage());
				throw ise;
			}
		}
		return defaultExpressionParser;
	}	
}