/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.executor.support;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import org.springframework.core.JdkVersion;
import org.springframework.core.style.StylerUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.execution.FlowExecutionContext;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.support.ExternalRedirect;
import org.springframework.webflow.execution.support.FlowDefinitionRedirect;
import org.springframework.webflow.execution.support.FlowExecutionRedirect;
import org.springframework.webflow.executor.FlowExecutor;

/**
 * A simple helper for extracting {@link FlowExecutor} method input arguments
 * from an request initiated by an {@link ExternalContext}.
 * <p>
 * In addition, after request processing this class supports response generation
 * by creating URLs and context info necessary to execute subsequent HTTP callbacks
 * into Spring Web Flow.
 * <p>
 * By default, this class extracts flow executor method arguments from the
 * {@link ExternalContext#getRequestParameterMap()}.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowExecutorArgumentExtractor {

	// data and behavior related to request argument extraction

	/**
	 * By default clients can send the id of the flow definition to be launched
	 * using a parameter with this name ("_flowId").
	 */
	private static final String FLOW_ID_PARAMETER = "_flowId";

	/**
	 * By default clients can send the key of a flow execution to be resumed
	 * using a parameter with this name ("_flowExecutionKey").
	 */
	private static final String FLOW_EXECUTION_KEY_PARAMETER = "_flowExecutionKey";

	/**
	 * By default clients can send the event to be signaled in a parameter with
	 * this name ("_eventId").
	 */
	private static final String EVENT_ID_PARAMETER = "_eventId";

	/**
	 * The default delimiter used when a parameter value is encoded as part of
	 * the name of a parameter, e.g. "_eventId_submit" ("_").
	 * <p>
	 * This form is typically used to support multiple HTML buttons on a form
	 * without resorting to Javascript to communicate the event that corresponds
	 * to a button.
	 */
	private static final String PARAMETER_VALUE_DELIMITER = "_";

	/**
	 * Identifies a flow definition to launch a new execution for, defaults to
	 * {@link #FLOW_ID_PARAMETER}.
	 */
	private String flowIdParameterName = FLOW_ID_PARAMETER;

	/**
	 * Input parameter that identifies an existing flow execution to participate
	 * in, defaults to {@link #FLOW_EXECUTION_KEY_PARAMETER}.
	 */
	private String flowExecutionKeyParameterName = FLOW_EXECUTION_KEY_PARAMETER;

	/**
	 * Identifies an event that occured in an existing flow execution, defaults
	 * to {@link #EVENT_ID_PARAMETER}.
	 */
	private String eventIdParameterName = EVENT_ID_PARAMETER;

	/**
	 * The embedded parameter name/value delimiter, used to parse a parameter
	 * value when a value is embedded in a parameter name (e.g.
	 * "_eventId_submit"). Defaults to {@link #PARAMETER_VALUE_DELIMITER}.
	 */
	private String parameterValueDelimiter = PARAMETER_VALUE_DELIMITER;

	/**
	 * The flow definition id to use if no flowId parameter value can be
	 * extracted during the {@link #extractFlowId(ExternalContext)} operation.
	 * Default value is <code>null</code>.
	 */
	private String defaultFlowId;

	/**
	 * Returns the flow id parameter name, used to request a flow to launch.
	 */
	public String getFlowIdParameterName() {
		return flowIdParameterName;
	}

	/**
	 * Sets the flow id parameter name, used to request a flow to launch.
	 */
	public void setFlowIdParameterName(String flowIdParameterName) {
		this.flowIdParameterName = flowIdParameterName;
	}

	/**
	 * Returns the flow execution key parameter name, used to request that an
	 * executing conversation resumes.
	 */
	public String getFlowExecutionKeyParameterName() {
		return flowExecutionKeyParameterName;
	}

	/**
	 * Sets the flow execution key parameter name, used to request that an
	 * executing conversation resumes.
	 */
	public void setFlowExecutionKeyParameterName(String flowExecutionKeyParameterName) {
		this.flowExecutionKeyParameterName = flowExecutionKeyParameterName;
	}

	/**
	 * Returns the event id parameter name, used to signal what user action
	 * happened within a paused flow execution.
	 */
	public String getEventIdParameterName() {
		return eventIdParameterName;
	}

	/**
	 * Sets the event id parameter name, used to signal what user action
	 * happened within a paused flow execution.
	 */
	public void setEventIdParameterName(String eventIdParameterName) {
		this.eventIdParameterName = eventIdParameterName;
	}

	/**
	 * Returns the delimiter used to parse a parameter value when a value is
	 * embedded in a parameter name (e.g. "_eventId_submit"). Defaults to
	 * {@link #PARAMETER_VALUE_DELIMITER}.
	 */
	public String getParameterValueDelimiter() {
		return parameterValueDelimiter;
	}

	/**
	 * Set the delimiter used to parse a parameter value when a value is
	 * embedded in a parameter name (e.g. "_eventId_submit"). Defaults to
	 * {@link #PARAMETER_VALUE_DELIMITER}.
	 */
	public void setParameterValueDelimiter(String parameterValueDelimiter) {
		this.parameterValueDelimiter = parameterValueDelimiter;
	}

	/**
	 * Returns the <i>default</i> flowId parameter value. If no flow id
	 * parameter is provided, the default acts as a fallback. Defaults to
	 * <code>null</code>.
	 */
	public String getDefaultFlowId() {
		return defaultFlowId;
	}

	/**
	 * Sets the default flowId parameter value.
	 * <p>
	 * This value will be used if no flowId parameter value can be extracted
	 * from the request by the {@link #extractFlowId(ExternalContext)}
	 * operation.
	 */
	public void setDefaultFlowId(String defaultFlowId) {
		this.defaultFlowId = defaultFlowId;
	}

	/**
	 * Returns true if the flow id is extractable from the context.
	 * @param context the context in which a external user event occured
	 * @return true if extractable, false if not
	 */
	public boolean isFlowIdPresent(ExternalContext context) {
		return context.getRequestParameterMap().contains(getFlowIdParameterName());
	}

	/**
	 * Extracts the flow id from the external context, falling back on the
	 * defaultFlowId (if set) if the flow id could not be extracted.
	 * @param context the context in which a external user event occured
	 * @return the extracted flow id
	 * @throws FlowExecutorArgumentExtractionException if the flow id could not
	 * be extracted and no default value was set
	 */
	public String extractFlowId(ExternalContext context) throws FlowExecutorArgumentExtractionException {
		String flowId = context.getRequestParameterMap().get(getFlowIdParameterName());
		if (flowId == null) {
			flowId = defaultFlowId;
		}
		if (!StringUtils.hasText(flowId)) {
			throw new FlowExecutorArgumentExtractionException(
					"Unable to extract the flow definition id parameter: make sure the client provides the '"
					+ getFlowIdParameterName()
					+ "' parameter as input or set the 'defaultFlowId' property; "
					+ "the parameters provided in this request are: "
					+ StylerUtils.style(context.getRequestParameterMap()));
		}
		return flowId;
	}

	/**
	 * Returns true if the flow execution key is extractable from the context.
	 * @param context the context in which a external user event occured
	 * @return true if extractable, false if not
	 */
	public boolean isFlowExecutionKeyPresent(ExternalContext context) {
		return context.getRequestParameterMap().contains(getFlowExecutionKeyParameterName());
	}

	/**
	 * Extract the flow execution key from the external context.
	 * @param context the context in which the external user event occured
	 * @return the obtained flow execution key
	 * @throws FlowExecutorArgumentExtractionException if the flow execution key
	 * could not be extracted
	 */
	public String extractFlowExecutionKey(ExternalContext context) throws FlowExecutorArgumentExtractionException {
		String encodedKey = context.getRequestParameterMap().get(getFlowExecutionKeyParameterName());
		if (!StringUtils.hasText(encodedKey)) {
			throw new FlowExecutorArgumentExtractionException(
					"Unable to extract the flowExecutionKey parameter: make sure the client provides the '"
					+ getFlowExecutionKeyParameterName()
					+ "' parameter as input; the parameters provided in this request are: "
					+ StylerUtils.style(context.getRequestParameterMap()));
		}
		return encodedKey;
	}

	/**
	 * Returns true if the event id is extractable from the context.
	 * @param context the context in which a external user event occured
	 * @return true if extractable, false if not
	 */
	public boolean isEventIdPresent(ExternalContext context) {
		return StringUtils.hasText(findParameter(getEventIdParameterName(), context.getRequestParameterMap()));
	}

	/**
	 * Extract the flow execution event id from the external context.
	 * <p>
	 * This method should only be called if a {@link FlowExecutionKey} was
	 * successfully extracted, indicating a request to resume a flow execution.
	 * @param context the context in which a external user event occured
	 * @return the event id
	 * @throws FlowExecutorArgumentExtractionException if the event id could not
	 * be extracted
	 */
	public String extractEventId(ExternalContext context) throws FlowExecutorArgumentExtractionException {
		String eventId = findParameter(getEventIdParameterName(), context.getRequestParameterMap());
		if (!StringUtils.hasText(eventId)) {
			throw new FlowExecutorArgumentExtractionException(
					"Unable to extract the eventId parameter: make sure the client provides the '"
					+ getEventIdParameterName() + "' parameter as input along with the '"
					+ getFlowExecutionKeyParameterName()
					+ "' parameter; the parameters provided in this request are: "
					+ StylerUtils.style(context.getRequestParameterMap()));
		}
		return eventId;
	}

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
	 * <tt>logicalName_value = xyz</tt> with "_" being the configured
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
	protected String findParameter(String logicalParameterName, ParameterMap parameters) {
		// first try to get it as a normal name=value parameter
		String value = parameters.get(logicalParameterName);
		if (value != null) {
			return value;
		}
		// if no value yet, try to get it as a name_value=xyz parameter
		String prefix = logicalParameterName + getParameterValueDelimiter();
		Iterator paramNames = parameters.asMap().keySet().iterator();
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

	// data and behavior for response issuance

	/**
	 * The string-encoded id of the flow execution will be exposed to the view
	 * in a model attribute with this name ("flowExecutionKey").
	 */
	private static final String FLOW_EXECUTION_KEY_ATTRIBUTE = "flowExecutionKey";

	/**
	 * The flow execution context itself will be exposed to the view in a model
	 * attribute with this name ("flowExecutionContext").
	 */
	private static final String FLOW_EXECUTION_CONTEXT_ATTRIBUTE = "flowExecutionContext";

	/**
	 * The default URL encoding scheme: UTF-8.
	 */
	private static final String DEFAULT_URL_ENCODING_SCHEME = "UTF-8";

	/**
	 * Model attribute that identifies the flow execution participated in,
	 * defaults to {@link #FLOW_EXECUTION_KEY_ATTRIBUTE}.
	 */
	private String flowExecutionKeyAttributeName = FLOW_EXECUTION_KEY_ATTRIBUTE;

	/**
	 * Model attribute that provides state about the flow execution participated
	 * in, defaults to {@link #FLOW_EXECUTION_CONTEXT_ATTRIBUTE}.
	 */
	private String flowExecutionContextAttributeName = FLOW_EXECUTION_CONTEXT_ATTRIBUTE;

	/**
	 * The url encoding scheme to be used to encode URLs built by this parameter
	 * extractor. Defaults to {@link #DEFAULT_URL_ENCODING_SCHEME}.
	 */
	private String urlEncodingScheme = DEFAULT_URL_ENCODING_SCHEME;

	/**
	 * A flag indicating whether to interpret a redirect URL that starts with a
	 * slash ("/") as relative to the current ServletContext, i.e. as relative
	 * to the web application root, as opposed to absolute. Default is true.
	 */
	private boolean redirectContextRelative = true;

	/**
	 * Returns the flow execution key attribute name, used as a model attribute
	 * for identifying the executing flow being participated in.
	 */
	public String getFlowExecutionKeyAttributeName() {
		return flowExecutionKeyAttributeName;
	}

	/**
	 * Sets the flow execution key attribute name, used as a model attribute for
	 * identifying the current state of the executing flow being participated in
	 * (typically used by view templates during rendering).
	 */
	public void setFlowExecutionKeyAttributeName(String flowExecutionKeyAttributeName) {
		this.flowExecutionKeyAttributeName = flowExecutionKeyAttributeName;
	}

	/**
	 * Put the flow execution key into the provided model map under the
	 * configured model attribute name.
	 * @param flowExecutionKey the flow execution key (may be null if the
	 * conversation has ended).
	 * @param model the model
	 */
	public void put(String flowExecutionKey, Map model) {
		if (flowExecutionKey != null) {
			model.put(getFlowExecutionKeyAttributeName(), flowExecutionKey);
		}
	}

	/**
	 * Returns the flow execution context attribute name.
	 */
	public String getFlowExecutionContextAttributeName() {
		return flowExecutionContextAttributeName;
	}

	/**
	 * Sets the flow execution context attribute name.
	 */
	public void setFlowExecutionContextAttributeName(String flowExecutionContextAttributeName) {
		this.flowExecutionContextAttributeName = flowExecutionContextAttributeName;
	}

	/**
	 * Put the flow execution context into the provided model map under the
	 * configured model attribute name.
	 * @param context the flow execution context
	 * @param model the model
	 */
	public void put(FlowExecutionContext context, Map model) {
		model.put(getFlowExecutionContextAttributeName(), context);
	}

	/**
	 * Returns the url encoding scheme to be used to encode URLs built by this
	 * parameter extractor. Defaults to {@link #DEFAULT_URL_ENCODING_SCHEME}.
	 */
	public String getUrlEncodingScheme() {
		return urlEncodingScheme;
	}

	/**
	 * Set the url encoding scheme to be used to encode URLs built by this
	 * parameter extractor. Defaults to {@link #DEFAULT_URL_ENCODING_SCHEME}.
	 */
	public void setUrlEncodingScheme(String urlEncodingScheme) {
		this.urlEncodingScheme = urlEncodingScheme;
	}

	/**
	 * Set whether to interpret a given redirect URL that starts with a slash
	 * ("/") as relative to the current ServletContext, i.e. as relative to the
	 * web application root.
	 * <p>
	 * Default is "true": A redirect URL that starts with a slash will be
	 * interpreted as relative to the web application root, i.e. the context
	 * path will be prepended to the URL.
	 */
	public void setRedirectContextRelative(boolean redirectContextRelative) {
		this.redirectContextRelative = redirectContextRelative;
	}

	/**
	 * Return whether to interpret a given redirect URL that starts with a slash
	 * ("/") as relative to the current ServletContext, i.e. as relative to the
	 * web application root.
	 */
	public boolean isRedirectContextRelative() {
		return redirectContextRelative;
	}

	/**
	 * Create a URL that when redirected to launches a entirely new execution of
	 * a flow definition (starts a new conversation). Used to support the <i>restart flow</i>
	 * and <i>redirect to flow</i> use cases.
	 * @param flowDefinitionRedirect the flow definition redirect view selection
	 * @param context the external context
	 * @return the relative flow URL path to redirect to
	 */
	public String createFlowDefinitionUrl(FlowDefinitionRedirect flowDefinitionRedirect, ExternalContext context) {
		StringBuffer url = new StringBuffer();
		appendFlowExecutorPath(url, context);
		url.append('?');
		appendQueryParameter(url, getFlowIdParameterName(), flowDefinitionRedirect.getFlowDefinitionId());
		if (!flowDefinitionRedirect.getExecutionInput().isEmpty()) {
			url.append('&');
		}
		appendQueryParameters(url, flowDefinitionRedirect.getExecutionInput());
		return url.toString();
	}

	/**
	 * Create a URL path that when redirected to renders the <i>current</i> (or
	 * last) view selection made by the flow execution identified by the flow
	 * execution key. Used to support the <i>flow execution redirect</i> use
	 * case.
	 * @param flowExecutionKey the flow execution key
	 * @param flowExecution the flow execution
	 * @param context the external context
	 * @return the relative conversation URL path
	 * @see FlowExecutionRedirect
	 */
	public String createFlowExecutionUrl(String flowExecutionKey, FlowExecutionContext flowExecution,
			ExternalContext context) {
		StringBuffer url = new StringBuffer();
		appendFlowExecutorPath(url, context);
		url.append('?');
		appendQueryParameter(url, getFlowExecutionKeyParameterName(), flowExecutionKey);
		return url.toString();
	}

	/**
	 * Create a URL path that when redirected to communicates with an external
	 * system outside of Spring Web Flow.
	 * @param redirect the external redirect request
	 * @param flowExecutionKey the flow execution key to send through the
	 * redirect (optional)
	 * @param context the external context
	 */
	public String createExternalUrl(ExternalRedirect redirect, String flowExecutionKey, ExternalContext context) {
		StringBuffer externalUrl = new StringBuffer();
		if (redirect.getUrl().startsWith("/") && isRedirectContextRelative()) {
			externalUrl.append(context.getContextPath());
		}
		externalUrl.append(redirect.getUrl());
		if (flowExecutionKey != null) {
			boolean first = redirect.getUrl().indexOf('?') < 0;
			if (first) {
				externalUrl.append('?');
			}
			else {
				externalUrl.append('&');
			}
			appendQueryParameter(externalUrl, getFlowExecutionKeyParameterName(), flowExecutionKey);
		}
		return externalUrl.toString();
	}

	// internal helpers

	/**
	 * Append the URL path to the flow executor capable of accepting new
	 * requests.
	 * @param url the url buffer to append to
	 * @param context the context of this request
	 */
	protected void appendFlowExecutorPath(StringBuffer url, ExternalContext context) {
		url.append(context.getContextPath());
		url.append(context.getDispatcherPath());
	}

	/**
	 * Append query parameters to the redirect URL. Stringifies, URL-encodes and
	 * formats model attributes as query parameters.
	 * @param url the StringBuffer to append the parameters to
	 * @param parameters Map that contains attributes
	 */
	protected void appendQueryParameters(StringBuffer url, Map parameters) {
		Iterator entries = parameters.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry)entries.next();
			appendQueryParameter(url, entry.getKey(), entry.getValue());
			if (entries.hasNext()) {
				url.append('&');
			}
		}
	}

	/**
	 * Appends a single query parameter to a URL.
	 * @param url the target url to append to
	 * @param key the parameter name
	 * @param value the parameter value
	 */
	protected void appendQueryParameter(StringBuffer url, Object key, Object value) {
		String encodedKey = urlEncode(key.toString());
		String encodedValue = encodeValue(value);
		url.append(encodedKey).append('=').append(encodedValue);
	}

	/**
	 * URL-encode the given input String with the configured encoding scheme.
	 * @param value the unencoded value
	 * @return the encoded output String
	 */
	protected String encodeValue(Object value) {
		return value != null ? urlEncode(value.toString()) : "";
	}

	/**
	 * URL-encode the given input String with the configured encoding scheme.
	 * <p>
	 * Default implementation uses <code>URLEncoder.encode(input, enc)</code>
	 * on JDK 1.4+, falling back to <code>URLEncoder.encode(input)</code>
	 * (which uses the platform default encoding) on JDK 1.3.
	 * @param input the unencoded input String
	 * @return the encoded output String
	 */
	private String urlEncode(String input) {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return URLEncoder.encode(input);
		}
		try {
			return URLEncoder.encode(input, getUrlEncodingScheme());
		}
		catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Cannot encode URL " + input);
		}
	}
}