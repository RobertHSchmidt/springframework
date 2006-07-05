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

import org.springframework.core.NestedRuntimeException;

/**
 * Thrown when a FlowExecutionContinuation could not be deserialized into a
 * FlowExecution.
 * @author Keith Donald
 */
public class ContinuationUnmarshalException extends NestedRuntimeException {

	/**
	 * Creates a new flow execution deserialization exception.
	 * @param message the exception message
	 * @param cause the cause
	 */
	public ContinuationUnmarshalException(String message, Throwable cause) {
		super(message, cause);
	}
}