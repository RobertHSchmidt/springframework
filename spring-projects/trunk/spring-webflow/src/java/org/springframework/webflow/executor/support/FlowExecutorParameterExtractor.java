package org.springframework.webflow.executor.support;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import org.springframework.binding.attribute.UnmodifiableAttributeMap;
import org.springframework.binding.format.Formatter;
import org.springframework.core.style.StylerUtils;
import org.springframework.util.Assert;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.executor.FlowExecutionKeyFormatter;
import org.springframework.webflow.executor.FlowExecutor;

/**
 * A strategy for extracting parameters needed by a {@link FlowExecutor} to
 * launch and resume flow executions. Parameters are extracted from a
 * {@link ExternalContext}, an abstraction representing a request into Spring
 * Web Flow from an external system.
 * 
 * @author Keith Donald
 */
public class FlowExecutorParameterExtractor {

	/**
	 * By default, clients can send the id (name) of the flow to be started
	 * using an event parameter with this name ("_flowId").
	 */
	public static final String FLOW_ID_PARAMETER = "_flowId";

	/**
	 * By default, clients can send the flow execution key using a parameter
	 * with this name ("_flowExecutionKey").
	 */
	public static final String FLOW_EXECUTION_KEY_PARAMETER = "_flowExecutionKey";

	/**
	 * By default, clients can send the event to be signaled in a parameter with
	 * this name ("_eventId").
	 */
	public static final String EVENT_ID_PARAMETER = "_eventId";

	/**
	 * By default, clients can send the conversation id using a parameter with
	 * this name ("_conversationId").
	 */
	public static final String CONVERSATION_ID_PARAMETER = "_conversationId";

	/**
	 * The default delimiter used when a parameter value is encoed as part of
	 * the name of an event parameter (e.g. "_eventId_submit").
	 * <p>
	 * This form is typically used to support multiple HTML buttons on a form
	 * without resorting to Javascript to communicate the event that corresponds
	 * to a button.
	 */
	public static final String PARAMETER_VALUE_DELIMITER = "_";

	/**
	 * Event id value indicating that the event has not been set ("@NOT_SET@").
	 */
	public static final String NOT_SET_EVENT_ID = "@NOT_SET@";

	/**
	 * The flow execution context itself will be exposed to the view in a model
	 * attribute with this name ("flowExecutionContext").
	 */
	public static final String FLOW_EXECUTION_CONTEXT_ATTRIBUTE = "flowExecutionContext";

	/**
	 * The string-encoded id of the flow execution will be exposed to the view
	 * in a model attribute with this name ("flowExecutionKey").
	 */
	public static final String FLOW_EXECUTION_KEY_ATTRIBUTE = "flowExecutionKey";

	/**
	 * Identifies a flow definition to launch a new execution for, defaults to
	 * ("_flowId").
	 */
	private String flowIdParameterName = FLOW_ID_PARAMETER;

	/**
	 * The default flowId value that will be returned if no flowId parameter
	 * value can be extracted during {@link #extractFlowId(ExternalContext)}
	 * operation.
	 */
	private String defaultFlowId;

	/**
	 * Identifies an existing flow execution to participate in, defaults
	 * {@link #FLOW_EXECUTION_KEY_PARAMETER }.
	 */
	private String flowExecutionKeyParameterName = FLOW_EXECUTION_KEY_PARAMETER;

	/**
	 * Identifies the id of the flow execution participated in, defaults to
	 * {@link #FLOW_EXECUTION_KEY_ATTRIBUTE }.
	 */
	private String flowExecutionKeyAttributeName = FLOW_EXECUTION_KEY_PARAMETER;

	/**
	 * The formatter that will parse encoded _flowExecutionId strings into
	 * {@link FlowExecutionKey} objects.
	 */
	private Formatter flowExecutionKeyFormatter = new FlowExecutionKeyFormatter();

	/**
	 * Identifies an event that occured in an existing flow execution, defaults
	 * to ("_eventId_submit").
	 */
	private String eventIdParameterName = EVENT_ID_PARAMETER;

