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

import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.StateException;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.config.FlowLocator;

/**
 * A <i>client instance</i> of an executing top-level flow, representing a
 * single instance of a web conversation.
 * <p>
 * This is the central facade interface for managing one runtime execution of a
 * Flow. Implementations of this interface are the finite state machine that is
 * the heart of Spring Web Flow.
 * <p>
 * Typically, when a browser wants to launch a new execution of a Flow at
 * runtime, it passes in the id of the Flow definition to launch to a governing
 * <code>FlowExecutionManager</code>. The manager then creates an instance of
 * an object implementing this interface, passing it the requested Flow
 * definition which becomes the execution's "root", or top-level flow. After
 * creation, the start operation is called, which causes the execution to
 * activate a new session for its root flow definition. That session is then
 * pushed onto a stack and its definition becomes the "active flow". A local,
 * internal {@link org.springframework.webflow.FlowExecutionControlContext}
 * object (which extends ({@link org.springframework.webflow.RequestContext})
 * is then created and the active Flow's start
 * {@link org.springframework.webflow.State} is entered.
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
 * the user pressed the "submit" button, or pressed "cancel". After the user
 * event is processed, control again goes back to the caller and if this
 * execution is still active, it is saved out to storage. This continues until a
 * client event causes this flow execution to end (by the root flow reaching an
 * EndState). At that time, this object is removed from storage and discarded.
 * 
 * @see org.springframework.webflow.execution.FlowExecutionManager
 * @see org.springframework.webflow.execution.FlowExecutionStorage
 * @see org.springframework.webflow.Flow
 * @see org.springframework.webflow.State
 * @see org.springframework.webflow.FlowSession
 * @see org.springframework.webflow.FlowExecutionControlContext
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecution extends FlowExecutionContext {

	/**
	 * Start this flow execution, transitioning it to the root flow's start
	 * state and returning the starting model and view selection. Typically
	 * called by a flow controller, but also from test code.
	 * @param sourceEvent the "launch event" that occured that triggered flow
	 * execution creation
	 * @return the starting view selection, which requests that the calling
	 * client render a view with configured model data (so the user may
	 * participate in this flow execution)
	 * @throws StateException if an exception was thrown within a state of the
	 * resumed flow execution during event processing
	 */
	public ViewSelection start(String stateId, ExternalContext externalContext) throws StateException;

	/**
	 * Signal an occurence of the specified event in the current state of this
	 * executing flow. The event will be processed in full and control will be
	 * returned once procssing is complete.
	 * @param sourceEvent the event that occured within the current state of
	 * this flow execution
	 * @return the next view selection to display for this flow execution, which
	 * requests that the calling client render a view with configured model data
	 * (so the user may participate in this flow execution)
	 * @throws StateException if an exception was thrown within a state of the
	 * resumed flow execution during event processing
	 */
	public ViewSelection signalEvent(String eventId, String stateId, ExternalContext externalContext)
			throws StateException;

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