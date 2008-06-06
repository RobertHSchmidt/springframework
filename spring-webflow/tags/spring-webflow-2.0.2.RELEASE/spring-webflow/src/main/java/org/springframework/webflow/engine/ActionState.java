/*
 * Copyright 2004-2008 the original author or authors.
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
package org.springframework.webflow.engine;

import java.util.Iterator;

import org.springframework.core.style.StylerUtils;
import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.ActionExecutor;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.RequestContext;

/**
 * A transitionable state that executes one or more actions when entered. When the action(s) are executed this state
 * responds to their result(s) to decide what state to transition to next.
 * <p>
 * If more than one action is configured they are executed in an ordered chain until one returns a result event that
 * matches a state transition out of this state. This is a form of the Chain of Responsibility (CoR) pattern.
 * <p>
 * The result of an action's execution is typically the criteria for a transition out of this state. Additional
 * information in the current {@link RequestContext} may also be tested as part of custom transitional criteria,
 * allowing for sophisticated transition expressions that reason on contextual state.
 * 
 * @see org.springframework.webflow.execution.Action
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ActionState extends TransitionableState {

	/**
	 * The list of actions to be executed when this state is entered.
	 */
	private ActionList actionList = new ActionList();

	/**
	 * Creates a new action state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @throws IllegalArgumentException when this state cannot be added to given flow, e.g. beasue the id is not unique
	 * @see #getActionList()
	 */
	public ActionState(Flow flow, String id) throws IllegalArgumentException {
		super(flow, id);
	}

	/**
	 * Returns the list of actions executable by this action state. The returned list is mutable.
	 * @return the state action list
	 */
	public ActionList getActionList() {
		return actionList;
	}

	/*
	 * Overrides getRequiredTransition(RequestContext) to throw a local NoMatchingActionResultTransitionException if a
	 * transition on the occurrence of an action result event cannot be matched. Used to facilitate an action invocation
	 * chain. <p>Note that we cannot catch NoMatchingTransitionException since that could lead to unwanted situations
	 * where we're catching an exception that's generated by another state, e.g. because of a configuration error!
	 */
	public Transition getRequiredTransition(RequestContext context) throws NoMatchingTransitionException {
		Transition transition = getTransitionSet().getTransition(context);
		if (transition == null) {
			throw new NoMatchingActionResultTransitionException(this, context.getCurrentEvent());
		}
		return transition;
	}

	/**
	 * Specialization of State's <code>doEnter</code> template method that executes behavior specific to this state
	 * type in polymorphic fashion.
	 * <p>
	 * This implementation iterates over each configured <code>Action</code> instance and executes it. Execution
	 * continues until an <code>Action</code> returns a result event that matches a transition in this request
	 * context, or the set of all actions is exhausted.
	 * @param context the control context for the currently executing flow, used by this state to manipulate the flow
	 * execution
	 * @throws FlowExecutionException if an exception occurs in this state
	 */
	protected void doEnter(RequestControlContext context) throws FlowExecutionException {
		int executionCount = 0;
		String[] eventIds = new String[actionList.size()];
		Iterator it = actionList.iterator();
		while (it.hasNext()) {
			Action action = (Action) it.next();
			Event event = ActionExecutor.execute(action, context);
			if (event != null) {
				eventIds[executionCount] = event.getId();
				try {
					context.handleEvent(event);
					return;
				} catch (NoMatchingActionResultTransitionException e) {
					if (logger.isDebugEnabled()) {
						logger.debug("Action execution ["
								+ (executionCount + 1)
								+ "] resulted in no matching transition on event '"
								+ event.getId()
								+ "'"
								+ (it.hasNext() ? ": proceeding to the next action in the list"
										: ": action list exhausted"));
					}
				}
			} else {
				if (logger.isDebugEnabled()) {
					logger
							.debug("Action execution ["
									+ (executionCount + 1)
									+ "] returned a [null] event"
									+ (it.hasNext() ? ": proceeding to the next action in the list"
											: ": action list exhausted"));
				}
				eventIds[executionCount] = null;
			}
			executionCount++;
		}
		if (executionCount > 0) {
			throw new NoMatchingTransitionException(getFlow().getId(), getId(), context.getCurrentEvent(),
					"No transition was matched on the event(s) signaled by the [" + executionCount
							+ "] action(s) that executed in this action state '" + getId() + "' of flow '"
							+ getFlow().getId() + "'; transitions must be defined to handle action result outcomes -- "
							+ "possible flow configuration error? Note: the eventIds signaled were: '"
							+ StylerUtils.style(eventIds)
							+ "', while the supported set of transitional criteria for this action state is '"
							+ StylerUtils.style(getTransitionSet().getTransitionCriterias()) + "'");
		} else {
			throw new IllegalStateException(
					"No actions were executed, thus I cannot execute any state transition "
							+ "-- programmer configuration error; make sure you add at least one action to this state's action list");
		}
	}

	protected void appendToString(ToStringCreator creator) {
		creator.append("actionList", actionList);
		super.appendToString(creator);
	}

	/**
	 * Local "no transition found" exception used to report that an action result could not be mapped to a state
	 * transition.
	 * @author Keith Donald
	 * @author Erwin Vervaet
	 */
	private static class NoMatchingActionResultTransitionException extends NoMatchingTransitionException {

		/**
		 * Creates a new exception.
		 * @param state the action state
		 * @param resultEvent the action result event
		 */
		public NoMatchingActionResultTransitionException(ActionState state, Event resultEvent) {
			super(state.getFlow().getId(), state.getId(), resultEvent,
					"Cannot find a transition matching an action result event; continuing with next action...");
		}
	}
}