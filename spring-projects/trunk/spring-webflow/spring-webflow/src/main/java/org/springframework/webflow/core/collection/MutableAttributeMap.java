package org.springframework.webflow.core.collection;

public interface MutableAttributeMap extends AttributeMap {

	/**
	 * Put the attribute into this map.
	 * @param attributeName the attribute name.
	 * @param attributeValue the attribute value.
	 * @return the previous value of the attribute, or null of there was no
	 * previous value.
	 */
	public Object put(String attributeName, Object attributeValue);

	/**
	 * Put all the attributes into this map.
	 * @param attributes the attributes to put into this scope.
	 * @return this, to support call chaining.
	 */
	public MutableAttributeMap putAll(AttributeMap attributes);

	/**
	 * Remove an attribute from this map.
	 * @param attributeName the name of the attribute to remove
	 * @return previous value associated with specified attribute name, or
	 * <tt>null</tt> if there was no mapping for the name
	 */
	public Object remove(String attributeName);

	/**
	 * Replace the contents of this attribute map with the contents of the
	 * provided collection.
	 * @param attributes the attribute collection
	 * @return this, to support call chaining
	 */
	public MutableAttributeMap replaceWith(AttributeMap attributes) throws UnsupportedOperationException;

}