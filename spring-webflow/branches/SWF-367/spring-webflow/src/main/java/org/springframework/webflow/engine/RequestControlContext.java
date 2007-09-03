/*
 * Copyright 2004-2007 the original author or authors.
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
package org.springframework.webflow.engine;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowExecutionContext;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.RequestContext;

/**
 * Mutable control interface used to manipulate an ongoing flow execution in the context of one client request.
 * Primarily used internally by the various flow artifacts when they are invoked.
 * <p>
 * This interface acts as a facade for core definition constructs such as the central <code>Flow</code> and
 * <code>State</code> classes, abstracting away details about the runtime execution machine defined in the
 * {@link org.springframework.webflow.engine.impl execution engine implementation} package.
 * <p>
 * Note this type is not the same as the {@link FlowExecutionContext}. Objects of this type are <i>request specific</i>:
 * they provide a control interface for manipulating exactly one flow execution locally from exactly one request. A
 * <code>FlowExecutionContext</code> provides information about a single flow execution (conversation), and it's scope
 * is not local to a specific request (or thread).
 * 
 * @see org.springframework.webflow.engine.Flow
 * @see org.springframework.webflow.engine.State
 * @see org.springframework.webflow.execution.FlowExecution
 * @see FlowExecutionContext
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface RequestControlContext extends RequestContext {

	/**
	 * Record the current state that has entered in the executing flow. This method will be called as part of entering a
	 * new state by the State type itself.
	 * @param state the current state
	 * @see State#enter(RequestControlContext)
	 */
	public void setCurrentState(State state);

	/**
	 * Record the last event signaled in the executing flow. This method will be called as part of signaling an event in
	 * a flow to indicate the 'lastEvent' that was signaled.
	 * @param lastEvent the last event signaled
	 * @see Flow#handleEvent(RequestControlContext)
	 */
	public void setLastEvent(Event lastEvent);

	/**
	 * Record the last transition that executed in the executing flow. This method will be called as part of executing a
	 * transition from one state to another.
	 * @param lastTransition the last transition that executed
	 * @see Transition#execute(State, RequestControlContext)
	 */
	public void setLastTransition(Transition lastTransition);

	/**
	 * Assign the ongoing flow execution its flow execution key. This method will be called before a state is about to
	 * render a view and pause the flow execution.
	 */
	public void assignFlowExecutionKey();

	/**
	 * Spawn a new flow session and activate it in the currently executing flow. Also transitions the spawned flow to
	 * its start state. This method should be called by clients that wish to spawn new flows, such as subflow states.
	 * <p>
	 * This will start a new flow session in the current flow execution, which is already active.
	 * @param flow the flow to start, its <code>start()</code> method will be called
	 * @param input initial contents of the newly created flow session (may be <code>null</code>, e.g. empty)
	 * @throws FlowExecutionException if an exception was thrown within a state of the flow during execution of this
	 * start operation
	 * @see Flow#start(RequestControlContext, MutableAttributeMap)
	 */
	public void start(Flow flow, MutableAttributeMap input) throws FlowExecutionException;

	/**
	 * Signals the occurrence of an event in the current state of this flow execution request context. This method
	 * should be called by clients that report internal event occurrences, such as action states. The
	 * <code>onEvent()</code> method of the flow involved in the flow execution will be called.
	 * @param event the event that occurred
	 * @throws FlowExecutionException if an exception was thrown within a state of the flow during execution of this
	 * signalEvent operation
	 * @see Flow#handleEvent(RequestControlContext)
	 */
	public void handleEvent(Event event) throws FlowExecutionException;

	/**
	 * End the active flow session of the current flow execution. This method should be called by clients that terminate
	 * flows, such as end states. The <code>end()</code> method of the flow involved in the flow execution will be
	 * called.
	 * @param output output produced by the session that is eligible for mapping by a resuming parent flow
	 * @return the ended session
	 * @throws IllegalStateException when the flow execution is not active
	 * @see Flow#end(RequestControlContext, MutableAttributeMap)
	 */
	public FlowSession endActiveFlowSession(MutableAttributeMap output) throws IllegalStateException;

	/**
	 * Execute this transition out of the current source state. Allows for privileged execution of an arbitrary
	 * transition.
	 * @param transition the transition
	 * @see Transition#execute(State, RequestControlContext)
	 */
	public void execute(Transition transition);

	/**
	 * Request that a flow execution redirect be sent as the response. A flow execution redirect tells the caller to
	 * refresh this flow execution in a new request.
	 */
	public void sendFlowExecutionRedirect();

	/**
	 * Request that a flow definition redirect be sent as the response. A flow definition redirect tells the caller to
	 * start a new execution of the flow definition with the input provided.
	 */
	public void sendFlowDefinitionRedirect(String flowId, MutableAttributeMap input);

	/**
	 * Request that a external redirect be sent as the response. An external redirect tells the caller to access the
	 * resource at the given resource URI.
	 * @param resourceUri the resource uri string
	 */
	public void sendExternalRedirect(String resourceUri);

	/**
	 * Returns true if the 'always redirect pause' flow execution attribute is set to true, false otherwise.
	 * @return true or false
	 */
	public boolean getAlwaysRedirectOnPause();

}