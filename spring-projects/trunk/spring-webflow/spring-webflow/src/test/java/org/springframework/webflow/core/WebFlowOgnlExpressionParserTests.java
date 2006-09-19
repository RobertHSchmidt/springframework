package org.springframework.webflow.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.binding.collection.MapAdaptable;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.PropertyExpression;
import org.springframework.webflow.core.collection.LocalAttributeMap;

public class WebFlowOgnlExpressionParserTests extends TestCase {
	WebFlowOgnlExpressionParser parser = new WebFlowOgnlExpressionParser();

	public void testEvalSimpleExpression() {
		ArrayList list = new ArrayList();
		Expression exp = parser.parseExpression("size()");
		Integer size = (Integer)exp.evaluateAgainst(list, null);
		assertEquals(0, size.intValue());
	}

	public void testEvalMapAdaptable() {
		MapAdaptable adaptable = new MapAdaptable() {
			public Map asMap() {
				HashMap map = new HashMap();
				map.put("size", new Integer(0));
				return map;
			}
		};
		Expression exp = parser.parseExpression("size");
		Integer size = (Integer)exp.evaluateAgainst(adaptable, null);
		assertEquals(0, size.intValue());
	}
	
	public void testEvalAndSetMutableMap() {
		LocalAttributeMap map = new LocalAttributeMap();
		map.put("size", new Integer(0));
		Expression exp = parser.parseExpression("size");
		Integer size = (Integer)exp.evaluateAgainst(map, null);
		assertEquals(0, size.intValue());
		assertTrue(exp instanceof PropertyExpression);
		PropertyExpression pexp = (PropertyExpression)exp;
		pexp.setValue(map, new Integer(1), null);
		size = (Integer)exp.evaluateAgainst(map, null);
		assertEquals(1, size.intValue());
	}
}
