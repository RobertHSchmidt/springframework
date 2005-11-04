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
package org.springframework.webflow;

import java.util.Map;

/**
 * Mutable control interface for states to use to manipulate an ongoing flow
 * execution. Used internally by the various state types when they are entered.
 * 
 * @see org.springframework.webflow.State
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface StateContext extends RequestContext {

	/**
	 * Update the last event that was signaled in the executing flow.
	 * @param event the last event
	 */
	public void setLastEvent(Event lastEvent);

	/**
	 * Update the last transition that executed in the executing flow.
	 * @param lastTransition the last transition that executed
	 */
	public void setLastTransition(Transition lastTransition);

	/**
	 * Set the current state of the flow execution linked to this request.
	 * @param state the current state
	 */
	public void setCurrentState(State state);

	/**
	 * Spawn a new flow session and activate it in the currently executing flow.
	 * Also transitions the spawned flow to its start state.
	 * @param flow the flow to start
	 * @param input initial contents of the newly created flow session
	 * @return the selected starting view, which returns control to the client
	 * and requests that a view be rendered with model data
	 */
	public ViewSelection start(Flow flow, Map input) throws StateException;

	/**
	 * Signals the occurence of an event in the state of this flow execution
	 * request context.
	 * 
	 * @param event the event that occured
	 * @param state the state the event should be signaled in (if
	 * <code>null</code>, defaults to the current flow execution state)
	 * @return the next selected view, which returns control to the client and
	 * requests that a view be rendered with model data
	 */
	public ViewSelection signalEvent(Event event, State state) throws StateException;

	/**
	 * End the active flow session.
	 * @return the ended session
	 * @throws IllegalStateException when the flow execution is not active
	 */
	public FlowSession endActiveSession() throws IllegalStateException;

}