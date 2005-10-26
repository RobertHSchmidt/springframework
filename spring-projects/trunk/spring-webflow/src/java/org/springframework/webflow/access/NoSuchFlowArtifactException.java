package org.springframework.webflow.access;

import org.springframework.webflow.Flow;

/**
 * Thrown when a requested flow artifact like a Flow, Action, etc. could not be
 * found found during a lookup operation.
 * @author Keith Donald
 */
public class NoSuchFlowArtifactException extends FlowArtifactLookupException {

	/**
	 * Creates an exception indicating a flow artifact could not be found.
	 * @param artifactType the artifact type
	 * @param id the artifact id
	 * @param cause the root cause
	 */
	public NoSuchFlowArtifactException(Class artifactType, String id, Throwable cause) {
		super(Flow.class, id, "No such artifact of type: " + artifactType + " with id: '" + id + "' found", cause);
	}

	/**
	 * Creates an exception indicating a flow aritfact could not be found.
	 * @param artifactType the artifact type
	 * @param id the artifact id
	 * @param message a custom message
	 * @param cause the root cause
	 */
	public NoSuchFlowArtifactException(Class artifactType, String id, String message, Throwable cause) {
		super(artifactType, id, message, cause);
	}

}