package org.springframework.webflow.manager.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.FlowException;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.manager.FlowExecutionManager;

/**
 * An immutable helper for flow controllers that encapsulates reusable workflow
 * required to use launch and resume flow executions using a
 * {@link FlowExecutionManager}.
 * <p>
 * <p>
 * The {@link #handleFlowRequest(ExternalContext)} method is the central helper
 * operation and implements the following algorithm:
 * <ol>
 * <li>Extract the flow execution id by calling
 * {@link FlowExecutionManagerParameterExtractor#extractFlowExecutionId(ExternalContext)}.</li>
 * <li>If a valid flow execution id was extracted, signal an event in that
 * existing execution. The event to signal is determined by calling the
 * {@link FlowExecutionManagerParameterExtractor#extractEventId(ExternalContext)}
 * method.
 * <li>If no flow execution id was extracted, launch a new flow execution. The
 * top-level flow definition for which an execution is created is determined by
 * extracting the flow id using the
 * {@link FlowExecutionManagerParameterExtractor#extractFlowId(ExternalContext)}.
 * If this parameter parameter is not present, an exception is thrown.</li>
 * 
 * @author Keith Donald
 */
public class FlowExecutionManagerHelper {

	/**
	 * Logger.
	 */
	private static final Log logger = LogFactory.getLog(FlowExecutionManagerHelper.class);

	/**
	 * The flow execution manager this helper will coordinate with.
	 */
	private FlowExecutionManager flowExecutionManager;

	/**
	 * A helper for extracting parameters needed by the flowExecutionManager.
	 */
	private FlowExecutionManagerParameterExtractor parameterExtractor;

	/**
	 * Creates a new flow controller helper.
	 * @param flowExecutionManager the flow execution manager to delegate to.
	 */
	public FlowExecutionManagerHelper(FlowExecutionManager flowExecutionManager) {
		this(flowExecutionManager, new FlowExecutionManagerParameterExtractor());
	}

	/**
	 * Creates a new flow controller helper.
	 * @param flowExecutionManager the flow execution manager to delegate to.
	 */
	public FlowExecutionManagerHelper(FlowExecutionManager flowExecutionManager,
			FlowExecutionManagerParameterExtractor parameterExtractor) {
		Assert.notNull(flowExecutionManager, "The flow execution manager is required");
		Assert.notNull(parameterExtractor, "The parameter extractor is required");
		this.flowExecutionManager = flowExecutionManager;
		this.parameterExtractor = parameterExtractor;
	}

	/**
	 * Returns the flow execution manager.
	 */
	public FlowExecutionManager getFlowExecutionManager() {
		return flowExecutionManager;
	}

	/**
	 * Returns the parameter extractor strategy.
	 */
	public FlowExecutionManagerParameterExtractor getParameterExtractor() {
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
			return flowExecutionManager.signalEvent(parameterExtractor.extractEventId(context), flowExecutionId,
					context);
		}
		else {
			return flowExecutionManager.launch(parameterExtractor.extractFlowId(context), context);
		}
	}
}