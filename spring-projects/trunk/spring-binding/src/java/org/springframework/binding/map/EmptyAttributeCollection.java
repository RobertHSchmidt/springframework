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
package org.springframework.binding.map;

import java.io.Serializable;
import java.util.Map;

/**
 * An attribute collection with no entries.
 * @author Keith Donald
 */
public class EmptyAttributeCollection implements AttributeCollection, Serializable {

	/**
	 * The shared, singleton empty attribute collection instance.
	 */
	public static final AttributeCollection INSTANCE = new EmptyAttributeCollection();

	private EmptyAttributeCollection() {

	}

	public int size() {
		return UnmodifiableAttributeMap.EMPTY_MAP.size();
	}

	public Object get(String attributeName) {
		return UnmodifiableAttributeMap.EMPTY_MAP.get(attributeName);
	}

	public Map getMap() {
		return UnmodifiableAttributeMap.EMPTY_MAP.getMap();
	}

	public UnmodifiableAttributeMap unmodifiable() {
		return UnmodifiableAttributeMap.EMPTY_MAP;
	}
}