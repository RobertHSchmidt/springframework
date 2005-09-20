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

import org.springframework.webflow.Event;

/**
 * Flow execution storage that stores flow executions as <i>continuations</i>
 * in the HttpSession.
 * <p>
 * A downside of this storage strategy (and of server-side continuations in general)
 * is that there could be many copies of the flow execution stored in the HTTP
 * session, increasing server memory requirements. It is advised that you use the
 * {@link org.springframework.webflow.execution.servlet.ExpiredFlowCleanupFilter} to
 * cleanup any flow execution continuations as soon as they can be considered
 * to have expired.
 * <p>
 * This storage strategy requires a <code>ServletEvent</code>.
 * 
 * @see org.springframework.webflow.execution.servlet.ExpiredFlowCleanupFilter
 * @see org.springframework.webflow.execution.servlet.ServletEvent
 * 
 * @author Erwin Vervaet
 */
public class ContinuationFlowExecutionStorage extends DefaultFlowExecutionStorage {

	private boolean compress = false;

	public ContinuationFlowExecutionStorage(ExternalScopeAccessor scopeAccessor) {
		super(scopeAccessor);
	}
	
	/**
	 * Returns whether or not continuations should be compressed.
	 */
	public boolean isCompress() {
		return compress;
	}

	/**
	 * Set whether or not continuations should be compressed.
	 */
	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public FlowExecution load(Serializable id, Event sourceEvent) throws NoSuchFlowExecutionException,
			FlowExecutionStorageException {
		try {
			FlowExecutionContinuation continuation = (FlowExecutionContinuation)getFlowExecutionAttribute(id, sourceEvent);
			return continuation.getFlowExecution();
		}
		catch (IllegalStateException e) {
			throw new NoSuchFlowExecutionException(id, e);
		}
	}

	public Serializable save(Serializable id, FlowExecution flowExecution, Event sourceEvent)
			throws FlowExecutionStorageException {
		// generate a new id for each continuation
		id = createId();
		setFlowExecutionAttribute(id, new FlowExecutionContinuation(flowExecution, isCompress()), sourceEvent);
		return id;
	}

	public void remove(Serializable id, Event requestingEvent) throws FlowExecutionStorageException {
		// nothing to do
		// note that we shouldn't remove the identified flow execution continuation
		// because that id actually identifies the 'previous' flow execution, not the
		// one that has ended (because that one is never saved so doesn't even have
		// an id!)
	}
}