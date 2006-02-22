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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.springframework.binding.util.MapAccessor;
import org.springframework.core.style.StylerUtils;

/**
 * A base class for map decorators who manage the storage of String-keyed
 * attributes in a backing {@link Map} implementation. This base provides
 * convenient operations for accessing attributes in a typed-manner.
 * 
 * @author Keith Donald
 */
public abstract class AbstractAttributeMap implements AttributeCollection, Serializable {

	/**
	 * The backing map storing the attributes.
	 */
	private Map attributes;

	/**
	 * A helper for accessing attributes. Marked transient and restored on
	 * deserialization.
	 */
	private transient MapAccessor attributesAccessor;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.binding.attribute.AttributeCollection#getMap()
	 */
	public Map getMap() {
		return attributesAccessor.getMap();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.binding.attribute.AttributeCollection#getAttributesCount()
	 */
	public int getAttributeCount() {
		return attributes.size();
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
	public boolean containsAttribute(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributesAccessor.containsKey(attributeName, requiredType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.binding.util.AttributesGetter#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String attributeName) {
		return attributes.get(attributeName);
	}

	/**
	 * Get an attribute value, returning the defaultValue if no value is found.
	 * @param attributeName the attribute name
	 * @param defaultValue the default value
	 * @return the attribute value
	 */
	public Object getAttribute(String attributeName, Object defaultValue) throws IllegalArgumentException {
		return attributesAccessor.get(attributeName, defaultValue);
	}

	/**
	 * Get an attribute value and make sure it is of the required type.
	 * @param attributeName name of the attribute to get
	 * @param requiredType the required type of the attribute value
	 * @return the attribute value, or null if not found
	 * @throws IllegalStateException when the value is not of the required type
	 */
	public Object getAttribute(String attributeName, Class requiredType) throws IllegalStateException {
		return attributesAccessor.get(attributeName, requiredType);
	}

	/**
	 * Get an attribute value and make sure it is of the required type,
	 * returning the default if not found.
	 * @param attributeName name of the attribute to get
	 * @param requiredType the required type of the attribute value
	 * @param defaultValue the default value
	 * @return the attribute value, or the default if not found
	 * @throws IllegalStateException when the value is not of the required type
	 */
	public Object getAttribute(String attributeName, Class requiredType, Object defaultValue)
			throws IllegalStateException {
		return attributesAccessor.get(attributeName, requiredType, defaultValue);
	}

	/**
	 * Get the value of a required attribute.
	 * @param attributeName name of the attribute to get
	 * @return the attribute value
	 * @throws IllegalStateException when the attribute is not found
	 */
	public Object getRequiredAttribute(String attributeName) throws IllegalStateException {
		return attributesAccessor.getRequired(attributeName);
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
		return attributesAccessor.getRequired(attributeName);
	}
	
	/**
	 * Returns a string attribute value in the map, returning <code>null</code>
	 * if no value was found.
	 * @param attributeName the attribute name
	 * @return the string attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * string
	 */
	public String getStringAttribute(String attributeName) throws IllegalArgumentException {
		return attributesAccessor.getString(attributeName);
	}

	/**
	 * Returns a string attribute value in the map, returning the defaultValue
	 * if no value was found.
	 * @param attributeName the attribute name
	 * @param defaultValue the default
	 * @return the string attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * string
	 */
	public String getStringAttribute(String attributeName, String defaultValue) throws IllegalArgumentException {
		return attributesAccessor.getString(attributeName, defaultValue);
	}

	/**
	 * Returns a string attribute value in the map, throwing an exception if the
	 * attribute is not present and of the correct type.
	 * @param attributeName the attribute name
	 * @return the string attribute value
	 * @throws IllegalArgumentException if the attribute is not present or
	 * present but not a string
	 */
	public String getRequiredStringAttribute(String attributeName) throws IllegalArgumentException {
		return attributesAccessor.getRequiredString(attributeName);
	}

	/**
	 * Returns a collection attribute value in the map.
	 * @param attributeName the attribute name
	 * @return the collection attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * collection
	 */
	public Collection getCollectionAttribute(String attributeName) throws IllegalArgumentException {
		return attributesAccessor.getCollection(attributeName);
	}

	/**
	 * Returns a collection attribute value in the map and make sure it is of
	 * the required type.
	 * @param attributeName the attribute name
	 * @param requiredType the required type of the attribute value
	 * @return the collection attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * collection of the required type
	 */
	public Collection getCollectionAttribute(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributesAccessor.getCollection(attributeName, requiredType);
	}

	/**
	 * Returns a collection attribute value in the map, throwing an exception if
	 * the attribute is not present or not a collection.
	 * @param attributeName the attribute name
	 * @return the collection attribute value
	 * @throws IllegalArgumentException if the attribute is not present or is
	 * present but not a collection
	 */
	public Collection getRequiredCollectionAttribute(String attributeName) throws IllegalArgumentException {
		return attributesAccessor.getRequiredCollection(attributeName);
	}

	/**
	 * Returns a collection attribute value in the map, throwing an exception if
	 * the attribute is not present or not a collection of the required type.
	 * @param attributeName the attribute name
	 * @return the collection attribute value
	 * @throws IllegalArgumentException if the attribute is not present or is
	 * present but not a collection of the required type
	 */
	public Collection getRequiredCollectionAttribute(String attributeName, Class requiredType)
			throws IllegalArgumentException {
		return attributesAccessor.getRequiredCollection(attributeName);
	}

	/**
	 * Returns a number attribute value in the map that is of the specified
	 * type, returning <code>null</code> if no value was found.
	 * @param attributeName the attribute name
	 * @param requiredType the required number type
	 * @return the number attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * number of the required type
	 */
	public Number getNumberAttribute(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributesAccessor.getNumber(attributeName, requiredType);
	}

	/**
	 * Returns a number attribute value in the map of the specified type,
	 * returning the defaultValue if no value was found.
	 * @param attributeName the attribute name
	 * @param defaultValue the default
	 * @return the number attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * number of the required type
	 */
	public Number getNumberAttribute(String attributeName, Class requiredType, Number defaultValue)
			throws IllegalArgumentException {
		return attributesAccessor.getNumber(attributeName, requiredType, defaultValue);
	}

	/**
	 * Returns a number attribute value in the map, throwing an exception if the
	 * attribute is not present and of the correct type.
	 * @param attributeName the attribute name
	 * @return the number attribute value
	 * @throws IllegalArgumentException if the attribute is not present or
	 * present but not a number of the required type
	 */
	public Number getRequiredNumberAttribute(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributesAccessor.getRequiredNumber(attributeName, requiredType);
	}

	/**
	 * Returns an integer attribute value in the map, returning
	 * <code>null</code> if no value was found.
	 * @param attributeName the attribute name
	 * @return the integer attribute value
	 * @throws IllegalArgumentException if the attribute is present but not an
	 * integer
	 */
	public Integer getIntegerAttribute(String attributeName) throws IllegalArgumentException {
		return attributesAccessor.getInteger(attributeName);
	}

	/**
	 * Returns an integer attribute value in the map, returning the defaultValue
	 * if no value was found.
	 * @param attributeName the attribute name
	 * @param defaultValue the default
	 * @return the integer attribute value
	 * @throws IllegalArgumentException if the attribute is present but not an
	 * integer
	 */
	public Integer getIntegerAttribute(String attributeName, Integer defaultValue) throws IllegalArgumentException {
		return attributesAccessor.getInteger(attributeName, defaultValue);
	}

	/**
	 * Returns an integer attribute value in the map, throwing an exception if
	 * the attribute is not present and of the correct type.
	 * @param attributeName the attribute name
	 * @return the integer attribute value
	 * @throws IllegalArgumentException if the attribute is not present or
	 * present but not an integer
	 */
	public Integer getRequiredIntegerAttribute(String attributeName) throws IllegalArgumentException {
		return attributesAccessor.getRequiredInteger(attributeName);
	}

	/**
	 * Returns a long attribute value in the map, returning <code>null</code>
	 * if no value was found.
	 * @param attributeName the attribute name
	 * @return the long attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * long
	 */
	public Long getLongAttribute(String attributeName) throws IllegalArgumentException {
		return attributesAccessor.getLong(attributeName);
	}

	/**
	 * Returns a long attribute value in the map, returning the defaultValue if
	 * no value was found.
	 * @param attributeName the attribute name
	 * @param defaultValue the default
	 * @return the long attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * long
	 */
	public Long getLongAttribute(String attributeName, Long defaultValue) throws IllegalArgumentException {
		return attributesAccessor.getLong(attributeName, defaultValue);
	}

	/**
	 * Returns a long attribute value in the map, throwing an exception if the
	 * attribute is not present and of the correct type.
	 * @param attributeName the attribute name
	 * @return the long attribute value
	 * @throws IllegalArgumentException if the attribute is not present or
	 * present but not a long
	 */
	public Long getRequiredLongAttribute(String attributeName) throws IllegalArgumentException {
		return attributesAccessor.getRequiredLong(attributeName);
	}

	/**
	 * Returns a boolean attribute value in the map, returning <code>null</code>
	 * if no value was found.
	 * @param attributeName the attribute name
	 * @return the long attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * boolean
	 */
	public Boolean getBooleanAttribute(String attributeName) {
		return attributesAccessor.getBoolean(attributeName);
	}

	/**
	 * Returns a boolean attribute value in the map, returning the defaultValue
	 * if no value was found.
	 * @param attributeName the attribute name
	 * @param defaultValue the default
	 * @return the boolean attribute value
	 * @throws IllegalArgumentException if the attribute is present but not a
	 * boolean
	 */
	public Boolean getBooleanAttribute(String attributeName, Boolean defaultValue) {
		return attributesAccessor.getBoolean(attributeName, defaultValue);
	}

	/**
	 * Returns a boolean attribute value in the map, throwing an exception if
	 * the attribute is not present and of the correct type.
	 * @param attributeName the attribute name
	 * @return the boolean attribute value
	 * @throws IllegalArgumentException if the attribute is not present or
	 * present but is not a boolean
	 */
	public Boolean getRequiredBooleanAttribute(String attributeName) {
		return attributesAccessor.getRequiredBoolean(attributeName);
	}

	/**
	 * Initializes this attribute map.
	 * @param attributes the attributes
	 */
	protected void initAttributes(Map attributes) {
		this.attributes = attributes;
		attributesAccessor = new MapAccessor(this.attributes);
	}

	/**
	 * Returns the wrapped, modifiable map implementation.
	 */
	protected Map getMapInternal() {
		return attributes;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		attributesAccessor = new MapAccessor(attributes);
	}

	public String toString() {
		return StylerUtils.style(attributes);
	}
}