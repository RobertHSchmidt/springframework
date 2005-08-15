/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.binding.support;

import java.util.Collection;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.binding.AttributeSource;
import org.springframework.util.ObjectUtils;

/**
 * Support class for generally useful data binding related assertions.
 * 
 * @author Keith Donald
 */
public class Assert extends org.springframework.util.Assert {

	/**
	 * Assert that an attribute with specified name is present in given attributes.
	 */
	public static void attributePresent(AttributeSource attributes, String attributeName) {
		isTrue(attributes.containsAttribute(attributeName), "The attribute '" + attributeName
				+ "' is not present but should be, present attributes are: " + attributes);
	}

	/**
	 * Assert that an attribute with specified name is not present in given
	 * attributes map.
	 */
	public static void attributeNotPresent(AttributeSource attributes, String attributeName) {
		isTrue(!attributes.containsAttribute(attributeName), "The attribute '" + attributeName
				+ "' is present but shouldn't be");
	}

	/**
	 * Assert that an attribute exists in the attributes map of the specified type.
	 * @param attributes the attributes map
	 * @param attributeName the attribute name
	 * @param clazz the required type
	 */
	public static void attributeInstanceOf(AttributeSource attributes, String attributeName, Class clazz) {
		attributePresent(attributes, attributeName);
		isInstanceOf(clazz, attributes.getAttribute(attributeName));
	}

	/**
	 * Assert that an attribute exists in the attributes map of the specified value.
	 * @param attributes the attributes map
	 * @param attributeName the attribute name
	 * @param attributeValue the attribute value
	 */
	public static void attributeEquals(AttributeSource attributes, String attributeName, Object attributeValue) {
		if (attributeValue != null) {
			attributeInstanceOf(attributes, attributeName, attributeValue.getClass());
		}
		valueEquals(attributes.getAttribute(attributeName), attributeValue);
	}
	
	/**
	 * Assert that a value equals an expected value
	 * @param value the actual value
	 * @param expected the expected value
	 */
	public static void valueEquals(Object value, Object expected) {
		Assert.isTrue(ObjectUtils.nullSafeEquals(expected, value),
				"The attribute '" + value + "' must equal '" + expected + "'");
	}

	/**
	 * Assert that a collection exists in the attributes map under the provided
	 * attribute name, with the specified size.
	 * @param attributes the attributes map
	 * @param attributeName the attribute name
	 * @param size the expected collection size
	 */
	public static void collectionAttributeSizeEquals(AttributeSource attributes, String attributeName, int size) {
		attributeInstanceOf(attributes, attributeName, Collection.class);
		Assert.isTrue((((Collection)attributes.getAttribute(attributeName)).size() == size),
				"The collection attribute '" + attributeName + "' must have " + size + " elements");
	}

	/**
	 * Assert that a bean attribute in the attributes map has a property
	 * with the provided property value.
	 * @param attributes the attributes map
	 * @param attributeName the attribute name (of a javabean)
	 * @param propertyName the bean property name
	 * @param propertyValue the expected property value
	 */
	public static void attributePropertyEquals(AttributeSource attributes, String attributeName, String propertyName,
			Object propertyValue) {
		attributePresent(attributes, attributeName);
		Object value = attributes.getAttribute(attributeName);
		org.springframework.util.Assert.isTrue(!BeanUtils.isSimpleProperty(value.getClass()),
				"Attribute value must be a bean");
		BeanWrapper wrapper = new BeanWrapperImpl(value);
		valueEquals(wrapper.getPropertyValue(propertyName), propertyValue);
	}
}