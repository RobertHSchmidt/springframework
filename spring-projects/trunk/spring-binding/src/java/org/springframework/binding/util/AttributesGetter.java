package org.springframework.binding.util;

/**
 * An interface for objects that can get attribute values.
 * @author Keith Donald
 */
public interface AttributesGetter {

	/**
	 * Get an attribute value, returning <code>null</code> if not found.
	 * @param attributeName the attribute name
	 * @return the attribute value
	 */
	public Object getAttribute(String attributeName);

}