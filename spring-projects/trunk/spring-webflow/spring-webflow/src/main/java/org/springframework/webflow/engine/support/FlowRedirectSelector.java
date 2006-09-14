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
package org.springframework.webflow.engine.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.expression.Expression;
import org.springframework.util.StringUtils;
import org.springframework.webflow.engine.ViewSelector;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ViewSelection;
import org.springframework.webflow.execution.support.LaunchFlowRedirect;

/**
 * Makes a {@link LaunchFlowRedirect} response selection when requested, calculating
 * the flowId and flow input by evaluating an expression against the request
 * context.
 * 
 * @see org.springframework.webflow.execution.support.LaunchFlowRedirect
 * 
 * @author Keith Donald
 */
public class FlowRedirectSelector implements ViewSelector {

	/**
	 * The parsed flow expression, evaluatable to the string format:
	 * flowId?param1Name=parmValue&param2Name=paramValue.
	 */
	private Expression expression;

	/**
	 * Creates a new flow redirect selector
	 * @param expression the parsed flow redirect expression, evaluatable to the
	 * string format: flowId?param1Name=parmValue&param2Name=paramValue
	 */
	public FlowRedirectSelector(Expression expression) {
		this.expression = expression;
	}

	public boolean isEntrySelectionRenderable(RequestContext context) {
		return true;
	}

	public ViewSelection makeEntrySelection(RequestContext context) {
		String flowRedirect = (String)expression.evaluateAgainst(context, Collections.EMPTY_MAP);
		if (flowRedirect == null) {
			throw new IllegalStateException("Flow redirect expression evaluated to [null], the expression was " + expression);
		}
		// the encoded flowRedirect should look something like
		// "flowId?param0=value0&param1=value1"
		// now parse that and build a corresponding view selection
		int index = flowRedirect.indexOf('?');
		String flowId;
		Map input = null;
		if (index != -1) {
			flowId = flowRedirect.substring(0, index);
			String[] parameters = StringUtils.delimitedListToStringArray(flowRedirect.substring(index + 1), "&");
			input = new HashMap(parameters.length, 1);
			for (int i = 0; i < parameters.length; i++) {
				String nameAndValue = parameters[i];
				index = nameAndValue.indexOf('=');
				if (index != -1) {
					input.put(nameAndValue.substring(0, index), nameAndValue.substring(index + 1));
				}
				else {
					input.put(nameAndValue, "");
				}
			}
		}
		else {
			flowId = flowRedirect;
		}
		if (!StringUtils.hasText(flowId)) {
			// equivalent to restart
			flowId = context.getFlowExecutionContext().getDefinition().getId();
		}
		return new LaunchFlowRedirect(flowId, input);
	}

	public ViewSelection makeRefreshSelection(RequestContext context) {
		return makeEntrySelection(context);
	}
}