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

import org.springframework.binding.MutableAttributeSource;
import org.springframework.webflow.Event;

/**
 * Strategy interface for objects that can provide an interface to an external
 * attribute data store.
 * <p>
 * Objects implementing this interface act  as factories for attribute sources
 * that when invoked pull attributes from an externally managed source.
 * <p>
 * Used by
 * {@link org.springframework.webflow.execution.DataStoreFlowExecutionStorage}
 * storage implementation to make the underlying storage strategy of an flow
 * execution pluggable.
 * <p>
 * Used by
 * {@link org.springframework.webflow.execution.DataStoreTokenTransactionSynchronizer}
 * to make the underlying storage strategy of an transaction token pluggable.
 * 
 * @see org.springframework.binding.AttributeSource
 * @see org.springframework.binding.MutableAttributeSource
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface DataStoreAccessor {

	/**
	 * Returns a mutable attribute accessor providing access to the data store.
	 * @param sourceEvent the event requesting access to the data store
	 * @return the mutable attribute source given access to the data store
	 */
	public MutableAttributeSource getDataStore(Event sourceEvent);
}