/*
 * Copyright 2004-2007 the original author or authors.
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
package org.springframework.webflow.engine.model;

import java.util.LinkedList;

/**
 * Model support for transitionable states.
 * 
 * @author Scott Andrews
 */
public abstract class AbstractTransitionableStateModel extends AbstractStateModel {
	private LinkedList transitions;
	private LinkedList onExitActions;

	/**
	 * @return the transitions
	 */
	public LinkedList getTransitions() {
		return transitions;
	}

	/**
	 * @param transitions the transitions to set
	 */
	public void setTransitions(LinkedList transitions) {
		this.transitions = transitions;
	}

	/**
	 * @param transition the transition to add
	 */
	public void addTransition(TransitionModel transition) {
		if (transition == null) {
			return;
		}
		if (transitions == null) {
			transitions = new LinkedList();
		}
		transitions.add(transition);
	}

	/**
	 * @param transitions the transitions to add
	 */
	public void addTransitions(LinkedList transitions) {
		if (transitions == null || transitions.isEmpty()) {
			return;
		}
		if (this.transitions == null) {
			this.transitions = new LinkedList();
		}
		this.transitions.addAll(transitions);
	}

	/**
	 * @return the on exit actions
	 */
	public LinkedList getOnExitActions() {
		return onExitActions;
	}

	/**
	 * @param onExitActions the on exit actions to set
	 */
	public void setOnExitActions(LinkedList onExitActions) {
		this.onExitActions = onExitActions;
	}

	/**
	 * @param onExitAction the on exit action to add
	 */
	public void addOnExitAction(AbstractActionModel onExitAction) {
		if (onExitAction == null) {
			return;
		}
		if (this.onExitActions == null) {
			this.onExitActions = new LinkedList();
		}
		this.onExitActions.add(onExitAction);
	}

	/**
	 * @param onExitActions the on exit actions to add
	 */
	public void addOnExitsActions(LinkedList onExitActions) {
		if (onExitActions == null || onExitActions.isEmpty()) {
			return;
		}
		if (this.onExitActions == null) {
			this.onExitActions = new LinkedList();
		}
		this.onExitActions.addAll(onExitActions);
	}
}
