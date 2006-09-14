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

import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.FlowExecutionContext;
import org.springframework.webflow.execution.support.LaunchFlowRedirect;

/**
 * Extracts flow executor arguments from the request path.
 * <p>
 * This allows for REST-style URLs to launch flows in the general format:
 * <code>http://${host}/${context path}/${dispatcher path}/${flowId}</code>
 * <p>
 * For example, the url
 * <code>http://localhost/springair/reservation/booking</code> would launch a
 * new execution of the <code>booking</code> flow, assuming a context path of
 * <code>/springair</code> and a servlet mapping of
 * <code>/reservation/*</code>.
 * <p>
 * This also allows for URLS to resume flow execution in the format:
 * <code>http://${host}/${context path}/${dispatcher path}/${key delimiter}/${flowExecutionKey}</code>
 * 
 * Note: this implementation only works with <code>ExternalContext</code>
 * implementations that return a valid
 * {@link ExternalContext#getRequestPathInfo()} such as the
 * {@link ServletExternalContext}.
 * 
 * @author Keith Donald
 */
public class RequestPathFlowExecutorArgumentExtractor extends FlowExecutorArgumentExtractor {

	private static final char PATH_SEPARATOR_CHARACTER = '/';

	private static final String KEY_DELIMITER = "k";

	/**
	 * The delimiter that when present in the requestPathInfo indicates the
	 * flowExecutionKey follows in the URL.
	 * @see #extractFlowExecutionKey(ExternalContext)
	 */
	private String keyDelimiter = KEY_DELIMITER;

	/**
	 * Returns the key delimiter
	 * @return the key delimiter
	 */
	protected String getKeyDelimiter() {
		return keyDelimiter;
	}

	/**
	 * Sets the delimiter that when present in the requestPathInfo indicates the
	 * flowExecutionKey follows in the URL.
	 * @param keyDelimiter the key delimiter.
	 * @see #extractFlowExecutionKey(ExternalContext)
	 */
	public void setKeyDelimiter(String keyDelimiter) {
		this.keyDelimiter = keyDelimiter;
	}

	public boolean isFlowIdPresent(ExternalContext context) {
		String requestPathInfo = getRequestPathInfo(context);
		boolean hasFileName = StringUtils.hasText(WebUtils.extractFilenameFromUrlPath(requestPathInfo));
		return hasFileName || super.isFlowIdPresent(context);
	}

	public String extractFlowId(ExternalContext context) {
		String requestPathInfo = getRequestPathInfo(context);
		String extractedFilename = WebUtils.extractFilenameFromUrlPath(requestPathInfo);
		return StringUtils.hasText(extractedFilename) ? extractedFilename : super.extractFlowId(context);
	}

	public boolean isFlowExecutionKeyPresent(ExternalContext context) {
		String requestPathInfo = getRequestPathInfo(context);
		return requestPathInfo.startsWith(keyPath()) || super.isFlowExecutionKeyPresent(context);
	}

	public String extractFlowExecutionKey(ExternalContext context) throws FlowExecutorArgumentExtractionException {
		String requestPathInfo = getRequestPathInfo(context);
		int index = requestPathInfo.indexOf(keyPath());
		if (index != -1) {
			return requestPathInfo.substring(index + keyPathLength());
		}
		else {
			return super.extractFlowExecutionKey(context);
		}
	}

	public String createFlowUrl(LaunchFlowRedirect flowRedirect, ExternalContext context) {
		StringBuffer flowUrl = new StringBuffer();
		appendFlowExecutorPath(flowUrl, context);
		flowUrl.append(PATH_SEPARATOR_CHARACTER);
		flowUrl.append(flowRedirect.getFlowDefinitionId());
		if (!flowRedirect.getInput().isEmpty()) {
			flowUrl.append('?');
			appendQueryParameters(flowRedirect.getInput(), flowUrl);
		}
		return flowUrl.toString();
	}

	public String createFlowExecutionUrl(String flowExecutionKey, FlowExecutionContext flowExecution,
			ExternalContext context) {
		StringBuffer flowExecutionUrl = new StringBuffer();
		appendFlowExecutorPath(flowExecutionUrl, context);
		flowExecutionUrl.append(PATH_SEPARATOR_CHARACTER);
		flowExecutionUrl.append(keyDelimiter);
		flowExecutionUrl.append(PATH_SEPARATOR_CHARACTER);
		flowExecutionUrl.append(flowExecutionKey);
		return flowExecutionUrl.toString();
	}

	// internal helpers

	/**
	 * Returns the request path info for given external context. Never returns
	 * null, an empty string is returned instead.
	 */
	private String getRequestPathInfo(ExternalContext context) {
		String requestPathInfo = context.getRequestPathInfo();
		return requestPathInfo != null ? requestPathInfo : "";
	}
	
	private String keyPath() {
		return PATH_SEPARATOR_CHARACTER + keyDelimiter + PATH_SEPARATOR_CHARACTER;
	}

	private int keyPathLength() {
		return keyDelimiter.length() + 2;
	}
}