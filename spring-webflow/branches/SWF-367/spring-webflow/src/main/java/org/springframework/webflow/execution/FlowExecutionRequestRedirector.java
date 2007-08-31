package org.springframework.webflow.execution;

import org.springframework.webflow.core.collection.MutableAttributeMap;

/**
 * A redirector that can instruct clients to perform specific actions.
 * @author Keith Donald
 */
public interface FlowExecutionRequestRedirector {

	/**
	 * Ask the client to redirect to the flow execution with the key provided.
	 * @param key the flow execution key
	 */
	public void sendFlowExecutionRedirect(FlowExecutionKey key);

	/**
	 * Ask the client to launch a new flow execution with the input provided.
	 * @param flowId the flow definition identifier
	 * @param input flow executioninput
	 */
	public void sendFlowDefinitionRedirect(String flowId, MutableAttributeMap input);

	/**
	 * Ask the client to contact the resource at the provided Uri.
	 * @param resourceUri the resource Uri.
	 */
	public void sendExternalRedirect(String resourceUri);

}
