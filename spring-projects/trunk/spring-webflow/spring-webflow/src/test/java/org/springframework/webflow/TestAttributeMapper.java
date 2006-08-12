package org.springframework.webflow;

import org.springframework.webflow.core.collection.support.LocalAttributeMap;
import org.springframework.webflow.engine.FlowAttributeMapper;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.support.UnmodifiableAttributeMap;

class TestAttributeMapper implements FlowAttributeMapper {
	public LocalAttributeMap createFlowInput(RequestContext context) {
		LocalAttributeMap inputMap = new LocalAttributeMap();
		inputMap.put("childInputAttribute", context.getFlowScope().get("parentInputAttribute"));
		return inputMap;
	}

	public void mapFlowOutput(UnmodifiableAttributeMap subflowOutput, RequestContext context) {
		LocalAttributeMap parentAttributes = context.getFlowExecutionContext().getActiveSession().getScope();
		parentAttributes.put("parentOutputAttribute", subflowOutput.get("childInputAttribute"));
	}
}