package org.springframework.binding.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A generic attribute map with string keys.
 * 
 * @author Keith Donald
 */
public class AttributeMap implements Serializable {

	/**
	 * The backing map storing the attributes.
	 */
	private Map attributes = new HashMap();

	/**
	 * A helper for accessing attributes.
	 */
	private transient MapAccessor attributesAccessor;

	/**
	 * Creates a new attribute map, initially empty.
	 */
	public AttributeMap() {
		initAttributes(Collections.EMPTY_MAP);
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
	public AttributeMap(AttributeMap attributeMap) {
		initAttributes(new HashMap(attributeMap.getMap()));
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

	/**
	 * Asserts that the attribute is present in the attribute map.
	 * @param key the attribute name
	 * @throws IllegalArgumentException if the key is not present
	 */
	public void assertContainsAttribute(String attributeName) throws IllegalArgumentException {
		attributesAccessor.assertContainsKey(attributeName);
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
	 * @throws IllegalStateException when the value is not of the required type
	 */
	public Object getAttribute(String attributeName, Class requiredType) throws IllegalStateException {
		return attributesAccessor.get(attributeName, requiredType);
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
	 * @param attributeNamethe attribute name
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
	public Number getNumberAttribute(String attributeName, Number defaultValue, Class requiredType)
			throws IllegalArgumentException {
		return attributesAccessor.getNumber(attributeName, defaultValue, requiredType);
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
	 * Sets the attribute to the value provided in this scope.
	 * @param attributeName the attribute name
	 * @param attributeValue the attribute value
	 * @return the previous attribute value, or <code>null</code> if there was
	 * no previous value set
	 */
	public Object addAttribute(String attributeName, Object attributeValue) {
		if (attributes == Collections.EMPTY_MAP) {
			initAttributes(createAttributeMap());
		}
		return attributes.put(attributeName, attributeValue);
	}

	/**
	 * Put all the attributes into this scope.
	 * @param attributes the attributes to put into this scope.
	 */
	public void addAttributes(AttributeMap attributeMap) {
		attributes.putAll(attributeMap.getMap());
	}

	/**
	 * Returns the count of the number of attributes contained within this
	 * scope.
	 * @return the number of attributes in this scope
	 */
	public int getAttributesCount() {
		return attributes.size();
	}

	/**
	 * Remove an attribute from this scope.
	 * @param attributeName the name of the attribute to remove
	 * @return previous value associated with specified attribute name, or
	 * <tt>null</tt> if there was no mapping for the name
	 */
	public Object removeAttribute(String attributeName) {
		return attributes.remove(attributeName);
	}

	/**
	 * Clear the attributes in this map.
	 * @throws UnsupportedOperationException clear is not supported
	 */
	public void clear() throws UnsupportedOperationException {
		attributes.clear();
	}

	/**
	 * Returns the contents of this scope as an unmodifiable map.
	 * @return the scope contents, unmodifiable
	 */
	public Map getMap() {
		return attributesAccessor.getMap();
	}

	/**
	 * Factory method to return the default attribute map used by this scope.
	 */
	protected Map createAttributeMap() {
		return new HashMap();
	}

	private void initAttributes(Map attributes) {
		this.attributes = attributes;
		attributesAccessor = new MapAccessor(this.attributes);
	}
}