	/**
	 * Identifies an existing conversation to redirect to, defaults to
	 * ("_conversationId").
	 */
	private String conversationIdParameterName = CONVERSATION_ID_PARAMETER;

	/**
	 * The embedded parameter name/value delimiter value, used to parse a
	 * parameter value when a value is embedded in a parameter name (e.g.
	 * "_eventId_bar").
	 */
	private String parameterDelimiter = PARAMETER_VALUE_DELIMITER;

	/**
	 * Returns the flow id parameter name.
	 */
	public String getFlowIdParameterName() {
		return flowIdParameterName;
	}

	/**
	 * Sets the flow id parameter name.
	 */
	public void setFlowIdParameterName(String flowIdParameterName) {
		this.flowIdParameterName = flowIdParameterName;
	}

	/**
	 * Returns the default flowId parameter value.
	 */
	public String getDefaultFlowId() {
		return defaultFlowId;
	}

	/**
	 * Sets the default flowId parameter value.
	 * <p>
	 * This value will be used if no flowId parameter value can be extracted
	 * from the request during {@link #extractFlowId(ExternalContext)}
	 * operation.
	 */
	public void setDefaultFlowId(String defaultFlowId) {
		this.defaultFlowId = defaultFlowId;
	}

	/**
	 * Returns the flow execution key parameter name.
	 */
	public String getFlowExecutionKeyParameterName() {
		return flowExecutionKeyParameterName;
	}

	/**
	 * Sets the flow execution key parameter name.
	 */
	public void setFlowExecutionKeyParameterName(String flowExecutionIdParameterName) {
		this.flowExecutionKeyParameterName = flowExecutionIdParameterName;
	}

	/**
	 * Returns the flow execution key attribute name.
	 */
	public String getFlowExecutionKeyAttributeName() {
		return flowExecutionKeyAttributeName;
	}

	/**
	 * Sets the flow execution key attribute name.
	 */
	public void setFlowExecutionKeyAttributeName(String flowExecutionKeyAttributeName) {
		this.flowExecutionKeyAttributeName = flowExecutionKeyAttributeName;
	}

	/**
	 * Returns the continuation key formatting strategy.
	 */
	public Formatter getFlowExecutionKeyFormatter() {
		return flowExecutionKeyFormatter;
	}

	/**
	 * Sets the flow execution continuation key formatting strategy.
	 * @param continuationKeyFormatter the continuation key formatter
	 */
	public void setFlowExecutionKeyFormatter(Formatter continuationKeyFormatter) {
		this.flowExecutionKeyFormatter = continuationKeyFormatter;
	}

	/**
	 * Returns the event id parameter name.
	 */
	public String getEventIdParameterName() {
		return eventIdParameterName;
	}

	/**
	 * Sets the event id parameter name.
	 */
	public void setEventIdParameterName(String eventIdParameterName) {
		this.eventIdParameterName = eventIdParameterName;
	}

	/**
	 * Returns the conversation id parameter name.
	 */
	public String getConversationIdParameterName() {
		return conversationIdParameterName;
	}

	/**
	 * Sets the conversation id parameter name.
	 */
	public void setConversationIdParameterName(String conversationIdParameterName) {
		this.conversationIdParameterName = conversationIdParameterName;
	}

	/**
	 * Returns the embedded eventId parameter delimiter.
	 */
	public String getParameterDelimiter() {
		return parameterDelimiter;
	}

	/**
	 * Sets the embedded eventId parameter delimiter.
	 */
	public void setParameterDelimiter(String parameterDelimiter) {
		this.parameterDelimiter = parameterDelimiter;
	}

	/**
	 * Returns the marker value indicating that the event id parameter was not
	 * set properly in the event because of a view configuration error
	 * ("@NOT_SET@").
	 * <p>
	 * This is useful when a view relies on an dynamic means to set the eventId
	 * event parameter, for example, using javascript. This approach assumes the
	 * "not set" marker value will be a static default (a kind of fallback,
	 * submitted if the eventId does not get set to the proper dynamic value
	 * onClick, for example, if javascript was disabled).
	 */
	public String getNotSetEventIdParameterMarker() {
		return NOT_SET_EVENT_ID;
	}

