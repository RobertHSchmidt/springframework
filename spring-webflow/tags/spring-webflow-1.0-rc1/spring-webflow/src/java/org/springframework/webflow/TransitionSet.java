/*
 * Copyright 2002-2006 the original author or authors.
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
import java.util.LinkedList;
import java.util.List;

import org.springframework.core.style.StylerUtils;

/**
 * A typed set of transitions, mainly for use internally by artifacts that can
 * apply transition execution logic.
 * 
 * @see TransitionableState#getTransitionSet()
 * @see Flow#getGlobalTransitionSet()
 * 
 * @author Keith Donald
 */
public class TransitionSet {

	/**
	 * The set of transitions.
	 */
	private List transitions = new LinkedList();

	/**
	 * Add a transition to this set.
	 * @param transition the transition to add
	 * @return true if this set's contents changed as a result of the add
	 * operation
	 */
	public boolean add(Transition transition) {
		if (contains(transition)) {
			return false;
		}
		return transitions.add(transition);
	}

	/**
	 * Add a collection of transition instances to this set.
	 * @param transitions the transitions to add
	 * @return true if this set's contents changed as a result of the add
	 * operation
	 */
	public boolean addAll(Transition[] transitions) {
		if (transitions == null) {
			return false;
		}
		boolean changed = false;
		for (int i = 0; i < transitions.length; i++) {
			if (add(transitions[i]) && !changed) {
				changed = true;
			}
		}
		return changed;
	}

	/**
	 * Tests if this transition is in this set.
	 * @param transition the transition
	 * @return true if the transition is contained in this set, false otherwise
	 */
	public boolean contains(Transition transition) {
		return transitions.contains(transition);
	}

	/**
	 * Remove the transition instance from this set.
	 * @param transition the transition to remove
	 * @return true if this list's contents changed as a result of the remove
	 * operation
	 */
	public boolean remove(Transition transition) {
		return transitions.remove(transition);
	}

	/**
	 * Returns the size of this transition set.
	 * @return the exception handler set size.
	 */
	public int size() {
		return transitions.size();
	}

	/**
	 * Convert this set to a typed transition array.
	 * @return the transition set as a typed array
	 */
	public Transition[] toArray() {
		return (Transition[])transitions.toArray(new Transition[transitions.size()]);
	}

	/**
	 * Returns a list of the supported transitional criteria used to match
	 * transitions in this state.
	 * @return the list of transitional criteria
	 */
	public TransitionCriteria[] getTransitionCriterias() {
		TransitionCriteria[] criterias = new TransitionCriteria[transitions.size()];
		int i = 0;
		Iterator it = transitions.iterator();
		while (it.hasNext()) {
			criterias[i++] = ((Transition)it.next()).getMatchingCriteria();
		}
		return criterias;
	}

	/**
	 * Gets a transition for given flow execution request context.
	 * @param context a flow execution context
	 * @return the transition, or null if not found
	 */
	public Transition getTransition(RequestContext context) {
		Iterator it = transitions.iterator();
		while (it.hasNext()) {
			Transition transition = (Transition)it.next();
			if (transition.matches(context)) {
				return transition;
			}
		}
		return null;
	}

	/**
	 * Returns whether or not this state has a transition that will fire for
	 * given flow execution request context.
	 * @param context a flow execution context
	 */
	public boolean transitionMatches(RequestContext context) {
		return getTransition(context) != null;
	}

	public String toString() {
		return StylerUtils.style(transitions);
	}
}