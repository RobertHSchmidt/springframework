/*
 * Copyright 2004-2008 the original author or authors.
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
package org.springframework.webflow.engine.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.message.MessageContextFactory;
import org.springframework.binding.message.StateManageableMessageContext;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.CollectionUtils;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.RequestControlContext;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.FlowExecutionKeyFactory;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.execution.View;

/**
 * Default implementation of FlowExecution that uses a stack-based data structure to manage spawned flow sessions. This
 * class is closely coupled with package-private <code>FlowSessionImpl</code> and
 * <code>RequestControlContextImpl</code>. The three classes work together to form a complete flow execution
 * implementation based on a finite state machine.
 * <p>
 * This implementation of FlowExecution is serializable so it can be safely stored in an HTTP session or other
 * persistent store such as a file, database, or client-side form field. Once deserialized, the
 * {@link FlowExecutionImplStateRestorer} strategy is expected to be used to restore the execution to a usable state.
 * 
 * @see FlowExecutionImplFactory
 * @see FlowExecutionImplStateRestorer
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 * @author Jeremy Grelle
 */
public class FlowExecutionImpl implements FlowExecution, Externalizable {

	private static final Log logger = LogFactory.getLog(FlowExecutionImpl.class);

	private static final String FLASH_SCOPE_ATTRIBUTE = "flashScope";

	/**
	 * The execution's root flow; the top level flow that acts as the starting point for this flow execution.
	 * <p>
	 * Transient to support restoration by the {@link FlowExecutionImplStateRestorer}.
	 */
	private transient Flow flow;

	/**
	 * A flag indicating if this execution has started.
	 */
	private boolean started;

	/**
	 * The stack of active, currently executing flow sessions. As subflows are spawned, they are pushed onto the stack.
	 * As they end, they are popped off the stack.
	 */
	private LinkedList flowSessions;

	/**
	 * A thread-safe listener list, holding listeners monitoring the lifecycle of this flow execution.
	 * <p>
	 * Transient to support restoration by the {@link FlowExecutionImplStateRestorer}.
	 */
	private transient FlowExecutionListeners listeners;

	/**
	 * The factory for getting the key to assign this flow execution when needed for persistence.
	 */
	private transient FlowExecutionKeyFactory keyFactory;

	/**
	 * The factory for message contexts for tracking flow execution messages.
	 */
	private transient MessageContextFactory messageContextFactory;

	/**
	 * The key assigned to this flow execution. May be null if a key has not been assigned.
	 */
	private transient FlowExecutionKey key;

	/**
	 * A data structure for attributes shared by all flow sessions.
	 * <p>
	 * Transient to support restoration by the {@link FlowExecutionImplStateRestorer}.
	 */
	private transient MutableAttributeMap conversationScope;

	/**
	 * A data structure for runtime system execution attributes.
	 * <p>
	 * Transient to support restoration by the {@link FlowExecutionImplStateRestorer}.
	 */
	private transient AttributeMap attributes;

	/**
	 * Set so the transient {@link #flow} field can be restored by the {@link FlowExecutionImplStateRestorer}.
	 */
	private String flowId;

	/**
	 * The flow execution outcome event.
	 */
	private transient Event outcome;

	/**
	 * Default constructor required for externalizable serialization. Should NOT be called programmatically.
	 */
	public FlowExecutionImpl() {
	}

	/**
	 * Create a new flow execution executing the provided flow. Flow executions are normally created by a flow execution
	 * factory.
	 * @param flow the root flow of this flow execution
	 */
	public FlowExecutionImpl(Flow flow) {
		Assert.notNull(flow, "The flow definition is required");
		this.flow = flow;
		this.listeners = new FlowExecutionListeners();
		this.attributes = CollectionUtils.EMPTY_ATTRIBUTE_MAP;
		this.flowSessions = new LinkedList();
		this.conversationScope = new LocalAttributeMap();
		this.conversationScope.put(FLASH_SCOPE_ATTRIBUTE, new LocalAttributeMap());
	}

	/**
	 * Package private constructor only useful for testing restoration behavior for this object.
	 * @param flowId the flow id
	 * @param flowSessions the flow sessions
	 */
	FlowExecutionImpl(String flowId, LinkedList flowSessions) {
		this.flowId = flowId;
		this.flowSessions = flowSessions;
	}

	public String getCaption() {
		return "execution of '" + flow.getId() + "'";
	}

	// implementing FlowExecutionContext

	public FlowExecutionKey getKey() {
		return key;
	}

	public FlowDefinition getDefinition() {
		return flow;
	}

	public boolean hasStarted() {
		return started;
	}