	/**
	 * Extracts the flow id from the external context. If not found, the
	 * <code>defaultFlowId</code> will be returned instead.
	 * @see #getDefaultFlowId()
	 * @param context the context in which the external user event occurred
	 * @return the obtained id or <code>null</code> if not found
	 */
	public String extractFlowId(ExternalContext context) {
		String flowId = verifySingleStringInputParameter(getFlowIdParameterName(), getParameterMap(context).get(
				getFlowIdParameterName()));
		return (flowId != null ? flowId : getDefaultFlowId());
	}

	/**
	 * Create a URL path to that launches a new execution of the flow that
	 * produced the given response. Used to support the <i>restart flow</i> use
	 * case.
	 * @param flowId the flow id
	 * @param externalContext the external context
	 * @return the relative flow URL path
	 */
	public String createFlowUrl(String flowId, ExternalContext externalContext) {
		return externalContext.getDispatcherPath() + "?" + getFlowIdParameterName() + "=" + flowId;
	}

	/**
	 * Extract the flow execution key from the external context.
	 * @param context the context in which the external user event occured
	 * @return the obtained key or <code>null</code> if not found
	 */
	public FlowExecutionKey extractFlowExecutionKey(ExternalContext context) {
		String flowExecutionId = verifySingleStringInputParameter(getFlowExecutionKeyParameterName(), getParameterMap(
				context).get(getFlowExecutionKeyParameterName()));
		return flowExecutionId != null ? (FlowExecutionKey)flowExecutionKeyFormatter.parseValue(flowExecutionId,
				FlowExecutionKey.class) : null;
	}

	/**
	 * Extract the flow execution event id from the external context.
	 * <p>
	 * By default, this is a multi-step process consisting of:
	 * <ol>
	 * <li>Try the {@link #getEventIdParameterName()} parameter first, if it is
	 * present, return its value as the eventId.
	 * <li>Try a parameter search looking for parameters of the format:
	 * {@link #getEventIdParameterName()}_value. If a match is found, return
	 * the value as the eventId (to support multiple HTML buttons per form
	 * without Javascript).
	 * </ol>
	 * @param context the context in which the external user event occured
	 * @return the event id
	 */
	public String extractEventId(ExternalContext context) throws IllegalArgumentException {
		Object parameter = findParameter(getEventIdParameterName(), getParameterMap(context));
		String eventId = verifySingleStringInputParameter(getEventIdParameterName(), parameter);
		Assert.hasText(eventId, "No eventId could be obtained: make sure the client provides the '"
				+ getEventIdParameterName() + "' parameter as input; "
				+ "the parameters provided for this request were:"
				+ StylerUtils.style(context.getRequestParameterMap()));
		if (eventId.equals(getNotSetEventIdParameterMarker())) {
			throw new IllegalArgumentException("The received eventId was the 'not set' marker '"
					+ getNotSetEventIdParameterMarker()
					+ "' -- this is likely a client view (jsp, etc) configuration error --" + "the '"
					+ getEventIdParameterName() + "' parameter must be set to a valid event");
		}
		return eventId;
	}

	/**
	 * Extract the flow execution conversation id from the external context.
	 * @param context the context in which the external user event occured
	 * @return the conversation id
	 */
	public String extractConversationId(ExternalContext context) {
		return verifySingleStringInputParameter(getConversationIdParameterName(), getParameterMap(context).get(
				getConversationIdParameterName()));
	}

	/**
	 * Create a URL path to that when redirected to renders the <i>current view
	 * selection</i> of the conversation that that produced the given response.
	 * Used to support the <i>conversation redirect</i> use case.
	 * @param conversationId the conversation id
	 * @param context the external context
	 * @return the relative conversation URL path
	 */
	public String createConversationUrl(Serializable conversationId, ExternalContext context) {
		return context.getDispatcherPath() + "?" + getConversationIdParameterName() + "=" + conversationId;
	}

