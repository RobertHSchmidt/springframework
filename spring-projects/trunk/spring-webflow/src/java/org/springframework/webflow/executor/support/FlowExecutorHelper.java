package org.springframework.webflow.executor.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.FlowException;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.executor.FlowExecutor;

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
 * {@link FlowExecutorParameterExtractor#extractFlowExecutionId(ExternalContext)}.</li>
 * <li>If a valid flow execution id was extracted, signal an event in that
 * existing execution. The event to signal is determined by calling the
 * {@link FlowExecutorParameterExtractor#extractEventId(ExternalContext)}
 * method.
 * <li>If no flow execution id was extracted, launch a new flow execution. The
 * top-level flow definition for which an execution is created is determined by
 * extracting the flow id using the
 * {@link FlowExecutorParameterExtractor#extractFlowId(ExternalContext)}.
 * If this parameter parameter is not present, an exception is thrown.</li>
 * 
 * @author Keith Donald
 */
public class FlowExecutorHelper {

	/**
	 * Logger.
	 */
	private static final Log logger = LogFactory.getLog(FlowExecutorHelper.class);

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
	public FlowExecutorHelper(FlowExecutor flowExecutor) {
		this(flowExecutor, new FlowExecutorParameterExtractor());
	}

	/**
	 * Creates a new flow controller helper.
	 * @param flowExecutor the flow execution manager to delegate to.
	 */
	public FlowExecutorHelper(FlowExecutor flowExecutor,
			FlowExecutorParameterExtractor parameterExtractor) {
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
	public ViewSelection handleFlowRequest(ExternalContext context) throws FlowException {
		if (logger.isDebugEnabled()) {
			logger.debug("Event signaled in " + context);
		}
		String flowExecutionId = parameterExtractor.extractFlowExecutionId(context);
		if (StringUtils.hasText(flowExecutionId)) {
			return flowExecutor.signalEvent(parameterExtractor.extractEventId(context), flowExecutionId,
					context);
		}
		else {
			return flowExecutor.launch(parameterExtractor.extractFlowId(context), context);
		}
	}
}