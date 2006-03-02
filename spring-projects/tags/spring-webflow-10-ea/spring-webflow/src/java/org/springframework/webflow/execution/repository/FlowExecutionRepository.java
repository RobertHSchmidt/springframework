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

import java.io.Serializable;

import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecution;

/**
 * Central interface responsible for the saving and restoring of flow
 * executions, where each flow execution represents a state of an active user
 * conversation.
 * <p>
 * Flow execution repositories are responsible for managing the creation,
 * storage, restoration, and invalidation of conversations between clients and
 * the Spring Web Flow system.
 * <p>
 * When placed in a repository, a {@link FlowExecution} object representing the
 * state of a conversation at a point in time is indexed under a unique
 * {@link FlowExecutionKey}. This key provides enough information to track a
 * single active user conversation with the server, as well as provide an index
 * into one or more restorable conversational snapshots taken at points in time
 * during conversation execution. These restorable conversational snapshots are
 * also called <i>continuations</i>.
 * 
 * @author Keith Donald
 */
public interface FlowExecutionRepository {

	/**
	 * Create a new flow execution persistable by this repository.
	 * <p>
	 * The returned flow execution logically represents the state of a new
	 * conversation before it has been started. The execution is eligible for
	 * persistence by this repository if it still active after startup request
	 * processing.
	 * @param flowId the flow definition identifier defining the blueprint for a
	 * conversation
	 * @return the flow execution, representing the state of a new conversation
	 * that has not yet been started
	 */
	public FlowExecution createFlowExecution(String flowId);

	/**
	 * Generate a unique flow execution key to be used as an index into an
	 * active flow execution representing the start of a new user conversation
	 * in this repository. Both the <code>conversationId</code> and
	 * <code>continuationId</code> key parts are guaranteed to be unique.
	 * @param flowExecution the flow execution
	 * @throws FlowExecutionStorageException a problem occured generating the
	 * key
	 */
	public FlowExecutionKey generateKey(FlowExecution flowExecution) throws FlowExecutionRepositoryException;

	/**
	 * Generate a unique flow execution key to be used as an index into a new
	 * flow execution continuation associated with an <i>existing</i> user
	 * conversation managed in this repository. The returned key consists of the
	 * provided <code>conversationId</code> provided and a new, unique
	 * <code>continuationId</code>.
	 * @param flowExecution the flow execution
	 * @throws FlowExecutionStorageException a problem occured generating the
	 * key
	 */
	public FlowExecutionKey generateKey(FlowExecution flowExecution, Serializable conversationId)
			throws FlowExecutionRepositoryException;

	/**
	 * Return the lock for the conversation, allowing for the lock to be
	 * acquired or released.
	 * <p>
	 * CAUTION: care should be made not to allow for a deadlock situation. If
	 * you acquire a lock make sure you release it when you are done.
	 * <p>
	 * The general pattern for safely doing work against a locked conversation
	 * follows:
	 * 
	 * <pre>
	 * ConversationLock lock = repository.getLock(conversationId);
	 * lock.lock();
	 * try {
	 * 	// do conversation work
	 * }
	 * finally {
	 * 	lock.unlock();
	 * }
	 * </pre>
	 * 
	 * @param conversationId the conversation id
	 * @return the conversation lock
	 */
	public ConversationLock getLock(Serializable conversationId);

	/**
	 * Return the <code>FlowExecution</code> indexed by the provided key. The
	 * returned flow execution represents the restored state of a user
	 * conversation captured by the indexed continuation at a point in time.
	 * @param key the flow execution key
	 * @return the flow execution, representing the state of a conversation at a
	 * point in time, fully hydrated and ready to signal an event against.
	 * @throws FlowExecutionStorageException if no flow execution was indexed
	 * with the key provided
	 */
	public FlowExecution getFlowExecution(FlowExecutionKey key) throws FlowExecutionRepositoryException;

	/**
	 * Place the <code>FlowExecution</code> in this repository, indexed under
	 * the provided key.
	 * <p>
	 * If this flow execution represents the start of a new conversation, that
	 * conversation will begin to be tracked and a continuation capturing the
	 * current state of the conversation will be created.
	 * <p>
	 * If this flow execution represents a change in the state of an existing,
	 * ongoing conversation, a new continuation capturing this most recent state
	 * of the conversation will be created.
	 * 
	 * @param key the flow execution key
	 * @param flowExecution the flow execution
	 * @throws FlowExecutionStorageException the flow execution could not be
	 * stored
	 */
	public void putFlowExecution(FlowExecutionKey key, FlowExecution flowExecution)
			throws FlowExecutionRepositoryException;

	/**
	 * Returns the current (or last) flow execution key generated for the
	 * specified conversation.
	 * @param conversationId the conversation id
	 * @return the current continuation key
	 * @throws FlowExecutionRepositoryException if an exception occured getting
	 * the continuationk ey
	 */
	public FlowExecutionKey getCurrentFlowExecutionKey(Serializable conversationId) throws FlowExecutionRepositoryException;

	/**
	 * Returns the current (or last) view selection made for the specified
	 * conversation, or <code>null</code> if no such view selection exists.
	 * <p>
	 * The "current view selection" is simply a descriptor for the last response
	 * issued to the actor participating with this conversation. This method
	 * facilitates access of that descriptor for purposes of re-issuing the same
	 * response multiple times, for example to support browser refresh.
	 * @param conversationId the id of an existing conversation
	 * @return the current view selection
	 * @throws FlowExecutionRepositoryException if an exception occured
	 * retrieving the current view selection
	 */
	public ViewSelection getCurrentViewSelection(Serializable conversationId) throws FlowExecutionRepositoryException;

	/**
	 * Sets the current (or last) view selection made for the specified
	 * conversation.
	 * @param conversationId the id of an existing conversation
	 * @param viewSelection the view selection, to be set as the current
	 * @throws FlowExecutionRepositoryException if an exception occured setting
	 * the current view selection
	 */
	public void setCurrentViewSelection(Serializable conversationId, ViewSelection viewSelection)
			throws FlowExecutionRepositoryException;

	/**
	 * Invalidate the executing conversation with the specified id. This method
	 * will remove all data associated with the conversation, including any
	 * managed continuations. Any future clients that reference this
	 * conversation in a flow execution continuation key will be thrown a
	 * FlowExecutionRepositoryException on any access attempt.
	 * @param conversationId the conversationId
	 * @throws FlowExecutionStorageException the conversation could not be
	 * invalidated
	 */
	public void invalidateConversation(Serializable conversationId) throws FlowExecutionRepositoryException;

}