	public boolean isActive() {
		return !flowSessions.isEmpty();
	}

	public boolean hasEnded() {
		return hasStarted() && !isActive();
	}

	public Event getOutcome() {
		return outcome;
	}

	public FlowSession getActiveSession() {
		if (!isActive()) {
			if (started) {
				throw new IllegalStateException("No active session to access; this flow execution has ended");
			} else {
				throw new IllegalStateException("No active session to access; this flow execution has not been started");
			}
		}
		return getActiveSessionInternal();
	}

	public MutableAttributeMap getFlashScope() {
		return (MutableAttributeMap) conversationScope.get(FLASH_SCOPE_ATTRIBUTE);
	}

	public MutableAttributeMap getConversationScope() {
		return conversationScope;
	}

	public AttributeMap getAttributes() {
		return attributes;
	}

	// methods implementing FlowExecution

	public void start(MutableAttributeMap input, ExternalContext externalContext) throws FlowExecutionException,
			IllegalStateException {
		Assert.state(!started, "This flow has already been started; you cannot call 'start()' more than once");
		if (logger.isDebugEnabled()) {
			logger.debug("Starting execution in " + externalContext);
		}
		started = true;
		RequestControlContext context = createControlContext(externalContext, createMessageContext());
		RequestContextHolder.setRequestContext(context);
		listeners.fireRequestSubmitted(context);
		try {
			start(flow, input, context);
		} catch (FlowExecutionException e) {
			handleException(e, context);
		} catch (Exception e) {
			handleException(wrap(e), context);
		} finally {
			if (!hasEnded()) {
				saveMessages(context);
				try {
					listeners.firePaused(context);
				} catch (Throwable e) {
					logger.error("Flow execution listener threw exception", e);
				}
			}
			try {
				listeners.fireRequestProcessed(context);
			} catch (Throwable e) {
				logger.error("Flow execution listener threw exception", e);
			}
			RequestContextHolder.setRequestContext(null);
		}
	}

	public void setCurrentState(String stateId) {
		State state = flow.getStateInstance(stateId);
		FlowSessionImpl session;
		if (started) {
			session = getActiveSessionInternal();
		} else {
			session = activateSession(flow);
			started = true;
		}
		session.setCurrentState(state);
	}

