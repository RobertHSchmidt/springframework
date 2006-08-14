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
package org.springframework.webflow.test.engine;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.RequestControlContext;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.FlowSessionStatus;
import org.springframework.webflow.execution.ViewSelection;

/**
 * Mock implementation of the <code>FlowControlContext</code> interface to
 * facilitate standalone Flow and State unit tests.
 * <p>
 * NOT intended to be used for anything but standalone unit tests. This is a
 * simple state holder, a <i>stub</i> implementation, at least if you follow <a
 * href="http://www.martinfowler.com/articles/mocksArentStubs.html">Martin
 * Fowler's</a> reasoning. This class is called <i>Mock</i>FlowControlContext
 * to be consistent with the naming convention in the rest of the Spring
 * framework (e.g. MockHttpServletRequest, ...).
 * 
 * @see org.springframework.webflow.execution.RequestContext
 * @see org.springframework.webflow.execution.FlowSession
 * @see org.springframework.webflow.engine.State
 * 
 * @author Keith Donald
 */
public class MockRequestControlContext extends MockRequestContext implements RequestControlContext {

	/**
	 * Creates a new mock control context for controlling a mock execution of the
	 * provided flow definition.
	 */
	public MockRequestControlContext(Flow rootFlow) {
		super(rootFlow);
	}
	
	public ViewSelection start(Flow flow, MutableAttributeMap input) throws IllegalStateException {
		getMockFlowExecutionContext().setActiveSession(new MockFlowSession(flow, input));
		getMockFlowExecutionContext().getMockActiveSession().setStatus(FlowSessionStatus.STARTING);
		ViewSelection selectedView = flow.start(this, input);
		return selectedView;
	}

	public ViewSelection signalEvent(Event event) {
		setLastEvent(event);
		ViewSelection selectedView = ((Flow)getActiveFlow()).onEvent(this);
		return selectedView;
	}

	public FlowSession endActiveFlowSession(MutableAttributeMap output) throws IllegalStateException {
		MockFlowSession endingSession = getMockFlowExecutionContext().getMockActiveSession();
		endingSession.getDefinitionInternal().end(this, output);
		endingSession.setStatus(FlowSessionStatus.ENDED);
		getMockFlowExecutionContext().setActiveSession(null);
		return endingSession;
	}

	public void setCurrentState(State state) {
		getMockFlowExecutionContext().getMockActiveSession().setState(state);
		State previousState = (State)getCurrentState();
		if (previousState == null) {
			getMockFlowExecutionContext().getMockActiveSession().setStatus(FlowSessionStatus.ACTIVE);
		}
	}

	public ViewSelection execute(Transition transition) {
		return transition.execute((TransitionableState)getCurrentState(), this);
	}
}