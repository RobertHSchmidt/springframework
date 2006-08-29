package org.springframework.binding.util;

import java.util.Map;

/**
 * An object whose contents are capable of being exposed as an unmodifiable map.
 * @author Keith Donald
 */
public interface MapAdaptable {

	/**
	 * Returns this object's contents as a {@link Map}. The returned map may or 
	 * may not be modifiable depending on this implementation.
	 * <p>
	 * Warning: this operation may be called frequently; if so care should be
	 * taken so that the map contents (if calculated) be cached as appropriate.
	 * @return the object's contents as a map.
	 */
	public Map asMap();

}