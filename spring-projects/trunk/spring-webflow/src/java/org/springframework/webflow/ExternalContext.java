/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
