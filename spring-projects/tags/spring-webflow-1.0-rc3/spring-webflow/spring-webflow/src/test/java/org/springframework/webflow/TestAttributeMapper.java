package org.springframework.webflow;

class TestAttributeMapper implements FlowAttributeMapper {
	public AttributeMap createFlowInput(RequestContext context) {
		AttributeMap inputMap = new AttributeMap();
		inputMap.put("childInputAttribute", context.getFlowScope().get("parentInputAttribute"));
		return inputMap;
	}

	public void mapFlowOutput(UnmodifiableAttributeMap subflowOutput, RequestContext context) {
		AttributeMap parentAttributes = context.getFlowExecutionContext().getActiveSession().getScope();
		parentAttributes.put("parentOutputAttribute", subflowOutput.get("childInputAttribute"));
	}
}