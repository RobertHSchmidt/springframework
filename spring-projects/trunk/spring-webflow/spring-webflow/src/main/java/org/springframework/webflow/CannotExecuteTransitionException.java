/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow;

/**
 * Thrown when a state transition could not be executed.
 * 
 * @author Keith Donald
 */
public class CannotExecuteTransitionException extends StateException {
	
	/**
	 * The transition that could not be executed. 
	 */
	private Transition transition;

	/**
	 * Create a new exception.
	 * @param transition the transition that failed to execute
	 */
	public CannotExecuteTransitionException(Transition transition, State state) {
		super(state, "Cannot execute transition " + transition);
		this.transition = transition;
	}

	/**
	 * Returns the transition that could not be executed.
	 * @return the transition
	 */
	public Transition getTransition() {
		return transition;
	}
}