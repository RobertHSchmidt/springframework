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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.springframework.core.style.ToStringCreator;

/**
 * A typed list of actions, mainly for use internally by artifacts that can
 * execute groups of actions.
 * 
 * @see Flow#getStartActionList()
 * @see State#getEntryActionList()
 * @see ActionState#getActionList()
 * @see TransitionableState#getExitActionList()
 * @see Flow#getEndActionList()
 * 
 * @author Keith Donald
 */
public class ActionList {

	/**
	 * The lists of actions.
	 */
	private List actions = new LinkedList();

	/**
	 * Add an action to this list.
	 * @param action the action to add
	 */
	public boolean add(Action action) {
		return actions.add(action);
	}

	/**
	 * Add a collection of action instances to this list.
	 * @param actions the actions to add
	 */
	public boolean addAll(Action[] actions) {
		if (actions == null) {
			return false;
		}
		return this.actions.addAll(Arrays.asList(actions));
	}

	/**
	 * Tests if this action is in this list.
	 * @param action the action
	 * @return true if the action is contained in this list, false otherwise
	 */
	public boolean contains(Action action) {
		return actions.contains(action);
	}

	/**
	 * Remove a action instance from this list.
	 * @param action the action to add
	 */
	public boolean remove(Action action) {
		return actions.remove(action);
	}

	/**
	 * Returns the size of this action list.
	 * @return the action list size.
	 */
	public int size() {
		return actions.size();
	}

	/**
	 * Returns an iterator that lists the set of actions to execute for this
	 * state. It iterates over a collection of {@link ActionExecutor} objects.
	 * @return the ActionExecutor iterator
	 */
	public Iterator iterator() {
		return actions.iterator();
	}

	/**
	 * Returns the action in this list.
	 * @return the action
	 */
	public Action get(int index) throws IndexOutOfBoundsException {
		return (Action)actions.get(index);
	}

	/**
	 * Returns the action in this list as an annotated action.
	 * @return the action
	 */
	public AnnotatedAction getAnnotated(int index) throws IndexOutOfBoundsException {
		Action action = get(index);
		if (action instanceof AnnotatedAction) {
			System.out.println("Returning " + action);
			return (AnnotatedAction)action;
		}
		else {
			return new AnnotatedAction(action);
		}
	}

	/**
	 * Convert this list to an action array.
	 * @return the action list, as a typed array
	 */
	public Action[] toArray() {
		return (Action[])actions.toArray(new Action[0]);
	}

	/**
	 * Convert this list to a java.util.List.
	 * @return the action list, as a java.util.List
	 */
	public List toList() {
		return Collections.unmodifiableList(actions);
	}

	/**
	 * Returns the list of actions in this list as annotated actions. This is
	 * purely a convenience method.
	 * @return the annotated action list, as a typed array
	 */
	public AnnotatedAction[] toAnnotatedArray() {
		AnnotatedAction[] annotatedActions = new AnnotatedAction[actions.size()];
		for (int i = 0; i < size(); i++) {
			Action action = (Action)get(i);
			if (action instanceof AnnotatedAction) {
				annotatedActions[i] = (AnnotatedAction)action;
			}
			else {
				annotatedActions[i] = new AnnotatedAction(action);
			}
		}
		return annotatedActions;
	}

	/**
	 * Execute the actions in this list - a convenience method.
	 * @param context the action execution request context
	 */
	public void execute(RequestContext context) {
		Iterator it = actions.iterator();
		while (it.hasNext()) {
			Action action = (Action)it.next();
			new ActionExecutor(action).execute(context);
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("actions", actions).toString();
	}
}