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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.CollectionFactory;
import org.springframework.core.style.StylerUtils;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A single definition of a web flow.
 * <p>
 * A Flow represents a reusable, self-contained controller module that captures
 * the definition (or blueprint) of a logical page flow within a web
 * application. A page flow is defined as a controlled navigation that guides a
 * user through fulfillment of a business process/goal that takes place over a
 * series of steps (modeled as states).
 * <p>
 * A simple Flow definition could do nothing more than execute an action and
 * display a view (all in one request). A more involved Flow definition may be
 * long-lived (executing accross a series of requests, invoking many possible
 * paths, actions, and subflows).
 * 
 * Note: A flow is not a welcome page, a menu, or an index page: don't use flows
 * for those cases, use simple controllers/actions/portlets instead. Don't use
 * flows where your application demands "free browsing": flows force strict
 * navigation. Especially in Intranet applications, there are often "controlled
 * navigations", where the user is not free to do what he/she wants but must
 * follow the guidelines provided by the system (the quinessential example would
 * be a 'checkout' flow of a shopping cart application). This is a typical use
 * case appropriate for a web flow.
 * <p>
 * Structurally, a Flow is composed of a set of states. A state is a point in
 * the flow where something happens; for example, showing a view, executing an
 * action, spawning a sub flow, or terminating the flow. Different types of
 * states execute different behaiviors.
 * <p>
 * Each state can have transitions that are used to move to another state. A
 * transition is triggered by the occurence of a event. An event is an
 * identifier signaling the occurence of something: e.g. "submit", "back",
 * "success" or "error".
 * <p>
 * Each Flow has exactly one start state. A start state is simply a marker
 * noting the state flow executions (running instances of this Flow) should
 * start in.
 * <p>
 * Instances of this class are typically built by FlowBuilder implementations,
 * but may also be subclassed. This class, and the rest of the web flow core,
 * has been designed with minimal dependencies on other parts of Spring, and is
 * usable in a standalone fashion (as well as in the context of other frameworks
 * like Struts, WebWork, Tapestry, or JSF, for example). The core system is also
 * fully usable outside a HTTP servlet environment, for example in Portlets,
 * tests, or standalone applications.
 * <p>
 * Note: flows are singleton objects so they should be thread-safe!
 * 
 * @see org.springframework.webflow.State
 * @see org.springframework.webflow.TransitionableState
 * @see org.springframework.webflow.Transition
 * @see org.springframework.webflow.ActionState
 * @see org.springframework.webflow.ViewState
 * @see org.springframework.webflow.SubflowState
 * @see org.springframework.webflow.EndState
 * @see org.springframework.webflow.DecisionState
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 * @author Colin Sampaleanu
 */
public class Flow extends AnnotatedObject {

	/**
	 * Name of the property used to indicate if this flow is transactional.
	 */
	public static final String TRANSACTIONAL_PROPERTY = "transactional";

	/**
	 * Name of the property used to indicate the start state in which to start a
	 * flow.
	 */
	public static final String START_STATE_PROPERTY = "startState";

	/**
	 * Logger, for use in subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The flow identifier uniquely identifying this flow among all other flows.
	 */
	private String id;

	/**
	 * The default start state for this flow.
	 */
	private State startState;

	/**
	 * The set of state definitions for this flow.
	 */
	private Set states = CollectionFactory.createLinkedSetIfPossible(6);

	/**
	 * Construct a new flow definition with the given id. The id should be
	 * unique among all flows.
	 * @param id the flow identifier
	 */
	public Flow(String id) {
		setId(id);
	}

	/**
	 * Construct a new flow definition with the given id. The id should be
	 * unique among all flows.
	 * @param id the flow identifier
	 * @param properties additional properties describing the flow
	 */
	public Flow(String id, Map properties) {
		setId(id);
		setProperties(properties);
	}

	/**
	 * Returns the unique id of this flow.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the unique id of this flow.
	 */
	public void setId(String id) {
		Assert.hasText(id, "This flow must have a unique, non-blank identifier");
		this.id = id;
	}

