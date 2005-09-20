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
 * Flow execution storage implementation that stores the flow execution
 * in the HTTP session.
 * <p>
 * This storage strategy requires a <code>ServletEvent</code>.
 * 
 * @see org.springframework.webflow.execution.servlet.ServletEvent
 * 
 * @author Erwin Vervaet
 */
public class DefaultFlowExecutionStorage implements FlowExecutionStorage {

	/**
	 * Logger, can be used in subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	private ExternalScopeAccessor scopeAccessor;
	
	private boolean createScope = true;

	public DefaultFlowExecutionStorage(ExternalScopeAccessor scopeAccessor) {
		Assert.notNull(scopeAccessor, "The scope accessor property is required to load and save flow executions");
		this.scopeAccessor = scopeAccessor;
	}
	
	/**
	 * Returns whether or not an HTTP session should be created if non
	 * exists. Defaults to true.
	 */
	public boolean isCreateScope() {
		return createScope;
	}

	/**
	 * Set whether or not an HTTP session should be created if non exists.
	 */
	public void setCreateScope(boolean createScope) {
		this.createScope = createScope;
	}

	public FlowExecution load(Serializable id, Event sourceEvent) throws NoSuchFlowExecutionException,
			FlowExecutionStorageException {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Loading flow execution from HTTP session with id '" + id + "'");
			}
			return (FlowExecution)getFlowExecutionAttribute(id, sourceEvent);
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
				logger.debug("Saving flow execution in HTTP session using id '" + id + "'");
			}
		}
		// always update session attribute, even if just overwriting
		// an existing one to make sure the servlet engine knows that this
		// attribute has changed!
		setFlowExecutionAttribute(id, flowExecution, sourceEvent);
		return id;
	}

	public void remove(Serializable id, Event sourceEvent) throws FlowExecutionStorageException {
		if (logger.isDebugEnabled()) {
			logger.debug("Removing flow execution with id '" + id + "' from HTTP session");
		}
		removeFlowExecutionAttribute(id, sourceEvent);
	}
	
	// subclassing hooks

	protected Object getFlowExecutionAttribute(Serializable id, Event sourceEvent) {
		return scopeAccessor.getScope(sourceEvent, createScope).getAttribute(attributeName(id));
	}
	
	protected Object setFlowExecutionAttribute(Serializable id, Object value, Event sourceEvent) {
		return scopeAccessor.getScope(sourceEvent, createScope).setAttribute(attributeName(id), value);
	}
	
	protected void removeFlowExecutionAttribute(Serializable id, Event sourceEvent) {
		scopeAccessor.getScope(sourceEvent, createScope).removeAttribute(attributeName(id));
	}
	
	/**
	 * Helper to generate a unique id for a flow execution in the storage.
	 */
	protected Serializable createId() {
		return new RandomGuid().toString();
	}
	
	/**
	 * Returns an appropriate session attribute name for the flow execution id.
	 */
	protected String attributeName(Serializable id) {
		return FlowExecution.class.getName() + "." + id;
	}
}