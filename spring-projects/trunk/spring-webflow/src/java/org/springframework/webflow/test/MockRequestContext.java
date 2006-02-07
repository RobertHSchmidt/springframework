/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow.test;

import java.util.HashMap;
import java.util.Map;

import org.springframework.webflow.Event;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.Scope;
import org.springframework.webflow.State;
import org.springframework.webflow.Transition;

/**
 * Mock implementation of the <code>RequestContext</code> interface to
 * facilitate standalone Action unit tests.
 * <p>
 * NOT intended to be used for anything but standalone unit tests. This is a
 * simple state holder, a <i>stub</i> implementation, at least if you follow <a
 * href="http://www.martinfowler.com/articles/mocksArentStubs.html">Martin
 * Fowler's</a> reasoning. This class is called <i>Mock</i>RequestContext to
 * be consistent with the naming convention in the rest of the Spring framework
 * (e.g. MockHttpServletRequest, ...).
 * 
 * @see org.springframework.webflow.RequestContext
 * @see org.springframework.webflow.Action
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class MockRequestContext implements RequestContext {

	private FlowExecutionContext flowExecutionContext = new MockFlowExecutionContext();

	private ExternalContext externalContext = new MockExternalContext();

	private Scope requestScope = new Scope();

	private Event lastEvent;

	private Transition lastTransition;

	private Map properties = new HashMap();

	/**
	 * Creates a new mock request context with the following defaults:
	 * <ul>
	 * <li>A flow execution context with a active session of flow "mockFlow" in
	 * state "mockState".
	 * <li>A mock external context with no request parameters set.
	 * </ul>
	 * To add request parameters to this request, use the
	 * {@link #addRequestParameter(Object, Object) } method.
	 */
	public MockRequestContext() {

	}

	/**
	 * Creates a new mock request context with the specified external context
	 * providing access to externally-managed attributes such as request
	 * parameters.
	 * 
	 * @param externalContext the external context
	 */
	public MockRequestContext(ExternalContext externalContext) {
		setExternalContext(externalContext);
	}

	// implementing RequestContext

	public Flow getActiveFlow() {
		return getFlowExecutionContext().getActiveSession().getFlow();
	}

	public State getCurrentState() {
		return getFlowExecutionContext().getActiveSession().getState();
	}

	public Scope getRequestScope() {
		return requestScope;
	}

	public Scope getFlowScope() {
		return getFlowExecutionContext().getActiveSession().getScope();
	}

	public Scope getConversationScope() {
		return getFlowExecutionContext().getScope();
	}

	public Map getRequestParameters() {
		return externalContext.getRequestParameterMap();
	}
	
	public ExternalContext getExternalContext() {
		return externalContext;
	}

	public FlowExecutionContext getFlowExecutionContext() {
		return flowExecutionContext;
	}

	public Event getLastEvent() {
		return lastEvent;
	}

	public Transition getLastTransition() {
		return lastTransition;
	}

	public Map getProperties() {
		return properties;
	}

	public void setProperties(Map properties) {
		this.properties = properties;
	}

	public Map getModel() {
		// merge request and flow scope
		Map model = new HashMap(getFlowScope().size() + getRequestScope().size());
		model.putAll(getFlowScope().getAttributeMap());
		model.putAll(getRequestScope().getAttributeMap());
		return model;
	}

	/**
	 * Set the external context--usefully when unit testing an artifact that
	 * depends on a specific external context implementation.
	 */
	public void setExternalContext(ExternalContext externalContext) {
		this.externalContext = externalContext;
	}

	/**
	 * Set the flow execution context. Typically not needed to be called.
	 */
	public void setFlowExecutionContext(FlowExecutionContext flowExecutionContext) {
		this.flowExecutionContext = flowExecutionContext;
	}

	/**
	 * Set the last event that occured in this request context.
	 * @param lastEvent the event to set
	 */
	public void setLastEvent(Event lastEvent) {
		this.lastEvent = lastEvent;
	}

	/**
	 * Set the last transition that executed in this request context.
	 * @param lastTransition the last transition to set
	 */
	public void setLastTransition(Transition lastTransition) {
		this.lastTransition = lastTransition;
	}

	/**
	 * Set an execution property.
	 * @param propertyName the attribute name
	 * @param propertyValue the attribute value
	 */
	public void setProperty(String propertyName, Object propertyValue) {
		properties.put(propertyName, propertyValue);
	}

	/**
	 * Returns the flow execution context as a {@link MockFlowExecutionContext}.
	 */
	public MockFlowExecutionContext getMockFlowExecutionContext() {
		return (MockFlowExecutionContext)flowExecutionContext;
	}

	/**
	 * Returns the external context as a {@link MockExternalContext}.
	 */
	public MockExternalContext getMockExternalContext() {
		return (MockExternalContext)externalContext;
	}

	/**
	 * Sets the active flow session of the executing flow associated with this request.
	 */
	public void setActiveSession(FlowSession flowSession) {
		getMockFlowExecutionContext().setActiveSession(flowSession);
	}

	/**
	 * Adds a request parameter to the configured external context.
	 * @param parameterName the parameter name
	 * @param parameterValue the parameter value
	 */
	public void addRequestParameter(Object parameterName, Object parameterValue) {
		getMockExternalContext().addRequestParameter(parameterName, parameterValue);
	}
}