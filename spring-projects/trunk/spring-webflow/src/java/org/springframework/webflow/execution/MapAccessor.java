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
package org.springframework.webflow.execution;

import java.util.Map;

import org.springframework.webflow.ExternalContext;

/**
 * Strategy interface for objects that can provide an interface to an externally
 * managed data map.
 * <p>
 * Objects implementing this interface act as factories for attribute sources
 * that when invoked pull attributes from an externally managed source.
 * <p>
 * Used by
 * {@link org.springframework.webflow.execution.RepositoryFlowExecutionStorage}
 * storage implementation to make the underlying storage strategy of an flow
 * execution repository pluggable.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface MapAccessor {

	/**
	 * Returns a mutable attribute map providing access to an underlying data
	 * store.
	 * @param context an external user context object which may provide
	 * assistance in locating the datastore.
	 * @return the mutable attribute source providing access to the data store
	 */
	public Map getMap(ExternalContext context);
}