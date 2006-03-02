/*
 * Copyright 2002-2006 the original author or authors.
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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.jsf.DecoratingNavigationHandler;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory;
import org.springframework.webflow.executor.support.FlowExecutorArgumentExtractor;
import org.springframework.webflow.support.ApplicationView;
import org.springframework.webflow.support.ConversationRedirect;
import org.springframework.webflow.support.ExternalRedirect;
import org.springframework.webflow.support.FlowRedirect;

/**
 * An implementation of a JSF <code>NavigationHandler</code> that provides
 * integration with Spring Web Flow. It delegates handling to the standard
 * NavigationHandler implementation when a navigation request does not pertain
 * to a flow execution.
 * <p>
 * Specifically, the following navigation handler algorithm is implemented:
 * <ul>
 * <li>If a flow execution is <strong>not</strong> currently in progress:
 * <ul>
 * <li>If the specified logical outcome <strong>is</strong> of the form
 * <em>flowId:xxx</em>, look up the corresponding
 * {@link org.springframework.webflow.Flow} definition with that id and launch a
 * new flow execution in the starting state. Expose information to indicate that
 * this flow is in progress and render the starting {@link ViewSelection}.</li>
 * <li>If the specified logical outcome is <strong>not</strong> of the form
 * <em>flowId:xxx</em>, simply delegate to the standard
 * <code>NavigationHandler</code> implementation and return.</li>
 * </ul>
 * </li>
 * <li>If a flow execution <strong>is</strong> currently in progress:
 * <ul>
 * <li>Load the reference to the current in-progress flow execution using the
 * submitted <em>_flowExecutionId</em> parameter.</li>
 * <li>Resume the flow execution by signaling what action outcome (aka event)
 * the user took in the current state.
 * <li>Once state event processing to complete, render the
 * <code>ViewSelection</code> returned.</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @author Craig McClanahan
 * @author Colin Sampaleanu
 * @author Keith Donald
 */
public class FlowNavigationHandler extends DecoratingNavigationHandler {

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
	 * A helper for extracting parameters needed by this flow navigation
	 * handler.
	 */
	private FlowExecutorArgumentExtractor argumentExtractor = new FlowNavigationHandlerArgumentExtractor();

	/**
	 * Resolves selected Web Flow view names to JSF view ids.
	 */
	private ViewIdResolver viewIdResolver = new DefaultViewIdResolver();

	/**
	 * Create a new {@link FlowNavigationHandler} using the default constructor.
	 */
	public FlowNavigationHandler() {
		super();
	}

	/**
	 * Create a new {@link FlowNavigationHandler}, wrapping the specified
	 * standard navigation handler implementation.
	 * @param originalNavigationHandler Standard <code>NavigationHandler</code>
	 * we are wrapping
	 */
	public FlowNavigationHandler(NavigationHandler originalNavigationHandler) {
		super(originalNavigationHandler);
	}

	/**
	 * Returns the repository factory used by this navigation handler.
	 */
	public FlowExecutionRepositoryFactory getRepositoryFactory() {
		return repositoryFactory;
	}

	/**
	 * Sets the repository factory used by this navigation handler.
	 */
	public void setRepositoryFactory(FlowExecutionRepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
	}

	/**
	 * Returns the argument extractor used by this navigation handler.
	 */
	public FlowExecutorArgumentExtractor getArgumentExtractor() {
		return argumentExtractor;
	}

	/**
	 * Sets the argument extractor to use.
	 */
	public void setArgumentExtractor(FlowExecutorArgumentExtractor argumentExtractor) {
		this.argumentExtractor = argumentExtractor;
	}

	/**
	 * Returns the JSF view id resolver used by this navigation handler.
	 */
	public ViewIdResolver getViewIdResolver() {
		return viewIdResolver;
	}

	/**
	 * Sets the JSF view id resolver used by this navigation handler.
	 */
	public void setViewIdResolver(ViewIdResolver viewIdResolver) {
		this.viewIdResolver = viewIdResolver;
	}

