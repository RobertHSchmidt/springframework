package org.springframework.binding.attribute;

import java.io.Serializable;
import java.util.Map;

/**
 * An attribute collection with no entries.
 * @author Keith Donald
 */
public class EmptyAttributeCollection implements AttributeCollection, Serializable {

	/**
	 * The shared, singleton empty attribute collection instance.
	 */
	public static final AttributeCollection INSTANCE = new EmptyAttributeCollection();

	private EmptyAttributeCollection() {

	}

	public int getAttributeCount() {
		return UnmodifiableAttributeMap.EMPTY_MAP.getAttributeCount();
	}

	public Object getAttribute(String attributeName) {
		return UnmodifiableAttributeMap.EMPTY_MAP.getAttribute(attributeName);
	}

	public Map getMap() {
		return UnmodifiableAttributeMap.EMPTY_MAP.getMap();
	}

	public UnmodifiableAttributeMap unmodifiable() {
		return UnmodifiableAttributeMap.EMPTY_MAP;
	}
}