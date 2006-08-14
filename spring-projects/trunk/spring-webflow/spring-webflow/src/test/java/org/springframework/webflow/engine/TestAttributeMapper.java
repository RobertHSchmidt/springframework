package org.springframework.webflow.engine;

import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;

class TestAttributeMapper implements FlowAttributeMapper {
	public MutableAttributeMap createFlowInput(RequestContext context) {
		LocalAttributeMap inputMap = new LocalAttributeMap();
		inputMap.put("childInputAttribute", context.getFlowScope().get("parentInputAttribute"));
		return inputMap;
	}

	public void mapFlowOutput(AttributeMap subflowOutput, RequestContext context) {
		MutableAttributeMap parentAttributes = context.getFlowExecutionContext().getActiveSession().getScope();
		parentAttributes.put("parentOutputAttribute", subflowOutput.get("childInputAttribute"));
	}
}