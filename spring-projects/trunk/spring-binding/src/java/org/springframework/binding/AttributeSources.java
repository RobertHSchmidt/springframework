package org.springframework.binding;

import org.springframework.binding.support.MapAttributeSource;

/**
 * A static factory for producing <code>AttributeSource</code>
 * implementations.
 * @author Keith
 */
public class AttributeSources {

	/**
	 * Returns an immutable attribute source with a single name->value pair.
	 * @param attributeName the attribute name
	 * @param attributeValue the attribute value
	 * @return the attribute source
	 */
	public static AttributeSource single(String attributeName, Object attributeValue) {
		MapAttributeSource source = new MapAttributeSource(1);
		source.setAttribute(attributeName, attributeValue);
		return source;
	}
}