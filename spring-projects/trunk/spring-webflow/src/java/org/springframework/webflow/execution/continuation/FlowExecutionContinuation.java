package org.springframework.webflow.execution.continuation;

import java.io.Serializable;

import org.springframework.webflow.execution.FlowExecution;

/**
 * A contract for a flow execution continuation, which represents a snapshot of
 * a user conversation at a point in time relavent to the user.
 * 
 * @author Keith Donald
 */
public interface FlowExecutionContinuation extends Serializable {

	/**
	 * Returns the continuation identifier, guaranteed to be unique in the
	 * context of a logical user conversation.
	 * @return the continuation id
	 */
	public Serializable getId();

	/**
	 * Restores the flow execution representing the state of a conversation at a
	 * point in time relavent to the user.
	 * @return the flow execution
	 */
	public FlowExecution getFlowExecution();
	
	/**
	 * Convert this continuation to a byte array.
	 * @return the continuation as a byte array
	 */
	public byte[] toByteArray();
}