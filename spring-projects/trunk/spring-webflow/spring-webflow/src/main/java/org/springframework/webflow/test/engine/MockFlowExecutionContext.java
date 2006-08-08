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
import org.springframework.webflow.core.collection.support.LocalAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.FlowExecutionContext;
import org.springframework.webflow.execution.FlowSession;

/**
 * A stub implementation of the flow execution context interface.
 * 
 * @author Keith Donald
 */
public class MockFlowExecutionContext implements FlowExecutionContext {

	private FlowDefinition flow;

	private FlowSession activeSession;

	private MutableAttributeMap conversationScope = new LocalAttributeMap();

	/**
	 * Creates a new mock flow execution context--automatically installs a root
	 * flow definition and active flow session.
	 */
	public MockFlowExecutionContext() {
		activeSession = new MockFlowSession();
		this.flow = activeSession.getDefinition();
	}

	/**
	 * Creates a new mock flow execution context for the specified root flow
	 * definition.
	 */
	public MockFlowExecutionContext(Flow rootFlow) {
		this.flow = rootFlow;
		activeSession = new MockFlowSession(rootFlow);
	}

	public String getCaption() {
		return "Mock flow execution context";
	}

	// implementing flow execution context

	public FlowDefinition getFlowDefinition() {
		return flow;
	}

	public boolean isActive() {
		return activeSession != null;
	}

	public FlowSession getActiveSession() throws IllegalStateException {
		if (activeSession == null) {
			throw new IllegalStateException("No flow session is active");
		}
		return activeSession;
	}

	public MutableAttributeMap getConversationScope() {
		return conversationScope;
	}

	/**
	 * Sets the top-level flow definition.
	 */
	public void setFlow(Flow rootFlow) {
		this.flow = rootFlow;
	}

	/**
	 * Sets the mock session to be the <i>active session</i>.
	 */
	public void setActiveSession(FlowSession activeSession) {
		this.activeSession = activeSession;
	}

	/**
	 * Sets flow execution (conversational) scope.
	 */
	public void setConversationScope(MutableAttributeMap scope) {
		this.conversationScope = scope;
	}

	/**
	 * Returns the mock active flow session.
	 */
	public MockFlowSession getMockActiveSession() {
		return (MockFlowSession)activeSession;
	}
}