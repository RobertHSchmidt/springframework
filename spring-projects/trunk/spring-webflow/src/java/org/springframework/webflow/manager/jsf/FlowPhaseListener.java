/*
 * Copyright 2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow.manager.jsf;

import java.io.Serializable;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.format.Formatter;
import org.springframework.util.StringUtils;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKey;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKeyFormatter;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory;
import org.springframework.webflow.execution.repository.SharedMapFlowExecutionRepositoryFactory;
import org.springframework.webflow.manager.support.FlowExecutionManagerParameterExtractor;

/**
 * JSF phase listener that is responsible for managing a {@link FlowExecution}
 * object representing an active user conversation so that other JSF artifacts
 * that execute in different phases of the JSF lifecycle may have access to it.
 * <p>
 * This phase listener implements the following algorithm:
 * <ul>
 * <li>On RESTORE_VIEW, restore the {@link FlowExecution} the user is
 * participating in if a call to
 * {@link FlowExecutionManagerParameterExtractor#extractFlowExecutionId(ExternalContext)}
 * returns a submitted flow execution identifier. Place the restored flow
 * execution in a holder that other JSF artifacts such as VariableResolvers,
 * PropertyResolvers, and NavigationHandlers may access during the request
 * lifecycle.
 * <li>On BEFORE_RENDER_RESPONSE, if a flow execution was restored in the
 * RESTORE_VIEW phase generate a new key for identifying the updated execution
 * within a the selected {@link FlowExecutionRepository}. Expose managed flow
 * execution attributes to the views before rendering.
 * <li>On AFTER_RENDER_RESPONSE, if a flow execution was restored in the
 * RESTORE_VIEW phase <em>save</em> the updated execution to the repository
 * using the new key generated in the BEFORE_RENDER_RESPONSE phase.
 * </ul>
 * @author Colin Sampaleanu
 * @author Keith Donald
 */
public class FlowPhaseListener implements PhaseListener {

	/**
	 * The flow context itself will be exposed to the view in a model attribute
	 * with this name ("flowExecutionContext").
	 */
	public static final String FLOW_EXECUTION_CONTEXT_ATTRIBUTE = "flowExecutionContext";

	/**
	 * The id of the flow execution will be exposed to the view in a model
	 * attribute with this name ("flowExecutionId").
	 */
	public static final String FLOW_EXECUTION_ID_ATTRIBUTE = "flowExecutionId";

	/**
	 * Logger, usable by subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The formatter that will parse encoded _flowExecutionId strings into
	 * {@link FlowExecutionContinuationKey} objects.
	 */
	private Formatter continuationKeyFormatter = new FlowExecutionContinuationKeyFormatter();

	/**
	 * A helper for extracting parameters needed by this flow execution manager.
	 */
	private FlowExecutionManagerParameterExtractor parameterExtractor = new FlowExecutionManagerParameterExtractor();

	/**
	 * The flow execution repository factoring, for obtaining repository
	 * instances to save paused executions that require user input and load
	 * resuming executions that will process user events.
	 */
	private FlowExecutionRepositoryFactory repositoryFactory = new SharedMapFlowExecutionRepositoryFactory();

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
	 * Returns the repository factory used by this phase listener.
	 */
	public FlowExecutionRepositoryFactory getRepositoryFactory() {
		return repositoryFactory;
	}

	/**
	 * Set the repository factory used by this phase listener.
	 */
	public void setRepositoryFactory(FlowExecutionRepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
	}

	/**
	 * Returns the repository instance to be used by this phase listener.
	 */
	protected FlowExecutionRepository getRepository(ExternalContext context) {
		return repositoryFactory.getRepository(context);
	}

