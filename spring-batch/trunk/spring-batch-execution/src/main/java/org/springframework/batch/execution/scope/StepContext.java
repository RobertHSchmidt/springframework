/*
 * Copyright 2006-2007 the original author or authors.
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
package org.springframework.batch.execution.scope;

import org.springframework.batch.core.domain.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.AttributeAccessor;

/**
 * Interface for step-scoped context object and step-scoped services. This
 * interface extends {@link AttributeAccessor}, so there is an underlying map
 * that can be used for storing state during a step execution. The storage is
 * <em>volatile</em>: the attributes are not persisted and not durable across
 * steps in a job, or across restarts of a failed job.
 * 
 * @see ExecutionContext for access to durable attributes that will be restored
 * in the case of a restart.
 * 
 * @author Dave Syer
 * 
 */
public interface StepContext extends AttributeAccessor {

	/**
	 * Accessor for the {@link StepExecution} associated with the currently
	 * executing step.
	 * 
	 * @return the {@link StepExecution} associated with the current step
	 */
	StepExecution getStepExecution();

	/**
	 * Accessor for the parent context.
	 * 
	 * @return the parent of this context (or null if there isn't one)
	 */
	StepContext getParent();

	/**
	 * Register a destruction callback for the end of life of the scope.
	 */
	void registerDestructionCallback(String name, Runnable callback);

	/**
	 * Clean up any resources held during the context of the step.
	 */
	void close();

	/**
	 * Identify this context so that concurrently running jobs in the same VM
	 * can be distinguished.
	 * 
	 * @return a sufficiently unique identifier for this context
	 */
	String getIdentifier();

}