package org.springframework.binding.expression.support;

import java.util.Collections;

import junit.framework.TestCase;

import org.springframework.binding.expression.Expression;

public class OgnlExpressionParserTests extends TestCase {
	private OgnlExpressionParser parser = new OgnlExpressionParser();

	private TestBean bean = new TestBean();
	
	public void testParseSimpleDelimited() {
		String exp = "${flag}";
		Expression e = parser.parseExpression(exp);
		assertNotNull(e);
		Boolean b = (Boolean)e.evaluateAgainst(bean, Collections.EMPTY_MAP);
		assertFalse(b.booleanValue());
	}
	
	public void testParseSimple() {
		String exp = "flag";
		Expression e = parser.parseExpression(exp);
		assertNotNull(e);
		Boolean b = (Boolean)e.evaluateAgainst(bean, Collections.EMPTY_MAP);
		assertFalse(b.booleanValue());
	}
	
	public void testParseNull() {
		Expression e = parser.parseExpression(null);
		assertNotNull(e);
		assertNull(e.evaluateAgainst(bean, Collections.EMPTY_MAP));
	}
	
	public void testParseEmpty() {
		Expression e = parser.parseExpression("");
		assertNotNull(e);
		assertEquals("", e.evaluateAgainst(bean, Collections.EMPTY_MAP));
	}

	public void testEnclosedComposite() {
		String exp = "${hello ${flag} ${flag} ${flag}}";
		Expression e = parser.parseExpression(exp);
		assertNotNull(e);
		String str = (String)e.evaluateAgainst(bean, Collections.EMPTY_MAP);
		assertEquals("hello false false false", str);
	}

	public void testParseComposite() {
		String exp = "hello ${flag} ${flag} ${flag}";
		Expression e = parser.parseExpression(exp);
		assertNotNull(e);
		String str = (String)e.evaluateAgainst(bean, Collections.EMPTY_MAP);
		assertEquals("hello false false false", str);
	}
	
	public void testIsDelimitedExpression() {
		assertTrue(parser.isDelimitedExpression("${foo}"));
		assertTrue(parser.isDelimitedExpression("${foo ${foo}}"));
	}
	
	public void testIsNotDelimitedExpression() {
		assertFalse(parser.isDelimitedExpression("foo"));
		assertFalse(parser.isDelimitedExpression("foo ${bar}"));
	}
}