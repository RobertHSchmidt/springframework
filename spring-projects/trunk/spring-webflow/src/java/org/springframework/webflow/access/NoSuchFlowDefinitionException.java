package org.springframework.webflow.access;

import org.springframework.webflow.Flow;

/**
 * Thrown when no flow definition was found during a lookup operation.
 * @author Keith Donald
 */
public class NoSuchFlowDefinitionException extends NoSuchFlowArtifactException {

	/**
	 * Creates an exception indicating a flow definition could not be found.
	 * @param flowId the flow Id.
	 */
	public NoSuchFlowDefinitionException(String flowId) {
		this(flowId, null);
	}

	/**
	 * Creates an exception indicating a flow definition could not be found.
	 * @param flowId the flow Id.
	 * @param cause the root cause
	 */
	public NoSuchFlowDefinitionException(String flowId, Throwable cause) {
		super(Flow.class, flowId, "No such flow definition with id: '" + flowId + "' found", cause);
	}

	/**
	 * Creates an exception indicating a flow definition could not be found.
	 * @param flowId the flow Id
	 * @param message a custom message
	 * @param cause the root cause
	 */
	public NoSuchFlowDefinitionException(String flowId, String message, Throwable cause) {
		super(Flow.class, flowId, message, cause);
	}

}