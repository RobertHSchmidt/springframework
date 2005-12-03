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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.AttributeSource;
import org.springframework.binding.support.EmptyAttributeSource;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.DecisionState;
import org.springframework.webflow.Event;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.FlowExecutionControlContext;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.Scope;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.State;
import org.springframework.webflow.StateException;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionableState;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.ViewState;

/**
 * Default flow execution control context implementation used internally by the
 * web flow system. This class is closely coupled with
 * <code>FlowExecutionImpl</code> and <code>FlowSessionImpl</code>. The
 * three classes work together to form a complete flow execution implementation.
 * 
 * @see org.springframework.webflow.execution.FlowExecutionImpl
 * @see org.springframework.webflow.execution.FlowSessionImpl
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowExecutionControlContextImpl implements FlowExecutionControlContext {

	protected static final Log logger = LogFactory.getLog(FlowExecutionControlContextImpl.class);

	/**
	 * The owning flow execution.
	 */
	private FlowExecutionImpl flowExecution;

	/**
	 * The original event that triggered the creation of this state context.
	 */
	private ExternalContext externalContext;

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
	private Scope requestScope = new Scope(ScopeType.REQUEST);

	/**
	 * Create a new request context.
	 * @param sourceEvent the event at the origin of this request
	 * @param flowExecution the owning flow execution
	 */
	public FlowExecutionControlContextImpl(FlowExecutionImpl flowExecution, ExternalContext externalContext) {
		Assert.notNull(flowExecution, "The owning flow execution is required");
		this.externalContext = externalContext;
		this.flowExecution = flowExecution;
	}

	// implementing RequestContext

	public ExternalContext getExternalContext() {
		return externalContext;
	}

	public Event getLastResultEvent(String stateId) {
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

	// implementing FlowExecutionControlContext

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

	public ViewSelection start(Flow flow, State startState, Map input) throws StateException {
		if (input == null) {
			// create a mutable map so entries can be added by listeners!
			input = new HashMap(3);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Activating new session for flow '" + flow.getId() + "' in state '"
					+ (startState != null ? startState.getId() : flow.getStartState().getId()) + "' with input "
					+ input);
		}
		flowExecution.getListeners().fireSessionStarting(this, startState, input);
		flowExecution.activateSession(flow, input);
		ViewSelection selectedView = flow.start(startState, this);
		flowExecution.getListeners().fireSessionStarted(this);
		return selectedView;
	}

	public ViewSelection signalEvent(Event event) throws StateException {
		if (logger.isDebugEnabled()) {
			logger.debug("Signaling event '" + event.getId() + "' in state '" + getCurrentState().getId()
					+ "' of flow '" + getFlowExecutionContext().getActiveFlow().getId() + "'");
		}
		setLastEvent(event);
		flowExecution.getListeners().fireEventSignaled(this);
		ViewSelection selectedView = flowExecution.getActiveFlow().onEvent(event, this);
		return selectedView;
	}

	protected ViewSelection handleNewStateRequest(TransitionableState newState, Event event) {
		if (newState instanceof DecisionState) {
			return transitionTo((DecisionState)newState, event);
		}
		else if (newState instanceof ViewState) {
			return transitionFrom((ViewState)newState, event);
		}
		else {
			throw new IllegalArgumentException(
					"Only [ViewState] or [DecisionState] instances can be invoked externally; "
							+ "however the requested state was " + newState);
		}
	}

	protected ViewSelection transitionTo(DecisionState navigationState, Event event) {
		if (logger.isDebugEnabled()) {
			logger.debug("Transitioning to navigation decision state '" + getCurrentState().getId() + "' on event '"
					+ event.getId() + "' that occured in flow " + getFlowExecutionContext().getActiveFlow().getId()
					+ "'");
		}
		setLastEvent(event);
		flowExecution.getListeners().fireEventSignaled(this, navigationState);
		return new Transition((TransitionableState)getCurrentState(), navigationState).execute(this);
	}

	protected ViewSelection transitionFrom(ViewState previousViewState, Event event) {
		if (logger.isDebugEnabled()) {
			logger.debug("Transitioning from previous view state '" + getCurrentState().getId() + "' on event '"
					+ event.getId() + "' that occured in flow " + getFlowExecutionContext().getActiveFlow().getId()
					+ "'");
		}
		setLastEvent(event);
		flowExecution.getListeners().fireEventSignaled(this, previousViewState);
		return previousViewState.getRequiredTransition(this).execute(this);
	}

	public FlowSession endActiveFlowSession() throws IllegalStateException {
		flowExecution.getListeners().fireSessionEnding(this);
		if (logger.isDebugEnabled()) {
			logger.debug("Ending active session " + getFlowExecutionContext().getActiveSession());
		}
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
			parameters.put(event.getStateId(), event.getParameters());
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

		/**
		 * The id of the state that transitioned.
		 */
		private String stateId;

		/**
		 * The event that caused the transition.
		 */
		private Event event;

		/**
		 * Create a new state result event.
		 * @param stateId the state id
		 * @param event the event
		 */
		public StateResultEvent(String stateId, Event event) {
			Assert.hasText(stateId, "The stateId is required");
			this.stateId = stateId;
			this.event = event;
		}

		public boolean containsAttribute(String attributeName) {
			return event.containsAttribute(attributeName);
		}

		public Object getAttribute(String attributeName) {
			return event.getAttribute(attributeName);
		}

		/**
		 * Returns the id of the state that was transitioned out of.
		 */
		public String getStateId() {
			return stateId;
		}

		public Map getParameters() {
			if (event != null) {
				return event.getParameters();
			}
			else {
				return Collections.EMPTY_MAP;
			}
		}

		/**
		 * Returns the event that triggered the state transition.
		 */
		public Event getEvent() {
			return event;
		}

		public String toString() {
			return new ToStringCreator(this).append("stateId", stateId).append("event", event).toString();
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("externalContext", externalContext)
				.append("resultEvents", resultEvents).append("requestScope", requestScope).append(
						"executionProperties", executionProperties).append("flowExecution", flowExecution).toString();
	}
}