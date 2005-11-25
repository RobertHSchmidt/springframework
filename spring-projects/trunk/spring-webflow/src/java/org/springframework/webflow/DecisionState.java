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

/**
 * A simple decision state that when entered will execute the first transition
 * whose matching criteria evaluates to <code>true</code> in the context of
 * the current request.
 * <p>
 * A decision state is a convenient, simple way to encapsulate reusable state
 * transition logic in one place.
 * 
 * @author Keith Donald
 */
public class DecisionState extends TransitionableState {
	
	/**
	 * Default constructor for bean style usage.
	 * @see TransitionableState#TransitionableState()
	 */
	public DecisionState() {
	}

	/**
	 * Creates a new decision state with the supported set of transitions.
	 * @param flow the owning flow
	 * @param stateId the state identifier (must be unique to the flow)
	 * @param transitions the transitions
	 * @throws IllegalArgumentException when this state cannot be added to given
	 * flow
	 */
	public DecisionState(Flow flow, String stateId, Transition[] transitions) throws IllegalArgumentException {
		super(flow, stateId, transitions);
	}

	/**
	 * Creates a new decision state with the supported set of transitions.
	 * @param flow the owning flow
	 * @param stateId the state identifier (must be unique to the flow)
	 * @param transitions the transitions
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 * flow
	 */
	public DecisionState(Flow flow, String stateId, Transition[] transitions, Map properties)
			throws IllegalArgumentException {
		super(flow, stateId, transitions, properties);
	}

	/**
	 * Specialization of State's <code>doEnter</code> template method that
	 * executes behaviour specific to this state type in polymorphic fashion.
	 * <p>
	 * Simply looks up the first transition that matches the state of the
	 * context and executes it.
	 * @param context the control context for the currently executing flow, used
	 * by this state to manipulate the flow execution
	 * @return a view selection containing model and view information needed to
	 * render the results of the state execution
	 * @throws StateException if an exception occurs in this state
	 */
	protected ViewSelection doEnter(FlowExecutionControlContext context) throws StateException {
		return getRequiredTransition(context).execute(context);
	}
}