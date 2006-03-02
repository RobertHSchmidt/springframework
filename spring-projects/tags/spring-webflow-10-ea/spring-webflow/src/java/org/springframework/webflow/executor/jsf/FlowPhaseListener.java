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
package org.springframework.webflow.executor.jsf;

import java.io.Serializable;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory;
import org.springframework.webflow.executor.support.FlowExecutorArgumentExtractor;

/**
 * JSF phase listener that is responsible for managing a {@link FlowExecution}
 * object representing an active user conversation so that other JSF artifacts
 * that execute in different phases of the JSF lifecycle may have access to it.
 * <p>
 * This phase listener implements the following algorithm:
 * <ul>
 * <li>On BEFORE_RESTORE_VIEW, restore the {@link FlowExecution} the user is
 * participating in if a call to
 * {@link FlowExecutorArgumentExtractor#extractFlowExecutionKey(ExternalContext)}
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
	 * Logger, usable by subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The flow execution repository factoring, for obtaining repository
	 * instances to save paused executions that require user input and load
	 * resuming executions that will process user events.
	 */
	private FlowExecutionRepositoryFactory repositoryFactory;

	/**
	 * A helper for extracting arguments needed by this flow executor.
	 */
	private FlowExecutorArgumentExtractor argumentExtractor = new FlowExecutorArgumentExtractor();

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
	 * Returns the argument extractor used by this phase listener.
	 */
	public FlowExecutorArgumentExtractor getArgumentExtractor() {
		return argumentExtractor;
	}

	/**
	 * Sets the parameter extractor to use.
	 */
	public void setArgumentExtractor(FlowExecutorArgumentExtractor argumentExtractor) {
		this.argumentExtractor = argumentExtractor;
	}

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

	public void beforePhase(PhaseEvent event) {
		if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
			restoreFlowExecution(event.getFacesContext());
		}
		else if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			prepareView(event.getFacesContext());
		}
	}

	public void afterPhase(PhaseEvent event) {
		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			updateFlowExecution(event.getFacesContext());
		}
	}

	protected void restoreFlowExecution(FacesContext facesContext) {
		JsfExternalContext context = new JsfExternalContext(facesContext);
		FlowExecutionKey flowExecutionKey = argumentExtractor.extractFlowExecutionKey(context);
		if (flowExecutionKey != null) {
			// restore flow execution from repository so it will be
			// available to variable/property resolvers and the flow
			// navigation handler
			FlowExecutionRepository repository = getRepository(context);
			FlowExecution flowExecution = repository.getFlowExecution(flowExecutionKey);
			if (logger.isDebugEnabled()) {
				logger.debug("Loaded existing flow execution from repository with id '" + flowExecutionKey + "'");
			}
			FlowExecutionHolderUtils.setFlowExecutionHolder(new FlowExecutionHolder(flowExecutionKey, flowExecution),
					facesContext);
		}
	}

	/**
	 * Returns the repository instance to be used by this phase listener.
	 */
	protected FlowExecutionRepository getRepository(JsfExternalContext context) {
		if (repositoryFactory == null) {
			repositoryFactory = FlowFacesUtils.getRepositoryFactory(context.getFacesContext());
		}
		return repositoryFactory.getRepository(context);
	}

	protected void prepareView(FacesContext facesContext) {
		FlowExecutionHolder holder = FlowExecutionHolderUtils.getFlowExecutionHolder(facesContext);
		if (holder != null) {
			FlowExecution flowExecution = holder.getFlowExecution();
			if (flowExecution.isActive()) {
				// generate new continuation key for the flow execution
				// before rendering the response
				FlowExecutionKey flowExecutionKey = holder.getFlowExecutionKey();
				FlowExecutionRepository repository = getRepository(new JsfExternalContext(facesContext));
				if (flowExecutionKey == null) {
					// it is an new conversation, generate a brand new key
					flowExecutionKey = repository.generateKey(flowExecution);
				}
				else {
					// it is an existing conversaiton, use same conversation id,
					// generate a new continuation id
					flowExecutionKey = repository.generateKey(flowExecution, flowExecutionKey.getConversationId());
				}
				holder.setFlowExecutionKey(flowExecutionKey);
				Map requestMap = facesContext.getExternalContext().getRequestMap();
				argumentExtractor.put(flowExecutionKey, requestMap);
				argumentExtractor.put(flowExecution, requestMap);
			}
		}
	}

	protected void updateFlowExecution(FacesContext facesContext) {
		FlowExecutionHolder holder = FlowExecutionHolderUtils.getFlowExecutionHolder(facesContext);
		if (holder != null) {
			FlowExecution flowExecution = holder.getFlowExecution();
			FlowExecutionRepository repository = getRepository(new JsfExternalContext(facesContext));
			if (flowExecution.isActive()) {
				// save the flow execution out to the repository
				if (logger.isDebugEnabled()) {
					logger.debug("Saving continuation to repository with key " + holder.getFlowExecutionKey());
				}
				repository.putFlowExecution(holder.getFlowExecutionKey(), flowExecution);
			}
			else {
				// remove the conversation from the repository
				Serializable conversationId = holder.getFlowExecutionKey().getConversationId();
				if (logger.isDebugEnabled()) {
					logger.debug("Removing conversation in repository with id '" + conversationId + "'");
				}
				repository.invalidateConversation(conversationId);
			}
		}
	}
}