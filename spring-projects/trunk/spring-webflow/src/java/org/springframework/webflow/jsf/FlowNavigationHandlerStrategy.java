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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.springframework.core.style.StylerUtils;
import org.springframework.util.Assert;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.access.FlowLocator;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowExecutionListenerCriteria;
import org.springframework.webflow.execution.FlowExecutionManager;
import org.springframework.webflow.execution.FlowExecutionStorage;
import org.springframework.webflow.execution.TransactionSynchronizer;

/**
 * Note, this is the original strategy class for jsf flow navigation, but
 * (probably temporarilly) it's been made to derive from FlowExecutionManager.
 * There are some methods at the end that should be moved up to FlowExecutionManager.
 * 
 * <p>
 * Strategy methods for {@link FlowNavigationHandler}. Because a JSF
 * <code>NavigationHandler</code> must be registered directly with the JSF
 * runtime, the implementation class cannot be customized in the typical fashion
 * for a Spring-based application. Therefore, decisions that would typically be
 * placed in hook methods for a specialzed subclass have been isolated into an
 * instance of this class. You can either use this class (which contains the
 * default implementation), or you can subclass it and register a bean under the
 * name specified by manifest constant
 * <code>FlowNavigationHandler.NAVIGATION_STRATEGY_BEAN_NAME</code>.
 * </p>
 * 
 * @author Craig McClanahan
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class FlowNavigationHandlerStrategy extends FlowExecutionManager {

	/**
     * <p>
     * Prefix on a logical outcome value that identifies a logical outcome as
     * the identifier for a web flow that should be entered.
     * </p>
     */
	protected static final String WEBFLOW_PREFIX = "webflow:";

	/**
	 * Create a new flow execution manager. Before use, the manager should be
	 * appropriately configured using setter methods. At least the flow
	 * execution storage strategy should be set!
	 * 
	 * @see #setFlow(Flow)
	 * @see #setFlowLocator(FlowLocator)
	 * @see #setListener(FlowExecutionListener)
	 * @see #setListener(FlowExecutionListener, FlowExecutionListenerCriteria)
	 * @see #setListenerMap(Map)
	 * @see #setListeners(Collection)
	 * @see #setListeners(Collection, FlowExecutionListenerCriteria)
	 * @see #setStorage(FlowExecutionStorage)
	 * @see #setTransactionSynchronizer(TransactionSynchronizer)
	 */
	protected FlowNavigationHandlerStrategy() {
	}

	/**
	 * Create a new flow execution manager with the specified storage strategy.
	 * @param storage the storage strategy
	 * 
	 * @see #setFlow(Flow)
	 * @see #setFlowLocator(FlowLocator)
	 * @see #setListener(FlowExecutionListener)
	 * @see #setListener(FlowExecutionListener, FlowExecutionListenerCriteria)
	 * @see #setListenerMap(Map)
	 * @see #setListeners(Collection)
	 * @see #setListeners(Collection, FlowExecutionListenerCriteria)
	 * @see #setTransactionSynchronizer(TransactionSynchronizer)
	 */
	public FlowNavigationHandlerStrategy(FlowExecutionStorage storage) {
		super(storage);
	}

	
	/**
     * <p>
     * Return <code>true</code> if the current request is asking for the
     * creation of a new flow. The default implementation examines the logical
     * outcome to see if it starts with the prefix specified by
     * <code>WebFlowNavigationStrategy.PREFIX</code>.
     * </p>
     * 
     * @param context
     *            <code>FacesContext</code> for the current request
     * @param fromAction
     *            The action binding expression that was evaluated to retrieve
     *            the specified outcome (if any)
     * @param outcome
     *            The logical outcome returned by the specified action
     */
	public boolean isFlowLaunchRequest(FacesContext context, String fromAction,
			String outcome) {
		if (outcome == null) {
			return false;
		}
		return outcome.startsWith(WEBFLOW_PREFIX);
	}

	/**
     * <p>
     * Create a new flow execution for the current request. Proceed until a
     * <code>ViewDescriptor</code> is returned describing the next view that
     * should be rendered.
     * </p>
     * 
     * @param context
     *            <code>FacesContext</code> for the current request
     * @param fromAction
     *            The action binding expression that was evaluated to retrieve
     *            the specified outcome (if any)
     * @param outcome
     *            The logical outcome returned by the specified action
     * @param manager
     *            <code>FlowExecutionManager</code> used to manage this flow
     */
	public ViewDescriptor launchFlowExecution(FacesContext context, String fromAction,
			String outcome) {
		Map parameters = new HashMap(1);
		// strip of the webflow prefix, leaving the flowId to launch
		String flowId = outcome.substring(WEBFLOW_PREFIX.length());
		Assert.hasText(flowId,
				"The id of the flow to launch was not provided - programmer error");
		parameters.put(FlowExecutionManager.FLOW_ID_PARAMETER, flowId);
		return onEvent(createEvent(context, fromAction, outcome,
				parameters));
	}

	/**
     * <p>
     * Return <code>true</code> if there is an existing flow execution in
     * progress for the current request. Implementors must ensure that the
     * algorithm used to makes this determination matches the determination that
     * will be made by the <code>FlowExecutionManager</code> that is being
     * used. The default implementation looks for a request parameter named by
     * <code>WebFlowNavigationStrategy.FLOW_EXECUTION_ID</code>.
     * </p>
     * 
     * @param context
     *            <code>FacesContext</code> for the current request
     * @param fromAction
     *            The action binding expression that was evaluated to retrieve
     *            the specified outcome (if any)
     * @param outcome
     *            The logical outcome returned by the specified action
     */
	public boolean isFlowExecutionParticipationRequest(FacesContext context,
			String fromAction, String outcome) {

		boolean result = FlowExecutionHolder.getFlowExecution() == null ? false : true;

		// assert an invariant, just to be safe
		boolean requestKeyPresent = context.getExternalContext().getRequestParameterMap()
				.containsKey(FlowExecutionManager.FLOW_EXECUTION_ID_PARAMETER);
		Assert.isTrue(result == requestKeyPresent,
				"flow in thread context must match request attribute existence");

		return result;
	}

	/**
     * <p>
     * Resume an active flow execution for the current request. Proceed until a
     * <code>ViewDescriptor</code> is returned describing the next view that
     * should be rendered.
     * </p>
     * 
     * @param context
     *            <code>FacesContext</code> for the current request
     * @param fromAction
     *            The action binding expression that was evaluated to retrieve
     *            the specified outcome (if any)
     * @param outcome
     *            The logical outcome returned by the specified action
     * @param flowExecutionManager
     *            <code>FlowExecutionManager</code> used to manage this flow
     */
	public ViewDescriptor resumeFlowExecution(FacesContext context, String fromAction,
			String outcome) {

		Serializable flowExecutionId = FlowExecutionHolder.getFlowExecutionId();
		FlowExecution flowExecution = FlowExecutionHolder.getFlowExecution();
		Assert.notNull(flowExecution);

		Event event = createEvent(context, fromAction, outcome, null);
		ViewDescriptor selectedView = processEventExistingFlow(flowExecutionId, flowExecution,
				event);
		return afterEvent(selectedView, flowExecutionId, flowExecution, event, null);
	}

	/**
     * <p>
     * Construct and return an <code>Event</code> reflecting the flow
     * execution event that represents this request. The default implementation
     * constructs and returns a <code>ServletEvent</code> reflecting the
     * current request. FIXME - this will need to be refactored to support
     * portlet events as well as servlet events -- or perhaps a FacesEvent can
     * be constructed that hides the environmental differences.
     * </p>
     * 
     * @param context
     *            <code>FacesContext</code> for the current request
     * @param parameters
     *            Optional additional event parameters
     * @param fromAction
     *            The action binding expression that was evaluated to retrieve
     *            the specified outcome (if any)
     * @param outcome
     *            The logical outcome returned by the specified action
     */
	public Event createEvent(FacesContext context, String fromAction, String outcome,
			Map parameters) {
		return new JsfEvent(outcome, context, fromAction, parameters);
	}

	/**
     * <p>
     * Render the view specified by this <code>ViewDescriptor</code>, after
     * exposing any model data it includes.
     * </p>
     * 
     * @param context
     *            <code>FacesContext</code> for the current request
     * @param fromAction
     *            The action binding expression that was evaluated to retrieve
     *            the specified outcome (if any)
     * @param outcome
     *            The logical outcome returned by the specified action
     * @param manager
     *            <code>FlowExecutionManager</code> used to manage this flow
     * @param viewDescriptor
     *            <code>ViewDescriptor</code> for the view to render
     */
	public void renderView(FacesContext context, String fromAction, String outcome,
			ViewDescriptor viewDescriptor) {
		// Expose model data specified in the descriptor
		try {
			context.getExternalContext().getRequestMap()
					.putAll(viewDescriptor.getModel());
		} catch (UnsupportedOperationException e) {
			// work around nasty MyFaces bug where it's RequestMap doesn't
			// support putAll
			// remove after it's fixed in MyFaces
			Map requestMap = context.getExternalContext().getRequestMap();
			Iterator it = viewDescriptor.getModel().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				requestMap.put(entry.getKey(), entry.getValue());
			}
		}

		// Stay on the same view if requested
		if (viewDescriptor.getViewName() == null) {
			return;
		}
		// Create the specified view so that it can be rendered
		ViewHandler vh = context.getApplication().getViewHandler();
		UIViewRoot view = vh.createView(context, viewDescriptor.getViewName());
		context.setViewRoot(view);
	}

	// code below is here temporarilly, and duplicates stuff in
	// FlowExecutionManager
	// don't want to make a new class while figuring out new JSF flow
	// a lot of this should move to a common superclass of FlowExecutionManager
	// and some
	// new class

	/**
     * Load an existing FlowExecution based on data in the specified event
     * 
     * @param event
     *            the event that occured
     * @param listener
     *            a listener interested in flow execution lifecycle events that
     *            happen <i>while handling this event</i>. Will be added to
     *            flow execution listeners
     */
	public FlowExecution loadFlowExecution(Event event, FlowExecutionListener listener) {

		FlowExecution flowExecution = null;
		Serializable flowExecutionId = getFlowExecutionId(event);
		if (flowExecutionId == null) {
			logger.warn("Unable to load FlowExecution: no ID found");
		} else {
			// client is participating in an existing flow execution,
			// retrieve information about it
			flowExecution = getStorage().load(flowExecutionId, event);
			// rehydrate the execution if neccessary (if it had been serialized
			// out)
			flowExecution.rehydrate(getFlowLocator(), this, getTransactionSynchronizer());
			if (listener != null) {
				flowExecution.getListeners().add(listener);
			}
			flowExecution.getListeners().fireLoaded(flowExecution, flowExecutionId);
			if (logger.isDebugEnabled()) {
				logger.debug("Loaded existing flow execution from storage with id: '"
						+ flowExecutionId + "'");
			}
		}
		return flowExecution;
	}

	/**
     * Signal the occurence of the specified event on an existing flow
     * 
     * @param flowExecutionId
     *            the id of the existing flow
     * @param flowExecution
     *            the existing flow
     * @param event
     *            the event that occured
     * @return the raw or unprepared view descriptor of the model and view to
     *         render
     */
	public ViewDescriptor processEventExistingFlow(Serializable flowExecutionId,
			FlowExecution flowExecution, Event event) {

		if (logger.isDebugEnabled()) {
			logger.debug("New request received from client, source event is: " + event);
		}

		ViewDescriptor selectedView;
		// signal the event within the current state
		Assert
				.hasText(
						event.getId(),
						"No eventId could be obtained -- "
								+ "make sure the client provides the _eventId parameter as input; the parameters provided for this request were:"
								+ StylerUtils.style(event.getParameters()));
		// see if the eventId was set to a static marker placeholder because
		// of a client configuration error
		if (event.getId().equals(getNotSetEventIdParameterMarker())) {
			throw new IllegalArgumentException(
					"The received eventId was the 'not set' marker '"
							+ getNotSetEventIdParameterMarker()
							+ "' -- this is likely a client view (jsp, etc) configuration error --"
							+ "the _eventId parameter must be set to a valid event");
		}
		selectedView = flowExecution.signalEvent(event);

		return selectedView;
	}

	/**
     * Cleanup (non-active view) or store the FlowExecution, and prepare the
     * ViewDescriptor for the client
     * 
     * @param flowExecutionId
     *            the id of the existing flow
     * @param flowExecution
     *            the existing flow
     * @param event
     *            the event that occured
     * @param listener
     *            a listener interested in flow execution lifecycle events that
     *            happen <i>while handling this event</i>. Will be removed at
     *            end from flow execution listeners
     * @return the prepared view descriptor of the model and view to render
     */
	public ViewDescriptor afterEvent(ViewDescriptor selectedView,
			Serializable flowExecutionId, FlowExecution flowExecution, Event event,
			FlowExecutionListener listener) {

		if (flowExecution.isActive()) {
			// save the flow execution for future use
			flowExecutionId = getStorage().save(flowExecutionId, flowExecution, event);
			flowExecution.getListeners().fireSaved(flowExecution, flowExecutionId);
			if (logger.isDebugEnabled()) {
				logger.debug("Saved flow execution out to storage with id: '"
						+ flowExecutionId + "'");
			}
		} else {
			// event execution resulted in the entire flow execution ending,
			// cleanup
			if (flowExecutionId != null) {
				getStorage().remove(flowExecutionId, event);
				flowExecution.getListeners().fireRemoved(flowExecution, flowExecutionId);
				if (logger.isDebugEnabled()) {
					logger.debug("Removed flow execution from storage with id: '"
							+ flowExecutionId + "'");
				}
			}
		}
		if (listener != null) {
			flowExecution.getListeners().remove(listener);
		}
		selectedView = prepareViewDescriptor(selectedView, flowExecutionId, flowExecution);
		if (logger.isDebugEnabled()) {
			logger.debug("Returning selected view to client: " + selectedView);
		}
		return selectedView;
	}

}
