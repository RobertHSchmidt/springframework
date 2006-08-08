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
package org.springframework.webflow.core.collection.support;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;

/**
 * A generic, mutable attribute map with string keys.
 * 
 * @author Keith Donald
 */
public class LocalAttributeMap extends AbstractAttributeMap implements MutableAttributeMap, Serializable {

	/**
	 * Creates a new attribute map, initially empty.
	 */
	public LocalAttributeMap() {
		initAttributes(createTargetMap());
	}

	/**
	 * Creates a new attribute map, initially empty.
	 */
	public LocalAttributeMap(int size, int loadFactor) {
		initAttributes(createTargetMap(size, loadFactor));
	}

	/**
	 * Creates a new attribute map with a single entry.
	 */
	public LocalAttributeMap(String attributeName, Object attributeValue) {
		initAttributes(createTargetMap(1, 1));
		put(attributeName, attributeValue);
	}
	
	/**
	 * Creates a new attribute map wrapping the specified map.
	 */
	public LocalAttributeMap(Map map) {
		Assert.notNull(map, "The target map is required");
		initAttributes(map);
	}

	// implementing attribute map
	
	public AttributeMap union(AttributeMap attributes) {
		if (attributes == null) {
			return new LocalAttributeMap(getMapInternal());
		}
		else {
			Map map = createTargetMap();
			map.putAll(getMapInternal());
			map.putAll(attributes.getMap());
			return new LocalAttributeMap(map);
		}
	}

	// mutators
	
	public Object put(String attributeName, Object attributeValue) {
		return getMapInternal().put(attributeName, attributeValue);
	}

	public MutableAttributeMap putAll(AttributeMap attributes) {
		if (attributes == null) {
			return this;
		}
		getMapInternal().putAll(attributes.getMap());
		return this;
	}

	public Object remove(String attributeName) {
		return getMapInternal().remove(attributeName);
	}

	/**
	 * Clear the attributes in this map.
	 * @throws UnsupportedOperationException clear is not supported
	 * @return this, to support call chaining
	 */
	public LocalAttributeMap clear() throws UnsupportedOperationException {
		getMapInternal().clear();
		return this;
	}

	public MutableAttributeMap replaceWith(AttributeMap attributes) throws UnsupportedOperationException {
		clear();
		putAll(attributes);
		return this;
	}
	
	// helpers

	/**
	 * Factory method that returns the target map storing the data in this
	 * attribute map.
	 * @return the target map
	 */
	protected Map createTargetMap() {
		return new HashMap();
	}

	/**
	 * Factory method that returns the target map storing the data in this
	 * attribute map.
	 * @return the target map
	 */
	protected Map createTargetMap(int size, int loadFactor) {
		return new HashMap(size, loadFactor);
	}

	public boolean equals(Object o) {
		if (!(o instanceof LocalAttributeMap)) {
			return false;
		}
		LocalAttributeMap other = (LocalAttributeMap)o;
		return getMapInternal().equals(other.getMapInternal());
	}
	
	public int hashCode() {
		return getMapInternal().hashCode();
	}
}