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

import java.util.Map;

/**
 * An interface for objects that can get attribute values.
 * @author Keith Donald
 */
public interface AttributeCollection {

	/**
	 * Returns the number of attributes in this collection.
	 * 
	 * @return the number of attributes in this collection
	 */
	public int getAttributeCount();

	/**
	 * Get an attribute value out of this collection, returning
	 * <code>null</code> if not found.
	 * @param attributeName the attribute name
	 * @return the attribute value
	 */
	public Object getAttribute(String attributeName);

	/**
	 * Returns this collection as a {@link Map}.  The returned map cannot be modified.
	 * @return the attribute collection as as map.
	 */
	public Map getMap();
	
	/**
	 * Returns this collection as an {@link UnmodifiableAttributeMap}.
	 * @return the attribute collection as an unmodifiable map.
	 */
	public UnmodifiableAttributeMap unmodifiable();
}