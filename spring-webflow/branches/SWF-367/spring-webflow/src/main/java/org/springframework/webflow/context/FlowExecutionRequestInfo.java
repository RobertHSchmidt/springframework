package org.springframework.webflow.context;

import org.springframework.util.Assert;
import org.springframework.webflow.core.collection.ParameterMap;

public class FlowExecutionRequestInfo extends AbstractFlowRequestInfo {
	private String flowExecutionKey;

	public FlowExecutionRequestInfo(String flowDefinitionId, String flowExecutionKey) {
		this(flowDefinitionId, flowExecutionKey, null, null, null);
	}

	public FlowExecutionRequestInfo(String flowDefinitionId, String flowExecutionKey, RequestPath requestPath,
			ParameterMap requestParameters, String fragment) {
		super(flowDefinitionId, requestPath, requestParameters, fragment);
		Assert.hasText(flowDefinitionId,
				"The definition identifier of the flow execution to redirect to cannot be null");
		Assert.hasText(flowExecutionKey, "The flow execution key to redirect to cannot be null");
		this.flowExecutionKey = flowExecutionKey;
	}

	public String getFlowExecutionKey() {
		return flowExecutionKey;
	}
}
