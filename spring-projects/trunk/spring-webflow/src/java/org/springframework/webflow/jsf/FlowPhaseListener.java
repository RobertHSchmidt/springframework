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
package org.springframework.webflow.jsf;

import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;
import org.springframework.webflow.Event;
import org.springframework.webflow.access.BeanFactoryFlowLocator;
import org.springframework.webflow.execution.DataStoreFlowExecutionStorage;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.SessionDataStoreAccessor;

/**
 * JSF phase listener that is responsible for loading the current flow execution
 * to a threadlocal, so that components may bind to it as needed.
 * 
 * Note: this class currently uses a FlowNavigationHandlerStrategy for
 * convenience. That class may actually be broken up.
 * 
 * @since 1.0
 * @author Colin Sampaleanu
 */
public class FlowPhaseListener implements PhaseListener {

	/**
	 * Logger, usable by subclasses.
	 */
	protected final Log logger = LogFactory.getLog(FlowPhaseListener.class);

	/**
	 * <p>
	 * The {@link FlowNavigationHandlerStrategy} instance to use, lazily
	 * instantiated upon first use.
	 * </p>
	 */
	private FlowNavigationHandlerStrategy flowNavigationHandlerStrategy;

	public void beforePhase(PhaseEvent event) {
		logger.trace("JSF before phase: " + event.getPhaseId());
		if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
			// logger.trace("JSF before phase: Restore View");
		}
		else if (event.getPhaseId() == PhaseId.APPLY_REQUEST_VALUES) {
			// logger.trace("JSF before phase: Apply request values");
		}
		else if (event.getPhaseId() == PhaseId.PROCESS_VALIDATIONS) {
			// logger.trace("JSF before phase: Process validations");
		}
		else if (event.getPhaseId() == PhaseId.UPDATE_MODEL_VALUES) {
			// logger.trace("JSF before phase: Update model vlaues");
		}
		else if (event.getPhaseId() == PhaseId.INVOKE_APPLICATION) {
			// logger.trace("JSF before phase: Invoke application");
		}
		else if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			// logger.trace("JSF before phase: Render response");
		}
	}

	public void afterPhase(PhaseEvent event) {
		logger.trace("JSF after phase: " + event.getPhaseId());
		FacesContext context = event.getFacesContext();
		FlowNavigationHandlerStrategy strategy = getStrategy(context);
		if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
			Event jsfEvent = strategy.createEvent(context, null, null, null);
			Serializable id = strategy.getFlowExecutionId(jsfEvent);
			if (id != null) {
				FlowExecution flowExecution = strategy.loadFlowExecution(id, jsfEvent);
				FlowExecutionHolder.setFlowExecution(id, flowExecution);
			}
		}
		else if (event.getPhaseId() == PhaseId.APPLY_REQUEST_VALUES) {
			// logger.trace("JSF before phase: Apply request values");
		}
		else if (event.getPhaseId() == PhaseId.PROCESS_VALIDATIONS) {
			// logger.trace("JSF before phase: Process validations");
		}
		else if (event.getPhaseId() == PhaseId.UPDATE_MODEL_VALUES) {
			// logger.trace("JSF before phase: Update model vlaues");
		}
		else if (event.getPhaseId() == PhaseId.INVOKE_APPLICATION) {
			// logger.trace("JSF before phase: Invoke application");
		}
		else if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			// logger.trace("JSF before phase: Render response");
			FlowExecutionHolder.clearFlowExecution();
		}
	}

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

	/**
	 * <p>
	 * Return the {@link FlowNavigationHandlerStrategy} instance we will use to
	 * make navigation handler decisions. The instance to use is discovered by
	 * looking for a bean named by
	 * <code>WebFlowNavigationHandler.STRATEGY</code>, or defaulting to an
	 * instance of {@link FlowNavigationHandlerStrategy}.
	 * </p>
	 * 
	 * @param context <code>FacesContext</code> for the current request
	 */
	private FlowNavigationHandlerStrategy getStrategy(FacesContext context) {
		if (flowNavigationHandlerStrategy == null) {
			WebApplicationContext wac = FacesContextUtils.getWebApplicationContext(context);
			if (wac != null) {
				if (wac.containsBean(FlowNavigationHandlerStrategy.BEAN_NAME)) {
					flowNavigationHandlerStrategy = (FlowNavigationHandlerStrategy)wac.getBean(
							FlowNavigationHandlerStrategy.BEAN_NAME, FlowNavigationHandlerStrategy.class);
				}
			}
			if (flowNavigationHandlerStrategy == null) {
				flowNavigationHandlerStrategy = new FlowNavigationHandlerStrategy(new DataStoreFlowExecutionStorage(
						new SessionDataStoreAccessor()));
				flowNavigationHandlerStrategy.setFlowLocator(new BeanFactoryFlowLocator(wac));
			}
		}
		return flowNavigationHandlerStrategy;
	}
}