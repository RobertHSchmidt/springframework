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

import java.io.Serializable;

import org.springframework.webflow.ExternalContext;

/**
 * Storage strategy for flow executions. A flow execution manager uses this
 * interface to load and save flow executions on every request into the webflow
 * system.
 * <p>
 * Note that implementations of this interface may impact on application
 * transaction management for a flow execution. For instance, the default
 * application transaction synchronization implementation ({@link org.springframework.webflow.execution.FlowScopeTokenTransactionSynchronizer})
 * uses a simple <i>synchronizer token</i> stored in the flow scope, which
 * implies that there is a single flow execution for the transaction However,
 * some flow execution storage strategies (like
 * {@link org.springframework.webflow.execution.ClientContinuationFlowExecutionStorage})
 * create copies (clones) of a flow execution to enable <i>free browsing</i> in
 * a flow. These strategies are not compatible with the default application
 * transaction implementation.
 * <p>
 * Usually this is not a problem since free browing is not really compatible
 * with any kind of transactional semantics. However, if required, you can
 * always plug in another transaction synchronizer, e.g. one that stores a
 * transaction token in an external data store such as a database or shared
 * session variable, no longer requiring a single flow execution per application
 * transaction (see
 * {@link org.springframework.webflow.execution.DataStoreTokenTransactionSynchronizer})
 * 
 * @see org.springframework.webflow.execution.FlowExecutionManager
 * @see org.springframework.webflow.execution.FlowExecution
 * @see org.springframework.webflow.execution.TransactionSynchronizer
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public interface FlowExecutionStorage {

	/**
	 * Load the existing flow execution identified by the provided
	 * <code>id</code> from this storage.
	 * @param id the unique id of the flow execution, as returned by the
	 * {@link #save(Serializable, FlowExecution, ExternalContext) save} method
	 * or {@link #generateId(Serializable)} method
	 * @param externalContext the external user context that has triggered the
	 * load of the flow execution
	 * @return the loaded flow execution
	 * @throws FlowExecutionStorageException when there is a problem accessing
	 * the flow execution storage
	 */
	public FlowExecution load(Serializable id, ExternalContext context) throws FlowExecutionStorageException;

	/**
	 * Save the flow execution out to storage.
	 * @param id the unique id of the flow execution, or <code>null</code> if
	 * the flow execution does not yet have an id (was not previously saved)
	 * @param flowExecution the flow execution to save
	 * @param externalContext the external user context that has triggered the
	 * manipulation of the flow execution
	 * @return the unique id that identifies the saved flow execution; note this
	 * could be different from the id passed into the method
	 * @throws FlowExecutionStorageException when there is a problem accessing
	 * the flow execution storage
	 */
	public Serializable save(Serializable id, FlowExecution flowExecution, ExternalContext context)
			throws FlowExecutionStorageException;

	/**
	 * Remove the identified flow execution from the storage.
	 * @param id the unique id of the flow execution, as returned by the
	 * {@link #save(Serializable, FlowExecution, ExternalContext) save} method
	 * @param externalContext the external user context that has triggered the
	 * removal of the flow execution
	 * @throws FlowExecutionStorageException when there is a technical problem
	 * accessing the flow execution storage
	 */
	public void remove(Serializable id, ExternalContext context) throws FlowExecutionStorageException;

	/* Optional methods */

	/**
	 * Allows the storage strategy to be queried as to whether it supports
	 * pre-generation of storage IDs, assigned before an explicit save step
	 * @return true if the storage supports two step saves (an id may be
	 * generated by calling {@link #generateId(Serializable)}, followed by a
	 * subsequent call to
	 * {@link #saveWithGeneratedId(Serializable, FlowExecution, ExternalContext)},
	 * or false if only a one-step save is supported, via a call to
	 * {@link #save(Serializable, FlowExecution, ExternalContext)}
	 */
	public boolean supportsTwoPhaseSave();

	/**
	 * Generates (or reuses) an unique <code>id</code> to be used later to
	 * save a flow execution out to this storage - the first part of a two phase
	 * save.
	 * @param previousId the unique id of the flow execution, or
	 * <code>null</code> if the flow execution does not yet have an id (was
	 * not previously saved)
	 * @return the unique id that identifies the saved flow execution, this
	 * could be different from the id passed into the method
	 * @throws UnsupportedOperationException when this storage does not support
	 * generation of storage ids as a separate step from saving of flows to the
	 * storage
	 * @throws FlowExecutionStorageException when there is a problem accessing
	 * the flow execution storage
	 */
	public Serializable generateId(Serializable previousId) throws UnsupportedOperationException,
			FlowExecutionStorageException;

	/**
	 * Save the flow execution to storage using the previously generated storage
	 * id - the second part of a two phase save.
	 * @param id the unique id of the flow execution, as returned by the
	 * {@link #generateId(Serializable)} method
	 * @param flowExecution the flow execution to save
	 * @param externalContext the external user context that has triggered the
	 * manipulation of the flow execution
	 * @throws UnsupportedOperationException when this storage does not support
	 * generation of storage ids as a separate step from saving of flows to the
	 * storage
	 * @throws FlowExecutionStorageException
	 */
	public void saveWithGeneratedId(Serializable id, FlowExecution flowExecution, ExternalContext context)
			throws UnsupportedOperationException, FlowExecutionStorageException;
}