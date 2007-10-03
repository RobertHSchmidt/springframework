package org.springframework.binding.expression.el;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;

import junit.framework.TestCase;

import org.jboss.el.ExpressionFactoryImpl;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionVariable;

public class ELExpressionParserTests extends TestCase {

	private ELExpressionParser parser = new ELExpressionParser(new ExpressionFactoryImpl());

	public void setUp() {
		parser.putContextFactory(TestBean.class, new TestELContextFactory());
	}

	private static class TestELContextFactory implements ELContextFactory {
		public ELContext getELContext(final Object target, final VariableMapper variableMapper) {
			return new ELContext() {
				public ELResolver getELResolver() {
					return new DefaultELResolver(target);
				}

				public FunctionMapper getFunctionMapper() {
					return null;
				}

				public VariableMapper getVariableMapper() {
					return variableMapper;
				}
			};
		}
	}

	public void testParseEvalExpression() {
		String expressionString = "#{value}";
		Class expressionTargetType = TestBean.class;
		ExpressionVariable[] expressionVariables = null;
		boolean isAlwaysAnEvalExpression = true;
		Expression exp = parser.parseExpression(expressionString, expressionTargetType, expressionVariables,
				isAlwaysAnEvalExpression);
		TestBean target = new TestBean();
		assertEquals("foo", exp.getValue(target));
	}

	public void testParseLiteralExpressionStringAsEvalExpression() {
		String expressionString = "value";
		Class expressionTargetType = TestBean.class;
		ExpressionVariable[] expressionVariables = null;
		boolean isAlwaysAnEvalExpression = true;
		Expression exp = parser.parseExpression(expressionString, expressionTargetType, expressionVariables,
				isAlwaysAnEvalExpression);
		TestBean target = new TestBean();
		assertEquals("foo", exp.getValue(target));
	}

	public void testParseLiteralExpression() {
		String expressionString = "value";
		Class expressionTargetType = TestBean.class;
		ExpressionVariable[] expressionVariables = null;
		boolean isAlwaysAnEvalExpression = false;
		Expression exp = parser.parseExpression(expressionString, expressionTargetType, expressionVariables,
				isAlwaysAnEvalExpression);
		TestBean target = new TestBean();
		assertEquals("value", exp.getValue(target));
	}

	public void testParseExpressionWithVariables() {
		String expressionString = "#{value}#{max}";
		Class expressionTargetType = TestBean.class;
		ExpressionVariable[] expressionVariables = new ExpressionVariable[] { new ExpressionVariable("max",
				"#{maximum}") };
		boolean isAlwaysAnEvalExpression = false;
		Expression exp = parser.parseExpression(expressionString, expressionTargetType, expressionVariables,
				isAlwaysAnEvalExpression);
		TestBean target = new TestBean();
		assertEquals("foo2", exp.getValue(target));
	}

	public static class TestBean {
		private String value = "foo";

		private int maximum = 2;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {

		}

		public int getMaximum() {
			return maximum;
		}

		public void setMaximum(int maximum) {
			this.maximum = maximum;
		}

	}
}
