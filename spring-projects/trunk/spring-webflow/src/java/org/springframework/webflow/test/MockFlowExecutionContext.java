/**
 * 
 */
package org.springframework.webflow.test;

import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.Scope;

/**
 * A stub implementation of the flow execution context interface.
 * 
 * @author Keith Donald
 */
public class MockFlowExecutionContext implements FlowExecutionContext {

	private Flow rootFlow;

	private FlowSession activeSession;

	private Scope scope = new Scope();

	public MockFlowExecutionContext() {
		activeSession = new MockFlowSession();
		this.rootFlow = activeSession.getFlow();
	}
	
	public MockFlowExecutionContext(Flow rootFlow) {
		this.rootFlow = rootFlow;
		activeSession = new MockFlowSession(rootFlow);
	}

	// implementing flow execution statistics
	
	public String getCaption() {
		return "Mock flow execution context";
	}

	public boolean isActive() {
		return activeSession != null;
	}

	// implementing flow execution context
	
	public Flow getRootFlow() {
		return rootFlow;
	}

	public FlowSession getActiveSession() throws IllegalStateException {
		if (activeSession == null) {
			throw new IllegalStateException("No flow session is active");
		}
		return activeSession;
	}

	public Scope getScope() {
		return scope;
	}

	/**
	 * Sets the top-level flow definition.
	 */
	public void setRootFlow(Flow rootFlow) {
		this.rootFlow = rootFlow;
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
	public void setScope(Scope scope) {
		this.scope = scope;
	}

	/**
	 * Returns the mock active flow session.
	 */
	public MockFlowSession getMockActiveSession() {
		return (MockFlowSession)activeSession;
	}
}