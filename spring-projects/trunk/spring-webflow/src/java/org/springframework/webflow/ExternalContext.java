package org.springframework.webflow;

import java.util.Map;

/**
 * Provides access to the state of an external system that has interacted with
 * Spring Web Flow.
 * <p>
 * This context object provides a single, consistent interface for internal SWF
 * artifacts to use to reason and manipulate the state of an external actor
 * calling into SWF to execute flows.
 * 
 * @author Keith Donald
 */
public interface ExternalContext {

	/**
	 * Returns access to the parameters associated with the request that led to
	 * SWF being called.  This map is immutable and cannot be changed
	 * @return the request parameter map
	 */
	public Map getRequestParameterMap();

	/**
	 * Returns access to an external request attribute map, providing a storage for 
	 * data local to the current request and accessible to both internal and external SWF artifacts.
	 * @return the request attribute map
	 */
	public Map getRequestMap();

	/**
	 * Returns access to an external session map, providing a storage for 
	 * data local to the current user session and accessible to both internal and external SWF artifacts.
	 * @return the session attribute map
	 */
	public Map getSessionMap();

	/**
	 * Returns access to an external application map, providing a storage for 
	 * data local to the current application and accessible to both internal and external SWF artifacts.
	 * @return the application attribute map
	 */
	public Map getApplicationMap();
}
