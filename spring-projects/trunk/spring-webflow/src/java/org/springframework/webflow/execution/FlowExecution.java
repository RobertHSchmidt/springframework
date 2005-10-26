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

import org.springframework.webflow.Event;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.StateException;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.access.FlowLocator;

/**
 * A <i>client instance</i> of an executing top-level flow, representing a
 * single instance of a web conversation.
 * <p>
 * This is the central facade interface for managing one runtime execution of a
 * Flow. Implementations of this interface are the finite state machine that is
 * the heart of Spring Web Flow.
 * <p>
 * Typically, when a browser wants to launch a new instance of a Flow at
 * runtime, it passes in the id of the Flow definition to launch to a governing
 * <code>FlowExecutionManager</code>. The manager then creates an instance of
 * an object implementing this interface, passing it the requested Flow
 * definition -- which becomes the execution's "root", or top-level flow. After
 * creation, the start operation is called, which causes the execution to
 * activate a new session for its root flow definition. That session is then
 * pushed onto a stack and its definition becomes the "active flow". A local,
 * internal StateContext object (which extends ({@link org.springframework.webflow.RequestContext})
 * is then created and the active Flow's start State is entered.
 * <p>
 * In a distributed environment such as HTTP, after a start or signalEvent
 * operation has completed and control returns to the caller (manager), this
 * execution object (if still active) is typically saved out to some form of
 * storage before the server request ends. For example it might be saved out to
 * the HttpSession, a Database, or a client-side hidden form field for later
 * restoration and manipulation.
 * <p>
 * Subsequent requests from the client to manipuate this flow execution trigger
 * restoration and rehydration of this object, followed by an invocation of the
 * signalEvent operation. The signalEvent operation tells this state machine
 * what action the user took from within the context of the current state: e.g
 * the user pressed "submit", or pressed "cancel". After the user event is
 * processed, control again goes back to the caller and if this execution is
 * still active, it is saved out to storage. This continues until a client event
 * causes this flow execution to end (by the root flow reaching an EndState). At
 * that time, this object is removed from storage and discarded.
 * 
 * @see org.springframework.webflow.execution.FlowExecutionManager
 * @see org.springframework.webflow.execution.FlowExecutionStorage
 * @see org.springframework.webflow.Flow
 * @see org.springframework.webflow.State
 * @see org.springframework.webflow.FlowSession
 * @see org.springframework.webflow.StateContext
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecution extends FlowExecutionContext {

	/**
	 * Start this flow execution, transitioning it to the root flow's start
	 * state and returning the starting model and view descriptor. Typically
	 * called by a flow controller, but also from test code.
	 * @param sourceEvent the "launch event" that occured that triggered flow
	 * execution creation
	 * @return the starting view descriptor, which returns control to the client
	 * and requests that a view be rendered with model data
	 * @throws StateException if the signaled event does not map to any
	 * state transitions in the current state
	 * @throws IllegalStateException if this execution has already been started,
	 * or no state is marked as the start state.
	 */
	public ViewDescriptor start(Event sourceEvent) throws StateException, IllegalStateException;

	/**
	 * Signal an occurence of the specified event in the current state of this
	 * executing flow. The event will be processed in full and control will be
	 * returned once procssing is complete.
	 * @param sourceEvent the event that occured within the current state of
	 * this flow execution
	 * @return the next model and view descriptor to display for this flow
	 * execution, this returns control to the client and requests that a view be
	 * rendered with model data
	 * @throws StateException if the signaled event does not map to any
	 * state transitions in the current state
	 * @throws IllegalStateException if the flow execution is not active and
	 * thus is no longer (or not yet) processing events
	 */
	public ViewDescriptor signalEvent(Event sourceEvent) throws StateException, IllegalStateException;

	/**
	 * Rehydrate this flow execution after deserialization. This is called after
	 * the flow execution has been restored from storaged but before the
	 * signalEvent method is called.
	 * @param flowLocator the flow locator
	 * @param listenerLoader the flow execution listener loader to use to obtain
	 * all listeners that apply
	 * @param transactionSynchronizer application transaction synchronization
	 * strategy to use
	 */
	public void rehydrate(FlowLocator flowLocator, FlowExecutionListenerLoader listenerLoader,
			TransactionSynchronizer transactionSynchronizer);

	/**
	 * Return a list of listeners monitoring the lifecycle of this flow
	 * execution.
	 * @return the flow execution listeners
	 */
	public FlowExecutionListenerList getListeners();
}