	/**
	 * Add given state definition to this flow definition. Marked protected, as
	 * this method is to be called by the (privileged) state definition classes
	 * themselves during state construction as part of a FlowBuilder invocation.
	 * @param state the state, if already added nothing happens, if another
	 * instance is added with the same id, an exception is thrown
	 * @throws IllegalArgumentException when the state cannot be added to the
	 * flow; specifically, if another state shares the same id as the one
	 * provided
	 */
	protected void add(State state) throws IllegalArgumentException {
		if (this != state.getFlow() && state.getFlow() != null) {
			throw new IllegalArgumentException("State " + state + " cannot be added to this flow '" + getId()
					+ "' -- it already belongs to a different flow");
		}
		if (containsStateInstance(state)) {
			return;
		}
		if (containsState(state.getId())) {
			throw new IllegalArgumentException("This flow '" + getId() + "' already contains a state with id '"
					+ state.getId() + "' -- state ids must be locally unique to the flow definition; "
					+ "existing state-ids of this flow include: " + StylerUtils.style(getStateIds()));
		}
		boolean firstAdd = states.isEmpty();
		this.states.add(state);
		if (firstAdd) {
			setStartState(state);
		}
	}

	/**
	 * Returns the number of states managed by this flow.
	 * @return the state count
	 */
	public int getStateCount() {
		return this.states.size();
	}

	/**
	 * Returns an ordered iterator over the state definitions of this flow. The
	 * order is determined by the order in which the states were added.
	 * @return the states iterator
	 */
	public Iterator statesIterator() {
		return this.states.iterator();
	}

	/**
	 * Returns the list of states in this flow.
	 */
	public State[] getStates() {
		return (State[])this.states.toArray(new State[this.states.size()]);
	}

	/**
	 * Return the start state, throwing an exception if it has not yet been
	 * marked.
	 * @return the start state
	 * @throws IllegalStateException when no start state has been marked
	 */
	public State getStartState() throws IllegalStateException {
		if (startState == null) {
			throw new IllegalStateException(
					"No start state has been set for this flow -- flow builder configuration error?");
		}
		return startState;
	}

	/**
	 * Set the start state for this flow to the state with the provided
	 * <code>stateId</code>; a state must exist by the provided
	 * <code>stateId</code>.
	 * @param stateId the id of the new start state
	 * @throws NoSuchFlowStateException when no state exists with the id you
	 * provided
	 */
	public void setStartState(String stateId) throws IllegalStateException, NoSuchFlowStateException {
		setStartState(getRequiredState(stateId));
	}

	/**
	 * Set the start state for this flow to the state provided; any state may be
	 * the start state.
	 * @param state the new start state
	 * @throws NoSuchFlowStateException given state has not been added to this
	 * flow
	 */
	public void setStartState(State state) throws NoSuchFlowStateException {
		if (!containsStateInstance(state)) {
			throw new NoSuchFlowStateException(this, state.getId());
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Setting start state for flow '" + getId() + "' to '" + state.getId() + "'");
		}
		this.startState = state;
	}

