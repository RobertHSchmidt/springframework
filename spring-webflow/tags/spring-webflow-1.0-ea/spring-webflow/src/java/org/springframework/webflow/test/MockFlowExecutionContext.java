/**
 * 
 */
package org.springframework.webflow.test;

import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.FlowSession;

/**
 * A stub implementation of the flow execution context interface.
 * 
 * @author Keith Donald
 */
public class MockFlowExecutionContext implements FlowExecutionContext {

	private Flow rootFlow;

	private FlowSession activeSession;

	private AttributeMap scope = new AttributeMap();

	/**
	 * Creates a new mock flow execution context--automatically installs a root
	 * flow definition and active flow session.
	 */
	public MockFlowExecutionContext() {
		activeSession = new MockFlowSession();
		this.rootFlow = activeSession.getFlow();
	}

	/**
	 * Creates a new mock flow execution context for the specified root flow
	 * definition.
	 */
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

	public Flow getFlow() {
		return rootFlow;
	}

	public FlowSession getActiveSession() throws IllegalStateException {
		if (activeSession == null) {
			throw new IllegalStateException("No flow session is active");
		}
		return activeSession;
	}

	public AttributeMap getScope() {
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
	public void setScope(AttributeMap scope) {
		this.scope = scope;
	}

	/**
	 * Returns the mock active flow session.
	 */
	public MockFlowSession getMockActiveSession() {
		return (MockFlowSession)activeSession;
	}
}