	public void resume(ExternalContext externalContext) throws FlowExecutionException, IllegalStateException {
		if (!isActive()) {
			if (started) {
				throw new IllegalStateException("This flow execution cannot be resumed; it has ended");
			} else {
				throw new IllegalStateException("This flow execution cannot be resumed; it has not been started");
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Resuming execution in " + externalContext);
		}
		RequestControlContext context = createControlContext(externalContext, createMessageContext());
		RequestContextHolder.setRequestContext(context);
		listeners.fireRequestSubmitted(context);
		try {
			listeners.fireResuming(context);
			getActiveSessionInternal().getFlow().resume(context);
		} catch (FlowExecutionException e) {
			handleException(e, context);
		} catch (Exception e) {
			handleException(wrap(e), context);
		} finally {
			if (!hasEnded()) {
				saveMessages(context);
				try {
					listeners.firePaused(context);
				} catch (Throwable e) {
					logger.error("Flow execution listener threw exception", e);
				}
			}
			try {
				listeners.fireRequestProcessed(context);
			} catch (Throwable e) {
				logger.error("Flow execution listener threw exception", e);
			}
			RequestContextHolder.setRequestContext(null);
		}
	}

	private MessageContext createMessageContext() {
		StateManageableMessageContext messageContext = messageContextFactory.createMessageContext();
		Serializable messagesMemento = (Serializable) getFlashScope().extract("messagesMemento");
		if (messagesMemento != null) {
			messageContext.restoreMessages(messagesMemento);
		}
		return messageContext;
	}

	private void saveMessages(RequestContext context) {
		Serializable messagesMemento = ((StateManageableMessageContext) context.getMessageContext())
				.createMessagesMemento();
		getFlashScope().put("messagesMemento", messagesMemento);
	}

	// subclassing hooks

	/**
	 * Create a flow execution control context.
	 * @param externalContext the external context triggering this request
	 */
	protected RequestControlContext createControlContext(ExternalContext externalContext, MessageContext messageContext) {
		return new RequestControlContextImpl(this, externalContext, messageContext);
	}

	/**
	 * Create a new flow session object. Subclasses can override this to return a special implementation if required.
	 * @param flow the flow that should be associated with the flow session
	 * @param parent the flow session that should be the parent of the newly created flow session (may be null)
	 * @return the newly created flow session
	 */
	protected FlowSessionImpl createFlowSession(Flow flow, FlowSessionImpl parent) {
		return new FlowSessionImpl(flow, parent);
	}

	// package private request control context callbacks

	void start(Flow flow, MutableAttributeMap input, RequestControlContext context) {
		listeners.fireSessionCreating(context, flow);
		FlowSession session = activateSession(flow);
		if (input == null) {
			input = new LocalAttributeMap();
		}
		listeners.fireSessionStarting(context, session, input);
		flow.start(context, input);
		listeners.fireSessionStarted(context, session);
	}

	void setCurrentState(State newState, RequestContext context) {
		listeners.fireStateEntering(context, newState);
		FlowSessionImpl session = getActiveSessionInternal();
		State previousState = (State) session.getState();
		session.setCurrentState(newState);
		listeners.fireStateEntered(context, previousState);
	}

	public void viewRendering(View view, RequestContext context) {
		listeners.fireViewRendering(context, view);
	}

	public void viewRendered(View view, RequestContext context) {
		listeners.fireViewRendered(context, view);
	}

	boolean handleEvent(Event event, RequestControlContext context) {
		listeners.fireEventSignaled(context, event);
		return getActiveSessionInternal().getFlow().handleEvent(context);
	}

	boolean execute(Transition transition, RequestControlContext context) {
		listeners.fireTransitionExecuting(context, transition);
		return transition.execute(getCurrentState(), context);
	}

	FlowSession endActiveFlowSession(MutableAttributeMap output, RequestControlContext context) {
		FlowSessionImpl session = getActiveSessionInternal();
		listeners.fireSessionEnding(context, session, output);
		session.getFlow().end(context, output);
		flowSessions.removeLast();
		listeners.fireSessionEnded(context, session, output);
		if (hasEnded()) {
			outcome = new Event(this, session.getState().getId(), output);
		}
		return session;
	}

	FlowExecutionKey assignKey() {
		key = keyFactory.getKey(this);
		if (logger.isDebugEnabled()) {
			logger.debug("Assigned key " + key);
		}
		return key;
	}

	void updateCurrentFlowExecutionSnapshot() {
		keyFactory.updateFlowExecutionSnapshot(this);
	}

	void removeCurrentFlowExecutionSnapshot() {
		keyFactory.removeFlowExecutionSnapshot(this);
	}

	void removeAllFlowExecutionSnapshots() {
		keyFactory.removeAllFlowExecutionSnapshots(this);
	}

	// package private setters for restoring transient state used by FlowExecutionImplServicesConfigurer

	FlowExecutionListener[] getListeners() {
		return listeners.getArray();
	}

	void setListeners(FlowExecutionListener[] listeners) {
		this.listeners = new FlowExecutionListeners(listeners);
	}

	void setAttributes(AttributeMap attributes) {
		this.attributes = attributes;
	}

	void setKeyFactory(FlowExecutionKeyFactory keyFactory) {
		this.keyFactory = keyFactory;
	}

	void setMessageContextFactory(MessageContextFactory messageContextFactory) {
		this.messageContextFactory = messageContextFactory;
	}

	// Used by FlowExecutionImplStateRestorer

	/**
	 * Returns the flow definition id of this flow execution.
	 */
	String getFlowId() {
		return flowId;
	}

	/**
	 * Returns the list of flow session maintained by this flow execution.
	 */
	LinkedList getFlowSessions() {
		return flowSessions;
	}

	/**
	 * Are there any flow sessions in this flow execution?
	 */
	boolean hasSessions() {
		return !flowSessions.isEmpty();
	}

	/**
	 * Are there any sessions for sub flows in this flow execution?
	 */
	boolean hasSubflowSessions() {
		return flowSessions.size() > 1;
	}

	/**
	 * Returns the flow session for the root flow of this flow execution.
	 */
	FlowSessionImpl getRootSession() {
		return (FlowSessionImpl) flowSessions.getFirst();
	}

	/**
	 * Returns an iterator looping over the subflow sessions in this flow execution.
	 */
	ListIterator getSubflowSessionIterator() {
		return flowSessions.listIterator(1);
	}

	/**
	 * Restore the flow definition of this flow execution.
	 */
	void setFlow(Flow flow) {
		this.flow = flow;
	}

	/**
	 * Restore conversation scope for this flow execution.
	 */
	void setConversationScope(MutableAttributeMap conversationScope) {
		this.conversationScope = conversationScope;
	}

	/**
	 * Restore the flow execution key.
	 */
	void setKey(FlowExecutionKey key) {
		this.key = key;
	}

	// custom serialization (implementation of Externalizable for optimized storage)

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		started = in.readBoolean();
		flowId = (String) in.readObject();
		flowSessions = (LinkedList) in.readObject();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeBoolean(started);
		out.writeObject(flow.getId());
		out.writeObject(flowSessions);
	}

