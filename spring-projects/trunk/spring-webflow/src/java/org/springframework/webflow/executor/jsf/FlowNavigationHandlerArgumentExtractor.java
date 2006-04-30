package org.springframework.webflow.executor.jsf;

import org.springframework.util.StringUtils;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.execution.EventId;
import org.springframework.webflow.executor.support.FlowExecutorArgumentExtractor;

/**
 * An extension of {@link FlowExecutorArgumentExtractor} that is aware of JSF
 * outcomes that communicate requests to launch flow executions and signal event
 * in existing flow executions.
 * 
 * @author Keith Donald
 */
public class FlowNavigationHandlerArgumentExtractor extends FlowExecutorArgumentExtractor {

	/**
	 * The prefix on a logical outcome value that identifies a logical outcome
	 * as the identifier for a new flow execution that should be launched.
	 */
	protected static final String FLOW_ID_PREFIX = "flowId:";

	public boolean isEventIdPresent(ExternalContext context) {
		return StringUtils.hasText(getOutcome(context));
	}

	/*
	 * Overidden to return the eventId from the action outcome string.
	 * @see org.springframework.webflow.manager.support.FlowExecutionManagerParameterExtractor#extractEventId(org.springframework.webflow.ExternalContext)
	 */
	public EventId extractEventId(ExternalContext context) throws IllegalArgumentException {
		return new EventId(getOutcome(context));
	}

	public boolean isFlowIdPresent(ExternalContext context) {
		String outcome = getOutcome(context);
		if (outcome != null && outcome.startsWith(FLOW_ID_PREFIX)) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * Overidden to return the flowId from a JSF outcome in format <code>flowId:${flowId}</code>
	 * @see org.springframework.webflow.manager.support.FlowExecutionManagerParameterExtractor#extractFlowId(org.springframework.webflow.ExternalContext)
	 */
	public String extractFlowId(ExternalContext context) {
		return getOutcome(context).substring(FLOW_ID_PREFIX.length());
	}
	
	private String getOutcome(ExternalContext context) {
		return ((JsfExternalContext)context).getOutcome();
	}
}