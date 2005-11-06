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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * A transition takes a flow from one state to another when executed. A
 * transition is associated with exactly one source <code>TransitionableState</code>.
 * <p>
 * This class provides a simple implementation of a Transition that offers the
 * following functionality:
 * <ul>
 * <li>Execution of a transition is guarded by a
 * <code>TransitionCriteria</code> object, the so called "matching criteria",
 * which, when matched, makes the transition elligible for execution.</li>
 * <li>Optionally, completion of transition execution is guarded by a
 * <code>TransitionCriteria</code> object, the so called "execution criteria".
 * When the execution criteria test fails, the transition will <i>roll back</i>,
 * reentering its source state. When the execution criteria test
 * succeeds, the transition continues onto the target state.</li>
 * <li>The target state of the transition is specified at configuration time
 * using the target state id.</li>
 * </ul>
 * 
 * @see org.springframework.webflow.TransitionableState
 * @see org.springframework.webflow.TransitionCriteria
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class Transition extends AnnotatedObject {

	/**
	 * Logger, for use in subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The source state that owns this transition.
	 */
	private TransitionableState sourceState;

	/**
	 * The criteria that determine whether or not this transition matches as
	 * elligible for execution.
	 */
	private TransitionCriteria matchingCriteria = WildcardTransitionCriteria.INSTANCE;

	/**
	 * The criteria that determine whether or not this transition, once matched,
	 * should complete execution or should <i>roll back</i>.
	 */
	private TransitionCriteria executionCriteria = WildcardTransitionCriteria.INSTANCE;

	/**
	 * The state id for the target state - used temporarily until the target
	 * state is resolved after flow construction (and all possible states have
	 * been added).
	 */
	private String targetStateId;

	/**
	 * The resolved target state to transition to.
	 */
	private State targetState;

	/**
	 * Create a new transition that always matches and always executes,
	 * transitioning to the specified target state.
	 * @param targetStateId the id of the starget state of the transition
	 */
	public Transition(String targetStateId) {
		setTargetStateId(targetStateId);
	}

	/**
	 * Create a new transition that transitions to the specified state when the
	 * provided criteria matches.
	 * @param matchingCriteria strategy object used to determine if this
	 * transition should be matched as elligible for execution
	 * @param targetStateId the id of the starget state of the transition
	 */
	public Transition(TransitionCriteria matchingCriteria, String targetStateId) {
		setMatchingCriteria(matchingCriteria);
		setTargetStateId(targetStateId);
	}

	/**
	 * Create a new transition that transitions to the specified target state
	 * when the provided criteria matches. Transition execution is guarded using
	 * given execution criteria.
	 * @param matchingCriteria strategy object used to determine if this
	 * transition should be matched as elligible for execution
	 * @param executionCriteria strategy for determining if a matched transition
	 * should complete execution or roll back
	 * @param targetStateId the id of the starget state of the transition
	 */
	public Transition(TransitionCriteria matchingCriteria, TransitionCriteria executionCriteria, String targetStateId) {
		setMatchingCriteria(matchingCriteria);
		setExecutionCriteria(executionCriteria);
		setTargetStateId(targetStateId);
	}

	/**
	 * Create a new transition that transitions to the target state
	 * when the provided criteria matches. Transition execution is guarded using
	 * given execution criteria.
	 * @param matchingCriteria strategy object used to determine if this
	 * transition should be matched as elligible for execution
	 * @param executionCriteria strategy for determining if a matched transition
	 * should complete execution or roll back
	 * @param targetStateId the id of the starget state of the transition
	 * @param properties additional properties describing this transition
	 */
	public Transition(TransitionCriteria matchingCriteria, TransitionCriteria executionCriteria, String targetStateId,
			Map properties) {
		setMatchingCriteria(matchingCriteria);
		setExecutionCriteria(executionCriteria);
		setTargetStateId(targetStateId);
		setProperties(properties);
	}

	/**
	 * Create a new transition at runtime that always matches and always
	 * executes, transitioning to the specified target state from the configured
	 * source state.
	 * @param sourceState the source state
	 * @param targetState the target state
	 */
	public Transition(TransitionableState sourceState, State targetState) {
		setSourceState(sourceState);
		this.targetState = targetState;
	}

	/**
	 * Returns the owning source (<i>from</i>) state of this transition.
	 * @return the source state
	 */
	public TransitionableState getSourceState() {
		return sourceState;
	}

	/**
	 * Set the owning source (<i>from</i>) state of this transition.
	 */
	public void setSourceState(TransitionableState sourceState) {
		Assert.isTrue(getSourceState() == null, "This transition was already added to a source state");
		Assert.notNull(sourceState, "The source state of this transition is required");
		this.sourceState = sourceState;
	}

	/**
	 * Returns the criteria that determine whether or not this transition
	 * matches as elligible for execution.
	 * @return the transition matching criteria
	 */
	public TransitionCriteria getMatchingCriteria() {
		return matchingCriteria;
	}

	/**
	 * Set the criteria that determine whether or not this transition matches as
	 * elligible for execution.
	 */
	public void setMatchingCriteria(TransitionCriteria matchingCriteria) {
		Assert.notNull(matchingCriteria, "The transition matching criteria is required");
		this.matchingCriteria = matchingCriteria;
	}

	/**
	 * Returns the criteria that determine whether or not this transition, once
	 * matched, should complete execution or should <i>roll back</i>.
	 * @return the transition execution criteria
	 */
	public TransitionCriteria getExecutionCriteria() {
		return executionCriteria;
	}

	/**
	 * Set the criteria that determine whether or not this transition, once
	 * matched, should complete execution or should <i>roll back</i>.
	 */
	public void setExecutionCriteria(TransitionCriteria executionCriteria) {
		this.executionCriteria = executionCriteria;
	}

	/**
	 * Returns the id of the target (<i>to</i>) state of this transition.
	 * @return the target state id
	 */
	public String getTargetStateId() {
		if (targetState != null) {
			return targetState.getId();
		}
		else {
			return targetStateId;
		}
	}

	/**
	 * Set the id of the target (<i>to</i>) state of this transtion.
	 */
	public void setTargetStateId(String targetStateId) {
		Assert.hasText(targetStateId, "The id of the target state of the transition is required");
		this.targetStateId = targetStateId;
	}

	/**
	 * Checks if this transition is elligible for execution given the state of
	 * the provided flow execution request context.
	 * @param context the flow execution request context
	 * @return true if this transition should execute, false otherwise
	 */
	public boolean matches(RequestContext context) {
		return getMatchingCriteria().test(context);
	}

	/**
	 * Checks if this transition can complete its execution or should be rolled
	 * back, given the state of the flow execution request context.
	 * @param context the flow execution request context
	 * @return true if this transition can complete execution, false if it
	 * should roll back
	 */
	public boolean canExecute(RequestContext context) {
		return getExecutionCriteria() != null ? getExecutionCriteria().test(context) : true;
	}

	/**
	 * Returns the target state of this transition
	 */
	protected State getTargetState() {
		if (targetState == null) {
			throw new IllegalStateException("The target state of the transition has not been resolved: "
					+ "call resolveTargetState() after the process of building the owning Flow has completed. "
					+ "Transition details: '" + this + "'");
		}
		return targetState;
	}

	/**
	 * Resolve the target State using the configured targetStateId. Sets the
	 * targetState instance variable. This method should be called at
	 * configuration time after Flow building has completed if possible.
	 */
	protected void resolveTargetState() throws NoSuchFlowStateException {
		targetState = getSourceState().getFlow().getState(getTargetStateId());
		if (targetState.equals(sourceState)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Loop detected: the source and target state of transition '" + this
						+ "' are the same -- make sure this is not a bug!");
			}
		}
	}

	/**
	 * Execute this state transition. Will only be called if the
	 * {@link #matches(RequestContext)} method returns true for given context.
	 * @param context the flow execution control context
	 * @return a view selection containing model and view information needed to
	 * render the results of the transition execution
	 * @throws StateException when transition execution fails
	 */
	public ViewSelection execute(FlowExecutionControlContext context) throws StateException {
		ViewSelection selectedView;
		if (canExecute(context)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Executing transition [" + this + "] out of state '" + getSourceState().getId() + "'");
			}
			getSourceState().exit(context);
			context.setLastTransition(this);
			// enter the target state (note: any exceptions are propagated)
			selectedView = getTargetState().enter(context);
		}
		else {
			// 'roll back' and re-enter the source state
			selectedView = getSourceState().reenter(context);
		}
		if (logger.isDebugEnabled()) {
			if (context.getFlowExecutionContext().isActive()) {
				logger.debug("Completed execution of transition [" + this + "]: as a result the new state is '"
						+ context.getFlowExecutionContext().getCurrentState().getId() + "' in flow '"
						+ context.getFlowExecutionContext().getActiveFlow().getId() + "'");
			}
			else {
				logger.debug("Completed execution of transition [" + this + "]: as a result the flow execution has ended");
			}
		}
		return selectedView;
	}

	public String toString() {
		return
			new ToStringCreator(this).append("targetState", getTargetStateId())
				.append("sourceState", getSourceState().getId()).append("matchingCriteria", getMatchingCriteria())
				.append("executionCriteria", getExecutionCriteria()).append("properties", getProperties()).toString();
	}
}