package org.springframework.webflow.support;

import junit.framework.TestCase;

import org.springframework.binding.expression.ExpressionParser;
import org.springframework.webflow.core.DefaultExpressionParserFactory;
import org.springframework.webflow.core.WebFlowOgnlExpressionParser;

public class DefaultExpressionParserFactoryTests extends TestCase {
	public void testGetDefaultExpressionParser() {
		DefaultExpressionParserFactory f = new DefaultExpressionParserFactory();
		ExpressionParser parser = f.getExpressionParser();
		assertNotNull(parser);
		assertTrue(parser instanceof WebFlowOgnlExpressionParser);
		assertSame(parser, f.getExpressionParser());
	}
}
