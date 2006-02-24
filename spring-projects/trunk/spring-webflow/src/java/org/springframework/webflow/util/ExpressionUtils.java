package org.springframework.webflow.util;

import java.util.Map;

import ognl.OgnlException;
import ognl.PropertyAccessor;

import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.support.OgnlExpressionParser;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.MapAdaptable;

/**
 * Static utilities dealing with <code>ExpressionParser</code>s.
 * 
 * @see org.springframework.binding.expression.ExpressionParser
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class ExpressionUtils {

	private static ExpressionParser defaultExpressionParser;

	public static void load(ExpressionParser defaultInstance) {
		defaultExpressionParser = defaultInstance;
	}

	/**
	 * Utility method that checks which expression parsers are available on the
	 * classpath and returns the appropriate default one.
	 */
	public static synchronized ExpressionParser getDefaultExpressionParser() {
		if (defaultExpressionParser == null) {
			try {
				Class.forName("ognl.Ognl");
				OgnlExpressionParser parser = new OgnlExpressionParser();
				parser.addPropertyAccessor(MapAdaptable.class, new MapAdaptablePropertyAccessor());
				parser.addPropertyAccessor(AttributeMap.class, new AttributeMapPropertyAccessor());
				defaultExpressionParser = parser;
			}
			catch (ClassNotFoundException e) {
				IllegalStateException ise = new IllegalStateException(
						"Unable to access the default expression parser: OGNL could not be found in the classpath.  "
								+ "Please add OGNL to your classpath or set the default ExpressionParser instance to something that is in the classpath.  "
								+ "Details: " + e.getMessage());
				throw ise;
			}
		}
		return defaultExpressionParser;
	}
	
	private static class MapAdaptablePropertyAccessor implements PropertyAccessor {
		public Object getProperty(Map context, Object target, Object name) throws OgnlException {
			return ((MapAdaptable)target).getMap().get(name);
		}

		public void setProperty(Map context, Object target, Object name, Object value) throws OgnlException {
			throw new UnsupportedOperationException(
					"Cannot mutate immutable attribute collections; operation disallowed");
		}
	}

	private static class AttributeMapPropertyAccessor extends MapAdaptablePropertyAccessor {
		public void setProperty(Map context, Object target, Object name, Object value) throws OgnlException {
			((AttributeMap)target).put((String)name, value);
		}
	}
}