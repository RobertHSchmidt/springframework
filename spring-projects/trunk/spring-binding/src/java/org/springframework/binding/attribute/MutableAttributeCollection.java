/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.binding.attribute;

/**
 * An interface for objects that can set attribute values.
 * 
 * @author Keith Donald
 */
public interface MutableAttributeCollection extends AttributeCollection {

	/**
	 * Sets the attribute to the value provided in this scope.
	 * @param attributeName the attribute name
	 * @param attributeValue the attribute value
	 * @return the previous attribute value, or <code>null</code> if there was
	 * no previous value set
	 */
	public Object setAttribute(String attributeName, Object attributeValue);

}