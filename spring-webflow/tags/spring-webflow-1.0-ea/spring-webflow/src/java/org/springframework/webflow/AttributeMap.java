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
package org.springframework.webflow;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * A generic, mutable attribute map with string keys.
 * 
 * @author Keith Donald
 */
public class AttributeMap extends AbstractAttributeMap {

	/**
	 * Creates a new attribute map, initially empty.
	 */
	public AttributeMap() {
		initAttributes(new HashMap());
	}

	/**
	 * Creates a new attribute map of the specified size.
	 */
	public AttributeMap(int size) {
		initAttributes(new HashMap(size));
	}

	/**
	 * Creates a new attribute map of the specified size and loadFactor.
	 */
	public AttributeMap(int size, int loadFactor) {
		initAttributes(new HashMap(size, loadFactor));
	}

	/**
	 * Creates a new attribute map from the provided attribute map.
	 */
	public AttributeMap(AttributeCollection attributes) {
		Assert.notNull(attributes, "The target attribute collection is required");
		initAttributes(new HashMap(attributes.getMap()));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.binding.util.AttributesSetter#setAttribute(java.lang.String,
	 * java.lang.Object)
	 */
	public Object put(String attributeName, Object attributeValue) {
		return getMapInternal().put(attributeName, attributeValue);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.binding.attribute.AttributeCollection#unmodifiable()
	 */
	public UnmodifiableAttributeMap unmodifiable() {
		return new UnmodifiableAttributeMap(getMap());
	}

	/**
	 * Creates a new attribute map wrapping the specified map.
	 */
	public AttributeMap(Map map) {
		Assert.notNull(map, "The target map is required");
		initAttributes(map);
	}

	/**
	 * Put all the attributes into this scope.
	 * @param attributes the attributes to put into this scope.
	 * @return this, to support call chaining.
	 */
	public AttributeMap putAll(AttributeCollection attributes) {
		if (attributes == null) {
			return this;
		}
		getMapInternal().putAll(attributes.getMap());
		return this;
	}

	/**
	 * Remove an attribute from this scope.
	 * @param attributeName the name of the attribute to remove
	 * @return previous value associated with specified attribute name, or
	 * <tt>null</tt> if there was no mapping for the name
	 */
	public Object remove(String attributeName) {
		return getMapInternal().remove(attributeName);
	}

	/**
	 * Clear the attributes in this map.
	 * @throws UnsupportedOperationException clear is not supported
	 * @return this, to support call chaining
	 */
	public AttributeMap clear() throws UnsupportedOperationException {
		getMapInternal().clear();
		return this;
	}

	/**
	 * Replace the contents of this attribute map with the contents of the
	 * provided collection.
	 * @param attributes the attribute collection
	 * @return this, to support call chaining
	 */
	public AttributeMap replaceWith(AttributeCollection attributes) throws UnsupportedOperationException {
		clear();
		putAll(attributes);
		return this;
	}

	/**
	 * Merge the attributes in the provided map into the attributes in this map,
	 * and returning a copy containing the union.
	 * @param attributes the attributes to merge in
	 * @return a new attribute map, the union of this map and the provided map.
	 */
	public AttributeMap union(AttributeCollection attributes) {
		Map map = new HashMap(size() + attributes.size(), 1);
		map.putAll(getMap());
		map.putAll(attributes.getMap());
		return new AttributeMap(map);
	}

	/**
	 * Factory method to return the default attribute map used by this scope.
	 */
	protected Map createAttributeMap() {
		return new HashMap();
	}
}