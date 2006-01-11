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
package org.springframework.webflow.execution.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.format.Formatter;
import org.springframework.core.CollectionFactory;
import org.springframework.core.style.StylerUtils;
import org.springframework.util.Assert;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowException;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowExecutionListenerCriteria;
import org.springframework.webflow.execution.FlowExecutionListenerCriteriaFactory;
import org.springframework.webflow.execution.FlowExecutionListenerLoader;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.TextToFlowExecutionListenerCriteria;
import org.springframework.webflow.execution.impl.FlowExecutionImpl;
import org.springframework.webflow.execution.repository.ExternalMapFlowExecutionRepositoryFactory;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKey;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKeyFormatter;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory;

/**
 * A central facade for the execution of flows within an application. This
 * object is responsible for creating and starting new flow executions as
 * requested by clients, as well as signaling events for processing by existing,
 * paused executions (that are waiting to be resumed in response to a user
 * event). This object is a facade or entry point into the flow execution
 * subsystem, and makes the overall subsystem easier to use.
 * <p>
 * The {@link #handleFlowRequest(ExternalContext)} method is the central facade operation
 * and implements the following algorithm:
 * <ol>
 * <li>Search for a flow execution id in the external context (in a request
 * parameter named {@link #getFlowExecutionIdParameterName()).</li>
 * <li>If no flow execution id was submitted, create a new flow execution. The
 * top-level flow definition for which an execution is created for is determined
 * by the value of the {@link #getFlowIdParameterName()} request parameter. If
 * this parameter parameter is not present, an exception is thrown.</li>
 * <li>If a flow execution id <em>was</em> submitted, load the previously
 * saved FlowExecution with that id from a repository ({@link #getRepository(ExternalContext)}).</li>
 * <li>If a new flow execution was created in the previous steps, start that
 * execution.</li>
 * <li>If an existing flow execution was loaded from a repository, extract the
 * value of the event id ({@link #getEventIdParameterName()). Signal the occurence of the user event, resuming the flow
 * execution in the current state.</li>
 * <li>If the flow execution is still active after event processing, save it
 * out to the repository. This process generates a unique flow execution id that
 * will be exposed to the caller for identifying the same FlowExecution
 * (conversation) on subsequent requests. The caller will also be given access
 * to the flow execution context and any data placed in request or flow scope.</li>
 * </ol>
 * <p>
 * By default, this class will use the flow execution implementation provided by
 * the <code>FlowExecutionImpl</code> class. If you would like to use a
 * different implementation, override the {@link #createFlowExecution(Flow)}
 * method in a subclass.
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
 * <td>repositoryFactory</td>
 * <td>A server-side, stateful-session-based repository factory</td>
 * <td>The strategy for accessing managed flow execution repositories</td>
 * </tr>
 * <tr>
 * <td>listeners</td>
 * <td>None</td>
 * <td>The listeners to observe the lifecycle of managed flow executions</td>
 * </tr>
 * </table>
 * </p>
 * @see org.springframework.webflow.execution.FlowExecution
 * @see org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory
 * @see org.springframework.webflow.execution.FlowExecutionListener
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class FlowExecutionManagerImpl implements FlowExecutionManager, FlowExecutionListenerLoader {

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
	 * The flow execution repository factoring, for obtaining repository
	 * instances to save paused executions that require user input and load
	 * resuming executions that will process user events.
	 */
	private FlowExecutionRepositoryFactory repositoryFactory = new ExternalMapFlowExecutionRepositoryFactory();

	/**
	 * The formatter that will parse encoded _flowExecutionId strings into
	 * {@link FlowExecutionContinuationKey} objects.
	 */
	private Formatter continuationKeyFormatter = new FlowExecutionContinuationKeyFormatter();

	/**
	 * A set of flow execution listeners to a list of flow execution listener
	 * criteria objects. The criteria list determines the conditions in which a
	 * single flow execution listener applies.
	 */
	private Set listenerSet = CollectionFactory.createLinkedSetIfPossible(6);

	/**
	 * Create a new flow execution manager using the specified flow locator for
	 * loading Flow definitions.
	 * @param flowLocator the flow locator to use
	 * 
	 * @see #setFlowLocator(FlowLocator)
	 * @see #setRepositoryFactory(FlowExecutionRepositoryFactory)
	 * @see #setListener(FlowExecutionListener)
	 * @see #setListenerCriteria(FlowExecutionListener,
	 * FlowExecutionListenerCriteria)
	 * @see #setListenerMap(Map)
	 * @see #setListeners(Collection)
	 * @see #setListenersCriteria(Collection, FlowExecutionListenerCriteria)
	 */
	public FlowExecutionManagerImpl(FlowLocator flowLocator) {
		setFlowLocator(flowLocator);
	}

	/**
	 * Returns the flow locator to use for lookup of flows specified using the
	 * {@link #FLOW_ID_PARAMETER} event parameter.
	 */
	protected FlowLocator getFlowLocator() {
		return flowLocator;
	}

	/**
	 * Set the flow locator to use for lookup of flows specified using the
	 * {@link #FLOW_ID_PARAMETER} parameter.
	 */
	public void setFlowLocator(FlowLocator flowLocator) {
		this.flowLocator = flowLocator;
	}

	/**
	 * Set the repository factory used by the flow execution manager.
	 */
	public void setRepositoryFactory(FlowExecutionRepositoryFactory repositoryLocator) {
		this.repositoryFactory = repositoryLocator;
	}

	/**
	 * Returns the repository instance to be used by the flow execution manager.
	 */
	protected FlowExecutionRepository getRepository(ExternalContext context) {
		return repositoryFactory.getRepository(context);
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
		for (Iterator it = listenerSet.iterator(); it.hasNext();) {
			ConditionalFlowExecutionListenerHolder listenerHolder = (ConditionalFlowExecutionListenerHolder)it.next();
			if (listenerHolder.listenerAppliesTo(flow)) {
				listenersToAttach.add(listenerHolder.getListener());
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Loaded [" + listenersToAttach.size() + "] of possible " + listenerSet.size()
					+ " listeners to this execution request for flow '" + flow.getId()
					+ "', the listeners to attach are " + StylerUtils.style(listenersToAttach));
		}
		return (FlowExecutionListener[])listenersToAttach.toArray(new FlowExecutionListener[listenersToAttach.size()]);
	}

	/**
	 * Returns a unmodifiable map of the configured flow execution listeners and
	 * the criteria in which those listeners apply.
	 */
	public Set getListenerSet() {
		return Collections.unmodifiableSet(listenerSet);
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
			logger.debug("Setting listeners " + listeners + " with criteria " + criteria);
		}
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			FlowExecutionListener listener = (FlowExecutionListener)it.next();
			if (containsListener(listener)) {
				removeListener(listener);
			}
			addListener(listener, criteria);
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

	/**
	 * Helper that converts from text to a FlowExecutionListenerCriteria
	 * @param encodedCriteria the encoded text
	 * @return the criteria
	 */
	protected FlowExecutionListenerCriteria convertEncodedListenerCriteria(String encodedCriteria) {
		return new TextToFlowExecutionListenerCriteria().convert(encodedCriteria);
	}

	/**
	 * Add a listener that will listen to executions for all flows.
	 * @param listener the listener to add
	 */
	public void addListener(FlowExecutionListener listener) {
		addListener(listener, FlowExecutionListenerCriteriaFactory.allFlows());
	}

	/**
	 * Add a listener that will listen to executions to flows matching the
	 * specified criteria.
	 * @param listener the listener
	 * @param criteria the listener criteria
	 */
	public void addListener(FlowExecutionListener listener, FlowExecutionListenerCriteria criteria) {
		if (logger.isDebugEnabled()) {
			logger.debug("Adding flow execution listener " + listener + " with criteria " + criteria);
		}
		ConditionalFlowExecutionListenerHolder conditional = getHolder(listener);
		if (conditional == null) {
			conditional = new ConditionalFlowExecutionListenerHolder(listener);
			listenerSet.add(conditional);
		}
		if (criteria == null) {
			criteria = FlowExecutionListenerCriteriaFactory.allFlows();
		}
		conditional.add(criteria);
	}

	protected ConditionalFlowExecutionListenerHolder getHolder(FlowExecutionListener listener) {
		Iterator it = listenerSet.iterator();
		while (it.hasNext()) {
			ConditionalFlowExecutionListenerHolder next = (ConditionalFlowExecutionListenerHolder)it.next();
			if (next.getListener().equals(listener)) {
				return next;
			}
		}
		return null;
	}

	/**
	 * Is the listener contained by this Flow execution manager?
	 * @param listener the listener
	 * @return true if yes, false otherwise
	 */
	public boolean containsListener(FlowExecutionListener listener) {
		return listenerSet.contains(listener);
	}

	/**
	 * Remove the flow execution listener from the listener list.
	 * @param listener the listener
	 */
	public void removeListener(FlowExecutionListener listener) {
		listenerSet.remove(listener);
	}

	/**
	 * Remove the criteria for the specified listener.
	 * @param listener the listener
	 * @param criteria the criteria
	 */
	public void removeListenerCriteria(FlowExecutionListener listener, FlowExecutionListenerCriteria criteria) {
		if (containsListener(listener)) {
			ConditionalFlowExecutionListenerHolder listenerHolder = getHolder(listener);
			listenerHolder.remove(criteria);
			if (listenerHolder.isCriteriaSetEmpty()) {
				removeListener(listener);
			}
		}
	}

	/**
	 * Returns the continuation key formatting strategy.
	 * 
	 * @return the continuation key formatter
	 */
	public Formatter getContinuationKeyFormatter() {
		return continuationKeyFormatter;
	}

	/**
	 * Sets the flow execution continuation key formatting strategy.
	 * @param continuationKeyFormatter the continuation key formatter
	 */
	public void setContinuationKeyFormatter(Formatter continuationKeyFormatter) {
		this.continuationKeyFormatter = continuationKeyFormatter;
	}

	public ViewSelection launch(String flowId, ExternalContext context) throws FlowException {
		Flow flow = getFlowLocator().getFlow(flowId);
		FlowExecution flowExecution = createFlowExecution(flow);
		ViewSelection selectedView = flowExecution.start(context);
		if (flowExecution.isActive()) {
			FlowExecutionRepository repository = getRepository(context);
			FlowExecutionContinuationKey continuationKey = repository.generateContinuationKey(flowExecution);
			repository.putFlowExecution(continuationKey, flowExecution);
			return prepareSelectedView(selectedView, continuationKey, flowExecution);
		}
		else {
			return selectedView;
		}
	}

	/**
	 * Create a new flow execution for given flow. Subclasses could redefine
	 * this if they wish to use a specialized FlowExecution implementation
	 * class.
	 * @param flow the flow
	 * @return the created flow execution
	 */
	protected FlowExecution createFlowExecution(Flow flow) {
		FlowExecution flowExecution = new FlowExecutionImpl(flow, getListeners(flow));
		if (logger.isDebugEnabled()) {
			logger.debug("Created a new flow execution for flow definition '" + flow.getId() + "'");
		}
		return flowExecution;
	}
	
	public ViewSelection signalEvent(String eventId, String flowExecutionId, ExternalContext context) throws FlowException {
		FlowExecutionRepository repository = getRepository(context);
		FlowExecutionContinuationKey continuationKey = parseContinuationKey(flowExecutionId);
		FlowExecution flowExecution = loadFlowExecution(repository, continuationKey);
		ViewSelection selectedView = flowExecution.signalEvent(eventId, context);
		if (flowExecution.isActive()) {
			continuationKey = repository.generateContinuationKey(flowExecution, continuationKey.getConversationId());
			repository.putFlowExecution(continuationKey, flowExecution);
			return prepareSelectedView(selectedView, continuationKey, flowExecution);
		} else {
			repository.invalidateConversation(continuationKey.getConversationId());
			return selectedView;
		}
	}
	
	/**
	 * Load an existing FlowExecution based on data in the specified source
	 * event.
	 * @param continuationKey the unique id of the flow execution
	 * @param context the context in which the external user event occured
	 */
	public FlowExecution loadFlowExecution(FlowExecutionRepository repository,
			FlowExecutionContinuationKey continuationKey) {
		// client is participating in an existing flow execution, retrieve
		// information about it
		FlowExecution flowExecution = repository.getFlowExecution(continuationKey);
		// rehydrate the execution if neccessary (if it had been serialized out)
		flowExecution.rehydrate(getFlowLocator(), this);
		if (logger.isDebugEnabled()) {
			logger.debug("Loaded existing flow execution from repository with id '" + continuationKey + "'");
		}
		return flowExecution;
	}

	protected FlowExecutionContinuationKey parseContinuationKey(String flowExecutionId) {
		return (FlowExecutionContinuationKey)continuationKeyFormatter.parseValue(flowExecutionId,
				FlowExecutionContinuationKey.class);
	}

	/**
	 * Perform any processing necessary before the view selection made is
	 * returned to the client of the flow execution manager and rendered out.
	 * This implementation adds a number of <i>infrastructure attributes</i> to
	 * the model that will be exposed to the view so clients may record
	 * information about the flow to support participation in the flow on a
	 * subsequent request. More specifically, this method will add the
	 * {@link #FLOW_EXECUTION_CONTEXT_ATTRIBUTE},
	 * {@link #FLOW_EXECUTION_ID_ATTRIBUTE}.
	 * @param selectedView the view selection to be prepared
	 * @param continuationKey the assigned repository continuation key
	 * @param continuationKey the unique id of the flow execution
	 * @param flowExecutionContext the flow context providing info about the
	 * flow execution
	 * @return the prepped view selection
	 */
	protected ViewSelection prepareSelectedView(ViewSelection selectedView,
			FlowExecutionContinuationKey continuationKey, FlowExecutionContext flowExecutionContext) {
		if (flowExecutionContext.isActive() && selectedView != null) {
			String id = formatContinuationKey(continuationKey);
			if (selectedView.isRedirect()) {
				throw new UnsupportedOperationException("Redirect from a view state is not yet supported");
			}
			else {
				exposeFlowExecutionAttributes(selectedView.getModel(), id, flowExecutionContext);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Returning selected view to client " + selectedView);
		}
		return selectedView;
	}

	/**
	 * Convert the continuation key to encoded string form.
	 * @param key the continuation key
	 * @return the string-encoded key
	 */
	protected String formatContinuationKey(FlowExecutionContinuationKey key) {
		return continuationKeyFormatter.formatValue(key);
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

	/**
	 * A holder that holds a listener plus a set of criteria defining the flows
	 * in which that listener applies.
	 * @author Keith Donald
	 */
	public static final class ConditionalFlowExecutionListenerHolder {
		private FlowExecutionListener listener;

		private Set criteriaSet = CollectionFactory.createLinkedSetIfPossible(3);

		public ConditionalFlowExecutionListenerHolder(FlowExecutionListener listener) {
			this.listener = listener;
		}

		public FlowExecutionListener getListener() {
			return listener;
		}

		public void add(FlowExecutionListenerCriteria criteria) {
			criteriaSet.add(criteria);
		}

		public void remove(FlowExecutionListenerCriteria criteria) {
			criteriaSet.remove(criteria);
		}

		public boolean isCriteriaSetEmpty() {
			return criteriaSet.isEmpty();
		}

		public boolean equals(Object o) {
			if (!(o instanceof ConditionalFlowExecutionListenerHolder)) {
				return false;
			}
			return listener.equals(((ConditionalFlowExecutionListenerHolder)o).listener);
		}

		public int hashCode() {
			return listener.hashCode();
		}

		public boolean listenerAppliesTo(Flow flow) {
			Iterator it = criteriaSet.iterator();
			while (it.hasNext()) {
				FlowExecutionListenerCriteria criteria = (FlowExecutionListenerCriteria)it.next();
				if (criteria.appliesTo(flow)) {
					return true;
				}
			}
			return false;
		}
	}
}