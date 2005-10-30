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

import org.springframework.util.Assert;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.FlowSessionStatus;
import org.springframework.webflow.Scope;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.State;

/**
 * Mock implementation of the <code>FlowSession</code> interface.
 * 
 * @author Erwin Vervaet
 */
public class MockFlowSession implements FlowSession {
	
	private Flow flow;
	
	private State state;
	
	private FlowSessionStatus status = FlowSessionStatus.ACTIVE;
	
	private Scope scope = new Scope(ScopeType.FLOW);
	
	private FlowSession parent;
	
	public MockFlowSession() {
		Flow flow = new Flow("mockFlow");
		new EndState(flow, "end");
		setFlow(flow);
		setCurrentState(flow.getStartState());
	}
	
	public MockFlowSession(Flow flow) {
		setFlow(flow);
		setCurrentState(flow.getStartState());
	}

	public MockFlowSession(Flow flow, Map input) {
		setFlow(flow);
		setCurrentState(flow.getStartState());
		scope.putAll(input);
	}

	public Flow getFlow() {
		return flow;
	}

	/**
	 * Set the flow associated with this flow session.
	 */
	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	public FlowSession getParent() {
		return parent;
	}

	/**
	 * Set the parent flow session of this flow session in the ongoing
	 * flow execution.
	 */
	public void setParent(FlowSession parent) {
		this.parent = parent;
	}

	public boolean isRoot() {
		return this.parent == null;
	}
	
	public Scope getScope() {
		return scope;
	}

	/**
	 * Set the scope data maintained by this flow session. This will
	 * be the flow scope data of the ongoing flow execution. As such, the
	 * given scope should be of type {@link ScopeType#FLOW}.
	 */
	public void setScope(Scope scope) {
		Assert.notNull(scope, "The flow scope is required");
		Assert.isTrue(scope.getScopeType() == ScopeType.FLOW, "The session maintains flow scope data");
		this.scope = scope;
	}

	public State getCurrentState() {
		return state;
	}

	/**
	 * Set the currently active state.
	 */
	public void setCurrentState(State state) {
		this.state = state;
	}

	public FlowSessionStatus getStatus() {
		return status;
	}

	/**
	 * Set the status of this flow session.
	 */
	public void setStatus(FlowSessionStatus status) {
		this.status = status;
	}	
}