	/**
	 * Checks if given state instance is present in this flow. Does a "same"
	 * (==) check.
	 * @param state the state to search for
	 * @return true if yes (the same instance is present), false otherwise
	 */
	protected boolean containsStateInstance(State state) {
		Iterator it = statesIterator();
		while (it.hasNext()) {
			if (it.next() == state) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Is a state with the provided id present in this flow?
	 * @param stateId the state id
	 * @return true if yes, false otherwise
	 */
	public boolean containsState(String stateId) {
		return getState(stateId) != null;
	}

	/**
	 * Return the state with the provided id, returning <code>null</code> if
	 * no state exists with that id.
	 * @param stateId the state id
	 * @return the state with that id, or null if none exists
	 */
	public State getState(String stateId) {
		if (!StringUtils.hasText(stateId)) {
			throw new IllegalArgumentException(
					"The specified stateId is invalid: state identifiers must be non-blank and unique to the containing flow definition");
		}
		Iterator it = statesIterator();
		while (it.hasNext()) {
			State state = (State)it.next();
			if (state.getId().equals(stateId)) {
				return state;
			}
		}
		return null;
	}

	/**
	 * Return the state with the provided id, throwing a exception if no state
	 * exists with that id.
	 * @param stateId the state id
	 * @return the state with that id
	 * @throws NoSuchFlowStateException when no state exists with that id
	 */
	public State getRequiredState(String stateId) throws NoSuchFlowStateException {
		State state = getState(stateId);
		if (state == null) {
			throw new NoSuchFlowStateException(this, stateId);
		}
		return state;
	}

	/**
	 * Return the <code>TransitionableState</code> with given <id>stateId</id>,
	 * or <code>null</code> when not found.
	 * @param stateId id of the state to look up
	 * @return the transitionable state, or null when not found
	 * @throws IllegalStateException when the identified state is not
	 * transitionable
	 */
	public TransitionableState getTransitionableState(String stateId) throws IllegalStateException {
		State state = getState(stateId);
		if (state != null && !state.isTransitionable()) {
			throw new IllegalStateException("The state '" + stateId + "' of flow '" + getId()
					+ "' must be transitionable");
		}
		return (TransitionableState)state;
	}

	/**
	 * Return the <code>TransitionableState</code> with given <id>stateId</id>,
	 * throwing an exception if not found.
	 * @param stateId id of the state to look up
	 * @return the transitionable state
	 * @throws IllegalStateException when the identified state is not
	 * transitionable
	 * @throws NoSuchFlowStateException when no transitionable state exists by
	 * this id
	 */
	public TransitionableState getRequiredTransitionableState(String stateId) throws IllegalStateException,
			NoSuchFlowStateException {
		TransitionableState state = getTransitionableState(stateId);
		if (state == null) {
			throw new NoSuchFlowStateException(this, stateId);
		}
		return state;
	}

	/**
	 * Convenience accessor that returns an ordered array of the String
	 * <code>ids</code> for the state definitions associated with this flow
	 * definition.
	 * @return the state ids
	 */
	public String[] getStateIds() {
		String[] stateIds = new String[getStateCount()];
		Iterator it = statesIterator();
		int i = 0;
		while (it.hasNext()) {
			stateIds[i++] = ((State)it.next()).getId();
		}
		return stateIds;
	}

	/**
	 * Start a new execution of this flow in the specified state with the
	 * provided input.
	 * @param context the executing state request context
	 */
	public ViewDescriptor start(StateContext context) {
		if (isTransactional()) {
			context.beginTransaction();
		}
		return getStartState(context).enter(context);
	}

	/**
	 * Returns the flow start state, for starting in the current request
	 * context. Allows for dynamic calcuation of the start state at execution
	 * time.
	 * @param context the context
	 * @return the start state
	 */
	protected State getStartState(StateContext context) {
		FlowExecutionContext execution = context.getFlowExecutionContext();
		if (execution.isActive() && execution.getCurrentState() != null) {
			if (execution.getCurrentState().containsProperty(START_STATE_PROPERTY)) {
				return getRequiredState((String)execution.getCurrentState().getProperty(START_STATE_PROPERTY));
			}
		}
		return getStartState();
	}

	/**
	 * Resume an execution of this flow in the current state context.
	 * @param context the state request context
	 */
	public void resume(StateContext context) {
		if (isTransactional()) {
			context.assertInTransaction(false);
		}
	}

	/**
	 * Pause an execution of this flow in the current state context.
	 * @param context the state request context
	 * @param selectedView the view to be rendered to the user while the flow is
	 * paused
	 * @return the selected view
	 */
	public ViewDescriptor pause(StateContext context, ViewDescriptor selectedView) {
		return selectedView;
	}

	/**
	 * End the active session for this flow in the context of the current
	 * request.
	 * @param context the state request context
	 */
	public void end(StateContext context) {
		if (isTransactional()) {
			context.endTransaction();
		}
	}

	/**
	 * Is this flow annotated as transactional?
	 * @return true if yes, false otherwise
	 */
	public boolean isTransactional() {
		return getBooleanProperty(TRANSACTIONAL_PROPERTY, false);
	}

	public String toString() {
		return new ToStringCreator(this).append("id", id).append("startState", startState)
				.append("states", this.states).toString();
	}
}