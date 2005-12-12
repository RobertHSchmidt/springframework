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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.webflow.Event;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactLookupException;
import org.springframework.webflow.FlowExecutionControlContext;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.FlowSessionStatus;
import org.springframework.webflow.State;
import org.springframework.webflow.StateException;
import org.springframework.webflow.TransitionableState;
import org.springframework.webflow.ViewSelection;

/**
 * Default implementation of FlowExecution that uses a stack-based data
 * structure to manage
 * {@link org.springframework.webflow.FlowSession flow sessions}. This class is
 * closely coupled with <code>FlowSessionImpl</code> and
 * <code>FlowControlContextImpl</code>. The three classes work together to
 * form a complete flow execution implementation based on a finite state
 * machine.
 * <p>
 * This implementation of FlowExecution is serializable so it can be safely
 * stored in an HTTP session or other persistent store such as a file, database,
 * or client-side form field.
 * <p>
 * Note: this implementation synchronizes the signalEvent Flow execution entry
 * point. It is locked on a per client basis for this flow execution.
 * Synchronization prevents a client from being able to signal other events
 * before previously signaled ones have processed in-full, preventing possible
 * race conditions.
 * 
 * @see org.springframework.webflow.FlowSession
 * @see org.springframework.webflow.execution.FlowSessionImpl
 * @see org.springframework.webflow.execution.FlowExecutionControlContextImpl
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowExecutionImpl implements FlowExecution, Externalizable {

	/**
	 * The serialization id.
	 */
	private static final long serialVersionUID = -898397026261844347L;

	private static final Log logger = LogFactory.getLog(FlowExecutionImpl.class);

	/**
	 * Key uniquely identifying this flow execution.
	 */
	private Serializable key;

	/**
	 * The time at which this object was created.
	 */
	private long creationTimestamp;

	/**
	 * The execution's root flow; the top level flow that acts as the starting
	 * point for this flow execution.
	 */
	private transient Flow rootFlow;

	/**
	 * Set only on deserialization so this object can be fully reconstructed.
	 */
	private String rootFlowId;

	/**
	 * The id of the last event that was signaled in this flow execution.
	 * <p>
	 * Note that we're not storing the event itself because that would cause
	 * serialization related issues.
	 */
	private String lastEventId;

	/**
	 * The timestamp when the last request to manipulate this flow execution was
	 * processed.
	 */
	private long lastRequestTimestamp;

	/**
	 * The stack of active, currently executing flow sessions. As subflows are
	 * spawned, they are pushed onto the stack. As they end, they are popped off
	 * the stack.
	 */
	private Stack executingFlowSessions = new Stack();

	/**
	 * A thread-safe listener list, holding listeners monitoring the lifecycle
	 * of this flow execution.
	 */
	private transient FlowExecutionListenerList listenerList = new FlowExecutionListenerList();

	/**
	 * The application transaction synchronization strategy to use.
	 */
	private transient TransactionSynchronizer transactionSynchronizer;

	/**
	 * Default constructor required for externalizable serialization. Should NOT
	 * be called programmatically.
	 */
	public FlowExecutionImpl() {

	}

	/**
	 * Create a new flow execution executing the provided flow. This constructor
	 * is mainly used for testing
	 * @param rootFlow the root flow of this flow execution
	 */
	public FlowExecutionImpl(Flow rootFlow) {
		this(new RandomGuidKeyGenerator().generate(), rootFlow, new FlowExecutionListener[0],
				new FlowScopeTokenTransactionSynchronizer());
	}

	/**
	 * Create a new flow execution executing the provided flow.
	 * @param key the key uniquely identifying this flow execution.
	 * @param rootFlow the root flow of this flow execution
	 * @param listeners the listeners interested in flow execution lifecycle
	 * events
	 * @param transactionSynchronizer the application transaction
	 * synchronization strategy to use
	 */
	public FlowExecutionImpl(Serializable key, Flow rootFlow, FlowExecutionListener[] listeners,
			TransactionSynchronizer transactionSynchronizer) {
		Assert.notNull(key, "The unique key identifying this flow execution is required");
		Assert.notNull(rootFlow, "The root flow definition is required");
		Assert.notNull(transactionSynchronizer, "The transaction synchronizer is required");
		this.key = key;
		this.rootFlow = rootFlow;
		getListeners().add(listeners);
		this.transactionSynchronizer = transactionSynchronizer;
		creationTimestamp = System.currentTimeMillis();
		if (logger.isDebugEnabled()) {
			logger.debug("Created new execution of flow '" + rootFlow.getId() + "' with key '" + key + "'");
		}
	}

	/**
	 * Returns the transaction synchronizer in use.
	 */
	public TransactionSynchronizer getTransactionSynchronizer() {
		return transactionSynchronizer;
	}

	/**
	 * Set the transaction synchronization strategy to use.
	 */
	protected void setTransactionSynchronizer(TransactionSynchronizer transactionSynchronizer) {
		this.transactionSynchronizer = transactionSynchronizer;
	}

	// implementing FlowExecutionStatistics

	public Serializable getKey() {
		return key;
	}

	public String getCaption() {
		return "FlowExecution:rootFlow=" + (getRootFlow() != null ? getRootFlow().getId() : rootFlowId) + ", key="
				+ getKey();
	}

	public long getCreationTimestamp() {
		return this.creationTimestamp;
	}

	public long getUptime() {
		return System.currentTimeMillis() - this.creationTimestamp;
	}

	public long getLastRequestTimestamp() {
		return this.lastRequestTimestamp;
	}

	/**
	 * Update the last request timestamp to now.
	 */
	protected void updateLastRequestTimestamp() {
		this.lastRequestTimestamp = System.currentTimeMillis();
	}

	public String getLastEventId() {
		return lastEventId;
	}

	/**
	 * Set the last event processed by this flow execution.
	 * @param lastEvent the last event to set
	 */
	protected void setLastEvent(Event lastEvent) {
		Assert.notNull(lastEvent, "The last event is required");
		this.lastEventId = lastEvent.getId();
	}

	public boolean isActive() {
		return !executingFlowSessions.isEmpty();
	}

	public boolean isRootFlowActive() {
		if (isActive()) {
			return getActiveSession().isRoot();
		}
		else {
			return false;
		}
	}

	// implementing FlowExecutionContext

	public Flow getRootFlow() {
		return rootFlow;
	}

	public Flow getActiveFlow() {
		return getActiveSession().getFlow();
	}

	public State getCurrentState() {
		return getActiveSession().getCurrentState();
	}

	public FlowSession getActiveSession() {
		return getActiveSessionInternal();
	}

	public boolean equals(Object o) {
		if (!(o instanceof FlowExecutionImpl)) {
			return false;
		}
		FlowExecutionImpl other = (FlowExecutionImpl)o;
		// two executions ("conversations") are equal if their keys are equal
		return getKey().equals(other.getKey());
	}

	public int hashCode() {
		return getKey().hashCode();
	}

	/**
	 * Check that this flow execution is active and throw an exception if it's
	 * not.
	 */
	protected void assertActive() throws IllegalStateException {
		if (!isActive()) {
			throw new IllegalStateException(
					"This flow execution is not active, it has either ended or has never been started.");
		}
	}

	// methods implementing FlowExecution

	public ViewSelection start(String stateId, ExternalContext externalContext) throws StateException {
		Assert.state(!isActive(),
				"This flow is already executing -- you cannot call 'start(stateId, externalContxt)' more than once");
		if (!StringUtils.hasText(stateId)) {
			stateId = getRootFlow().getStartState().getId();
		}
		updateLastRequestTimestamp();
		if (logger.isDebugEnabled()) {
			logger.debug("Starting this execution in state " + stateId);
		}
		FlowExecutionControlContext context = createControlContext(externalContext);
		getListeners().fireRequestSubmitted(context);
		try {
			try {
				State startState = getRootFlow().getRequiredState(stateId);
				ViewSelection selectedView = context.start(getRootFlow(), startState, null);
				return pause(context, selectedView);
			}
			catch (StateException e) {
				return pause(context, handleException(e, context));
			}
		}
		finally {
			getListeners().fireRequestProcessed(context);
		}
	}

	/**
	 * Handles an exception that occured performing an operation on this flow
	 * execution. First trys the set of exception handlers associated with the
	 * offending state, then the handlers at the flow level.
	 * @param exception the exception that occured
	 * @param context the state context the exception occured in
	 * @return the selected error view
	 * @throws StateException rethrows the exception it was not handled at the
	 * state or flow level
	 */
	protected ViewSelection handleException(StateException exception, FlowExecutionControlContext context)
			throws StateException {
		ViewSelection selectedView = exception.getState().handleException(exception, context);
		if (selectedView != null) {
			return selectedView;
		}
		selectedView = exception.getState().getFlow().handleException(exception, context);
		if (selectedView != null) {
			return selectedView;
		}
		throw exception;
	}

	public synchronized ViewSelection signalEvent(String eventId, String stateId, ExternalContext externalContext)
			throws StateException {
		assertActive();
		updateLastRequestTimestamp();
		if (logger.isDebugEnabled()) {
			logger.debug("Resuming this execution on user event '" + eventId + "'");
		}
		FlowExecutionControlContextImpl context = createControlContext(externalContext);
		getListeners().fireRequestSubmitted(context);
		try {
			try {
				resume(context);
				ViewSelection selectedView;
				Event event = new Event(externalContext, eventId);
				if (!StringUtils.hasText(stateId)) {
					selectedView = context.signalEvent(event);
				}
				else {
					if (!stateId.equals(getCurrentState().getId())) {
						TransitionableState state = getActiveFlow().getRequiredTransitionableState(stateId);
						selectedView = context.handleNewStateRequest(state, event);
					}
					else {
						selectedView = context.signalEvent(event);
					}
				}
				return pause(context, selectedView);
			}
			catch (StateException e) {
				return pause(context, handleException(e, context));
			}
		}
		finally {
			getListeners().fireRequestProcessed(context);
		}
	}

	/**
	 * Resume this flow execution.
	 * @param context the state request context
	 */
	protected void resume(FlowExecutionControlContext context) {
		if (context.getFlowExecutionContext().getActiveFlow().isTransactional()) {
			context.assertInTransaction(false);
		}
		getActiveSessionInternal().setStatus(FlowSessionStatus.ACTIVE);
		getListeners().fireResumed(context);
	}

	/**
	 * Pause this flow execution.
	 * @param context the state request context
	 * @param selectedView the initial selected view to render
	 * @return the selected view to render
	 */
	protected ViewSelection pause(FlowExecutionControlContext context, ViewSelection selectedView) {
		if (!isActive()) {
			return selectedView;
		}
		getActiveSessionInternal().setStatus(FlowSessionStatus.PAUSED);
		getListeners().firePaused(context, selectedView);
		if (logger.isDebugEnabled()) {
			if (selectedView != null) {
				logger.debug("Paused to render " + selectedView + " and wait for user input");
			}
			else {
				logger.debug("Paused to wait for user input");
			}
		}
		return selectedView;
	}

	public FlowExecutionListenerList getListeners() {
		return listenerList;
	}

	// flow session management helpers

	/**
	 * Create a flow execution control context for given event.
	 * <p>
	 * The default implementation uses the <code>FlowControlContextImpl</code>
	 * class. Subclasses can override this to use a custom class.
	 * @param sourceEvent the event at the origin of this request
	 */
	protected FlowExecutionControlContextImpl createControlContext(ExternalContext externalContext) {
		return new FlowExecutionControlContextImpl(this, externalContext);
	}

	/**
	 * Returns the currently active flow session.
	 * @throws IllegalStateException this execution is not active
	 */
	protected FlowSessionImpl getActiveSessionInternal() throws IllegalStateException {
		assertActive();
		return (FlowSessionImpl)executingFlowSessions.peek();
	}

	/**
	 * Returns the parent flow session of the currently active flow session.
	 * @return the parent flow session
	 * @throws IllegalArgumentException when this execution is not active or
	 * when the current flow session has no parent (e.g. is the root flow
	 * session)
	 */
	protected FlowSession getParentSession() throws IllegalArgumentException {
		assertActive();
		Assert.state(!getActiveSession().isRoot(),
				"There is no parent flow session for the currently active flow session");
		return (FlowSession)executingFlowSessions.get(executingFlowSessions.size() - 2);
	}

	/**
	 * Returns the flow session associated with the root flow.
	 * @throws IllegalStateException this execution is not active
	 */
	protected FlowSession getRootSession() throws IllegalStateException {
		assertActive();
		return (FlowSession)executingFlowSessions.get(0);
	}

	/**
	 * Set the state that is currently active in this flow execution.
	 * @param newState the new current state
	 */
	protected void setCurrentState(State newState) {
		getActiveSessionInternal().setCurrentState(newState);
	}

	/**
	 * Activate a new <code>FlowSession</code> for the flow definition with
	 * the input provided. Pushes the new flow session onto the stack.
	 * @param flow the flow definition
	 * @param input the flow session input
	 * @return the new flow session
	 */
	public FlowSession activateSession(Flow flow, Map input) {
		FlowSessionImpl session;
		if (!executingFlowSessions.isEmpty()) {
			FlowSessionImpl parent = getActiveSessionInternal();
			parent.setStatus(FlowSessionStatus.SUSPENDED);
			session = createFlowSession(flow, input, parent);
		}
		else {
			session = createFlowSession(flow, input, null);
		}
		executingFlowSessions.push(session);
		session.setStatus(FlowSessionStatus.ACTIVE);
		if (logger.isDebugEnabled()) {
			logger.debug("Activated " + session);
		}
		return session;
	}

	/**
	 * Create a new flow session object. Subclasses can override this to return
	 * a special implementation if required.
	 * @param flow the flow that should be associated with the flow session
	 * @param input the input parameters used to populate the flow session
	 * @param parent the flow session that should be the parent of the newly
	 * created flow session
	 * @return the newly created flow session
	 */
	protected FlowSessionImpl createFlowSession(Flow flow, Map input, FlowSessionImpl parent) {
		return new FlowSessionImpl(flow, input, parent);
	}

	public FlowSession endActiveFlowSession() {
		FlowSessionImpl endingSession = (FlowSessionImpl)executingFlowSessions.pop();
		endingSession.setStatus(FlowSessionStatus.ENDED);
		if (!executingFlowSessions.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Resuming session '" + getActiveSessionInternal().getFlow().getId() + "' in state '"
						+ getActiveSessionInternal().getCurrentState().getId() + "'");
			}
			getActiveSessionInternal().setStatus(FlowSessionStatus.ACTIVE);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("[Ended] - this execution '" + getKey() + "' is now inactive");
			}
		}
		return endingSession;
	}

	// custom serialization (implementation of Externalizable for optimized
	// storage)

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		key = (Serializable)in.readObject();
		creationTimestamp = in.readLong();
		rootFlowId = (String)in.readObject();
		lastEventId = (String)in.readObject();
		lastRequestTimestamp = in.readLong();
		executingFlowSessions = (Stack)in.readObject();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(key);
		out.writeLong(creationTimestamp);
		if (this.getRootFlow() != null) {
			// avoid bogus NullPointerExceptions
			out.writeObject(getRootFlow().getId());
		}
		else {
			out.writeObject(null);
		}
		out.writeObject(lastEventId);
		out.writeLong(lastRequestTimestamp);
		out.writeObject(executingFlowSessions);
	}

	public synchronized void rehydrate(FlowLocator flowLocator, FlowExecutionListenerLoader listenerLoader,
			TransactionSynchronizer transactionSynchronizer) {
		// implementation note: we cannot integrate this code into the
		// {@link readExternal(ObjectInput)} method since we need the flow
		// locator, listener list and tx synchronizer!
		if (isHydrated()) {
			// nothing to do, we're already hydrated
			return;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Rehydrating");
		}
		Assert.notNull(rootFlowId, "The root flow id was not set during deserialization: cannot restore"
				+ " -- was this flow execution deserialized properly?");
		rootFlow = flowLocator.getFlow(rootFlowId);
		rootFlowId = null;
		// rehydrate all flow sessions
		Iterator it = this.executingFlowSessions.iterator();
		while (it.hasNext()) {
			FlowSessionImpl session = (FlowSessionImpl)it.next();
			session.rehydrate(new FlowSessionFlowLocator(rootFlow, flowLocator));
		}
		if (isActive()) {
			// sanity check
			Assert.isTrue(getRootFlow() == getRootSession().getFlow(),
					"The root flow of the execution should be the same as the flow in the root flow session");
		}
		listenerList = new FlowExecutionListenerList();
		listenerList.add(listenerLoader.getListeners(rootFlow));
		this.transactionSynchronizer = transactionSynchronizer;
		if (logger.isDebugEnabled()) {
			logger.debug("Rehydrated");
		}
	}

	private static class FlowSessionFlowLocator implements FlowLocator {
		private FlowLocator flowLocator;

		private Flow rootFlow;

		public FlowSessionFlowLocator(Flow rootFlow, FlowLocator flowLocator) {
			this.rootFlow = rootFlow;
			this.flowLocator = flowLocator;
		}

		public Flow getFlow(String id) throws FlowArtifactLookupException {
			if (rootFlow.getId().equals(id)) {
				return rootFlow;
			}
			else if (rootFlow.containsInlineFlow(id)) {
				return rootFlow.getInlineFlow(id);
			}
			else {
				return flowLocator.getFlow(id);
			}
		}

	}

	/**
	 * Returns whether this flow execution is hydrated.
	 */
	protected boolean isHydrated() {
		return rootFlow != null;
	}

	public String toString() {
		if (!isActive()) {
			return "[Inactive " + getCaption() + "]";
		}
		else {
			if (isHydrated()) {
				return new ToStringCreator(this).append("key", getKey()).append("activeFlow",
						getActiveSession().getFlow().getId()).append("currentState", getCurrentState().getId()).append(
						"rootFlow", getRootFlow().getId()).append("executingFlowSessions", executingFlowSessions)
						.toString();
			}
			else {
				return "[Unhydrated " + getCaption() + "]";
			}
		}
	}
}