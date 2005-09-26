package org.springframework.webflow.access;

import org.springframework.webflow.Flow;

public class NoSuchFlowDefinitionException extends FlowArtifactLookupException {
	public NoSuchFlowDefinitionException(String flowId) {
		this(flowId, null);
	}

	public NoSuchFlowDefinitionException(String flowId, Throwable cause) {
		super(Flow.class, flowId, "Unable to retrieve flow definition with id: '" + flowId + "'", cause);
	}
}
