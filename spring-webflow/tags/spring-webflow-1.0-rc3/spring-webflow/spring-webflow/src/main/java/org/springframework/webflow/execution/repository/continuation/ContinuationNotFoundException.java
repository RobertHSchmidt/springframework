/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.execution.repository.continuation;

import java.io.Serializable;

import org.springframework.core.NestedRuntimeException;

/**
 * Thrown when no flow execution continuation exists within a continuation
 * group. with the provided id This might occur if the continuation was expired
 * or was explictly invalidated but a client's browser page cache still
 * references it.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ContinuationNotFoundException extends NestedRuntimeException {

	/**
	 * The unique continuation identifier that was invalid.
	 */
	private Serializable continuationId;

	/**
	 * Creates a continuation not found exception.
	 * @param continuationId the invalid continuation id
	 */
	public ContinuationNotFoundException(Serializable continuationId) {
		super("No flow execution continuation could be found in this group with id '" + continuationId
				+ "' -- perhaps the continuation has expired or has been invalidated? ");
	}

	/**
	 * Returns the continuation id.
	 */
	public Serializable getContinuationId() {
		return continuationId;
	}
}