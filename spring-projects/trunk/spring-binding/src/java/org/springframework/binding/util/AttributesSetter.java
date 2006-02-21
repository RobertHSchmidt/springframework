package org.springframework.binding.util;

/**
 * An interface for objects that can set attribute values.
 * 
 * @author Keith Donald
 */
public interface AttributesSetter {

	/**
	 * Sets the attribute to the value provided in this scope.
	 * @param attributeName the attribute name
	 * @param attributeValue the attribute value
	 * @return the previous attribute value, or <code>null</code> if there was
	 * no previous value set
	 */
	public Object setAttribute(String attributeName, Object attributeValue);

}