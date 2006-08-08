package org.springframework.webflow.context;

import org.springframework.webflow.collection.MutableAttributeMap;

/**
 * An interface to be implemented by mutable attributes maps accessed by
 * multiple threads and in needed of mutex synchronization.
 * 
 * @author Keith Donald
 */
public interface SharedAttributeMap extends MutableAttributeMap {

	/**
	 * Returns the shared map's mutex, which may be synchronized on to block
	 * access to the map by other threads.
	 */
	public Object getMutex();
}