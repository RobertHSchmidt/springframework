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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.webflow.ExternalContext;

/**
 * Flow execution storage implementation that stores the flow execution in an
 * externally managed data store. The actual interface to the data store is
 * pluggable by using a {@link DataStoreAccessor}.
 * 
 * @see org.springframework.webflow.execution.DataStoreAccessor
 * 
 * @author Erwin Vervaet
 */
public class DataStoreFlowExecutionStorage implements FlowExecutionStorage {

	/**
	 * Logger, can be used in subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The data store access strategy.
	 */
	private DataStoreAccessor dataStoreAccessor;

	/**
	 * The Flow Execution storage key generation strategy.
	 */
	private KeyGenerator keyGenerator = new RandomGuidKeyGenerator();

	/**
	 * Create a new flow execution storage using the default session data store
	 * accessor.
	 */
	public DataStoreFlowExecutionStorage() {
		this(new SessionDataStoreAccessor());
	}

	/**
	 * Create a new flow execution storage using the configured data store accessor.
	 * @param dataStoreAccessor the data store accessor to use
	 */
	public DataStoreFlowExecutionStorage(DataStoreAccessor dataStoreAccessor) {
		Assert.notNull(dataStoreAccessor,
				"The data store accessor property is required to load and save flow executions");
		this.dataStoreAccessor = dataStoreAccessor;
	}

	/**
	 * Returns the flow execution key generation strategy in use.
	 */
	protected KeyGenerator getKeyGenerator() {
		return keyGenerator;
	}

	/**
	 * Sets the flow execution storage key generation strategy to use.
	 */
	public void setKeyGenerator(KeyGenerator keyGenerator) {
		this.keyGenerator = keyGenerator;
	}

	public FlowExecution load(Serializable id, ExternalContext context) throws FlowExecutionStorageException {
		if (logger.isDebugEnabled()) {
			logger.debug("Loading flow execution from data store with id '" + id + "'");
		}
		FlowExecution flowExecution = getFlowExecution(id, context);
		if (flowExecution == null) {
			throw new NoSuchFlowExecutionException(this, id);
		}
		return flowExecution;
	}

	/**
	 * Access the flow execution with the provided storage id in the configured
	 * data store.
	 */
	protected FlowExecution getFlowExecution(Serializable id, ExternalContext context) throws FlowExecutionStorageException {
		return (FlowExecution)getDataStore(context).get(attributeName(id));
	}

	public Serializable save(Serializable id, FlowExecution flowExecution, ExternalContext context)
			throws FlowExecutionStorageException {
		id = generateId(id);
		if (logger.isDebugEnabled()) {
			logger.debug("Saving flow execution to data store with id '" + id + "'");
		}
		// always update data store attribute, even if just overwriting
		// an existing one to make sure the data store knows that this
		// attribute has changed!
		setFlowExecution(id, flowExecution, context);
		return id;
	}

	public Serializable generateId(Serializable previousId) {
		if (previousId != null) {
			return previousId;
		}
		else {
			return createId();
		}
	}

	/**
	 * Helper to generate a unique id for a flow execution in the storage.
	 */
	protected Serializable createId() {
		return keyGenerator.generate();
	}

	/**
	 * Set the flow execution in the configured data store with the provided
	 * storage id.
	 */
	protected void setFlowExecution(Serializable id, FlowExecution flowExecution, ExternalContext context)
			throws FlowExecutionStorageException {
		getDataStore(context).put(attributeName(id), flowExecution);
	}

	public boolean supportsTwoPhaseSave() {
		return true;
	}

	public void saveWithGeneratedId(Serializable id, FlowExecution flowExecution, ExternalContext context)
			throws UnsupportedOperationException, FlowExecutionStorageException {
		// always update data store attribute, even if just overwriting
		// an existing one to make sure the data store knows that this
		// attribute has changed!
		setFlowExecution(id, flowExecution, context);
	}

	public void remove(Serializable id, ExternalContext context) throws FlowExecutionStorageException {
		if (logger.isDebugEnabled()) {
			logger.debug("Removing flow execution with id '" + id + "' from data store");
		}
		removeFlowExecution(id, context);
	}

	// helpers

	/**
	 * Remove identified attribute value from the data store.
	 */
	protected void removeFlowExecution(Serializable id, ExternalContext context) throws FlowExecutionStorageException {
		getDataStore(context).remove(attributeName(id));
	}

	// subclassing hooks

	/**
	 * Factory method that returns the data store attribute name for the flow
	 * execution to be placed or accessed in/from storage.
	 */
	protected String attributeName(Serializable id) {
		return FlowExecution.class.getName() + "." + id;
	}

	/**
	 * Returns the data store attribute map
	 * 
	 * @param sourceEvent the event
	 * @return the data store
	 */
	protected Map getDataStore(ExternalContext context) {
		return dataStoreAccessor.getDataStore(context);
	}
}