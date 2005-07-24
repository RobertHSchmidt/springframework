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
package org.springframework.webflow.execution.servlet;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.util.WebUtils;
import org.springframework.webflow.Event;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionStorage;
import org.springframework.webflow.execution.FlowExecutionStorageException;
import org.springframework.webflow.execution.NoSuchFlowExecutionException;
import org.springframework.webflow.util.RandomGuid;

/**
 * Flow execution storage implementation that stores the flow execution
 * in the HTTP session.
 * <p>
 * This storage strategy requires a <code>HttpServletRequestEvent</code>.
 * 
 * @author Erwin Vervaet
 */
public class HttpSessionFlowExecutionStorage implements FlowExecutionStorage {

	protected final Log logger = LogFactory.getLog(HttpSessionFlowExecutionStorage.class);

	private boolean createSession = true;

	/**
	 * Returns whether or not an HTTP session should be created if non
	 * exists. Defaults to true.
	 */
	public boolean isCreateSession() {
		return createSession;
	}

	/**
	 * Set whether or not an HTTP session should be created if non exists.
	 */
	public void setCreateSession(boolean createSession) {
		this.createSession = createSession;
	}

	public FlowExecution load(Serializable id, Event requestingEvent) throws NoSuchFlowExecutionException,
			FlowExecutionStorageException {
		try {
			return (FlowExecution)WebUtils.getRequiredSessionAttribute(
					ServletEvent.getRequest(requestingEvent), attributeName(id));
		}
		catch (IllegalStateException e) {
			throw new NoSuchFlowExecutionException(id, e);
		}
	}

	public Serializable save(Serializable id, FlowExecution flowExecution, Event requestingEvent)
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
		ServletEvent.getSession(requestingEvent, isCreateSession()).setAttribute(attributeName(id), flowExecution);
		return id;
	}

	public void remove(Serializable id, Event requestingEvent) throws FlowExecutionStorageException {
		if (logger.isDebugEnabled()) {
			logger.debug("Removing flow execution with id '" + id + "' from HTTP session");
		}
		ServletEvent.getSession(requestingEvent, isCreateSession()).removeAttribute(attributeName(id));
	}
	
	// subclassing hooks

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