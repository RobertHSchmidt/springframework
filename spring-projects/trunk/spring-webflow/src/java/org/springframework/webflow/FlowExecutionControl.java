package org.springframework.webflow;

import java.util.Map;

import org.springframework.webflow.execution.FlowExecutionListenerList;

/**
 * A control interface for manipulating the state of a flow execution
 * representing a single instance of a web conversation. Used by the Flow class
 * to start and end a FlowExecution.
 * @author Keith Donald
 */
public interface FlowExecutionControl extends FlowExecutionContext {

	/**
	 * Returns the list of listeners attached to this flow execution.
	 * @return the listener list
	 */
	public FlowExecutionListenerList getListeners();

	/**
	 * Create a new flow session and activate it in this flow execution. This
	 * will push the flow session onto the stack and mark it as the active flow
	 * session.
	 * @param context the flow execution request context
	 * @param flow the flow that should be associated with the flow session
	 * @param input the input parameters used to populate the flow session
	 * @return the created and activated flow session
	 */
	public FlowSession activateSession(Flow flow, Map input);

	/**
	 * End the active flow session of this flow execution. This will pop the top
	 * element from the stack and activate the new top flow session.
	 * @return the flow session that ended
	 */
	public FlowSession endActiveFlowSession();

}