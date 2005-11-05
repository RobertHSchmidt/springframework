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

import org.springframework.core.CollectionFactory;
import org.springframework.core.style.StylerUtils;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * A transitionable state that executes one or more actions when entered. When
 * the action(s) are executed, this state responds to their result(s) to decide
 * where to go next.
 * <p>
 * If more than one action is configured, they are executed in an ordered chain
 * until one returns a result event that matches a valid state transition out of
 * this state. This is a form of the Chain of Responsibility (CoR) pattern.
 * <p>
 * The result of an action's execution is treated as a contributing criterion
 * for a state transition. In addition, anything else in the Flow's
 * <code>RequestContext</code> may be tested as part of custom transitional
 * criteria, allowing for sophisticated transition expressions that reason on
 * contextual state.
 * <p>
 * Each action executed by this action state may be provisioned with a set of
 * arbitrary properties. These properties are made available to the action at
 * execution time.
 * <p>
 * Common action execution properties include:
 * <p>
 * <table border="1">
 * <th>Property</th>
 * <th>Description</th>
 * <tr>
 * <td valign="top">name</td>
 * <td>The 'name' property is used as a qualifier for the action's result
 * event. For example, if an action named <code>myAction</code> returns a
 * <code>success</code> result, a transition for event
 * <code>myAction.success</code> will be searched, and if found, executed. If
 * the action is not named a transition for the base <code>success</code>
 * event will be searched and if found, executed. <br>
 * This is useful in situations where you want to execute actions in an ordered
 * chain as part of one action state, and wish to transition on the result of
 * the last one in the chain. For example:
 * 
 * <pre>
 *     &lt;action-state id=&quot;setupForm&quot;&gt; 
 *         &lt;action name=&quot;setup&quot; bean=&quot;myAction&quot; method=&quot;setupForm&quot;/&gt; 
 *         &lt;action name=&quot;referenceData&quot; bean=&quot;myAction&quot; method=&quot;setupReferenceData&quot;/&gt; 
 *         &lt;transition on=&quot;referenceData.success&quot; to=&quot;displayForm&quot;/&gt; 
 *     &lt;/action-state&gt;
 * </pre>
 * 
 * The above will trigger the execution of the 'setup' action followed by the
 * 'referenceData' action. The flow will then respond to the referenceData
 * 'success' event by transitioning to 'displayForm'. </td>
 * <tr>
 * <td valign="top">method</td>
 * <td> The 'method' property is the name of the method on a
 * <code>{@link org.springframework.webflow.action.MultiAction}</code>
 * implementation to call when this action is executed. The named method must
 * have the signature <code>public Event ${method}(RequestContext)</code>,
 * for example a method property with value <code>setupForm</code> would bind
 * to a method on the MultiAction with the signature:
 * <code>public Event setupForm(RequestContext context)</code>. </td>
 * </tr>
 * </table>
 * 
 * @see org.springframework.webflow.Action
 * @see org.springframework.webflow.action.MultiAction
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ActionState extends TransitionableState {

	/**
	 * The set of actions to be executed when this action state is entered. Each
	 * action is wrapped in a {@link ActionExecutor}.
	 */
	private Set actionExecutors = CollectionFactory.createLinkedSetIfPossible(1);

	/**
	 * Create a new action state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param action the raw target action instance to execute in this state
	 * when entered
	 * @param transitions the transitions out of this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 * flow
	 */
	public ActionState(Flow flow, String id, Action action, Transition[] transitions) throws IllegalArgumentException {
		super(flow, id, transitions);
		addAction(action);
	}

	/**
	 * Create a new action state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param action the raw target action instance to execute in this state
	 * when entered
	 * @param transitions the transitions out of this state
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 * flow
	 */
	public ActionState(Flow flow, String id, Action action, Transition[] transitions, Map properties)
			throws IllegalArgumentException {
		super(flow, id, transitions, properties);
		addAction(action);
	}

	/**
	 * Create a new action state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param actions the raw actions to execute in this state
	 * @param transitions the transitions (paths) out of this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 * flow
	 */
	public ActionState(Flow flow, String id, Action[] actions, Transition[] transitions)
			throws IllegalArgumentException {
		super(flow, id, transitions);
		addActions(actions);
	}

	/**
	 * Create a new action state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param actions the raw actions to execute in this state
	 * @param transitions the transitions (paths) out of this state
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 * flow
	 */
	public ActionState(Flow flow, String id, Action[] actions, Transition[] transitions, Map properties)
			throws IllegalArgumentException {
		super(flow, id, transitions, properties);
		addActions(actions);
	}

	/**
	 * Add a target action instance to this state.
	 * @param action the action to add
	 */
	public void addAction(Action action) {
		this.actionExecutors.add(new ActionExecutor(action));
	}

	/**
	 * Add a collection of target action instances to this state.
	 * @param actions the actions to add
	 */
	public void addActions(Action[] actions) {
		Assert.notEmpty(actions, "You must add at least one action");
		for (int i = 0; i < actions.length; i++) {
			addAction(actions[i]);
		}
	}

	/**
	 * Returns an iterator that lists the set of actions to execute for this
	 * state. It iterates over a collection of {@link ActionExecutor} objects.
	 * @return the ActionExecutor iterator
	 */
	private Iterator actionExecutors() {
		return this.actionExecutors.iterator();
	}

	/**
	 * Returns the number of actions executed by this action state when it is
	 * entered.
	 * @return the action count
	 */
	public int getActionCount() {
		return actionExecutors.size();
	}

	/**
	 * Returns the first action executed by this action state.
	 * @return the first action
	 */
	public Action getAction() {
		return getActions()[0];
	}

	/**
	 * Returns the list of actions executed by this action state.
	 * @return the action list, as a typed array
	 */
	public Action[] getActions() {
		Action[] actions = new Action[actionExecutors.size()];
		int i = 0;
		for (Iterator it = actionExecutors(); it.hasNext();) {
			actions[i++] = ((ActionExecutor)it.next()).getAction();
		}
		return actions;
	}

	/**
	 * Returns the first action executed by this action state with its
	 * annotations. This is purely a convenience method.
	 * @return the annotated first action
	 */
	public AnnotatedAction getAnnotatedAction() {
		return getAnnotatedActions()[0];
	}

	/**
	 * Returns the list of actions executed by this action state with
	 * annotations. This is purely a convenience method.
	 * @return the annotated action list, as a typed array
	 */
	public AnnotatedAction[] getAnnotatedActions() {
		AnnotatedAction[] actions = new AnnotatedAction[actionExecutors.size()];
		int i = 0;
		for (Iterator it = actionExecutors(); it.hasNext();) {
			Action action = ((ActionExecutor)it.next()).getAction();
			if (action instanceof AnnotatedAction) {
				actions[i++] = (AnnotatedAction)action;
			}
			else {
				actions[i++] = new AnnotatedAction(action);
			}
		}
		return actions;
	}

	/*
	 * Overrides getRequiredTransition(RequestContext) to throw a local
	 * NoMatchingActionResultTransitionException if a transition on the
	 * occurence of an action result event cannot be matched. Used to facilitate
	 * an action invocation chain.
	 * @see org.springframework.webflow.TransitionableState#getRequiredTransition(org.springframework.webflow.RequestContext)
	 */
	public Transition getRequiredTransition(RequestContext context) throws NoMatchingTransitionException {
		Transition transition = getTransition(context);
		if (transition == null) {
			throw new NoMatchingActionResultTransitionException(this, context.getLastEvent());
		}
		return transition;
	}

	/**
	 * Specialization of State's <code>doEnter</code> template method that
	 * executes behaviour specific to this state type in polymorphic fashion.
	 * <p>
	 * This implementation iterates over each configured <code>Action</code>
	 * instance and executes it. Execution continues until an
	 * <code>Action</code> returns a result event that matches a state
	 * transition in this request context, or the set of all actions is
	 * exhausted.
	 * @param context the control context for the currently executing flow, used
	 * by this state to manipulate the flow execution
	 * @return a view selection signaling that control should be returned to the
	 * client and a view rendered
	 * @throws StateException if an exception occurs in this state
	 * @throws CannotExecuteTransitionException when no action execution
	 * resulted in a outcome event that could be mapped to a valid state
	 * transition
	 */
	protected ViewSelection doEnter(FlowExecutionControlContext context) throws StateException {
		int executionCount = 0;
		String[] eventIds = new String[actionExecutors.size()];
		Iterator it = actionExecutors();
		while (it.hasNext()) {
			ActionExecutor action = (ActionExecutor)it.next();
			Event event = action.execute(context);
			if (event != null) {
				eventIds[executionCount] = event.getId();
				try {
					return context.signalEvent(event, null);
				}
				catch (NoMatchingActionResultTransitionException e) {
					if (logger.isDebugEnabled()) {
						logger.debug("Action execution [#"
								+ (executionCount + 1)
								+ "] resulted in no matching transition on event '"
								+ event.getId()
								+ "'"
								+ (it.hasNext() ? ": proceeding to the next action in the chain"
										: ": action chain exhausted"));
					}
				}
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Action execution [#"
							+ (executionCount + 1)
							+ "] returned a [null] event"
							+ (it.hasNext() ? ": proceeding to the next action in the chain"
									: ": action chain exhausted"));
				}
				eventIds[executionCount] = null;
			}
			executionCount++;
		}
		if (executionCount > 0) {
			throw new NoMatchingTransitionException(this, context.getLastEvent(),
					"No transition was matched on the event(s) signaled by the " + executionCount
							+ " action(s) that executed in this action state '" + getId() + "' of flow '"
							+ getFlow().getId() + "'; transitions must be defined to handle action result outcomes -- "
							+ "possible flow configuration error? Note: the eventIds signaled were: '"
							+ StylerUtils.style(eventIds)
							+ "', while the supported set of transitional criteria for this action state is '"
							+ StylerUtils.style(getTransitionCriterias()) + "'");
		}
		else {
			throw new IllegalStateException("No actions were executed, thus I cannot execute any state transition "
					+ "-- programmer configuration error; make sure you add at least one action to this state");
		}
	}

	protected void createToString(ToStringCreator creator) {
		creator.append("actions", actionExecutors);
		super.createToString(creator);
	}

	/**
	 * Local "no transition found" exception used to report that an action
	 * result could not be mapped to a state transition.
	 * 
	 * @author Keith Donald
	 */
	private static class NoMatchingActionResultTransitionException extends NoMatchingTransitionException {

		/**
		 * Creates a new exception.
		 * @param state the action state
		 * @param resultEvent the action result event
		 */
		public NoMatchingActionResultTransitionException(ActionState state, Event resultEvent) {
			super(state, resultEvent);
		}
	}
}