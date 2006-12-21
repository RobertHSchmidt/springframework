/*
 * Copyright 2002-2006 the original author or authors.
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

import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.State;
import org.springframework.webflow.UnmodifiableAttributeMap;
import org.springframework.webflow.ViewSelection;

/**
 * Interface to be implemented by objects that wish to listen and respond to the
 * lifecycle of one or more <code>FlowExecution</code> objects.
 * <p>
 * An 'observer' that is very aspect like, allowing you to insert 'cross
 * cutting' behavior at well-defined points within one or more flow execution
 * lifecycles.
 * <p>
 * For example, one custom listener my apply security checks at the flow
 * execution level, preventing a flow from starting or a state from entering if
 * the curent user does not have the necessary permissions. Another listener may
 * track flow execution navigation history to support bread crumbs. Another may
 * perform auditing, or setup and tear down connections to a transactional
 * resource.
 * 
 * @see org.springframework.webflow.execution.FlowExecution
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecutionListener {

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
	 * @param startState the start state of the new flow session
	 * @param input a mutable input map to the starting flow session
	 * @throws EnterStateVetoException the start state transition should not be
	 * allowed
	 */
	public void sessionStarting(RequestContext context, State startState, AttributeMap input)
			throws EnterStateVetoException;

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
	 * @param state the state the event was signaled in
	 */
	public void eventSignaled(RequestContext context, State state);

	/**
	 * Called when a state transitions, after the transition is matched but
	 * before the transition occurs.
	 * @param context the source of the event
	 * @param state the proposed state to transition to
	 * @throws EnterStateVetoException the state transition should not be
	 * allowed
	 */
	public void stateEntering(RequestContext context, State state) throws EnterStateVetoException;

	/**
	 * Called when a state transitions, after the transition occured.
	 * @param context the source of the event
	 * @param previousState <i>from</i> state of the transition
	 * @param state <i>to</i> state of the transition
	 */
	public void stateEntered(RequestContext context, State previousState, State state);

	/**
	 * Called after a flow execution is successfully reactivated (but before
	 * event processing).
	 * @param context the source of the event
	 */
	public void resumed(RequestContext context);

	/**
	 * Called when a flow execution is paused, for instance when it is waiting
	 * for user input (after event processing).
	 * @param context the source of the event
	 * @param selectedView the view that will display
	 */
	public void paused(RequestContext context, ViewSelection selectedView);

	/**
	 * Called when the active flow execution session has been asked to end.
	 * @param context the source of the event
	 * @param sessionOutput initial, modifiable output produced by the active
	 * session. The map may be modified by this listener to affect the output
	 * returned.
	 */
	public void sessionEnding(RequestContext context, AttributeMap sessionOutput);

	/**
	 * Called when a flow execution session ends. If the ended session was the
	 * root session of the flow execution, the entire flow execution also ends.
	 * @param context the source of the event
	 * @param endedSession ending flow session
	 * @param sessionOutput final, unmodifiable output returned by the ended
	 * session that is eligible for mapping by this listener
	 */
	public void sessionEnded(RequestContext context, FlowSession endedSession, UnmodifiableAttributeMap sessionOutput);
}