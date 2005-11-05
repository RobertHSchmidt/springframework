/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow.test;

import java.util.Map;

import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.State;
import org.springframework.webflow.StateContext;
import org.springframework.webflow.ViewSelection;

/**
 * Mock implementation of the <code>StateContext</code> interface to
 * facilitate standalone Flow and State unit tests.
 * <p>
 * NOT intended to be used for anything but standalone unit tests. This
 * is a simple state holder, a <i>stub</i> implementation, at least if you follow <a
 * href="http://www.martinfowler.com/articles/mocksArentStubs.html">Martin
 * Fowler's</a> reasoning. This class is called <i>Mock</i>StateContext to
 * be consistent with the naming convention in the rest of the Spring framework
 * (e.g. MockHttpServletRequest, ...).
 * 
 * @see org.springframework.webflow.RequestContext
 * @see org.springframework.webflow.FlowSession
 * @see org.springframework.webflow.State
 * 
 * @author Keith Donald
 */
public class MockStateContext extends MockRequestContext implements StateContext {
	
	/**
	 * Create a new stub state context.
	 * @param session the active flow session
	 * @param sourceEvent the event originating this request context
	 */
	public MockStateContext(MockFlowSession session, Event sourceEvent) {
		super(session, sourceEvent);
	}

	public ViewSelection start(Flow flow, State startState, Map input) throws IllegalStateException {
		setActiveSession(new MockFlowSession(flow, input));
		return flow.start(startState, this);
	}
	
	public ViewSelection signalEvent(Event event, State state) {
		if (state != null && !getCurrentState().equals(state)) {
			state.enter(this);
		}
		return getActiveFlow().onEvent(event, this);
	}

	public FlowSession endActiveFlowSession() throws IllegalStateException {
		FlowSession endingSession = getActiveSession();
		endingSession.getFlow().end(this);
		setActiveSession(null);
		return endingSession;
	}
}