	public void beforePhase(PhaseEvent event) {
		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			FacesContext facesContext = event.getFacesContext();
			FlowExecutionHolder holder = FlowExecutionHolderUtils.getFlowExecutionHolder(facesContext);
			if (holder != null) {
				FlowExecution flowExecution = holder.getFlowExecution();
				if (flowExecution.isActive()) {
					// generate new continuation key for the flow execution
					// before rendering the response
					FlowExecutionContinuationKey continuationKey = holder.getContinuationKey();
					FlowExecutionRepository repository = getRepository(new JsfExternalContext(facesContext));
					if (continuationKey == null) {
						// it is an entirely new conversation, generate a new
						// conversation and continuation id
						continuationKey = repository.generateContinuationKey(flowExecution);
					}
					else {
						// it is an existing conversaiton, generate a new
						// continuation id
						continuationKey = repository.generateContinuationKey(flowExecution, continuationKey
								.getConversationId());
					}
					holder.setContinuationKey(continuationKey);
					String flowExecutionId = continuationKeyFormatter.formatValue(continuationKey);
					Map requestMap = facesContext.getExternalContext().getRequestMap();
					// expose string-encoded continuation key in the request map
					exposeFlowExecutionAttributes(requestMap, flowExecutionId, flowExecution);
				}
			}
		}
	}

	public void afterPhase(PhaseEvent event) {
		if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
			// restore flow execution if necessary so it will be available to
			// variable/property resolvers and the flow navigation handler
			FacesContext facesContext = event.getFacesContext();
			JsfExternalContext context = new JsfExternalContext(facesContext);
			String flowExecutionId = parameterExtractor.extractFlowExecutionId(context);
			if (StringUtils.hasText(flowExecutionId)) {
				FlowExecutionContinuationKey continuationKey = parseContinuationKey(flowExecutionId);
				FlowExecutionRepository repository = getRepository(context);
				FlowExecution flowExecution = repository.getFlowExecution(continuationKey);
				if (logger.isDebugEnabled()) {
					logger.debug("Loaded existing flow execution from repository with id '" + continuationKey + "'");
				}
				FlowExecutionHolderUtils.setFlowExecutionHolder(
						new FlowExecutionHolder(continuationKey, flowExecution), facesContext);
			}
		}
		else if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			FacesContext facesContext = event.getFacesContext();
			FlowExecutionHolder holder = FlowExecutionHolderUtils.getFlowExecutionHolder(facesContext);
			if (holder != null) {
				FlowExecution flowExecution = holder.getFlowExecution();
				FlowExecutionRepository repository = getRepository(new JsfExternalContext(facesContext));
				if (flowExecution.isActive()) {
					// save the flow execution out to the repository
					if (logger.isDebugEnabled()) {
						logger.debug("Saving continuation to repository with key " + holder.getContinuationKey());
					}
					repository.putFlowExecution(holder.getContinuationKey(), flowExecution);
				}
				else {
					// remove the conversation from the repository
					Serializable conversationId = holder.getContinuationKey().getConversationId();
					if (logger.isDebugEnabled()) {
						logger.debug("Removing conversation in repository with id '" + conversationId + "'");
					}
					repository.invalidateConversation(conversationId);
				}
			}
		}
	}

	protected FlowExecutionContinuationKey parseContinuationKey(String flowExecutionId) {
		return (FlowExecutionContinuationKey)continuationKeyFormatter.parseValue(flowExecutionId,
				FlowExecutionContinuationKey.class);
	}

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

	/**
	 * Stores contextual info in the view model, including the flow execution
	 * context, flow execution id
	 * @param model the view model
	 * @param flowExecutionId the flow execution id
	 * @param flowExecutionContext the flow execution context
	 */
	protected void exposeFlowExecutionAttributes(Map model, String flowExecutionId,
			FlowExecutionContext flowExecutionContext) {
		// make the entire flow execution context available in the model
		model.put(FLOW_EXECUTION_CONTEXT_ATTRIBUTE, flowExecutionContext);
		// make the unique flow execution id and current state id
		// available in the model as convenience to views
		model.put(FLOW_EXECUTION_ID_ATTRIBUTE, flowExecutionId);
	}
}