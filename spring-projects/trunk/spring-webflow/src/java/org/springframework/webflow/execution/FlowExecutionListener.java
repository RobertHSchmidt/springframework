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

import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.State;
import org.springframework.webflow.ViewSelection;

/**
 * Interface to be implemented by objects that wish to listen and respond to the
 * lifecycle of one or more <code>FlowExecution</code> objects.
 * <p>
 * An 'observer' that is very aspect like, allowing you to insert 'cross
 * cutting' behavior at well-defined points within one or more flow execution
 * lifecycles.
 * 
 * @see org.springframework.webflow.execution.FlowExecution
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecutionListener {

	/**
	 * Called after a new flow execution is created but before it is started or
	 * any requests have been submitted.
	 * @param context the flow execution context
	 */
	public void created(FlowExecutionContext context);

	/**
	 * Called when any client request is submitted to manipulate this flow
	 * execution.
	 * @param context the source of the event, with a 'sourceEvent' property for
	 * access to the request event
	 */
	public void requestSubmitted(RequestContext context);

	/**
	 * Called when a client request has completed processing.
	 * @param context the source of the event, with a 'sourceEvent' property for
	 * access to the request event
	 */
	public void requestProcessed(RequestContext context);

	/**
	 * Called immediately after a start event is signaled -- indicating the flow
	 * execution session is starting but hasn't yet entered its start state.
	 * @param context source of the event
	 * @param flow the definition of the Flow that is starting
	 * @param input a mutable input map to the starting flow session
	 * @throws EnterStateVetoException the start state transition should not be
	 * allowed
	 */
	public void sessionStarting(RequestContext context, Flow flow, Map input) throws EnterStateVetoException;

	/**
	 * Called when a new flow execution session was started -- the start state
	 * has been entered.
	 * @param context source of the event
	 */
	public void sessionStarted(RequestContext context);

	/**
	 * Called when an event is signaled in a state, but prior to any state
	 * transition.
	 * @param context the source of the event, with a 'lastEvent' property for
	 * accessing the signaled event
	 */
	public void eventSignaled(RequestContext context);

	/**
	 * Called when a state transitions, after the transition is matched but
	 * before the transition occurs.
	 * @param context the source of the event
	 * @param nextState the proposed state to transition to
	 * @throws EnterStateVetoException the state transition should not be
	 * allowed
	 */
	public void stateEntering(RequestContext context, State nextState) throws EnterStateVetoException;

	/**
	 * Called when a state transitions, after the transition occured.
	 * @param context the source of the event
	 * @param previousState <i>from</i> state of the transition
	 * @param state <i>to</i> state of the transition
	 */
	public void stateEntered(RequestContext context, State previousState, State state);

	/**
	 * Called when a flow execution is asked to resume as the result of 
	 * a user event.
	 * @param context the source of the event
	 */
	public void resuming(RequestContext context);

	/**
	 * Called after a flow execution is successfully reactivated.
	 * @param context the source of the event
	 */
	public void resumed(RequestContext context);

	/**
	 * Called when a flow execution is paused, for instance when it is waiting
	 * for user input.
	 * @param context the source of the event
	 * @param selectedView the view that will display
	 */
	public void paused(RequestContext context, ViewSelection selectedView);

	/**
	 * Called when the active flow execution session has been asked to end.
	 * @param context the source of the event
	 */
	public void sessionEnding(RequestContext context);

	/**
	 * Called when a flow execution session ends. If the ended session was the
	 * root session of the flow execution, the entire flow execute also ends.
	 * @param context the source of the event
	 * @param endedSession ending flow session
	 */
	public void sessionEnded(RequestContext context, FlowSession endedSession);

	/**
	 * Called after a new or resumed flow execution is saved to storage.
	 * @param context the flow execution that was saved
	 * @param id the unique id of the flow execution in the storage medium
	 */
	public void saved(FlowExecutionContext context, Serializable id);

	/**
	 * Called after an existing flow execution is loaded and rehydrated from
	 * storage.
	 * @param context the flow execution that was loaded
	 * @param id the unique id of the flow execution in the storage medium
	 */
	public void loaded(FlowExecutionContext context, Serializable id);

	/**
	 * Called after an ended flow execution is removed from storage.
	 * @param context the flow execution that was removed
	 * @param id the unique id of the flow execution in the storage medium
	 */
	public void removed(FlowExecutionContext context, Serializable id);
}