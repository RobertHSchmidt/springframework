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

import org.springframework.webflow.execution.FlowExecution;

/**
 * Central interface responsible for the saving and restoring of flow
 * executions, where each flow execution represents a state of an active flow.
 * <p>
 * Flow execution repositories are responsible for managing the creation,
 * storage, restoration, and removal of flow executions launched by clients of
 * the Spring Web Flow system.
 * <p>
 * When placed in a repository a {@link FlowExecution} object representing the
 * state of a flow at a point in time is indexed under a unique
 * {@link FlowExecutionKey}.
 * 
 * @author Keith Donald
 */
public interface FlowExecutionRepository {

	/**
	 * Create a new flow execution persistable by this repository.
	 * <p>
	 * The returned flow execution captures the state of a new flow instance
	 * before it has been started. The execution is eligible for persistence by
	 * this repository if it still active after startup request processing.
	 * @param flowId the flow definition identifier defining the blueprint for a
	 * conversation
	 * @return the flow execution representing the state of a launched flow that
	 * has not yet been started
	 * @throws FlowExecutionRepositoryException a problem occured creating the
	 * flow execution
	 */
	public FlowExecution createFlowExecution(String flowId) throws FlowExecutionRepositoryException;

	/**
	 * Generate a unique flow execution key to be used as the persistent
	 * identifier of the flow execution. This method should be called after a
	 * new flow execution is started and remains active; thus needing to be
	 * persisted. The FlowExecutionKey is the execution's persistent identity.
	 * @param flowExecution the flow execution
	 * @return the flow execution key
	 * @throws FlowExecutionRepositoryException a problem occured generating the
	 * key
	 */
	public FlowExecutionKey generateKey(FlowExecution flowExecution) throws FlowExecutionRepositoryException;

	/**
	 * Obtain the "next" flow execution key to be used as as the flow
	 * execution's persistent identity. The repository may choose to simply
	 * return the previous key or generate a new key.
	 * @param flowExecution the flow execution
	 * @throws FlowExecutionRepositoryException a problem occured generating the
	 * key
	 */
	public FlowExecutionKey getNextKey(FlowExecution flowExecution, FlowExecutionKey previousKey)
			throws FlowExecutionRepositoryException;

	/**
	 * Return the lock for the flow execution, allowing for the lock to be
	 * acquired or released.
	 * <p>
	 * CAUTION: care should be made not to allow for a deadlock situation. If
	 * you acquire a lock make sure you release it when you are done.
	 * <p>
	 * The general pattern for safely doing work against a locked conversation
	 * follows:
	 * 
	 * <pre>
	 * FlowExecutionLock lock = repository.getLock(key);
	 * lock.lock();
	 * try {
	 * 	FlowExecution execution = repository.getFlowExecution(key);
	 * 	// do work
	 * }
	 * finally {
	 * 	lock.unlock();
	 * }
	 * </pre>
	 * 
	 * @param key the identifier of the flow execution to lock
	 * @return the lock
	 * @throws FlowExecutionRepositoryException a problem occured accessing the
	 * lock object
	 */
	public FlowExecutionLock getLock(FlowExecutionKey key) throws FlowExecutionRepositoryException;

	/**
	 * Return the <code>FlowExecution</code> indexed by the provided key. The
	 * returned flow execution represents the restored state of an executing
	 * flow from a point in time.
	 * @param key the flow execution key
	 * @return the flow execution, fully hydrated and ready to signal an event
	 * against.
	 * @throws FlowExecutionRepositoryException if no flow execution was indexed
	 * with the key provided
	 */
	public FlowExecution getFlowExecution(FlowExecutionKey key) throws FlowExecutionRepositoryException;

	/**
	 * Place the <code>FlowExecution</code> in this repository under the
	 * provided key. This should be called to insert or update the persistent
	 * state of an active (but paused) flow execution.
	 * 
	 * @param key the flow execution key
	 * @param flowExecution the flow execution
	 * @throws FlowExecutionRepositoryException the flow execution could not be
	 * stored
	 */
	public void putFlowExecution(FlowExecutionKey key, FlowExecution flowExecution)
			throws FlowExecutionRepositoryException;

	/**
	 * Remove the flow execution from the repository. This should be called when
	 * the flow execution ends (is no longer active).
	 * @param key the flow execution key
	 * @throws FlowExecutionRepositoryException the flow execution could not be
	 * removed.
	 */
	public void removeFlowExecution(FlowExecutionKey key) throws FlowExecutionRepositoryException;

	/**
	 * @param encodedKey
	 * @return
	 */
	public FlowExecutionKey parseFlowExecutionKey(String encodedKey);

}