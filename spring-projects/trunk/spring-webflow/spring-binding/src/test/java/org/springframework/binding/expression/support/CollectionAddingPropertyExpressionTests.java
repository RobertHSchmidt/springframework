package org.springframework.binding.expression.support;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;

public class CollectionAddingPropertyExpressionTests extends TestCase {

	ExpressionParser parser = new BeanWrapperExpressionParser();

	TestBean bean = new TestBean();

	Expression exp = parser.parseExpression("collection");

	public void testEvaluation() {
		ArrayList list = new ArrayList();
		bean.setList(list);
		CollectionAddingPropertyExpression colExp = new CollectionAddingPropertyExpression(exp);
		assertSame(list, colExp.evaluateAgainst(bean, null));
	}

	public void testAddToCollection() {
		CollectionAddingPropertyExpression colExp = new CollectionAddingPropertyExpression(exp);
		colExp.setValue(bean, "1", null);
		colExp.setValue(bean, "2", null);
		assertEquals("1", bean.getList().get(0));
		assertEquals("2", bean.getList().get(1));
	}
	
	public void testNotACollection() {
		Expression exp = parser.parseExpression("flag");		
		CollectionAddingPropertyExpression colExp = new CollectionAddingPropertyExpression(exp);
		try {
			colExp.setValue(bean, "1", null);
			fail("not a collection");
		} catch (IllegalArgumentException e) {
			
		}
	}
	
	public void testNoAddOnNullValue() {
		CollectionAddingPropertyExpression colExp = new CollectionAddingPropertyExpression(exp);
		colExp.setValue(bean, null, null);
		colExp.setValue(bean, "2", null);
		assertEquals("2", bean.getList().get(0));
	}
}