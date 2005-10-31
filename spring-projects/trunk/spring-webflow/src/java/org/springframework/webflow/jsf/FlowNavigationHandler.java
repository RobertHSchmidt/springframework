/*
 * Copyright 2002-2005 the original author or authors.
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

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.webflow.ViewDescriptor;

/**
 * An implementation of a JSF <code>NavigationHandler</code> that provides
 * integration with Spring Web Flow. It delegates handling to the standard
 * implementation when there is no current flow.
 * <ul>
 * <li>If a flow execution is <strong>not</strong> currently in progress:
 * <ul>
 * <li>If the specified logical outcome is <strong>not</strong> of the form
 * <em>flowId:xxx</em>, delegate to the standard
 * <code>NavigationHandler</code> implementation and return.</li>
 * <li>If the specified logical outcome <strong>is</strong> of the form
 * <em>flowId:xxx</em>, look up the corresponding
 * {@link org.springframework.webflow.Flow} definition with that id and begin
 * flow execution in the starting state. Record state information to indicate
 * that this flow is in progress.</li>
 * </ul>
 * </li>
 * <li>If a flow execution <strong>is</strong> currently in progress:
 * <ul>
 * <li>Load the reference to the current in-progress flow execution.</li>
 * <li>Resume the flow execution by signaling what action (event) the user took
 * in the resuming state.
 * <li>Wait for state event processing to complete, which happens when a
 * <code>ViewDescriptor</code> is returned selecting the next view to be
 * rendered.</li>
 * <li>Cause navigation to render the requested view</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @author Craig McClanahan
 * @author Colin Sampaleanu
 * @author Keith Donald
 */
public class FlowNavigationHandler extends NavigationHandler {

	/**
	 * The <code>Log</code> instance for this class.
	 */
	private final Log log = LogFactory.getLog(getClass());

	/**
	 * The standard <code>NavigationHandler</code> implementation that we are
	 * wrapping.
	 */
	private NavigationHandler handlerDelegate;

	/**
	 * The {@link JsfFlowExecutionManager} instance to use, lazily instantiated
	 * upon first use.
	 */
	private JsfFlowExecutionManager flowExecutionManager;

	/**
	 * Create a new {@link FlowNavigationHandler}, wrapping the specified
	 * standard navigation handler implementation.
	 * @param handlerDelegate Standard <code>NavigationHandler</code> we are
	 * wrapping
	 */
	public FlowNavigationHandler(NavigationHandler handlerDelegate) {
		this.handlerDelegate = handlerDelegate;
	}

	/**
	 * Handle the navigation request implied by the specified parameters.
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 */
	public void handleNavigation(FacesContext context, String fromAction, String outcome) {
		if (log.isDebugEnabled()) {
			log.debug("handleNavigation(viewId=" + context.getViewRoot().getViewId() + ", fromAction=" + fromAction
					+ ", outcome=" + outcome + ")");
		}
		if (getExecutionManager(context).isFlowLaunchRequest(context, fromAction, outcome)) {
			ViewDescriptor nextView = getExecutionManager(context).launchFlowExecution(context, fromAction, outcome);
			getExecutionManager(context).renderView(context, fromAction, outcome, nextView);
		}
		else if (getExecutionManager(context).isFlowExecutionParticipationRequest(context, fromAction, outcome)) {
			ViewDescriptor nextView = getExecutionManager(context).resumeFlowExecution(context, fromAction, outcome);
			getExecutionManager(context).renderView(context, fromAction, outcome, nextView);
		}
		else {
			handlerDelegate.handleNavigation(context, fromAction, outcome);
		}
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
			flowExecutionManager = (JsfFlowExecutionManager.getFlowExecutionManager(context));
			// by default add a JsfFlowExecutionListener (TODO is this really needed?)
			flowExecutionManager.addListener(new JsfFlowExecutionListener());
		}
		return flowExecutionManager;
	}
}