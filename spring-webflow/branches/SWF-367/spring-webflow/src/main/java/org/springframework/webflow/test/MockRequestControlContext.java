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
package org.springframework.webflow.test;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.RequestControlContext;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowExecutionContext;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.FlowSessionStatus;

/**
 * Mock implementation of the {@link RequestControlContext} interface to facilitate standalone Flow and State unit
 * tests.
 * 
 * @see org.springframework.webflow.execution.RequestContext
 * @see org.springframework.webflow.execution.FlowSession
 * @see org.springframework.webflow.engine.State
 * 
 * @author Keith Donald
 */
public class MockRequestControlContext extends MockRequestContext implements RequestControlContext {

	private boolean flowExecutionRedirectSent;

	private boolean alwaysRedirectOnPause;

	/**
	 * Creates a new mock request control context for controlling a mock execution of the provided flow definition.
	 * @param flow the flow definition
	 */
	public MockRequestControlContext(Flow flow) {
		super(flow);
	}

	/**
	 * Creates a new mock request control context for controlling a flow execution.
	 * @param flowExecutionContext the flow execution context
	 */
	public MockRequestControlContext(FlowExecutionContext flowExecutionContext) {
		super(flowExecutionContext);
	}

	// implementing RequestControlContext

	public void setCurrentState(State state) {
		State previousState = (State) getCurrentState();
		getMockFlowExecutionContext().getMockActiveSession().setState(state);
		if (previousState == null) {
			getMockFlowExecutionContext().getMockActiveSession().setStatus(FlowSessionStatus.ACTIVE);
		}
	}

	public void start(Flow flow, MutableAttributeMap input) throws IllegalStateException {
		getMockFlowExecutionContext().setActiveSession(new MockFlowSession(flow, input));
		getMockFlowExecutionContext().getMockActiveSession().setStatus(FlowSessionStatus.STARTING);
		flow.start(this, input);
	}

	public void handleEvent(Event event) {
		setLastEvent(event);
		((Flow) getActiveFlow()).handleEvent(this);
	}

	public FlowSession endActiveFlowSession(MutableAttributeMap output) throws IllegalStateException {
		MockFlowSession endingSession = getMockFlowExecutionContext().getMockActiveSession();
		endingSession.getDefinitionInternal().end(this, output);
		endingSession.setStatus(FlowSessionStatus.ENDED);
		getMockFlowExecutionContext().setActiveSession(null);
		return endingSession;
	}

	public void execute(Transition transition) {
		transition.execute((TransitionableState) getCurrentState(), this);
	}

	public void assignFlowExecutionKey() {
	}

	public boolean getAlwaysRedirectOnPause() {
		return alwaysRedirectOnPause;
	}

	public void sendExternalRedirect(String resourceUri) {

	}

	public void sendFlowDefinitionRedirect(String flowId, MutableAttributeMap input) {

	}

	public void sendFlowExecutionRedirect() {
		this.flowExecutionRedirectSent = true;
	}

	public void setAlwaysRedirectOnPause(boolean alwaysRedirectOnPause) {
		this.alwaysRedirectOnPause = alwaysRedirectOnPause;
	}

	public boolean getFlowExecutionRedirectSent() {
		return this.flowExecutionRedirectSent;
	}
}