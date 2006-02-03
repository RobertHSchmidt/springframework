/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.binding.util;

import java.util.Map;

/**
 * A simple helper for getting attributes out of a map. May be instantiated
 * directly or used as a base class as a convenience.
 * 
 * @author Keith Donald
 */
public class AttributeMapAccessorSupport {

	/**
	 * Returns a boolean attribute value in the map, returning the defaultValue
	 * if no value was found.
	 * @param attributeName the attribute name
	 * @param attributes the attribute map
	 * @param defaultValue the default
	 * @return the boolean attribute value
	 */
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