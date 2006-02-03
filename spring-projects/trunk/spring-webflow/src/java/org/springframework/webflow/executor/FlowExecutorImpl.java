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
package org.springframework.webflow.executor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.format.Formatter;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.FlowException;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKey;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory;
import org.springframework.webflow.execution.repository.SimpleFlowExecutionRepositoryFactory;

/**
 * The default implementation of the central facade for <i>driving</i> the
 * execution of flows within an application.
 * <p>
 * This object is responsible creating and starting new flow executions as
 * requested by clients, as well as signaling events for processing by existing,
 * paused executions (that are waiting to be resumed in response to a user
 * event).
 * <p>
 * This object is a facade or entry point into the Spring Web Flow execution
 * system, and makes the overall system easier to use. The name <i>executor</i>
 * was chosen as <i>executors drive executions</i>.
 * <p>
 * <b>Commonly used configurable properties</b><br>
 * <table border="1">
 * <tr>
 * <td><b>name</b></td>
 * <td><b>description</b></td>
 * <td><b>default</b></td>
 * </tr>
 * <tr>
 * <td>repositoryFactory</td>
 * <td>The strategy for accessing a flow execution repositorie that is used to
 * create, save, and store managed flow executions driven by this executor.</td>
 * <td>A {@link SimpleFlowExecutionRepositoryFactory simple}, stateful
 * server-side session-based repository factory</td>
 * </tr>
 * <tr>
 * <td>continuationKeyFormatter</td>
 * <td>The strategy for formatting continuation keys that identified persisted
 * flow executions represented the state of a conversation at a point in time.</td>
 * <td>The default {@link FlowExecutionContinuationKeyFormatter}</td>
 * </tr>
 * <tr>
 * <td>alwaysRedirectOnPause</td>
 * <td>A flag indicating if this executor should <i>always</i> request a
 * <i>redirect to conversation</i> after pausing an active flow execution.</td>
 * <td>false</td>
 * </tr>
 * </table>
 * </p>
 * @see org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory
 * @see org.springframework.webflow.execution.support.FlowExecutionContinuationKeyFormatter
 * @see org.springframework.webflow.execution.FlowExecution
 * @see org.springframework.webflow.ViewSelection
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class FlowExecutorImpl implements FlowExecutor {

	/**
	 * The flow execution context itself will be exposed to the view in a model
	 * attribute with this name ("flowExecutionContext").
	 */
	public static final String FLOW_EXECUTION_CONTEXT_ATTRIBUTE = "flowExecutionContext";

	/**
	 * The string-encoded id of the flow execution will be exposed to the view
	 * in a model attribute with this name ("flowExecutionId").
	 */
	public static final String FLOW_EXECUTION_ID_ATTRIBUTE = "flowExecutionId";

	/**
	 * Logger, usable by subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The flow execution repository factoring, for obtaining repository
	 * instances to save paused executions that require user input and load
	 * resuming executions that will process user events.
	 * <p>
	 * The default value is the {@link SimpleFlowExecutionRepositoryFactory}
	 * repository factory that creates repositories within the user session map.
	 */
	private FlowExecutionRepositoryFactory repositoryFactory;

	/**
	 * The formatter that will parse encoded _flowExecutionId strings into
	 * {@link FlowExecutionContinuationKey} objects.
	 */
	private Formatter continuationKeyFormatter = new FlowExecutionContinuationKeyFormatter();

	/**
	 * A flag indicating if this executor should <i>always</i> request a
	 * <i>redirect to conversation</i> after pausing an active flow execution.
	 * <p>
	 * This allows the user to participate in the current state of the
	 * conversation using a bookmarkable URL.
	 */
	private boolean alwaysRedirectOnPause;

	/**
	 * Create a new flow executor that uses the repository factory to access a
	 * repository to create, save, and restore managed flow executions driven by
	 * this executor.
	 * @param repositoryFactory the repository factory
	 */
	public FlowExecutorImpl(FlowExecutionRepositoryFactory repositoryFactory) {
		setRepositoryFactory(repositoryFactory);
	}

	/**
	 * Convenience constructor that configures the default
	 * {@link FlowExecutionRepositoryFactory} implementation with the provided
	 * flow locator. This locator is responsible for loading flow definitions as
	 * needed by the repository to support execution creation.
	 * @param flowLocator the flow locator to use
	 * @see #setFlowLocator(FlowLocator)
	 */
	public FlowExecutorImpl(FlowLocator flowLocator) {
		setFlowLocator(flowLocator);
	}

	/**
	 * Set the flow locator to use for lookup of flow definitions to execute.
	 */
	public void setFlowLocator(FlowLocator flowLocator) {
		this.repositoryFactory = new SimpleFlowExecutionRepositoryFactory(flowLocator);
	}

	/**
	 * Returns the repository factory in use by this flow executor.
	 */
	public FlowExecutionRepositoryFactory getRepositoryFactory() {
		return repositoryFactory;
	}

	/**
	 * Set the repository factory to use for accessing a repository to create,
	 * save, and restore managed flow executions driven by this executor.
	 */
	public void setRepositoryFactory(FlowExecutionRepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
	}

	/**
	 * Returns the continuation key formatting strategy.
	 */
	public Formatter getContinuationKeyFormatter() {
		return continuationKeyFormatter;
	}

	/**
	 * Sets the flow execution continuation key formatting strategy.
	 * @param continuationKeyFormatter the continuation key formatter
	 */
	public void setContinuationKeyFormatter(Formatter continuationKeyFormatter) {
		this.continuationKeyFormatter = continuationKeyFormatter;
	}

	/**
	 * Returns the flag indicating if this executor should always request a
	 * <i>redirect to conversation</i> after pausing an active flow execution.
	 * <p>
	 * This allows the user to participate in the current view-state of a
	 * conversation using a bookmarkable URL.
	 */
	public boolean isAlwaysRedirectOnPause() {
		return alwaysRedirectOnPause;
	}

	/**
	 * Sets the flag indicating if this executor should always request a
	 * <i>redirect to conversation</i> after pausing an active flow execution.
	 * <p>
	 * If set to <code>true</code> this executor will always request a
	 * redirect. If this is set to <code>false</code> this executor will only
	 * request a redirect if the entered flow view state did so.
	 * <p>
	 * This allows the user to participate in the current view-state of a
	 * conversation using a bookmarkable URL.
	 */
	public void setAlwaysRedirectOnPause(boolean alwaysRedirectOnPause) {
		this.alwaysRedirectOnPause = alwaysRedirectOnPause;
	}

	public ViewSelection launch(String flowId, ExternalContext context) throws FlowException {
		FlowExecutionRepository repository = getRepository(context);
		FlowExecution flowExecution = repository.createFlowExecution(flowId);
		ViewSelection selectedView = flowExecution.start(context);
		if (flowExecution.isActive()) {
			FlowExecutionContinuationKey continuationKey = repository.generateContinuationKey(flowExecution);
			repository.putFlowExecution(continuationKey, flowExecution);
			return prepareSelectedView(selectedView, context, repository, continuationKey, flowExecution);
		}
		else {
			return selectedView;
		}
	}

	public ViewSelection signalEvent(String eventId, String flowExecutionId, ExternalContext context)
			throws FlowException {
		FlowExecutionRepository repository = getRepository(context);
		FlowExecutionContinuationKey continuationKey = parseContinuationKey(flowExecutionId);
		FlowExecution flowExecution = repository.getFlowExecution(continuationKey);
		ViewSelection selectedView = flowExecution.signalEvent(eventId, context);
		if (flowExecution.isActive()) {
			continuationKey = repository.generateContinuationKey(flowExecution, continuationKey.getConversationId());
			repository.putFlowExecution(continuationKey, flowExecution);
			return prepareSelectedView(selectedView, context, repository, continuationKey, flowExecution);
		}
		else {
			repository.invalidateConversation(continuationKey.getConversationId());
			return selectedView;
		}
	}

	public ViewSelection getCurrentViewSelection(String conversationId, ExternalContext context) throws FlowException {
		FlowExecutionRepository repository = getRepository(context);
		FlowExecutionContinuationKey continuationKey = repository.getCurrentContinuationKey(conversationId);
		ViewSelection currentViewSelection = repository.getCurrentViewSelection(conversationId);
		return createForward(currentViewSelection, continuationKey, repository.getFlowExecution(continuationKey));
	}

	/**
	 * Returns the repository retrieved by the configured
	 * {@link FlowExecutionRepositoryFactory}.
	 */
	protected FlowExecutionRepository getRepository(ExternalContext context) {
		return repositoryFactory.getRepository(context);
	}

	/**
	 * Helper to parse a flow execution continuation key from its string-encoded
	 * representation.
	 * @param flowExecutionId the string encoded key
	 * @return the parsed key
	 */
	protected FlowExecutionContinuationKey parseContinuationKey(String flowExecutionId) {
		return (FlowExecutionContinuationKey)continuationKeyFormatter.parseValue(flowExecutionId,
				FlowExecutionContinuationKey.class);
	}

	/**
	 * Perform any processing necessary before the view selection made is
	 * returned to the client of the flow execution manager and rendered out.
	 * This implementation adds a number of <i>infrastructure attributes</i> to
	 * the model that will be exposed to the view so clients may record
	 * information about the flow to support participation in the flow on a
	 * subsequent request. More specifically, this method will add the
	 * {@link #FLOW_EXECUTION_CONTEXT_ATTRIBUTE},
	 * {@link #FLOW_EXECUTION_ID_ATTRIBUTE}.
	 * @param selectedView the view selection to be prepared
	 * @param externalContext the externalContext that called into Spring Web
	 * Flow
	 * @param continuationKey the assigned repository continuation key
	 * @param flowExecutionContext the flow context providing info about the
	 * flow execution
	 * @return the prepped view selection
	 */
	protected ViewSelection prepareSelectedView(ViewSelection selectedView, ExternalContext externalContext,
			FlowExecutionRepository repository, FlowExecutionContinuationKey continuationKey,
			FlowExecutionContext flowExecutionContext) {
		if (flowExecutionContext.isActive()) {
			if (isAlwaysRedirectOnPause() || selectedView.isRedirect()) {
				// it's a redirect from a view state of a active flow execution,
				// save view selection to repository as the "current view
				// selection" and request redirection to the conversation URI
				repository.setCurrentViewSelection(continuationKey.getConversationId(), selectedView);
				if (logger.isDebugEnabled()) {
					logger.debug("Returning redirect view to client " + selectedView);
				}
				return createRedirect(continuationKey.getConversationId(), externalContext);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Returning view to client " + selectedView);
				}
				return createForward(selectedView, continuationKey, flowExecutionContext);
			}
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Returning confirmation view to client " + selectedView);
			}
			return selectedView;
		}
	}

	protected ViewSelection createRedirect(Serializable conversationId, ExternalContext context) {
		String viewName = context.getDispatcherPath() + "/_c" + conversationId;
		return new ViewSelection(viewName, null, true);
	}

	protected ViewSelection createForward(ViewSelection selectedView, FlowExecutionContinuationKey continuationKey,
			FlowExecutionContext flowExecutionContext) {
		// it's a forward from a view state of a active flow execution,
		// expose model and context attributes.
		Map model = new HashMap(selectedView.getModel().size() + 2);
		// expose all model attributes from the original view selection
		model.putAll(selectedView.getModel());
		// make the entire flow execution context available in the model
		model.put(FLOW_EXECUTION_CONTEXT_ATTRIBUTE, flowExecutionContext);
		// make the unique flow execution id available in the model as
		// convenience to views
		model.put(FLOW_EXECUTION_ID_ATTRIBUTE, formatContinuationKey(continuationKey));
		return new ViewSelection(selectedView.getViewName(), model, false);
	}

	/**
	 * Convert the continuation key to encoded string form.
	 * @param key the continuation key
	 * @return the string-encoded key
	 */
	protected String formatContinuationKey(FlowExecutionContinuationKey key) {
		return continuationKeyFormatter.formatValue(key);
	}
}