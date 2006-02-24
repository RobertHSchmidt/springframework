package org.springframework.webflow.support;

import java.util.Map;

import ognl.OgnlException;
import ognl.PropertyAccessor;

import org.springframework.binding.expression.support.OgnlExpressionParser;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.MapAdaptable;

public class WebFlowOgnlExpressionParser extends OgnlExpressionParser {

	public WebFlowOgnlExpressionParser() {
		addPropertyAccessor(MapAdaptable.class, new MapAdaptablePropertyAccessor());
		addPropertyAccessor(AttributeMap.class, new AttributeMapPropertyAccessor());
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