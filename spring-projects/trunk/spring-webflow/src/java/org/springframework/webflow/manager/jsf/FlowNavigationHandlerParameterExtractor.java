package org.springframework.webflow.manager.jsf;

import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.manager.support.FlowExecutionManagerParameterExtractor;

public class FlowNavigationHandlerParameterExtractor extends FlowExecutionManagerParameterExtractor {

	/**
	 * Prefix on a logical outcome value that identifies a logical outcome as
	 * the identifier for a web flow that should be entered.
	 */
	protected static final String FLOW_ID_PREFIX = "flowId:";

	/*
	 * Overriden to return the eventId from the actionId
	 * @see org.springframework.webflow.manager.support.FlowExecutionManagerParameterExtractor#extractEventId(org.springframework.webflow.ExternalContext)
	 */
	public String extractEventId(ExternalContext context) throws IllegalArgumentException {
		JsfExternalContext jsf = (JsfExternalContext)context;
		return jsf.getOutcome();
	}

	/*
	 * Overriden to return the flowId from a JSF outcome in format
	 * "flowId:${flowId}"/>
	 * @see org.springframework.webflow.manager.support.FlowExecutionManagerParameterExtractor#extractFlowId(org.springframework.webflow.ExternalContext)
	 */
	public String extractFlowId(ExternalContext context) {
		JsfExternalContext jsf = (JsfExternalContext)context;
		String outcome = jsf.getOutcome();
		if (outcome != null && outcome.startsWith(FLOW_ID_PREFIX)) {
			return outcome.substring(FLOW_ID_PREFIX.length());
		}
		else {
			return null;
		}
	}
}