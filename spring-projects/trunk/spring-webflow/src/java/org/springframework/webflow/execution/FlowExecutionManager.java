/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.execution;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.style.StylerUtils;
import org.springframework.util.Assert;
import org.springframework.util.CachingMapDecorator;
import org.springframework.util.StringUtils;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.access.FlowLocator;

/**
 * A manager for the executing flows of the application. This object is
 * responsible for creating new flow executions as requested by clients, as well
 * as signaling events for processing by existing, paused executions (that are
 * waiting to be resumed in response to a user event).
 * <p>
 * The {@link #onEvent(Event)} method is the central operation and implements
 * the following algorithm:
 * <ol>
 * <li>Look for a flow execution id in the event (in a parameter named
 * "_flowExecutionId").</li>
 * <li>If no flow execution id was submitted, create a new flow execution. The
 * top-level flow definition for which an execution is created for is determined
 * by the value of the "_flowId" event parameter. If this parameter parameter is
 * not present, an exception is thrown.</li>
 * <li>If a flow execution id <em>was</em> submitted, load the previously
 * saved FlowExecution with that id from storage.</li>
 * <li>If a new flow execution was created in the previous steps, start that
 * execution.</li>
 * <li>If an existing flow execution was loaded from storage, extract the
 * current state id ("_currentStateId") and event id ("_eventId") parameter
 * values from the event. Signal the occurence of the user event in the current
 * state, resuming the flow execution in that state.</li>
 * <li>If the flow execution is still active after event processing, save it
 * out to storage. This process generates a unique flow execution id that will
 * be exposed to the caller for identifying the same FlowExecution
 * (conversation) on subsequent requests. The caller will also be given access
 * to the flow execution context and any data placed in request or flow scope.</li>
 * </ol>
 * <p>
 * By default, this class will use the flow execution implementation provided by
 * the <code>FlowExecutionImpl</code> class. If you would like to use a
 * different implementation, just override the
 * {@link #createFlowExecution(Flow)} method in a subclass.
 * 
 * <p>
 * <b>Typical FlowExecutionManager configurable properties</b><br>
 * <table border="1">
 * <tr>
 * <td><b>name</b></td>
 * <td><b>default</b></td>
 * <td><b>description</b></td>
 * </tr>
 * <tr>
 * <td>flowLocator (required)</td>
 * <td>None</td>
 * <td>The locator that will load Flow definitions as needed for execution by
 * this manager</td>
 * </tr>
 * <tr>
 * <td>storage</td>
 * <td>A server-side session data store storage strategy</td>
 * <td>Strategy for saving and loading managed flow executions</td>
 * </tr>
 * <tr>
 * <td>listeners</td>
 * <td>None</td>
 * <td>Listeners for observing the lifecycle of managed flow executions</td>
 * </tr>
 * <tr>
 * <td>transactionSynchronizer</td>
 * <td>Flow scoped, token-based strategy</td>
 * <td>Strategy for demaracting application transactions within a flow
 * execution</td>
 * </tr>
 * </table>
 * </p>
 * @see org.springframework.webflow.execution.FlowExecution
 * @see org.springframework.webflow.execution.FlowExecutionStorage
 * @see org.springframework.webflow.execution.FlowExecutionListener
 * @see org.springframework.webflow.execution.TransactionSynchronizer
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class FlowExecutionManager implements FlowExecutionListenerLoader {

	/**
	 * Clients can send the id (name) of the flow to be started using an event
	 * parameter with this name ("_flowId").
	 */
	public static final String FLOW_ID_PARAMETER = "_flowId";

	/**
	 * Clients can send the flow execution id using an event parameter with this
	 * name ("_flowExecutionId").
	 */
	public static final String FLOW_EXECUTION_ID_PARAMETER = "_flowExecutionId";

	/**
	 * The id of the flow execution will be exposed to the view in a model
	 * attribute with this name ("flowExecutionId").
	 */
	public static final String FLOW_EXECUTION_ID_ATTRIBUTE = "flowExecutionId";

	/**
	 * The flow context itself will be exposed to the view in a model attribute
	 * with this name ("flowExecutionContext").
	 */
	public static final String FLOW_EXECUTION_CONTEXT_ATTRIBUTE = "flowExecutionContext";

	/**
	 * The current state of the flow execution will be exposed to the view in a
	 * model attribute with this name ("currentStateId").
	 */
	public static final String CURRENT_STATE_ID_ATTRIBUTE = "currentStateId";

	/**
	 * Event id value indicating that the event has not been set ("@NOT_SET@").
	 */
	public static final String NOT_SET_EVENT_ID = "@NOT_SET@";

	/**
	 * Logger, usable by subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The flow locator strategy for retrieving a flow definition using a flow
	 * id provided by the client. Defaults to a bean factory based lookup
	 * strategy.
	 */
	private FlowLocator flowLocator;

	/**
	 * The flow execution storage strategy, for saving paused executions that
	 * require user input and loading resuming executions that will process user
	 * events.
	 */
	private FlowExecutionStorage storage = new DataStoreFlowExecutionStorage();

	/**
	 * A map of flow execution listeners to a list of flow execution listener
	 * criteria objects. The criteria list determines the conditions in which a
	 * single flow execution listener applies.
	 */
	private CachingMapDecorator listenerMap = new CachingMapDecorator() {
		protected Object create(Object key) {
			return new LinkedList();
		}
	};

	/**
	 * The Flow Execution key generation strategy.
	 */
	private KeyGenerator keyGenerator = new RandomGuidKeyGenerator();

	/**
	 * The strategy for demarcating a logical application transaction within an
	 * executing flow. Defaults to a token-based strategy, where the transaction
	 * token is managed in flow scope.
	 */
	private TransactionSynchronizer transactionSynchronizer = new FlowScopeTokenTransactionSynchronizer();

	/**
	 * Create a new flow execution manager using the specified flow locator for
	 * loading Flow definitions.
	 * @param flowLocator the flow locator to use
	 * 
	 * @see #setFlowLocator(FlowLocator)
	 * @see #setStorage(FlowExecutionStorage)
	 * @see #setListener(FlowExecutionListener)
	 * @see #setListenerCriteria(FlowExecutionListener,
	 * FlowExecutionListenerCriteria)
	 * @see #setListenerMap(Map)
	 * @see #setListeners(Collection)
	 * @see #setListenersCriteria(Collection, FlowExecutionListenerCriteria)
	 * @see #setTransactionSynchronizer(TransactionSynchronizer)
	 */
	public FlowExecutionManager(FlowLocator flowLocator) {
		setFlowLocator(flowLocator);
	}

	/**
	 * Returns the flow locator to use for lookup of flows specified using the
	 * "_flowId" event parameter.
	 */
	protected FlowLocator getFlowLocator() {
		return flowLocator;
	}

	/**
	 * Set the flow locator to use for lookup of flows specified using the
	 * "_flowId" event parameter.
	 */
	public void setFlowLocator(FlowLocator flowLocator) {
		this.flowLocator = flowLocator;
	}

	/**
	 * Returns the storage strategy used by the flow execution manager.
	 */
	protected FlowExecutionStorage getStorage() {
		return storage;
	}

	/**
	 * Set the storage strategy used by the flow execution manager.
	 */
	public void setStorage(FlowExecutionStorage storage) {
		Assert.notNull(storage, "The flow execution storage strategy is required");
		this.storage = storage;
	}

	/**
	 * Returns the array of flow execution listeners for specified flow.
	 * @param flow the flow definition associated with the execution to be
	 * listened to
	 * @return the flow execution listeners
	 */
	public FlowExecutionListener[] getListeners(Flow flow) {
		Assert.notNull(flow, "The Flow to load listeners for cannot be null");
		List listenersToAttach = new LinkedList();
		for (Iterator entryIt = listenerMap.entrySet().iterator(); entryIt.hasNext();) {
			Map.Entry entry = (Map.Entry)entryIt.next();
			for (Iterator criteriaIt = ((List)entry.getValue()).iterator(); criteriaIt.hasNext();) {
				FlowExecutionListenerCriteria criteria = (FlowExecutionListenerCriteria)criteriaIt.next();
				if (criteria.appliesTo(flow)) {
					listenersToAttach.add((FlowExecutionListener)entry.getKey());
					break;
				}
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Loaded " + listenersToAttach.size() + " of possible " + listenerMap.size()
					+ " listeners to this execution request for flow: '" + flow.getId()
					+ "', the listeners to attach are: " + StylerUtils.style(listenersToAttach));
		}
		return (FlowExecutionListener[])listenersToAttach.toArray(new FlowExecutionListener[listenersToAttach.size()]);
	}

	/**
	 * Returns a unmodifiable map of the configured flow execution listeners and
	 * the criteria in which those listeners apply.
	 */
	public Map getListenerMap() {
		return Collections.unmodifiableMap(listenerMap);
	}

	/**
	 * Set the flow execution listener that will be notified of managed flow
	 * executions.
	 */
	public void setListener(FlowExecutionListener listener) {
		setListeners(Collections.singleton(listener));
	}

	/**
	 * Set the flow execution listener that will be notified of managed flow
	 * executions for the flows that match given criteria.
	 */
	public void setListenerCriteria(FlowExecutionListener listener, FlowExecutionListenerCriteria criteria) {
		setListenersCriteria(Collections.singleton(listener), criteria);
	}

	/**
	 * Sets the flow execution listeners that will be notified of managed flow
	 * executions.
	 */
	public void setListeners(Collection listeners) {
		setListenersCriteria(listeners, FlowExecutionListenerCriteriaFactory.allFlows());
	}

	/**
	 * Sets the flow execution listeners that will be notified of managed flow
	 * executions for flows that match given criteria.
	 */
	public void setListenersCriteria(Collection listeners, FlowExecutionListenerCriteria criteria) {
		if (logger.isDebugEnabled()) {
			logger.debug("Setting listeners: " + listeners + " with criteria: " + criteria);
		}
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			FlowExecutionListener listener = (FlowExecutionListener)it.next();
			if (containsListener(listener)) {
				removeListener(listener);
			}
			addListenerCriteria(listener, criteria);
		}
	}

	/**
	 * Sets the flow execution listeners that will be notified of managed flow
	 * executions. The map keys may be individual flow execution listener
	 * instances or collections of execution listener instances. The map values
	 * can either be string encoded flow execution listener criteria or direct
	 * references to <code>FlowExecutionListenerCriteria</code> objects.
	 */
	public void setListenerMap(Map listenerCriteriaMap) {
		Iterator it = listenerCriteriaMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			FlowExecutionListenerCriteria criteria;
			if (entry.getValue() instanceof FlowExecutionListenerCriteria) {
				criteria = (FlowExecutionListenerCriteria)entry.getValue();
			}
			else {
				criteria = convertEncodedListenerCriteria((String)entry.getValue());
			}
			if (entry.getKey() instanceof Collection) {
				setListenersCriteria((Collection)entry.getKey(), criteria);
			}
			else {
				setListenerCriteria((FlowExecutionListener)entry.getKey(), criteria);
			}
		}
	}

	protected FlowExecutionListenerCriteria convertEncodedListenerCriteria(String encodedCriteria) {
		return new TextToFlowExecutionListenerCriteria().convert(encodedCriteria);
	}

	/**
	 * Add a listener that will listen to executions for all flows.
	 * @param listener the listener to add
	 */
	public void addListener(FlowExecutionListener listener) {
		addListenerCriteria(listener, FlowExecutionListenerCriteriaFactory.allFlows());
	}

	/**
	 * Add a listener that will listen to executions to flows matching the
	 * specified criteria.
	 * @param listener the listener
	 * @param criteria the listener criteria
	 */
	public void addListenerCriteria(FlowExecutionListener listener, FlowExecutionListenerCriteria criteria) {
		if (logger.isDebugEnabled()) {
			logger.debug("Adding flow execution listener: " + listener + " with criteria: " + criteria);
		}
		List criteriaList = (List)this.listenerMap.get(listener);
		criteriaList.add(criteria);
	}

	/**
	 * Is the listener contained by this Flow execution manager?
	 * @param listener the listener
	 * @return true if yes, false otherwise
	 */
	public boolean containsListener(FlowExecutionListener listener) {
		return listenerMap.containsKey(listener);
	}

	/**
	 * Remove the flow execution listener from the listener list.
	 * @param listener the listener
	 */
	public void removeListener(FlowExecutionListener listener) {
		this.listenerMap.remove(listener);
	}

	/**
	 * Remove the criteria for the specified listener.
	 * @param listener the listener
	 * @param criteria the criteria
	 */
	public void removeListenerCriteria(FlowExecutionListener listener, FlowExecutionListenerCriteria criteria) {
		if (containsListener(listener)) {
			List criteriaList = (List)this.listenerMap.get(listener);
			criteriaList.remove(criteria);
			if (criteriaList.isEmpty()) {
				removeListener(listener);
			}
		}
	}

	/**
	 * Return the application transaction synchronization strategy to use. This
	 * defaults to a <i>synchronizer token</i> based transaction management
	 * system, as implemented by {@link FlowScopeTokenTransactionSynchronizer}.
	 */
	protected TransactionSynchronizer getTransactionSynchronizer() {
		return transactionSynchronizer;
	}

	/**
	 * Set the application transaction synchronization strategy to use.
	 */
	public void setTransactionSynchronizer(TransactionSynchronizer transactionSynchronizer) {
		this.transactionSynchronizer = transactionSynchronizer;
	}

	/**
	 * Returns the FlowExecution key generation strategy.
	 */
	protected KeyGenerator getKeyGenerator() {
		return keyGenerator;
	}

	/**
	 * Sets the FlowExecution key generation strategy.
	 */
	public void setKeyGenerator(KeyGenerator keyGenerator) {
		this.keyGenerator = keyGenerator;
	}

	// event processing

	/**
	 * Signal the occurence of the specified event. This is the entry point into
	 * the webflow system for managing all executing flows.
	 * @param sourceEvent the external event that occured
	 * @return the view descriptor of the model and view to render
	 */
	public ViewSelection onEvent(Event sourceEvent) {
		if (logger.isDebugEnabled()) {
			logger.debug("New request received from client, source event is: " + sourceEvent);
		}
		Serializable flowExecutionId = getFlowExecutionId(sourceEvent);
		FlowExecution flowExecution;
		ViewSelection selectedView;
		if (flowExecutionId == null) {
			flowExecution = createFlowExecution(getFlow(sourceEvent));
			selectedView = flowExecution.start(sourceEvent);
		}
		else {
			flowExecution = loadFlowExecution(flowExecutionId, sourceEvent);
			selectedView = signalEventIn(flowExecution, sourceEvent);
		}
		flowExecutionId = manageStorage(flowExecutionId, flowExecution, sourceEvent);
		return prepareSelectedView(selectedView, flowExecutionId, flowExecution);
	}

	/**
	 * Obtain a unique flow execution id from given event.
	 * @param sourceEvent the event
	 * @return the obtained id or <code>null</code> if not found
	 */
	public String getFlowExecutionId(Event sourceEvent) {
		return ExternalEvent.verifySingleStringInputParameter(getFlowExecutionIdParameterName(), sourceEvent
				.getParameter(getFlowExecutionIdParameterName()));
	}

	/**
	 * Create a new flow execution for given flow. Subclasses could redefine
	 * this if they wish to use a specialized FlowExecution implementation
	 * class.
	 * @param flow the flow
	 * @return the created flow execution
	 */
	protected FlowExecution createFlowExecution(Flow flow) {
		FlowExecution flowExecution = new FlowExecutionImpl(getKeyGenerator().generate(), flow, getListeners(flow),
				getTransactionSynchronizer());
		flowExecution.getListeners().fireCreated(flowExecution);
		if (logger.isDebugEnabled()) {
			logger.debug("Created a new flow execution for flow definition: '" + flow.getId() + "'");
		}
		return flowExecution;
	}

	/**
	 * Obtain a flow to use from given event. If there is a "_flowId" parameter
	 * specified in the event, the flow with that id will be returend after
	 * lookup using the flow locator. If no "_flowId" parameter is present in
	 * the event, the default top-level flow will be returned.
	 */
	protected Flow getFlow(Event sourceEvent) {
		String flowId = ExternalEvent.verifySingleStringInputParameter(getFlowIdParameterName(), sourceEvent
				.getParameter(getFlowIdParameterName()));
		if (StringUtils.hasText(flowId)) {
			return getFlowLocator().getFlow(flowId);
		}
		else {
			throw new IllegalArgumentException("The flow to launch must be provided by the client via the '"
					+ getFlowIdParameterName() + "' parameter, yet no such parameter was provided in this event."
					+ " Parameters provided were: " + StylerUtils.style(sourceEvent.getParameters()));
		}
	}

	/**
	 * Returns the name of the flow id parameter in the event ("_flowId").
	 */
	public String getFlowIdParameterName() {
		return FLOW_ID_PARAMETER;
	}

	/**
	 * Load an existing FlowExecution based on data in the specified source
	 * event.
	 * 
	 * @param flowExecutionId the unique id of the flow execution
	 * @param sourceEvent the source event
	 */
	public FlowExecution loadFlowExecution(Serializable flowExecutionId, Event sourceEvent) {
		// client is participating in an existing flow execution, retrieve
		// information about it
		FlowExecution flowExecution = getStorage().load(flowExecutionId, sourceEvent);
		// rehydrate the execution if neccessary (if it had been serialized out)
		flowExecution.rehydrate(getFlowLocator(), this, getTransactionSynchronizer());
		flowExecution.getListeners().fireLoaded(flowExecution, flowExecutionId);
		if (logger.isDebugEnabled()) {
			logger.debug("Loaded existing flow execution from storage with id: '" + flowExecutionId + "'");
		}
		return flowExecution;
	}

	/**
	 * Signal the occurence of the specified event on an existing flow
	 * 
	 * @param flowExecution the existing flow
	 * @param event the event that occured
	 * @return the raw or unprepared view descriptor of the model and view to
	 * render
	 */
	protected ViewSelection signalEventIn(FlowExecution flowExecution, Event event) {
		if (logger.isDebugEnabled()) {
			logger.debug("New request received from client, source event is: " + event);
		}
		// signal the event within the current state
		Assert.hasText(event.getId(),
				"No eventId could be obtained: make sure the client provides the _eventId parameter as input; "
						+ "the parameters provided for this request were:" + StylerUtils.style(event.getParameters()));
		// see if the eventId was set to a static marker placeholder because
		// of a client configuration error
		if (event.getId().equals(getNotSetEventIdParameterMarker())) {
			throw new IllegalArgumentException("The received eventId was the 'not set' marker '"
					+ getNotSetEventIdParameterMarker()
					+ "' -- this is likely a client view (jsp, etc) configuration error --"
					+ "the _eventId parameter must be set to a valid event");
		}
		return flowExecution.signalEvent(event);
	}

	/**
	 * Save the flow execution to storage.
	 * @param flowExecutionId the previous storage id (if previously saved)
	 * @param flowExecution the execution
	 * @param sourceEvent the source event
	 * @return the new storage id (may be different)
	 */
	public Serializable saveFlowExecution(Serializable flowExecutionId, FlowExecution flowExecution, Event sourceEvent) {
		flowExecutionId = getStorage().save(flowExecutionId, flowExecution, sourceEvent);
		flowExecution.getListeners().fireSaved(flowExecution, flowExecutionId);
		if (logger.isDebugEnabled()) {
			logger.debug("Saved flow execution out to storage with id: '" + flowExecutionId + "'");
		}
		return flowExecutionId;
	}

	/**
	 * Remove the flow execution from storage
	 * @param flowExecutionId the storage id
	 * @param flowExecution the execution
	 * @param sourceEvent the source event
	 */
	protected void removeFlowExecution(Serializable flowExecutionId, FlowExecution flowExecution, Event sourceEvent) {
		// event processing resulted in a previously saved flow execution
		// ending, cleanup
		getStorage().remove(flowExecutionId, sourceEvent);
		flowExecution.getListeners().fireRemoved(flowExecution, flowExecutionId);
		if (logger.isDebugEnabled()) {
			logger.debug("Removed flow execution from storage with id: '" + flowExecutionId + "'");
		}
	}

	/**
	 * Returns the name of the flow execution id parameter in the event
	 * ("_flowExecutionId").
	 */
	public String getFlowExecutionIdParameterName() {
		return FLOW_EXECUTION_ID_PARAMETER;
	}

	/**
	 * Returns the marker value indicating that the event id parameter was not
	 * set properly in the event because of a view configuration error
	 * ("@NOT_SET@").
	 * <p>
	 * This is useful when a view relies on an dynamic means to set the eventId
	 * event parameter, for example, using javascript. This approach assumes the
	 * "not set" marker value will be a static default (a kind of fallback,
	 * submitted if the eventId does not get set to the proper dynamic value
	 * onClick, for example, if javascript was disabled).
	 */
	public String getNotSetEventIdParameterMarker() {
		return NOT_SET_EVENT_ID;
	}

	/**
	 * Process updating FlowExecutionStorage if neccessary for the manipulated
	 * FlowExecution. Saves the FlowExecution out to storage if the execution is
	 * still active. Removes the FlowExecution from storage if it is no longer
	 * active.
	 * @param flowExecutionId the previous execution id (may be null if a new
	 * flow execution was launched)
	 * @param flowExecution the manipulated flow execution (state machine)
	 * @param sourceEvent the external event that triggered flow execution
	 * manipulation
	 * @return the id the managed FlowExecution is stored under (may be
	 * different if a new id was assigned, will be null if the flow execution
	 * was removed)
	 */
	protected Serializable manageStorage(Serializable flowExecutionId, FlowExecution flowExecution, Event sourceEvent) {
		if (flowExecution.isActive()) {
			// save the flow execution for future use
			flowExecutionId = saveFlowExecution(flowExecutionId, flowExecution, sourceEvent);
		}
		else {
			if (flowExecutionId != null) {
				removeFlowExecution(flowExecutionId, flowExecution, sourceEvent);
				flowExecutionId = null;
			}
		}
		return flowExecutionId;
	}

	/**
	 * Do any processing necessary before given view descriptor can be returned
	 * to the client of the flow execution manager. This implementation adds a
	 * number of <i>infrastructure attributes</i> to the model that will be
	 * exposed to the view. More specifically, it will add the
	 * {@link #FLOW_EXECUTION_CONTEXT_ATTRIBUTE},
	 * {@link #FLOW_EXECUTION_ID_ATTRIBUTE} and
	 * {@link #CURRENT_STATE_ID_ATTRIBUTE}.
	 * @param selectedView the view descriptor to be processed
	 * @param flowExecutionId the unique id of the flow execution
	 * @param flowExecutionContext the flow context providing info about the
	 * flow execution
	 * @return the processed view descriptor
	 */
	protected ViewSelection prepareSelectedView(ViewSelection selectedView, Serializable flowExecutionId,
			FlowExecutionContext flowExecutionContext) {
		if (flowExecutionContext.isActive() && selectedView != null) {
			if (selectedView.isRedirect()) {
				selectedView.addObject(getFlowExecutionIdParameterName(), flowExecutionId);
			}
			else {
				// make the entire flow execution context available in the model
				selectedView.addObject(FLOW_EXECUTION_CONTEXT_ATTRIBUTE, flowExecutionContext);
				// make the unique flow execution id and current state id
				// available in the model as convenience to views
				selectedView.addObject(FLOW_EXECUTION_ID_ATTRIBUTE, flowExecutionId);
				selectedView.addObject(CURRENT_STATE_ID_ATTRIBUTE, flowExecutionContext.getCurrentState().getId());
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Returning selected view to client: " + selectedView);
		}
		return selectedView;
	}
}