package org.springframework.webflow.execution.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.FlowException;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecutionManager;

/**
 * An immutable helper for flow controllers to use launch and resume flow
 * executions using a {@link FlowExecutionManager}.
 * 
 * @author Keith Donald
 */
public class ParameterizedFlowControllerHelper {

	/**
	 * Logger.
	 */
	private static final Log logger = LogFactory.getLog(ParameterizedFlowControllerHelper.class);

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
	public ParameterizedFlowControllerHelper(FlowExecutionManager flowExecutionManager) {
		this(flowExecutionManager, new FlowExecutionManagerParameterExtractor());
	}

	/**
	 * Creates a new flow controller helper.
	 * @param flowExecutionManager the flow execution manager to delegate to.
	 */
	public ParameterizedFlowControllerHelper(FlowExecutionManager flowExecutionManager,
			FlowExecutionManagerParameterExtractor parameterExtractor) {
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
		if (flowExecutionId == null) {
			return flowExecutionManager.launch(parameterExtractor.extractFlowId(context), context);
		}
		else {
			return flowExecutionManager.signalEvent(parameterExtractor.extractEventId(context), flowExecutionId,
					context);
		}
	}
}