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
 * Mutable control interface for internal artifacts to use to manipulate an
 * ongoing flow execution in the context of one client request. Primarily used
 * internally by the various state types when they are entered, but also used to
 * initially start a Flow execution.
 * 
 * @see org.springframework.webflow.State
 * @see org.springframework.webflow.FlowExecution
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowControlContext extends RequestContext {

	/**
	 * Record the last event signaled in the executing flow. This method should
	 * be called as part of signaling an event in a state to indicate the
	 * 'lastEvent' that was signaled.
	 * @param event the last event signaled
	 */
	public void setLastEvent(Event lastEvent);

	/**
	 * Record the last transition that executed in the executing flow. This
	 * method should be called as part of executing a transition from one state
	 * to another.
	 * @param lastTransition the last transition that executed
	 */
	public void setLastTransition(Transition lastTransition);

	/**
	 * Record the current state that has entered in the executing flow. This
	 * method should be called as part of entering a new state by the State type
	 * itself.
	 * @param state the current state
	 */
	public void setCurrentState(State state);

	/**
	 * Spawn a new flow session and activate it in the currently executing flow.
	 * Also transitions the spawned flow to its start state. This method should
	 * be called by states that wish to spawn new flows, such as subflow states.
	 * @param flow the flow to start
	 * @param startState the start state to use, when null, the default start
	 * state for the flow is used
	 * @param input initial contents of the newly created flow session
	 * @return the selected starting view, which returns control to the client
	 * and requests that a view be rendered with model data
	 * @throws StateException if an exception was thrown within a state of the
	 * flow during execution of this start operation
	 */
	public ViewSelection start(Flow flow, State startState, Map input) throws StateException;

	/**
	 * Signals the occurence of an event in the state of this flow execution
	 * request context. This method should be called by states that report
	 * internal event occurences, such as action states.
	 * @param event the event that occured
	 * @param state the state the event occured in (if <code>null</code>,
	 * defaults to the current flow execution state)
	 * @return the next selected view, which returns control to the client and
	 * requests that a view be rendered with model data
	 * @throws StateException if an exception was thrown within a state of the
	 * flow during execution of this signalEvent operation
	 */
	public ViewSelection signalEvent(Event event, State state) throws StateException;

	/**
	 * End the active flow session of the current flow execution. This method
	 * should be called by states that terminate flows, such as end states.
	 * @return the ended session
	 * @throws IllegalStateException when the flow execution is not active
	 */
	public FlowSession endActiveFlowSession() throws IllegalStateException;

}