/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow.execution;

import java.io.Serializable;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.State;
import org.springframework.webflow.ViewSelection;

/**
 * Mock implementation of the <code>FlowExecutionListener</code> interface for
 * use in unit tests.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class MockFlowExecutionListener extends FlowExecutionListenerAdapter {

	private boolean created;
	
	private boolean started;

	private boolean executing;
	
	private int flowNestingLevel;

	private boolean requestInProcess;

	private int loadCount;
	
	private int saveCount;
	
	private boolean removed;
	
	private int requestsSubmitted;

	private int requestsProcessed;

	private int eventsSignaled;

	private int stateTransitions;

	/**
	 * Make sure the flow execution has already been started.
	 */
	protected void assertStarted() {
		Assert.state(started, "The flow execution has not yet been started");
	}

	public void requestSubmitted(RequestContext context) {
		Assert.state(!requestInProcess, "There is already a request being processed");
		requestsSubmitted++;
		requestInProcess = true;
	}

	public void sessionStarting(RequestContext context, State startState, Map input) throws EnterStateVetoException {
		if (!context.getFlowExecutionContext().isActive()) {
			Assert.state(!started, "The flow execution was already started");
			flowNestingLevel = 0;
			eventsSignaled = 0;
			stateTransitions = 0;
			executing = true;
		}
	}
	
	public void sessionStarted(RequestContext context) {
		if (context.getFlowExecutionContext().getActiveSession().isRoot()) {
			Assert.state(!started, "The flow execution was already started");
			started = true;
		}
		else {
			assertStarted();
			flowNestingLevel++;
		}
	}

	public void requestProcessed(RequestContext context) {
		Assert.state(requestInProcess, "There is no request being processed");
		requestsProcessed++;
		requestInProcess = false;
	}

	public void eventSignaled(RequestContext context) {
		eventsSignaled++;
	}

	public void stateEntering(RequestContext context, State nextState) throws EnterStateVetoException {
	}

	public void stateEntered(RequestContext context, State previousState, State newState) {
		stateTransitions++;
	}

	public void paused(RequestContext context, ViewSelection selectedView) {
		executing = false;
	}

	public void resumed(RequestContext context) {
		executing = true;
	}

	public void sessionEnded(RequestContext context, FlowSession endedSession) {
		assertStarted();
		if (endedSession.isRoot()) {
			Assert.state(flowNestingLevel == 0, "The flow execution should have ended");
			started = false;
			executing = false;
		}
		else {
			flowNestingLevel--;
			Assert.state(started, "The flow execution prematurely ended");
		}
	}

	public void created(FlowExecutionContext context) {
		this.created = true;
	}

	public void loaded(FlowExecutionContext context, Serializable id) {
		loadCount++;
	}

	public void removed(FlowExecutionContext context, Serializable id) {
		removed = true;
	}

	public void saved(FlowExecutionContext context, Serializable id) {
		saveCount++;
	}
	
	/**
	 * Is the flow execution created?
	 */
	public boolean isCreated() {
		return created;
	}
	
	/**
	 * Is the flow execution running: it has started but not yet ended.
	 */
	public boolean isStarted() {
		return started;
	}

	/**
	 * Is the flow execution executing?
	 */
	public boolean isExecuting() {
		return executing;
	}
	/**
	 * Returns the nesting level of the currently active flow in the flow
	 * execution. The root flow is at level 0, a sub flow of the root flow
	 * is at level 1, and so on.
	 */
	public int getFlowNestingLevel() {
		return flowNestingLevel;
	}

	/**
	 * Checks if a request is in process. A request is in process if it was submitted
	 * but has not yet completed processing.
	 */
	public boolean isRequestInProcess() {
		return requestInProcess;
	}

	/**
	 * Returns the number of requests submitted so far.
	 */
	public int getRequestsSubmittedCount() {
		return requestsSubmitted;
	}

	/**
	 * Returns the number of requests processed so far.
	 */
	public int getRequestsProcessedCount() {
		return requestsProcessed;
	}

	/**
	 * Returns the number of events signaled so far.
	 */
	public int getEventsSignaledCount() {
		return eventsSignaled;
	}

	/**
	 * Returns the number of state transitions executed so far.
	 */
	public int getTransitionCount() {
		return stateTransitions;
	}

	/**
	 * Returns the number of times the flow execution was loaded from storage.
	 */
	public int getLoadCount() {
		return loadCount;
	}

	/**
	 * Returns the number of times the flow execution was saved to storage.
	 */
	public int getSaveCount() {
		return saveCount;
	}

	/**
	 * Returns whether or not the flow execution was removed from storage.
	 */
	public boolean isRemoved() {
		return removed;
	}

	/**
	 * Reset all state collected by this listener.
	 */
	public void reset() {
		created = false;
		started = false;
		executing = false;
		removed = false;
		requestsSubmitted = 0;
		requestsProcessed = 0;
		loadCount = 0;
		saveCount = 0;
	}
}