	public void handleNavigation(FacesContext facesContext, String fromAction, String outcome,
			NavigationHandler originalNavigationHandler) {
		JsfExternalContext context = new JsfExternalContext(facesContext, fromAction, outcome);
		FlowExecutionHolder holder = FlowExecutionHolderUtils.getFlowExecutionHolder(facesContext);
		if (holder != null) {
			// a flow execution has already been restored, signal an event in it
			FlowExecution flowExecution = holder.getFlowExecution();
			String eventId = argumentExtractor.extractEventId(context);
			ViewSelection selectedView = flowExecution.signalEvent(eventId, context);
			renderView(selectedView, context);
		}
		else {
			String flowId = argumentExtractor.extractFlowId(context);
			if (StringUtils.hasText(flowId)) {
				// a flow execution launch has been requested, start it
				FlowExecution flowExecution = getRepository(context).createFlowExecution(flowId);
				ViewSelection selectedView = flowExecution.start(context);
				FlowExecutionHolderUtils.setFlowExecutionHolder(new FlowExecutionHolder(flowExecution), facesContext);
				renderView(selectedView, context);
			}
			else {
				// neither has happened, delegate to std navigation handler
				originalNavigationHandler.handleNavigation(facesContext, fromAction, outcome);
			}
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

	/**
	 * Render the view specified by this <code>ViewSelection</code>, after
	 * exposing any model data it includes.
	 * @param selectedView <code>ViewSelection</code> for the view to render
	 * @param context <code>JsfExternalContext</code> for the current request
	 */
	public void renderView(ViewSelection selectedView, JsfExternalContext context) {
		if (selectedView == ViewSelection.NULL_VIEW) {
			return;
		}
		FacesContext facesContext = context.getFacesContext();
		if (selectedView instanceof ApplicationView) {
			ApplicationView forward = (ApplicationView)selectedView;
			putInto(facesContext.getExternalContext().getRequestMap(), forward.getModel());
			// stay on the same view if requested
			if (forward.getViewName() == null) {
				return;
			}
			// create the specified view so that it can be rendered
			ViewHandler handler = facesContext.getApplication().getViewHandler();
			UIViewRoot view = handler.createView(facesContext, viewIdResolver.resolveViewId(forward.getViewName()));
			facesContext.setViewRoot(view);
		}
		else if (selectedView instanceof ConversationRedirect) {
			FlowExecutionHolder holder = FlowExecutionHolderUtils.getFlowExecutionHolder(facesContext);
			String conversationUrl = argumentExtractor.createConversationUrl(holder.getFlowExecutionKey()
					.getConversationId(), context);
			sendRedirect(conversationUrl, facesContext);
		}
		else if (selectedView instanceof ExternalRedirect) {
			FlowExecutionHolder holder = FlowExecutionHolderUtils.getFlowExecutionHolder(facesContext);
			String externalUrl = argumentExtractor.createExternalUrl((ExternalRedirect)selectedView, holder
					.getFlowExecutionKey(), context);
			sendRedirect(externalUrl, facesContext);
		}
		else if (selectedView instanceof FlowRedirect) {
			String flowUrl = argumentExtractor.createFlowUrl((FlowRedirect)selectedView, context);
			sendRedirect(flowUrl, facesContext);
		}
		else {
			throw new IllegalArgumentException("Don't know how to handle view selection " + selectedView);
		}
	}

	private void sendRedirect(String url, FacesContext facesContext) {
		try {
			facesContext.getExternalContext().redirect(url);
		}
		catch (IOException e) {
			throw new IllegalArgumentException("Could not send redirect to " + url);
		}
	}

	/**
	 * Utility method needed needed only because we can not rely on JSF
	 * RequestMap supporting Map's putAll method. Tries putAll, falls back to
	 * individual adds
	 * @param targetMap the target map to add the model data to
	 * @param map the model data to add to the target map
	 */
	private void putInto(Map targetMap, Map map) {
		try {
			targetMap.putAll(map);
		}
		catch (UnsupportedOperationException e) {
			// work around nasty MyFaces bug where it's RequestMap doesn't
			// support putAll remove after it's fixed in MyFaces
			Iterator it = map.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				targetMap.put(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Standard default view id resolver which uses the web flow view name as
	 * the jsf view id
	 */
	public static class DefaultViewIdResolver implements ViewIdResolver {
		public String resolveViewId(String viewName) {
			return viewName;
		}
	}
}