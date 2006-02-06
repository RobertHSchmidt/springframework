/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.core.style.StylerUtils;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * Holder for data placed in a specific scope, for example "request scope" or
 * "flow scope". Clients should invoke operations on this class to access
 * attributes placed in a specific scope by <code>attributeName</code>.
 * <p>
 * This class is simply a thin wrapper around a <code>java.util.HashMap</code>.
 * <p>
 * Usage example:
 * 
 * <pre>
 * context.getFlowScope().getAttribute(&quot;foo&quot;);
 * context.getFlowScope().setAttribute(&quot;foo&quot;, &quot;bar&quot;);
 * </pre>
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class Scope implements Map, Serializable {

	/**
	 * Serialization id.
	 */
	private static final long serialVersionUID = -8075142903027393405L;

	/**
	 * The data holder map.
	 */
	private Map attributes = Collections.EMPTY_MAP;

	/**
	 * Creates a new scope, initially empty.
	 */
	public Scope() {
		
	}
	
	/**
	 * Creates a new scope based initially on the attributes provided.
	 * @param attributes the attribute map
	 */
	public Scope(Map attributes) {
		if (attributes != null) {
			this.attributes = attributes;
		}
	}
	
	/**
	 * Does the attribute with the provided name exist in this scope?
	 * @param attributeName the attribute name
	 * @return true if so, false otherwise
	 */
	public boolean containsAttribute(String attributeName) {
		return attributes.containsKey(attributeName);
	}

	/**
	 * Does the attribute with the provided name exist in this scope, and is its
	 * value of the specified class?
	 * @param attributeName the attribute name
	 * @param requiredType the required class of the attribute value
	 * @return true if so, false otherwise
	 */
	public boolean containsAttribute(String attributeName, Class requiredType) {
		if (containsAttribute(attributeName)) {
			Assert.notNull(requiredType, "The required type to assert is required");
			return requiredType.isInstance(attributes.get(attributeName));
		}
		else {
			return false;
		}
	}

	/**
	 * Get an attribute value, returning <code>null</code> if not found.
	 * @param attributeName the attribute name
	 * @return the value
	 */
	public Object getAttribute(String attributeName) {
		return attributes.get(attributeName);
	}

	/**
	 * Get an attribute value and make sure it is of the required type.
	 * @param attributeName name of the attribute to get
	 * @param requiredType the required type of the attribute value
	 * @return the attribute value, or null if not found
	 * @throws IllegalStatetException when the value is not of the required type
	 */
	public Object getAttribute(String attributeName, Class requiredType) throws IllegalStateException {
		Object value = getAttribute(attributeName);
		if (value != null) {
			Assert.notNull(requiredType, "The required type to assert is required");
			Assert.isInstanceOf(requiredType, value);
		}
		return value;
	}

	/**
	 * Get the value of a required attribute.
	 * @param attributeName name of the attribute to get
	 * @return the attribute value
	 * @throws IllegalStateException when the attribute is not found
	 */
	public Object getRequiredAttribute(String attributeName) throws IllegalStateException {
		Object value = getAttribute(attributeName);
		if (value == null) {
			throw new IllegalStateException("Required attribute '" + attributeName + "' is not present in " + this
					+ "; attributes present are " + StylerUtils.style(getAttributeMap()));
		}
		return value;
	}

	/**
	 * Get the value of a required attribute and make sure it is of the required
	 * type.
	 * @param attributeName name of the attribute to get
	 * @param requiredType the required type of the attribute value
	 * @return the attribute value
	 * @throws IllegalStateException when the attribute is not found or not of
	 * the required type
	 */
	public Object getRequiredAttribute(String attributeName, Class requiredType) throws IllegalStateException {
		Assert.notNull(requiredType, "The required type to assert is required");
		Object value = getRequiredAttribute(attributeName);
		Assert.isInstanceOf(requiredType, value);
		return value;
	}

	/**
	 * Returns the contents of this scope as an unmodifiable map.
	 */
	public Map getAttributeMap() {
		if (attributes == Collections.EMPTY_MAP) {
			return attributes;
		} else {
			return Collections.unmodifiableMap(attributes);
		}
	}

	/**
	 * Sets the attribute to the value provided in this scope.
	 * @param attributeName the attribute name
	 * @param attributeValue the attribute value
	 * @return the previous attribute value, or <code>null</code> if there was
	 * no previous value set
	 */
	public Object setAttribute(String attributeName, Object attributeValue) {
		if (attributes == Collections.EMPTY_MAP) {
			attributes = createAttributeMap();
		}
		return attributes.put(attributeName, attributeValue);
	}

	/**
	 * Factory method to return the default attribute map used by this scope.
	 */
	protected Map createAttributeMap() {
		return new HashMap();
	}
	
	/**
	 * Set all given attributes in this scope.
	 */
	public void setAttributes(Map attributes) {
		Iterator it = attributes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			Assert.isInstanceOf(String.class, entry.getKey());
			setAttribute((String)entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Remove an attribute from this scope.
	 * @param attributeName the name of the attribute to remove
	 * @return previous value associated with specified attribute name, or
	 * <tt>null</tt> if there was no mapping for the name
	 */
	public Object removeAttribute(String attributeName) {
		if (attributes == null) {
			return null;
		}
		return attributes.remove(attributeName);
	}

	// implementing Map

	public int size() {
		return attributes.size();
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public boolean containsKey(Object key) {
		return containsAttribute(String.valueOf(key));
	}

	public boolean containsValue(Object value) {
		return attributes.containsValue(value);
	}

	public Object get(Object key) {
		return getAttribute(String.valueOf(key));
	}

	public Object put(Object key, Object value) {
		return setAttribute(String.valueOf(key), value);
	}

	public Object remove(Object key) {
		return removeAttribute(String.valueOf(key));
	}

	public void putAll(Map attributes) {
		if (this.attributes == Collections.EMPTY_MAP) {
			this.attributes = new HashMap(attributes);
		} else {
			this.attributes.putAll(attributes);
		}
	}

	public void clear() {
		attributes.clear();
	}

	public Set keySet() {
		return attributes.keySet();
	}

	public Collection values() {
		return attributes.values();
	}

	public Set entrySet() {
		return attributes.entrySet();
	}

	public int hashCode() {
		return attributes.hashCode();
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof Scope)) {
			return false;
		}
		Scope other = (Scope)o;
		return attributes.equals(other.attributes);
	}
	
	public String toString() {
		return new ToStringCreator(this).append("attributes", attributes).toString();
	}
}