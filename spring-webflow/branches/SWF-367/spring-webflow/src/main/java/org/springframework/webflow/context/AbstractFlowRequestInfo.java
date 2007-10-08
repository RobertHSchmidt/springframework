package org.springframework.webflow.context;

import org.springframework.webflow.core.collection.ParameterMap;

public class AbstractFlowRequestInfo {
	private String flowDefinitionId;

	private RequestPath requestPath;

	private ParameterMap requestParameters;

	private String fragment;

	public AbstractFlowRequestInfo(String flowDefinitionId, RequestPath requestPath, ParameterMap requestParameters,
			String fragment) {
		this.flowDefinitionId = flowDefinitionId;
		this.requestPath = requestPath;
		this.requestParameters = requestParameters;
		this.fragment = fragment;
	}

	public String getFlowDefinitionId() {
		return flowDefinitionId;
	}

	public RequestPath getRequestPath() {
		return requestPath;
	}

	public ParameterMap getRequestParameters() {
		return requestParameters;
	}

	public String getFragment() {
		return fragment;
	}
}
