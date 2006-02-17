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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.FlowException;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.ResponseInstruction;

/**
 * An immutable helper for flow controllers that encapsulates reusable workflow
 * required to use launch and resume flow executions using a
 * {@link FlowExecutor}.
 * <p>
 * <p>
 * The {@link #handleFlowRequest(ExternalContext)} method is the central helper
 * operation and implements the following algorithm:
 * <ol>
 * <li>Extract the flow execution id by calling
 * {@link FlowExecutorParameterExtractor#extractFlowExecutionKey(ExternalContext)}.</li>
 * <li>If a valid flow execution id was extracted, signal an event in that
 * existing execution. The event to signal is determined by calling the
 * {@link FlowExecutorParameterExtractor#extractEventId(ExternalContext)}
 * method.
 * <li>If no flow execution id was extracted, launch a new flow execution. The
 * top-level flow definition for which an execution is created is determined by
 * extracting the flow id using the
 * {@link FlowExecutorParameterExtractor#extractFlowId(ExternalContext)}. If
 * this parameter parameter is not present, an exception is thrown.</li>
 * 
 * @author Keith Donald
 */
public class FlowRequestHandler {

	/**
	 * Logger.
	 */
	private static final Log logger = LogFactory.getLog(FlowRequestHandler.class);

	/**
	 * The flow execution executor this helper will coordinate with.
	 */
	private FlowExecutor flowExecutor;

	/**
	 * A helper for extracting parameters needed by the flowExecutionManager.
	 */
	private FlowExecutorParameterExtractor parameterExtractor;

	/**
	 * Creates a new flow controller helper.
	 * @param flowExecutor the flow execution manager to delegate to.
	 */
	public FlowRequestHandler(FlowExecutor flowExecutor) {
		this(flowExecutor, new FlowExecutorParameterExtractor());
	}

	/**
	 * Creates a new flow controller helper.
	 * @param flowExecutor the flow execution manager to delegate to.
	 */
	public FlowRequestHandler(FlowExecutor flowExecutor, FlowExecutorParameterExtractor parameterExtractor) {
		Assert.notNull(flowExecutor, "The flow executor is required");
		Assert.notNull(parameterExtractor, "The parameter extractor is required");
		this.flowExecutor = flowExecutor;
		this.parameterExtractor = parameterExtractor;
	}

	/**
	 * Returns the flow execution manager.
	 */
	public FlowExecutor getFlowExecutor() {
		return flowExecutor;
	}

	/**
	 * Returns the parameter extractor strategy.
	 */
	public FlowExecutorParameterExtractor getParameterExtractor() {
		return parameterExtractor;
	}

	/**
	 * Handle a request into the Spring Web Flow system from an external system.
	 * @param context the context in which the request occured.
	 * @return the selected view that should be rendered as a response
	 */
	public ResponseInstruction handleFlowRequest(ExternalContext context) throws FlowException {
		if (logger.isDebugEnabled()) {
			logger.debug("Event signaled in " + context);
		}
		FlowExecutionKey flowExecutionKey = parameterExtractor.extractFlowExecutionKey(context);
		if (flowExecutionKey != null) {
			ResponseInstruction response = flowExecutor.signalEvent(parameterExtractor.extractEventId(context),
					flowExecutionKey, context);
			if (logger.isDebugEnabled()) {
				logger.debug("Returning [resume] " + response);
			}
			return response;
		}
		else {
			String conversationId = parameterExtractor.extractConversationId(context);
			if (StringUtils.hasText(conversationId)) {
				ResponseInstruction response = flowExecutor.getCurrentResponseInstruction(conversationId, context);
				if (logger.isDebugEnabled()) {
					logger.debug("Returning [current] " + response);
				}
				return response;
			}
			else {
				ResponseInstruction response = flowExecutor.launch(parameterExtractor.extractFlowId(context), context);
				if (logger.isDebugEnabled()) {
					logger.debug("Returning [launch] " + response);
				}
				return response;
			}
		}
	}
}