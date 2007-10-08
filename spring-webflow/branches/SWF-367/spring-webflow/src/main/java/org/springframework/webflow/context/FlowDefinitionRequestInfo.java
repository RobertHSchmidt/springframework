package org.springframework.webflow.context;

import org.springframework.util.Assert;
import org.springframework.webflow.core.collection.ParameterMap;

public class FlowDefinitionRequestInfo extends AbstractFlowRequestInfo {
	public FlowDefinitionRequestInfo(String flowDefinitionId, RequestPath requestPath, ParameterMap requestParameters,
			String fragment) {
		super(flowDefinitionId, requestPath, requestParameters, fragment);
		Assert.hasText(flowDefinitionId, "The id of the flow definition to redirect to is required");
	}
}
