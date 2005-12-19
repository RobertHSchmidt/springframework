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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.webflow.Event;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.Scope;
import org.springframework.webflow.ScopeType;
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
public class MockRequestContext implements RequestContext, FlowExecutionContext {

	private String key = "Mock Flow Execution";

	private Flow rootFlow;

	private ExternalContext externalContext;

	private Scope requestScope = new Scope(ScopeType.REQUEST);

	private MockFlowSession activeSession;

	private Event lastEvent;

	private Transition lastTransition;

	private Map properties = new HashMap();

	private long creationTimestamp = System.currentTimeMillis();

	private long lastRequestTimestamp = System.currentTimeMillis();

	/**
	 * Create a new stub request context. Automatically creates an initial flow
	 * session that simulates a mock Flow in its start state.
	 */
	public MockRequestContext() {
		setExternalContext(new MockExternalContext());
		setActiveSession(new MockFlowSession());
	}

	/**
	 * Create a new stub request context. Automatically creates an initial flow
	 * session that simulates a mock Flow in its start state.
	 */
	public MockRequestContext(ExternalContext externalContext) {
		setExternalContext(externalContext);
		setActiveSession(new MockFlowSession());
	}

	/**
	 * Create a new stub request context.
	 */
	public MockRequestContext(MockFlowSession session, ExternalContext externalContext) {
		setActiveSession(session);
		setExternalContext(externalContext);
	}

	// implementing RequestContext

	public ExternalContext getExternalContext() {
		return externalContext;
	}

	public void setExternalContext(ExternalContext externalContext) {
		this.externalContext = externalContext;
	}

	public FlowExecutionContext getFlowExecutionContext() {
		return this;
	}

	public Scope getRequestScope() {
		return requestScope;
	}

	public Scope getFlowScope() {
		return activeSession.getScope();
	}

	public Event getLastEvent() {
		return lastEvent;
	}

	/**
	 * Set the last event that occured in this request context.
	 * @param lastEvent the event to set
	 */
	public void setLastEvent(Event lastEvent) {
		this.lastRequestTimestamp = System.currentTimeMillis();
		this.lastEvent = lastEvent;
	}

	public Transition getLastTransition() {
		return lastTransition;
	}

	/**
	 * Set the last transition that executed in this request context.
	 * @param lastTransition the last transition to set
	 */
	public void setLastTransition(Transition lastTransition) {
		this.lastTransition = lastTransition;
	}

	public Map getProperties() {
		return properties;
	}

	public void setProperties(Map properties) {
		this.properties = properties;
	}

	/**
	 * Set an execution property.
	 * @param propertyName the attribute name
	 * @param propertyValue the attribute value
	 */
	public void setProperty(String propertyName, Object propertyValue) {
		properties.put(propertyName, propertyValue);
	}

	public Map getModel() {
		// merge request and flow scope
		Map model = new HashMap(getFlowScope().size() + getRequestScope().size());
		model.putAll(getFlowScope().getAttributeMap());
		model.putAll(getRequestScope().getAttributeMap());
		return model;
	}

	// implementing FlowExecutionContext

	public Serializable getKey() {
		return key;
	}

	public String getCaption() {
		return getActiveFlow().getId();
	}

	public long getCreationTimestamp() {
		return creationTimestamp;
	}

	public long getUptime() {
		return System.currentTimeMillis() - getCreationTimestamp();
	}

	public long getLastRequestTimestamp() {
		return lastRequestTimestamp;
	}

	public String getLastEventId() {
		return lastEvent.getId();
	}

	public boolean isActive() {
		return activeSession != null;
	}

	public boolean isRootFlowActive() {
		return activeSession != null && activeSession.isRoot();
	}

	public Flow getRootFlow() {
		return rootFlow;
	}

	/**
	 * Set the root flow of this request context.
	 * @param rootFlow the root flow to set
	 */
	public void setRootFlow(Flow rootFlow) {
		this.rootFlow = rootFlow;
	}

	public Flow getActiveFlow() {
		return activeSession.getFlow();
	}

	public State getCurrentState() {
		State state = activeSession.getCurrentState();
		if (state == null) {
			throw new IllegalStateException("Active flow session 'currentState' not set");
		}
		return state;
	}

	/**
	 * Set the current state of this request context.
	 * @param state the current state to set
	 */
	public void setCurrentState(State state) {
		Assert.state(state.getFlow() == getActiveSession().getFlow(),
				"The current state to set must be a state in the active flow");
		this.activeSession.setCurrentState(state);
	}

	public FlowSession getActiveSession() throws IllegalStateException {
		if (activeSession == null) {
			throw new IllegalStateException("No flow session active");
		}
		return activeSession;
	}

	/**
	 * Set the active flow session of this request context. If the "rootFlow" is
	 * null when this method is called it will automaically be set to the flow
	 * associated with the provided session.
	 * @param session the active flow session to set
	 */
	public void setActiveSession(MockFlowSession session) {
		this.activeSession = session;
		if (this.rootFlow == null) {
			this.rootFlow = session.getFlow();
		}
	}
}