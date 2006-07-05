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
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.support.FlowRedirect;

/**
 * Extracts flow executor arguments from the request path.
 * <p>
 * This allows for REST-style URLs to launch flows in the general format:
 * <code>http://${host}/${context}/${servlet}/${flowId}</code>
 * <p>
 * For example, the url
 * <code>http://localhost/springair/reservation/booking</code> would launch a
 * new execution of the <code>booking</code> flow, assuming a context path of
 * <code>/springair</code> and a servlet mapping of <code>/reservation/*</code>.
 * <p>
 * Note: this implementation only works with <code>ExternalContext</code>
 * implementations that return a valid
 * {@link ExternalContext#getRequestPathInfo()} such as the
 * {@link ServletExternalContext}.
 * 
 * @author Keith Donald
 */
public class RequestPathFlowExecutorArgumentExtractor extends FlowExecutorArgumentExtractor {

	private static final char PATH_SEPARATOR_CHARACTER = '/';

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

	public String createFlowUrl(FlowRedirect flowRedirect, ExternalContext context) {
		StringBuffer flowUrl = new StringBuffer();
		flowUrl.append(context.getContextPath());
		flowUrl.append(context.getDispatcherPath());
		flowUrl.append(PATH_SEPARATOR_CHARACTER);
		flowUrl.append(flowRedirect.getFlowId());
		if (!flowRedirect.getInput().isEmpty()) {
			flowUrl.append('?');
			appendQueryParameters(flowRedirect.getInput(), flowUrl);
		}
		return flowUrl.toString();
	}

	public String createFlowExecutionUrl(String flowExecutionKey, FlowExecutionContext flowExecution,
			ExternalContext context) {
		StringBuffer flowExecutionUrl = new StringBuffer();
		flowExecutionUrl.append(context.getContextPath());
		flowExecutionUrl.append(context.getDispatcherPath());
		flowExecutionUrl.append(PATH_SEPARATOR_CHARACTER);
		flowExecutionUrl.append(flowExecution.getActiveSession().getFlow().getId());
		flowExecutionUrl.append('?');
		appendQueryParameter(getFlowExecutionKeyParameterName(), flowExecutionKey, flowExecutionUrl);
		return flowExecutionUrl.toString();
	}
	
	// internal helpers

	/**
	 * Returns the request path info for given external context.
	 * Never returns null, an empty string is returned instead.
	 */
	private String getRequestPathInfo(ExternalContext context) {
		String requestPathInfo = context.getRequestPathInfo();
		return requestPathInfo != null ? requestPathInfo : "";
	}
}