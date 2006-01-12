package org.springframework.webflow.manager.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowException;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.manager.FlowExecutionManager;

/**
 * An immutable helper for flow controllers to use launch and resume flow
 * executions using a {@link FlowExecutionManager}.
 * <p>
 * <p>
 * The {@link #handleFlowRequest(ExternalContext)} method is the central facade
 * operation and implements the following algorithm:
 * <ol>
 * <li>Search for a flow execution id in the external context (in a request
 * parameter named {@link #getFlowExecutionIdParameterName()).</li>
 * <li>If no flow execution id was submitted, create a new flow execution. The
 * top-level flow definition for which an execution is created for is determined
 * by the value of the {@link #getFlowIdParameterName()} request parameter. If
 * this parameter parameter is not present, an exception is thrown.</li>
 * <li>If a flow execution id <em>was</em> submitted, load the previously
 * saved FlowExecution with that id from a repository ({@link #getRepository(ExternalContext)}).</li>
 * <li>If a new flow execution was created in the previous steps, start that
 * execution.</li>
 * <li>If an existing flow execution was loaded from a repository, extract the
 * value of the event id ({@link #getEventIdParameterName()). Signal the occurence of the user event, resuming the flow
 * execution in the current state.</li>
 * <li>If the flow execution is still active after event processing, save it
 * out to the repository. This process generates a unique flow execution id that
 * will be exposed to the caller for identifying the same FlowExecution
 * (conversation) on subsequent requests. The caller will also be given access
 * to the flow execution context and any data placed in request or flow scope.</li>
 * </ol>
 * <p>
 * By default, this class will use the flow execution implementation provided by
 * the <code>FlowExecutionImpl</code> class. If you would like to use a
 * different implementation, override the {@link #createFlowExecution(Flow)}
 * method in a subclass.
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
			return flowExecutionManager.launch(parameterExtractor.extractFlowId(context), context);
		}
		else {
			return flowExecutionManager.signalEvent(parameterExtractor.extractEventId(context), flowExecutionId,
					context);
		}
	}
}