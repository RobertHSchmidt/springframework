package org.springframework.binding.util;

import java.util.Map;

public class AttributeMapAccessorSupport {

	public boolean getBooleanAttribute(String attributeName, Map attributes, boolean defaultValue) {
		if (attributes == null) {
			return defaultValue;
		}
		Object o = attributes.get(attributeName);
		if (o == null) {
			return defaultValue;
		}
		assertTypeOf(attributeName, o, Boolean.class);
		return ((Boolean)o).booleanValue();
	}

	private void assertTypeOf(String attributeName, Object attributeValue, Class type) {
		if (!type.isInstance(attributeValue)) {
			throw new IllegalArgumentException("Attribute '" + attributeName + "' has value [" + attributeValue
					+ "] that is not of expected type [" + type + "], instead it is of type ["
					+ attributeValue.getClass() + "]");
		}
	}
}
