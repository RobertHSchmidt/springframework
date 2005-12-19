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
 * Central interface that allows clients to access contextual information about
 * an ongoing flow execution within the context of a single client request. The
 * term <i>request</i> is used to describe a single call (thread) into the flow
 * system by an external actor to manipulate exactly one flow execution.
 * <p>
 * A new instance of this object is created when one of the operations on a
 * <code>FlowExecution</code> facade is invoked, either ({@link org.springframework.webflow.execution.FlowExecution#start(Event)}
 * to activate a newly created flow execution, or
 * {@link org.springframework.webflow.execution.FlowExecution#signalEvent(Event)})
 * to signal an event in the current state of a resumed flow execution.
 * <p>
 * Once created this context object is passed around throughout request
 * processing where it may be accessed and reasoned upon, typically by
 * user-implemented action code and/or state transition criteria.
 * <p>
 * When a call into a flow execution returns, this object goes out of scope and
 * is disposed of automatically. Thus, this object is an internal artifact used
 * within a FlowExecution: this object is NOT directly exposed to external
 * client code, e.g. a view implementation (JSP).
 * <p>
 * Note: the "requestScope" property may be used as a store for arbitrary data
 * that should exist for the life of this object.
 * <p>
 * Request-local data, along with all data in flow scope, is available for
 * exposing to view templates via a
 * {@link org.springframework.webflow.ViewSelection}'s "model" property,
 * returned when a {@link org.springframework.webflow.ViewState} or
 * {@link org.springframework.webflow.EndState} is entered.
 * <p>
 * This interface does not allow direct manipulation of the flow execution. That
 * is only possible via the
 * {@link org.springframework.webflow.FlowExecutionControlContext} sub
 * interface.
 * <p>
 * The web flow system will ensure that a RequestContext object is local to the
 * current thread, so it can be safely manipulated without needing to worry
 * about concurrent access.
 * <p>
 * Note: the <i>request</i> context is in no way linked to an HTTP or Portlet
 * request! It uses the familiar "request" naming convention to indicate a
 * single call to manipulate a runtime execution of a flow.
 * 
 * @see org.springframework.webflow.execution.FlowExecution
 * @see org.springframework.webflow.FlowExecutionControlContext
 * @see org.springframework.webflow.Action
 * @see org.springframework.webflow.TransitionCriteria
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface RequestContext {

	/**
	 * Returns the external client context that originated (or triggered) this
	 * request. Also known as the "user context".
	 * <p>
	 * Acting as a facade, the returned context object provides a single point
	 * of access to the calling client's environment. It provides normalized
	 * access to attributes of the client environment without tying you to
	 * specific constructs within that environment.
	 * <p>
	 * In addition, this context may be downcastable to a specific context type
	 * for a specific client environment, such as a
	 * {@link org.springframework.webflow.execution.servlet.ServletExternalContext}
	 * for servlets or a
	 * {@link org.springframework.webflow.execution.servlet.PortletExternalContext}
	 * for portlets. Such downcasting will give you full access to a native
	 * HttpServletRequest, for example. With that said, for portability reasons
	 * you should avoid coupling your flow artifacts to a specific deployment
	 * environment when possible.
	 * @return the originating external context, the one that triggered the
	 * current execution request
	 */
	public ExternalContext getExternalContext();

	/**
	 * Returns additional contextual information about the executing flow.
	 * @return the flow execution context
	 */
	public FlowExecutionContext getFlowExecutionContext();

	/**
	 * Returns a mutable accessor for accessing and/or setting attributes in
	 * request scope. <b>Request scoped attributes exist for the duration of
	 * this request only.</b>
	 * @return the request scope
	 */
	public Scope getRequestScope();

	/**
	 * Returns a mutable accessor for accessing and/or setting attributes in
	 * flow scope. <b>Flow scoped attributes exist for the life of the executing
	 * flow.</b>
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
	 * Returns the last state transition that executed in this request.
	 * @return the last transition, or <code>null</code> if no transition has
	 * occured yet
	 */
	public Transition getLastTransition();

	/**
	 * Returns a context map for accessing arbitrary properties about the state
	 * of the current request. Properties provisioned within this map are often
	 * used by {@link Action} implementations to influence their behavior.
	 * @return the current properties of this request, or empty if not set
	 */
	public Map getProperties();

	/**
	 * Update the set of contextual properties describing the state of this
	 * request.
	 * @param properties the properties
	 */
	public void setProperties(Map properties);

	/**
	 * Returns the data model for this context, suitable for exposing to clients
	 * (mostly web views). Typically the model will contain the union of the
	 * data available in request scope and flow scope.
	 * @return the model that can be exposed to a client view for rendering
	 * purposes
	 */
	public Map getModel();
}