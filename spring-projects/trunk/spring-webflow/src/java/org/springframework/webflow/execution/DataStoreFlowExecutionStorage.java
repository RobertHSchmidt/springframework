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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.webflow.Event;
import org.springframework.webflow.util.RandomGuid;

/**
 * Flow execution storage implementation that stores the flow execution in a data store
 * accessed using a pluggable data store accessor.
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

	private DataStoreAccessor dataStoreAccessor;
	
	private boolean createDataStore = true;

	/**
	 * Create a new flow execution storage using given data store accessor.
	 * @param dataStoreAccessor the data store accessor to use
	 */
	public DataStoreFlowExecutionStorage(DataStoreAccessor dataStoreAccessor) {
		Assert.notNull(dataStoreAccessor, "The data store accessor property is required to load and save flow executions");
		this.dataStoreAccessor = dataStoreAccessor;
	}
	
	/**
	 * Returns whether or not the data store should be created if it doesn't already
	 * exist. Defaults to true.
	 */
	public boolean isCreateDataStore() {
		return createDataStore;
	}

	/**
	 * Set whether or not the data store should be created if it doesn't already exist.
	 */
	public void setCreateDataStore(boolean createDataStore) {
		this.createDataStore = createDataStore;
	}

	public FlowExecution load(Serializable id, Event sourceEvent) throws NoSuchFlowExecutionException,
			FlowExecutionStorageException {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Loading flow execution from data store with id '" + id + "'");
			}
			return (FlowExecution)getDataSourceAttribute(id, sourceEvent);
		}
		catch (IllegalStateException e) {
			throw new NoSuchFlowExecutionException(id, e);
		}
	}

	public Serializable save(Serializable id, FlowExecution flowExecution, Event sourceEvent)
			throws FlowExecutionStorageException {
		if (id == null) {
			id = createId();
			if (logger.isDebugEnabled()) {
				logger.debug("Saving flow execution in data store using id '" + id + "'");
			}
		}
		// always update data store attribute, even if just overwriting
		// an existing one to make sure the data store knows that this
		// attribute has changed!
		setDataSourceAttribute(id, flowExecution, sourceEvent);
		return id;
	}

	public void remove(Serializable id, Event sourceEvent) throws FlowExecutionStorageException {
		if (logger.isDebugEnabled()) {
			logger.debug("Removing flow execution with id '" + id + "' from data store");
		}
		removeDataSourceAttribute(id, sourceEvent);
	}
	
	// helpers
	
	/**
	 * Get the attribute value associated with given id in the data store.
	 */
	protected Object getDataSourceAttribute(Serializable id, Event sourceEvent) {
		return dataStoreAccessor.getDataStore(sourceEvent, isCreateDataStore()).getAttribute(attributeName(id));
	}
	
	/**
	 * Associate given id with given attribute value in the data store.
	 */
	protected Object setDataSourceAttribute(Serializable id, Object value, Event sourceEvent) {
		return dataStoreAccessor.getDataStore(sourceEvent, isCreateDataStore()).setAttribute(attributeName(id), value);
	}

	/**
	 * Remove identified attribute value from the data store.
	 */
	protected void removeDataSourceAttribute(Serializable id, Event sourceEvent) {
		dataStoreAccessor.getDataStore(sourceEvent, isCreateDataStore()).removeAttribute(attributeName(id));
	}
	
	// subclassing hooks
	
	/**
	 * Helper to generate a unique id for a flow execution in the storage.
	 */
	protected Serializable createId() {
		return new RandomGuid().toString();
	}
	
	/**
	 * Returns an appropriate data store attribute name for the flow execution id.
	 */
	protected String attributeName(Serializable id) {
		return FlowExecution.class.getName() + "." + id;
	}
}