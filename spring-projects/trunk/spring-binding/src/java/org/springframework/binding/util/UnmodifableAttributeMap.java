package org.springframework.binding.util;

import java.io.Serializable;
import java.util.Map;

/**
 * A wrapper around a attribute map that hides mutable operations. Useful for
 * passing around when modification of the target map should be disallowed.
 * 
 * @author Keith Donald
 */
public class UnmodifableAttributeMap implements AttributesGetter, Serializable {

	/**
	 * The wrapped, modifiable attribute map.
	 */
	private AttributeMap attributes;

	/**
	 * Creates a new attribute map, initially empty.
	 */
	public UnmodifableAttributeMap(AttributeMap attributes) {
		this.attributes = attributes;
	}

	/**
	 * Does the attribute with the provided name exist in this scope?
	 * @param attributeName the attribute name
	 * @return true if so, false otherwise
	 */
	public boolean containsAttribute(String attributeName) {
		return attributes.containsAttribute(attributeName);
	}

	/**
	 * Does the attribute with the provided name exist in this scope, and is its
	 * value of the specified class?
	 * @param attributeName the attribute name
	 * @param requiredType the required class of the attribute value
	 * @return true if so, false otherwise
	 */
	public boolean containsAttribute(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributes.containsAttribute(attributeName, requiredType);
	}

	/**
	 * Asserts that the attribute is present in the attribute map.
	 * @param key the attribute name
	 * @throws IllegalArgumentException if the key is not present
	 */
	public void assertContainsAttribute(String attributeName) throws IllegalArgumentException {
		attributes.assertContainsAttribute(attributeName);
	}

	/* (non-Javadoc)
	 * @see org.springframework.binding.util.AttributesGetter#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String attributeName) {
		return attributes.getAttribute(attributeName);
	}

	/**
	 * Get an attribute value and make sure it is of the required type.
	 * @param attributeName name of the attribute to get
	 * @param requiredType the required type of the attribute value
	 * @return the attribute value, or null if not found
	 * @throws IllegalStateException when the value is not of the required type
	 */
	public Object getAttribute(String attributeName, Class requiredType) throws IllegalStateException {
		return attributes.getAttribute(attributeName, requiredType);
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
		return attributes.getStringAttribute(attributeName);
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
		return attributes.getStringAttribute(attributeName, defaultValue);
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
		return attributes.getRequiredStringAttribute(attributeName);
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
		return attributes.getNumberAttribute(attributeName, requiredType);
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
		return attributes.getNumberAttribute(attributeName, defaultValue, requiredType);
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
		return attributes.getRequiredNumberAttribute(attributeName, requiredType);
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
		return attributes.getIntegerAttribute(attributeName);
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
		return attributes.getIntegerAttribute(attributeName, defaultValue);
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
		return attributes.getRequiredIntegerAttribute(attributeName);
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
		return attributes.getLongAttribute(attributeName);
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
		return attributes.getLongAttribute(attributeName, defaultValue);
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
		return attributes.getRequiredLongAttribute(attributeName);
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
		return attributes.getBooleanAttribute(attributeName);
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
		return attributes.getBooleanAttribute(attributeName, defaultValue);
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
		return attributes.getRequiredBooleanAttribute(attributeName);
	}

	/**
	 * Get the value of a required attribute.
	 * @param attributeName name of the attribute to get
	 * @return the attribute value
	 * @throws IllegalStateException when the attribute is not found
	 */
	public Object getRequiredAttribute(String attributeName) throws IllegalStateException {
		return attributes.getRequiredAttribute(attributeName);
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
		return attributes.getRequiredAttribute(attributeName);
	}

	/**
	 * Returns the count of the number of attributes contained within this
	 * scope.
	 * @return the number of attributes in this scope
	 */
	public int getAttributesCount() {
		return attributes.getAttributesCount();
	}

	/**
	 * Returns the contents of this scope as an unmodifiable map.
	 * @return the scope contents, unmodifiable
	 */
	public Map getMap() {
		return attributes.getMap();
	}
}