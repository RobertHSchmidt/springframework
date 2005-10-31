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
import org.springframework.webflow.execution.FlowExecution;

/**
 * JSF phase listener that is responsible for loading the current flow execution
 * to a threadlocal, so that components may bind to it as needed.
 * 
 * Note: this class currently uses a FlowNavigationHandlerStrategy for
 * convenience. That class may actually be broken up.
 * 
 * @author Colin Sampaleanu
 */
public class FlowPhaseListener implements PhaseListener {

	/**
	 * Logger, usable by subclasses.
	 */
	protected final Log logger = LogFactory.getLog(FlowPhaseListener.class);

	/**
	 * <p>
	 * The {@link JsfFlowExecutionManager} instance to use, lazily
	 * instantiated upon first use.
	 * </p>
	 */
	private JsfFlowExecutionManager flowNavigationManager;

	public void beforePhase(PhaseEvent event) {
		logger.trace("JSF before phase: " + event.getPhaseId());
		if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
			logger.trace("JSF before phase: Restore View");
		}
		else if (event.getPhaseId() == PhaseId.APPLY_REQUEST_VALUES) {
			logger.trace("JSF before phase: Apply request values");
		}
		else if (event.getPhaseId() == PhaseId.PROCESS_VALIDATIONS) {
			logger.trace("JSF before phase: Process validations");
		}
		else if (event.getPhaseId() == PhaseId.UPDATE_MODEL_VALUES) {
			logger.trace("JSF before phase: Update model vlaues");
		}
		else if (event.getPhaseId() == PhaseId.INVOKE_APPLICATION) {
			logger.trace("JSF before phase: Invoke application");
		}
		else if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			logger.trace("JSF before phase: Render response");
		}
	}

	public void afterPhase(PhaseEvent event) {
		logger.trace("JSF after phase: " + event.getPhaseId());
		FacesContext context = event.getFacesContext();
		JsfFlowExecutionManager strategy = getStrategy(context);
		if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
			FlowExecutionHolder.clearFlowExecution();
			strategy.restoreFlowExecution(context);
		}
		else if (event.getPhaseId() == PhaseId.APPLY_REQUEST_VALUES) {
			logger.trace("JSF before phase: Apply request values");
		}
		else if (event.getPhaseId() == PhaseId.PROCESS_VALIDATIONS) {
			logger.trace("JSF before phase: Process validations");
		}
		else if (event.getPhaseId() == PhaseId.UPDATE_MODEL_VALUES) {
			logger.trace("JSF before phase: Update model vlaues");
		}
		else if (event.getPhaseId() == PhaseId.INVOKE_APPLICATION) {
			logger.trace("JSF before phase: Invoke application");
		}
		else if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			logger.trace("JSF before phase: Render response");
			if (!FlowExecutionHolder.isFlowExecutionSaved()) {
				Serializable flowExecutionId = FlowExecutionHolder.getFlowExecutionId();
				FlowExecution flowExecution = FlowExecutionHolder.getFlowExecution();
				JsfEvent jsfEvent = FlowExecutionHolder.getSourceEvent();
				// we do not need to keep id returned by save, it has been pre-generated
				flowNavigationManager.saveFlowExecution(flowExecutionId, flowExecution, jsfEvent);
			}
			FlowExecutionHolder.clearFlowExecution();
		}
	}

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

	/**
	 * <p>
	 * Return the {@link JsfFlowExecutionManager} instance we will use to
	 * make navigation handler decisions. The instanlce to use is returned by delegating to
	 * {@link JsfFlowExecutionManager#getFlowExecutionManager(FacesContext)}, but
	 * the value is cached, and subsequent requests return the same value.
	 * </p>
	 * 
	 * @param context <code>FacesContext</code> for the current request
	 */
	private JsfFlowExecutionManager getStrategy(FacesContext context) {
		if (flowNavigationManager == null)
			flowNavigationManager = (JsfFlowExecutionManager.getFlowExecutionManager(context));
		return flowNavigationManager;
	}
}
