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

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.springframework.binding.format.Formatter;
import org.springframework.util.StringUtils;
import org.springframework.web.jsf.FacesContextUtils;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.ExternalMapFlowExecutionRepositoryFactory;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKey;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKeyFormatter;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory;
import org.springframework.webflow.manager.support.FlowExecutionManagerParameterExtractor;

/**
 * JSF phase listener that is responsible for loading the current flow execution
 * to a threadlocal, so that components may bind to it as needed.
 * 
 * Note: this class currently uses a {@link JsfFlowExecutionManager} for
 * convenience.
 * 
 * @author Colin Sampaleanu
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

	private Formatter continuationKeyFormatter = new FlowExecutionContinuationKeyFormatter();

	private FlowExecutionManagerParameterExtractor parameterExtractor = new FlowExecutionManagerParameterExtractor();

	private FlowExecutionRepositoryFactory repositoryFactory = new ExternalMapFlowExecutionRepositoryFactory();

	public Formatter getContinuationKeyFormatter() {
		return continuationKeyFormatter;
	}

	public void setContinuationKeyFormatter(Formatter continuationKeyFormatter) {
		this.continuationKeyFormatter = continuationKeyFormatter;
	}

	public FlowExecutionManagerParameterExtractor getParameterExtractor() {
		return parameterExtractor;
	}

	public void setParameterExtractor(FlowExecutionManagerParameterExtractor parameterExtractor) {
		this.parameterExtractor = parameterExtractor;
	}

	public FlowExecutionRepositoryFactory getRepositoryFactory() {
		return repositoryFactory;
	}

	public void setRepositoryFactory(FlowExecutionRepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
	}

	public void beforePhase(PhaseEvent event) {
		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			FacesContext facesContext = event.getFacesContext();
			FlowExecutionHolder holder = FlowExecutionHolderUtils.getFlowExecutionHolder(facesContext);
			if (holder != null) {
				FlowExecution flowExecution = holder.getFlowExecution();
				if (flowExecution.isActive()) {
					// generate new continuation key for flow execution before
					// rendering response
					FlowExecutionContinuationKey continuationKey = holder.getContinuationKey();
					JsfExternalContext context = new JsfExternalContext(facesContext);
					FlowExecutionRepository repository = repositoryFactory.getRepository(context);
					continuationKey = repository.generateContinuationKey(flowExecution, continuationKey
							.getConversationId());
					holder.setContinuationKey(continuationKey);
					String flowExecutionId = continuationKeyFormatter.formatValue(continuationKey);
					Map requestMap = facesContext.getExternalContext().getRequestMap();
					// expose string-encoded flow execution key in the request
					// map
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
				FlowExecutionRepository repository = repositoryFactory.getRepository(context);
				FlowExecution flowExecution = repository.getFlowExecution(continuationKey);
				FlowExecutionHolderUtils.setFlowExecutionHolder(
						new FlowExecutionHolder(continuationKey, flowExecution), facesContext);
			}
		}
		else if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			// save the flow execution out to repository after response
			// rendering if necessary
			FacesContext facesContext = event.getFacesContext();
			FlowExecutionHolder holder = FlowExecutionHolderUtils.getFlowExecutionHolder(facesContext);
			if (holder != null) {
				FlowExecution flowExecution = holder.getFlowExecution();
				if (flowExecution.isActive()) {
					JsfExternalContext context = new JsfExternalContext(facesContext);
					FlowExecutionRepository repository = repositoryFactory.getRepository(context);
					repository.putFlowExecution(holder.getContinuationKey(), flowExecution);
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