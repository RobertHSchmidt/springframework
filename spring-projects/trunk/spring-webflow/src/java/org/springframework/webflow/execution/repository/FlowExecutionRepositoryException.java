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
package org.springframework.webflow.execution.repository;

import org.springframework.webflow.FlowException;

/**
 * The root of the {@link FlowExecutionRepository} exception hierarchy.
 * 
 * @author Keith Donald
 */
public abstract class FlowExecutionRepositoryException extends FlowException {

	/**
	 * The repository that threw this exception.
	 */
	private transient FlowExecutionRepository repository;

	/**
	 * Creates a new flow execution repository exception explicitly noting the
	 * repository that had the problem.
	 * @param repository the repository that had a problem
	 * @param message the message
	 */
	public FlowExecutionRepositoryException(FlowExecutionRepository repository, String message) {
		super(message);
		this.repository = repository;
	}

	/**
	 * Creates a new flow execution repository exception explicitly noting the
	 * repository that had the problem.
	 * @param repository the repository that had a problem
	 * @param message the message
	 * @param cause the root cause of the problem
	 */
	public FlowExecutionRepositoryException(FlowExecutionRepository repository, String message, Throwable cause) {
		super(message, cause);
		this.repository = repository;
	}

	/**
	 * Creates a new flow execution repository exception.
	 * @param message the message
	 * @param cause the root cause of the problem
	 */
	public FlowExecutionRepositoryException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Returns the repository.
	 */
	public FlowExecutionRepository getRepository() {
		return repository;
	}
}