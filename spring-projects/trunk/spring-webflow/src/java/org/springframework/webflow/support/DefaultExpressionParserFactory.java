package org.springframework.webflow.support;

import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.support.OgnlExpressionParser;

public class DefaultExpressionParserFactory extends OgnlExpressionParser {

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

	public ExpressionParser getExpressionParser() {
		return new WebFlowOgnlExpressionParser();
	}	
}