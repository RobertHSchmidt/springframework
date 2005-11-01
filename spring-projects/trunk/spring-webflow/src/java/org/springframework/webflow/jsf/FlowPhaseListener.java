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

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	 * Logger, usable by subclasses.
	 */
	protected final Log logger = LogFactory.getLog(FlowPhaseListener.class);

	/**
	 * The {@link JsfFlowExecutionManager} instance to use, lazily instantiated
	 * upon first use.
	 */
	private JsfFlowExecutionManager flowExecutionManager;

	public void beforePhase(PhaseEvent event) {
	}

	public void afterPhase(PhaseEvent event) {
		FacesContext context = event.getFacesContext();
		if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
			FlowExecutionHolder.clearFlowExecution();
			// restore flow execution to the current thread's storage so it will be
			// available to variable/property resolvers
			getExecutionManager(context).restoreFlowExecution(context);
		}
		else if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			// save the flow execution out to storage after response rendering if neccessary
			getExecutionManager(context).saveFlowExecutionIfNecessary(context);
			FlowExecutionHolder.clearFlowExecution();
		}
	}

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

	/**
	 * Return the {@link JsfFlowExecutionManager} instance we will use to make
	 * navigation handler decisions. The instance to use is returned by
	 * delegating to
	 * {@link JsfFlowExecutionManager#getFlowExecutionManager(FacesContext)},
	 * but the value is cached, and subsequent requests return the same value.
	 * @param context <code>FacesContext</code> for the current request
	 */
	private JsfFlowExecutionManager getExecutionManager(FacesContext context) {
		if (flowExecutionManager == null) {
			flowExecutionManager = JsfFlowExecutionManager.getFlowExecutionManager(context);
		}
		return flowExecutionManager;
	}
}