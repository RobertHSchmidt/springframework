/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.execution.repository.continuation;

import java.io.Serializable;

import org.apache.commons.codec.binary.Base64;
import org.springframework.util.Assert;
import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationException;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.ConversationManager;
import org.springframework.webflow.conversation.ConversationParameters;
import org.springframework.webflow.conversation.NoSuchConversationException;
import org.springframework.webflow.conversation.impl.SimpleConversationId;
import org.springframework.webflow.core.collection.CollectionUtils;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.FlowExecutionRestorationFailureException;
import org.springframework.webflow.execution.repository.support.AbstractConversationFlowExecutionRepository;
import org.springframework.webflow.execution.repository.support.FlowExecutionStateRestorer;

/**
 * <p>
 * Stores flow execution state client side, requiring no use of server-side
 * state.
 * </p>
 * <p>
 * More specifically, instead of putting {@link FlowExecution} objects in a
 * server-side store this repository <i>encodes</i> them directly into the
 * <code>continuationId</code> of the generated {@link FlowExecutionKey}.
 * When asked to load a flow execution by its key this repository decodes the
 * serialized <code>continuationId</code>, restoring the
 * {@link FlowExecution} object at the state it was when encoded.
 * </p>
 * <p>
 * Note: currently this repository implementation does not by default support
 * <i>conversation invalidation after completion</i>, which enables automatic
 * prevention of duplicate submission after a conversation is completed. Support
 * for this requires tracking active conversations using a conversation service
 * backed by some centralized storage medium like a database table.
 * </p>
 * <p>
 * Warning: storing state (a flow execution continuation) on the client entails
 * a certain security risk. This implementation does not provide a secure way of
 * storing state on the client, so a malicious client could reverse engineer a
 * continuation and get access to possible sensitive data stored in the flow
 * execution. If you need more security and still want to store continuations on
 * the client, subclass this class and override the methods
 * {@link #encode(FlowExecution)} and {@link #decode(String)}, implementing
 * them with a secure encoding/decoding algorithm, e.g. based on public/private
 * key encryption.
 * </p>
 * <p>
 * This class depends on the <code>Jakarta Commons Codec</code> library to do
 * <code>BASE64</code> encoding. Codec code must be available in the classpath
 * when using this implementation.
 * </p>
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ClientContinuationFlowExecutionRepository extends AbstractConversationFlowExecutionRepository {

	/**
	 * The continuation factory that will be used to create new continuations to
	 * be added to active conversations.
	 */
	private FlowExecutionContinuationFactory continuationFactory = new SerializedFlowExecutionContinuationFactory();

	/**
	 * The strategy for restoring transient flow execution state after
	 * unmarshaling.
	 */
	private FlowExecutionStateRestorer executionStateRestorer;

	/**
	 * Creates a new client continuation repository.  Uses a 'no op' conversation manager by default.
	 * @param executionStateRestorer the transient flow execution state restorer.
	 */
	public ClientContinuationFlowExecutionRepository(FlowExecutionStateRestorer executionStateRestorer) {
		this(executionStateRestorer, new NoOpConversationManager());
	}
	
	/**
	 * Creates a new client continuation repository.
	 * @param executionStateRestorer the transient flow execution state restorer.
	 * @param conversationManager the conversation manager for managing centralized conversational state
	 */
	public ClientContinuationFlowExecutionRepository(FlowExecutionStateRestorer executionStateRestorer,
			ConversationManager conversationManager) {
		super(conversationManager);
		this.executionStateRestorer = executionStateRestorer;
	}

	/**
	 * Returns the continuation factory in use by this repository.
	 */
	protected FlowExecutionContinuationFactory getContinuationFactory() {
		return continuationFactory;
	}

	/**
	 * Sets the continuation factory used by this repository.
	 */
	public void setContinuationFactory(FlowExecutionContinuationFactory continuationFactory) {
		Assert.notNull(continuationFactory, "The continuation factory is required");
		this.continuationFactory = continuationFactory;
	}

	public FlowExecution getFlowExecution(FlowExecutionKey key) {
		FlowExecutionContinuation continuation = decode((String)getContinuationId(key));
		try {
			FlowExecution execution = continuation.unmarshal();
			return executionStateRestorer.restoreState(execution, getConversationScope(key));
		}
		catch (ContinuationUnmarshalException e) {
			throw new FlowExecutionRestorationFailureException(key, e);
		}
	}

	public void putFlowExecution(FlowExecutionKey key, FlowExecution flowExecution) {
		putConversationScope(key, flowExecution.getConversationScope());
	}

	protected final Serializable generateContinuationId(FlowExecution flowExecution) {
		return encode(flowExecution);
	}

	protected final Serializable parseContinuationId(String encodedId) {
		// just return here, continuation decoding happens in get
		return encodedId;
	}

	/**
	 * Encode given flow execution object into data that can be stored on the
	 * client.
	 * <p>
	 * Subclasses can override this to change the encoding algorithm. This class
	 * just does a BASE64 encoding of the serialized flow execution.
	 * @param flowExecution the flow execution instance
	 * @return the encoded representation
	 */
	protected Serializable encode(FlowExecution flowExecution) {
		FlowExecutionContinuation continuation = continuationFactory.createContinuation(flowExecution);
		return new String(Base64.encodeBase64(continuation.toByteArray()));
	}

	/**
	 * Decode given data, received from the client, and return the corresponding
	 * flow execution object.
	 * <p>
	 * Subclasses can override this to change the decoding algorithm. This class
	 * just does a <code>BASE64</code> decoding and then deserializes the flow
	 * execution.
	 * @param encodedContinuation the encode flow execution data
	 * @return the decoded flow execution instance
	 */
	protected FlowExecutionContinuation decode(String encodedContinuation) {
		byte[] bytes = Base64.decodeBase64(encodedContinuation.getBytes());
		return continuationFactory.createContinuation(bytes);
	}

	/**
	 * Conversation service that doesn't do anything - the default.
	 * @author Keith Donald
	 */
	private static class NoOpConversationManager implements ConversationManager {

		private static final ConversationId conversationId = new SimpleConversationId("1");

		private static final NoOpConversation INSTANCE = new NoOpConversation();

		public Conversation beginConversation(ConversationParameters conversationParameters)
				throws ConversationException {
			return INSTANCE;
		}

		public Conversation getConversation(ConversationId id) throws NoSuchConversationException {
			return INSTANCE;
		}

		public ConversationId parseConversationId(String encodedId) throws ConversationException {
			return conversationId;
		}

		private static class NoOpConversation implements Conversation {

			public void end() {
			}

			public Object getAttribute(Object name) {
				return CollectionUtils.EMPTY_ATTRIBUTE_MAP;
			}

			public ConversationId getId() {
				return conversationId;
			}

			public void lock() {
			}

			public void putAttribute(Object name, Object value) {
			}

			public void removeAttribute(Object name) {
			}

			public void unlock() {
			}
		}
	}
}