	/**
	 * Add flow execution context attributes to the model under well-defined
	 * names.
	 * @param flowExecutionKey the flow execution key
	 * @param context the flow execution context
	 * @param model the model
	 */
	public void putContextAttributes(FlowExecutionKey flowExecutionKey, FlowExecutionContext context, Map model) {
		model.put(FLOW_EXECUTION_KEY_ATTRIBUTE, flowExecutionKeyFormatter.formatValue(flowExecutionKey));
		model.put(FLOW_EXECUTION_CONTEXT_ATTRIBUTE, context);
	}

	/**
	 * Returns the parameter map this extractor should use. Default
	 * implementations returns the request parameter map. Subclasses may
	 * override.
	 * @param context the external context
	 * @return the parameter map
	 */
	protected Map getParameterMap(ExternalContext context) {
		return context.getRequestParameterMap().getMap();
	}

	// utility methods

	/**
	 * Utility method that makes sure the value for the specified parameter, if
	 * present, is a single valued string.
	 * @param parameterName the parameter name
	 * @param parameterValue the parameter value
	 * @return the string value
	 */
	public String verifySingleStringInputParameter(String parameterName, Object parameterValue) {
		String str = null;
		if (parameterValue != null) {
			try {
				str = (String)parameterValue;
			}
			catch (ClassCastException e) {
				if (parameterValue.getClass().isArray()) {
					throw new IllegalArgumentException("The '" + parameterName
							+ "' parameter was unexpectedly set to an array with values: "
							+ StylerUtils.style(parameterValue) + "; this is likely a view configuration error: "
							+ "make sure you submit a single string value for the '" + parameterName + "' parameter!");
				}
				else {
					throw new IllegalArgumentException("Parameter '" + parameterName
							+ " should have been a single string value but was '" + parameterValue + "' of class + "
							+ parameterValue.getClass());
				}
			}
		}
		return str;
	}

	// support methods

	/**
	 * Obtain a named parameter from the event parameters. This method will try
	 * to obtain a parameter value using the following algorithm:
	 * <ol>
	 * <li>Try to get the parameter value using just the given <i>logical</i>
	 * name. This handles parameters of the form <tt>logicalName = value</tt>.
	 * For normal parameters, e.g. submitted using a hidden HTML form field,
	 * this will return the requested value.</li>
	 * <li>Try to obtain the parameter value from the parameter name, where the
	 * parameter name in the event is of the form
	 * <tt>logicalName_value = xyz</tt> with "_" being the specified
	 * delimiter. This deals with parameter values submitted using an HTML form
	 * submit button.</li>
	 * <li>If the value obtained in the previous step has a ".x" or ".y"
	 * suffix, remove that. This handles cases where the value was submitted
	 * using an HTML form image button. In this case the parameter in the event
	 * would actually be of the form <tt>logicalName_value.x = 123</tt>.
	 * </li>
	 * </ol>
	 * @param logicalParameterName the <i>logical</i> name of the request
	 * parameter
	 * @param parameters the available parameter map
	 * @return the value of the parameter, or <code>null</code> if the
	 * parameter does not exist in given request
	 */
	public Object findParameter(String logicalParameterName, Map parameters) {
		// first try to get it as a normal name=value parameter
		Object value = parameters.get(logicalParameterName);
		if (value != null) {
			return value;
		}
		// if no value yet, try to get it as a name_value=xyz parameter
		String prefix = logicalParameterName + getParameterDelimiter();
		Iterator paramNames = parameters.keySet().iterator();
		while (paramNames.hasNext()) {
			String paramName = (String)paramNames.next();
			if (paramName.startsWith(prefix)) {
				String strValue = paramName.substring(prefix.length());
				// support images buttons, which would submit parameters as
				// name_value.x=123
				if (strValue.endsWith(".x") || strValue.endsWith(".y")) {
					strValue = strValue.substring(0, strValue.length() - 2);
				}
				return strValue;
			}
		}
		// we couldn't find the parameter value
		return null;
	}
}