	public String toString() {
		if (!isActive()) {
			if (!hasStarted()) {
				return "[Not yet started " + getCaption() + "]";
			} else {
				return "[Ended " + getCaption() + "]";
			}
		} else {
			if (flow != null) {
				return new ToStringCreator(this).append("flow", flow.getId()).append("flowSessions", flowSessions)
						.toString();
			} else {
				return "[Unhydrated execution of '" + flowId + "']";
			}
		}
	}

	// internal helpers

	/**
	 * Activate a new <code>FlowSession</code> for the flow definition. Creates the new flow session and pushes it
	 * onto the stack.
	 * @param flow the flow definition
	 * @return the new flow session
	 */
	private FlowSessionImpl activateSession(Flow flow) {
		FlowSessionImpl session;
		if (!flowSessions.isEmpty()) {
			FlowSessionImpl parent = getActiveSessionInternal();
			session = createFlowSession(flow, parent);
		} else {
			session = createFlowSession(flow, null);
		}
		flowSessions.add(session);
		return session;
	}

	private FlowSessionImpl getActiveSessionInternal() {
		return (FlowSessionImpl) flowSessions.getLast();
	}

	private FlowExecutionException wrap(Exception e) {
		if (isActive()) {
			FlowSession session = getActiveSession();
			String flowId = session.getDefinition().getId();
			String stateId = session.getState() != null ? session.getState().getId() : null;
			return new FlowExecutionException(flowId, stateId, "Exception thrown in state '" + stateId + "' of flow '"
					+ flowId + "'", e);
		} else {
			return new FlowExecutionException(flow.getId(), null, "Exception thrown within inactive flow '"
					+ flow.getId() + "'", e);
		}
	}

	/**
	 * Handles an exception that occurred performing an operation on this flow execution. First tries the set of
	 * exception handlers associated with the offending state, then the handlers at the flow level.
	 * @param exception the exception that occurred
	 * @param context the request control context the exception occurred in
	 * @throws FlowExecutionException re-throws the exception if it was not handled at the state or flow level
	 */
	private void handleException(FlowExecutionException exception, RequestControlContext context)
			throws FlowExecutionException {
		listeners.fireExceptionThrown(context, exception);
		if (logger.isDebugEnabled()) {
			if (exception.getCause() != null) {
				logger.debug("Attempting to handle [" + exception + "] with root cause [" + getRootCause(exception)
						+ "]");
			} else {
				logger.debug("Attempting to handle [" + exception + "]");
			}
		}
		boolean handled = false;
		try {
			if (tryStateHandlers(exception, context) || tryFlowHandlers(exception, context)) {
				handled = true;
			}
		} catch (FlowExecutionException newException) {
			// exception handling itself resulted in a new FlowExecutionException, try to handle it
			handleException(newException, context);
		}
		if (!handled) {
			if (logger.isDebugEnabled()) {
				logger.debug("Rethrowing unhandled flow execution exception");
			}
			throw exception;
		}
	}

	/**
	 * Get the root cause of the given throwable.
	 */
	private Throwable getRootCause(Throwable e) {
		Throwable cause = e.getCause();
		return cause == null ? e : getRootCause(cause);
	}

	/**
	 * Try to handle given exception using execution exception handlers registered at the state level. Returns null if
	 * no handler handled the exception.
	 * @return true if the exception was handled
	 */
	private boolean tryStateHandlers(FlowExecutionException exception, RequestControlContext context) {
		if (exception.getStateId() != null) {
			return getCurrentFlow().getStateInstance(exception.getStateId()).handleException(exception, context);
		} else {
			return false;
		}
	}

	/**
	 * Try to handle given exception using execution exception handlers registered at the flow level. Returns null if no
	 * handler handled the exception.
	 * @return true if the exception was handled
	 */
	private boolean tryFlowHandlers(FlowExecutionException exception, RequestControlContext context) {
		return getCurrentFlow().handleException(exception, context);
	}

	/**
	 * Returns the current flow which may or may not yet be active.
	 */
	private Flow getCurrentFlow() {
		if (isActive()) {
			return getActiveSessionInternal().getFlow();
		} else {
			return flow;
		}
	}

	private State getCurrentState() {
		FlowSessionImpl session = getActiveSessionInternal();
		State currentState = (State) session.getState();
		return currentState;
	}

}