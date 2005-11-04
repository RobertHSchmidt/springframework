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
package org.springframework.webflow.execution;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.binding.AttributeSource;
import org.springframework.binding.support.EmptyAttributeSource;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.Scope;
import org.springframework.webflow.State;
import org.springframework.webflow.StateContext;
import org.springframework.webflow.StateException;
import org.springframework.webflow.Transition;
import org.springframework.webflow.ViewSelection;

/**
 * Default state context implementation used internally by the web flow system.
 * This class is closely coupled with <code>FlowExecutionImpl</code> and
 * <code>StateSessionImpl</code>. The three classes work together to form a
 * complete flow execution implementation.
 * 
 * @see org.springframework.webflow.execution.FlowExecutionImpl
 * @see org.springframework.webflow.execution.FlowSessionImpl
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class StateContextImpl implements StateContext {

	/**
	 * The owning flow execution.
	 */
	private FlowExecutionImpl flowExecution;

	/**
	 * The original event that triggered the creation of this state context.
	 */
	private Event sourceEvent;

	/**
	 * The last event that occured in this context. 
	 */
	private Event lastEvent;
	
	/**
	 * The list of state result events that have occured in this context.
	 */
	private List resultEvents = new LinkedList();

	/**
	 * The last transition that executed in this context.
	 */
	private Transition lastTransition;

	/**
	 * Holder for contextual execution properties.
	 */
	private AttributeSource executionProperties = EmptyAttributeSource.INSTANCE;

	/**
	 * The request scope data map.
	 */
	private Scope requestScope = new Scope();

	/**
	 * Create a new request context.
	 * @param sourceEvent the event at the origin of this request
	 * @param flowExecution the owning flow execution
	 */
	public StateContextImpl(Event sourceEvent, FlowExecutionImpl flowExecution) {
		Assert.notNull(sourceEvent, "The source event is required");
		Assert.notNull(flowExecution, "The owning flow execution is required");
		this.sourceEvent = sourceEvent;
		this.lastEvent = sourceEvent;
		this.flowExecution = flowExecution;
	}

	// implementing RequestContext

	public Event getSourceEvent() {
		return sourceEvent;
	}

	public Event getResultEvent(String stateId) {
		Iterator it = resultEvents.iterator();
		while (it.hasNext()) {
			StateResultEvent event = (StateResultEvent)it.next();
			if (event.getStateId().equals(stateId)) {
				return event.getEvent();
			}
		}
		return null;
	}

	public Event getLastEvent() {
		return lastEvent;
	}

	public FlowExecutionContext getFlowExecutionContext() {
		return flowExecution;
	}

	public Scope getRequestScope() {
		return requestScope;
	}

	public Scope getFlowScope() {
		return flowExecution.getActiveSession().getScope();
	}

	public Transition getLastTransition() {
		return lastTransition;
	}

	public AttributeSource getProperties() {
		return executionProperties;
	}

	public void setProperties(AttributeSource properties) {
		if (properties != null) {
			executionProperties = properties;
		}
		else {
			executionProperties = EmptyAttributeSource.INSTANCE;
		}
	}

	public Map getModel() {
		// merge flow, request, and state result event parameters
		Map stateResultParameters = getStateResultParameterMaps();
		Map model = new HashMap(getFlowScope().size() + getRequestScope().size() + stateResultParameters.size() + 1);
		model.putAll(getFlowScope().getAttributeMap());
		model.putAll(getRequestScope().getAttributeMap());
		model.putAll(stateResultParameters);
		model.putAll(flowExecution.getTransactionSynchronizer().getModel(this));
		return model;
	}

	public boolean inTransaction(boolean end) {
		return flowExecution.getTransactionSynchronizer().inTransaction(this, end);
	}

	public void assertInTransaction(boolean end) throws IllegalStateException {
		flowExecution.getTransactionSynchronizer().assertInTransaction(this, end);
	}

	public void beginTransaction() {
		flowExecution.getTransactionSynchronizer().beginTransaction(this);
	}

	public void endTransaction() {
		flowExecution.getTransactionSynchronizer().endTransaction(this);
	}

	// implementing StateContext

	public void setLastEvent(Event lastEvent) {
		this.lastEvent = lastEvent;
		flowExecution.setLastEvent(lastEvent);
	}

	public void setLastTransition(Transition lastTransition) {
		this.resultEvents.add(new StateResultEvent(lastTransition.getSourceState().getId(), getLastEvent()));
		this.lastTransition = lastTransition;
	}

	public void setCurrentState(State state) {
		flowExecution.getListeners().fireStateEntering(this, state);
		State previousState = flowExecution.getCurrentState();
		flowExecution.setCurrentState(state);
		flowExecution.getListeners().fireStateEntered(this, previousState);
	}

	public ViewSelection start(Flow flow, Map input) throws StateException {
		flowExecution.getListeners().fireSessionStarting(this, flow, input);
		flowExecution.activateSession(flow, input);
		ViewSelection selectedView = flow.start(this);
		flowExecution.getListeners().fireSessionStarted(this);
		return selectedView;
	}

	public ViewSelection signalEvent(Event event, State state) throws StateException {
		if (state != null && !getCurrentState().equals(state)) {
			state.enter(this);
		}
		ViewSelection selectedView = flowExecution.getActiveFlow().onEvent(event, this);
		flowExecution.getListeners().fireEventSignaled(this);
		return selectedView;
	}

	public FlowSession endActiveFlowSession() throws IllegalStateException {
		flowExecution.getListeners().fireSessionEnding(this);
		flowExecution.getActiveFlow().end(this);
		FlowSession endedSession = flowExecution.endActiveFlowSession();
		flowExecution.getListeners().fireSessionEnded(this, endedSession);
		return endedSession;
	}

	protected State getCurrentState() {
		return getFlowExecutionContext().getCurrentState();
	}

	private Map getStateResultParameterMaps() {
		if (resultEvents.size() == 0) {
			return Collections.EMPTY_MAP;
		}
		Map parameters = new HashMap(this.resultEvents.size());
		Iterator it = this.resultEvents.iterator();
		while (it.hasNext()) {
			StateResultEvent event = (StateResultEvent)it.next();
			parameters.put(event.getStateId(), event.getEvent().getParameters());
		}
		return parameters;
	}

	/**
	 * A parameter object storing the result event for exactly one state that
	 * was entered and transitioned out of in this state context.
	 * 
	 * @author Keith Donald
	 */
	private static class StateResultEvent implements AttributeSource {
		private String stateId;

		private Event event;

		public StateResultEvent(String stateId, Event event) {
			Assert.hasText(stateId, "The stateId is required");
			Assert.notNull(event, "The event is required");
			this.stateId = stateId;
			this.event = event;
		}

		public boolean containsAttribute(String attributeName) {
			return event.containsAttribute(attributeName);
		}

		public Object getAttribute(String attributeName) {
			return event.getAttribute(attributeName);
		}

		public String getStateId() {
			return stateId;
		}

		public Event getEvent() {
			return event;
		}

		public String toString() {
			return new ToStringCreator(this).append("stateId", stateId).append("event", event).toString();
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("sourceEvent", sourceEvent).append("resultEvents", resultEvents)
				.append("requestScope", requestScope).append("executionProperties", executionProperties).append(
						"flowExecution", flowExecution).toString();
	}
}