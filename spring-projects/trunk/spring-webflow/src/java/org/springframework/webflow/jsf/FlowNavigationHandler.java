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
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.execution.DataStoreFlowExecutionStorage;
import org.springframework.webflow.execution.FlowExecutionManager;
import org.springframework.webflow.execution.SessionDataStoreAccessor;

/**
 * <p>
 * An implementation of a JSF <code>NavigationHandler</code> that provides
 * integration with Spring Web Flow. It delegates handling to the standard
 * implementation when there is no current flow.
 * </p>
 * <ul>
 * <li>If a flow is <strong>not</strong> currently in progress:
 * <ul>
 * <li>If the specified logical outcome is <strong>not</strong> of the form
 * <em>webflow:xxx</em>, delegate to the standard
 * <code>NavigationHandler</code> implementation and return.</li>
 * <li>If the specified logical outcome <strong>is</strong> of the form
 * <em>webflow:xxx</em>, look up the corresponding <code>Flow</code>, and
 * begin its execution at its starting state. Record state information to
 * indicate that this flow is in progress.</li>
 * </ul>
 * </li>
 * <li>If a flow <strong>is</strong> currently in progress:
 * <ul>
 * <li>Acquire references to the current state information for the in progress
 * flow.</li>
 * <li>Continue execution of the flow until it returns a
 * <code>ViewDescriptor</code> describing the next view to be rendered.</li>
 * <li>Cause navigation to render the requested view</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @since 1.0
 * @author Craig McClanahan
 * @author Colin Sampaleanu
 * @author Keith Donald
 */
public class FlowNavigationHandler extends NavigationHandler {

	/**
	 * <p>
	 * Bean name under which we will find the configured instance of the
	 * {@link FlowNavigationHandlerStrategy} to be used for determining what
	 * logical actions to undertake.
	 * </p>
	 */
	private static final String NAVIGATION_STRATEGY_BEAN_NAME = "flowNavigationHandlerStrategy";

	/**
	 * <p>
	 * The <code>Log</code> instance for this class.
	 * </p>
	 */
	private final Log log = LogFactory.getLog(getClass());

	/**
	 * <p>
	 * The standard <code>NavigationHandler</code> implementation that we are
	 * wrapping.
	 * </p>
	 */
	private NavigationHandler handlerDelegate;

	/**
	 * <p>
	 * The {@link FlowNavigationHandlerStrategy} instance to use, lazily
	 * instantiated upon first use.
	 * </p>
	 */
	private FlowNavigationHandlerStrategy flowNavigationHandlerStrategy;

	/**
	 * <p>
	 * Create a new {@link FlowNavigationHandler}, wrapping the specified
	 * standard navigation handler implementation.
	 * </p>
	 * 
	 * @param handlerDelegate Standard <code>NavigationHandler</code> we are
	 * wrapping
	 */
	public FlowNavigationHandler(NavigationHandler handlerDelegate) {
		this.handlerDelegate = handlerDelegate;
	}

	/**
	 * <p>
	 * Handle the navigation request implied by the specified parameters.
	 * </p>
	 * 
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
		if (getStrategy(context).isFlowLaunchRequest(context, fromAction, outcome)) {
			ViewDescriptor nextView = getStrategy(context).launchFlowExecution(context, fromAction, outcome);
			getStrategy(context).renderView(context, fromAction, outcome, nextView);
		}
		else if (getStrategy(context).isFlowExecutionParticipationRequest(context, fromAction, outcome)) {
			ViewDescriptor nextView = getStrategy(context).resumeFlowExecution(context, fromAction, outcome);
			getStrategy(context).renderView(context, fromAction, outcome, nextView);
		}
		else {
			handlerDelegate.handleNavigation(context, fromAction, outcome);
		}
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
				if (wac.containsBean(NAVIGATION_STRATEGY_BEAN_NAME)) {
					flowNavigationHandlerStrategy = (FlowNavigationHandlerStrategy)wac.getBean(
							NAVIGATION_STRATEGY_BEAN_NAME, FlowNavigationHandlerStrategy.class);
				}
			}
			if (flowNavigationHandlerStrategy == null) {
				FlowExecutionManager manager = new FlowExecutionManager(new DataStoreFlowExecutionStorage(
						new SessionDataStoreAccessor()));
				manager.setBeanFactory(wac);
				flowNavigationHandlerStrategy = new FlowNavigationHandlerStrategy(manager);
			}
		}
		return flowNavigationHandlerStrategy;
	}
}