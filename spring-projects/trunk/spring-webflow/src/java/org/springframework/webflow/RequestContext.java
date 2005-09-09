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

import org.springframework.binding.AttributeSource;
import org.springframework.validation.Errors;

/**
 * Central interface that allows clients to access contextual information about
 * an ongoing flow execution within the context of a client request. The term
 * <i>request</i> is used to describe a single call (thread) into the flow system to
 * manipulate exactly one flow execution.
 * <p>
 * A new instance of this object is created when one of the operations on a
 * <code>FlowExecution</code> facade is invoked, either
 * ({@link org.springframework.webflow.execution.FlowExecution#start(Event)}
 * to activate a newly created flow execution, or
 * {@link org.springframework.webflow.execution.FlowExecution#signalEvent(Event)}) to
 * signal an event in the current state of a restored flow execution.
 * <p>
 * Once created this context object is passed around throughout request
 * processing where it may be accessed and reasoned upon, typically by
 * user-implemented action code and/or state transition criteria.
 * <p>
 * When a call into a flow execution returns, this object goes out of scope
 * and is disposed of automatically.  Thus, this object is an internal artifact
 * used within a FlowExecution: this object is NOT directly exposed to external
 * client code.
 * <p>
 * Note: the "requestScope" property may be used as a store for arbitrary data that 
 * should exist for the life of this object.  Such request-local data, along with
 * all data in flow scope, is available for exposing to view templates via a 
 * ViewDescriptor, returned when a ViewState or EndState is entered.
 * See ({@link org.springframework.webflow.ViewState}) for an example using 
 * a specific ({@link org.springframework.webflow.ViewDescriptorCreator}) strategy. 
 * <p>
 * Note: the <i>request</i> context is in no way linked to an HTTP or Portlet request!
 * It uses the familiar "request" naming convention to indicate a single call to
 * manipulate a runtime execution of a flow.
 * 
 * @see org.springframework.webflow.execution.FlowExecution
 * @see org.springframework.webflow.Action
 * @see org.springframework.webflow.TransitionCriteria
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface RequestContext extends AttributeSource {

	/**
	 * Returns the client event that originated (or triggered) this request.  This 
	 * event may contain parameters provided as input by the client.  In addition,
	 * this event may be downcastable to a specific event type for a specific client environment,
	 * such as a ServletEvent for servlets or a PortletEvent for portlets. Such downcasting will
	 * give you full access to a native HttpServletRequest, for example. That said, you should
	 * avoid coupling your flow artifacts to a specific deployment where possible.
	 * @return the originating event, the one that triggered the current execution request
	 */
	public Event getSourceEvent();

	/**
	 * Returns additional contextual information about the executing flow.
	 * @return the flow execution context
	 */
	public FlowExecutionContext getFlowExecutionContext();

	/**
	 * Returns a mutable accessor for accessing and/or setting attributes in
	 * request scope. Request scoped attributes exist for the duration of this request.
	 * @return the request scope
	 */
	public Scope getRequestScope();

	/**
	 * Returns a mutable accessor for accessing and/or setting attributes in
	 * flow scope. Flow scoped attributes exist for the life of the executing flow.
	 * @return the flow scope
	 */
	public Scope getFlowScope();

	/**
	 * Returns the last event signaled during this request. The event may or may
	 * not have caused a state transition to happen.
	 * @return the last signaled event
	 */
	public Event getLastEvent();

	/**
	 * Returns the last state transition executed in this request.
	 * @return the last transition, or <code>null</code> if none has occured yet
	 */
	public Transition getLastTransition();

	/**
	 * Returns a holder for arbitrary execution properties set for the current request.
	 * @return the execution properties, or empty if not set
	 */
	public AttributeSource getProperties();
	
	/**
	 * Update contextual execution properties for given request context.
	 * @param properties the execution properties
	 */
	public void setProperties(AttributeSource properties);
	
	/**
	 * Returns the data model for this context, suitable for exposing to clients
	 * (mostly web views). Typically the model will contain the union of the data
	 * available in request scope and flow scope.
	 * @return the model that can be exposed to a client
	 */
	public Map getModel();

	/**
	 * Get the errors object for a named command object in the flow or
	 * request scope.
	 */
	public Errors getErrors(String name);

	// application transaction demarcation

	/**
	 * Is the caller participating in the application transaction currently
	 * active in the flow execution?
	 * @param end indicates whether or not the transaction should end after
	 *        checking it
	 * @return true if it is participating in the active transaction, false
	 *         otherwise
	 */
	public boolean inTransaction(boolean end);

	/**
	 * Assert that there is an active application transaction in the flow
	 * execution and that the caller is participating in it.
	 * @param end indicates whether or not the transaction should end after
	 *        checking it
	 * @throws IllegalStateException there is no active transaction in the
	 *         flow execution, or the caller is not participating in it
	 */
	public void assertInTransaction(boolean end) throws IllegalStateException;

	/**
	 * Start a new transaction in the flow execution.
	 */
	public void beginTransaction();

	/**
	 * End the active transaction in the flow execution.
	 */
	public void endTransaction();
	
}