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
package org.springframework.webflow.execution.repository.support;

import java.io.Serializable;

import org.springframework.util.Assert;
import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationException;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.ConversationManager;
import org.springframework.webflow.conversation.ConversationParameters;
import org.springframework.webflow.conversation.NoSuchConversationException;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.BadlyFormattedFlowExecutionKeyException;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.FlowExecutionLock;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryException;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;

/**
 * A convenient base class for flow execution repository implementations that delegate
 * to a conversation service for managing conversations that govern the
 * persistent state of paused flow executions.
 * 
 * @see ConversationManager
 * 
 * @author Keith Donald
 */
public abstract class AbstractConversationFlowExecutionRepository implements FlowExecutionRepository {

	/**
	 * The conversation "scope" attribute.
	 */
	private static final String SCOPE_ATTRIBUTE = "scope";

	/**
	 * Flag to indicate whether or not a new flow execution key should always be
	 * generated before each put call. Default is true.
	 */
	private boolean alwaysGenerateNewNextKey = true;

	/**
	 * The conversation service to delegate to for managing conversations
	 * initiated by this repository.
	 */
	private ConversationManager conversationManager;

	/**
	 * Creates a new flow execution repository.
	 */
	protected AbstractConversationFlowExecutionRepository(ConversationManager conversationManager) {
		setConversationManager(conversationManager);
	}

	/**
	 * Returns the configured generate new next key flag.
	 */
	protected boolean isAlwaysGenerateNewNextKey() {
		return alwaysGenerateNewNextKey;
	}

	/**
	 * Sets a flag indicating if a new {@link FlowExecutionKey} should always be
	 * generated before each put call. By setting this to false a FlowExecution
	 * can remain identified by the same key throughout its life.
	 * @param alwaysGenerateNewNextKey the generate flag
	 */
	public void setAlwaysGenerateNewNextKey(boolean alwaysGenerateNewNextKey) {
		this.alwaysGenerateNewNextKey = alwaysGenerateNewNextKey;
	}

	/**
	 * Returns the configured conversation service.
	 */
	protected ConversationManager getConversationManager() {
		return conversationManager;
	}

	/**
	 * Sets the conversationService reference.
	 * @param conversationManager the conversation service, may not be null.
	 */
	private void setConversationManager(ConversationManager conversationManager) {
		Assert.notNull(conversationManager, "The conversation manager is required");
		this.conversationManager = conversationManager;
	}

	public FlowExecutionKey generateKey(FlowExecution flowExecution) {
		ConversationParameters parameters = createConversationParameters(flowExecution);
		Conversation conversation = conversationManager.beginConversation(parameters);
		onBegin(conversation);
		return new CompositeFlowExecutionKey(conversation.getId(), generateContinuationId(flowExecution));
	}

	public FlowExecutionKey getNextKey(FlowExecution flowExecution, FlowExecutionKey previousKey) {
		if (isAlwaysGenerateNewNextKey()) {
			CompositeFlowExecutionKey key = (CompositeFlowExecutionKey)previousKey;
			return new CompositeFlowExecutionKey(key.getConversationId(), generateContinuationId(flowExecution));
		}
		else {
			return previousKey;
		}
	}

	public FlowExecutionLock getLock(FlowExecutionKey key) throws FlowExecutionRepositoryException {
		return new ConversationBackedFlowExecutionLock(getConversation(key));
	}

	public abstract FlowExecution getFlowExecution(FlowExecutionKey key) throws FlowExecutionRepositoryException;

	public abstract void putFlowExecution(FlowExecutionKey key, FlowExecution flowExecution)
			throws FlowExecutionRepositoryException;

	public void removeFlowExecution(FlowExecutionKey key) throws FlowExecutionRepositoryException {
		try {
			getConversation(key).end();
		}
		catch (NoSuchConversationException e) {
			throw new NoSuchFlowExecutionException(key, e);
		}
	}

	public FlowExecutionKey parseFlowExecutionKey(String encodedKey) throws FlowExecutionRepositoryException {
		Assert.hasText(encodedKey, "The string encoded flow execution key is required");
		String[] keyParts = CompositeFlowExecutionKey.keyParts(encodedKey);
		ConversationId conversationId;
		try {
			conversationId = conversationManager.parseConversationId(keyParts[0]);
		}
		catch (ConversationException e) {
			throw new BadlyFormattedFlowExecutionKeyException(encodedKey,
					"Conversation id '" + keyParts[0] + "' contained in encoded flow execution key '"
					+ encodedKey + "' is invalid", e);
		}
		Serializable continuationId;
		try {
			continuationId = parseContinuationId(keyParts[1]);
		}
		catch (FlowExecutionRepositoryException e) {
			throw new BadlyFormattedFlowExecutionKeyException(encodedKey,
					"Continuation id '" + keyParts[1] + "' contained in encoded flow execution key '"
					+ encodedKey + "' is invalid", e);
		}
		return new CompositeFlowExecutionKey(conversationId, continuationId);
	}

	// overridable hooks

	/**
	 * Factory method that maps a new flow execution to a input
	 * {@link ConversationParameters conversation parameters} object.
	 * @param flowExecution the new flow execution
	 * @return the conversation parameters object to pass to the conversation
	 * manager when the conversation is started.
	 */
	protected ConversationParameters createConversationParameters(FlowExecution flowExecution) {
		FlowDefinition flow = flowExecution.getDefinition();
		return new ConversationParameters(flow.getId(), flow.getCaption(), flow.getDescription());
	}

	/**
	 * A "on begin conversation" callback, allowing for insertion of custom
	 * logic after a new conversation has begun.
	 * @param conversation the conversation that has begun
	 */
	protected void onBegin(Conversation conversation) {
	}

	/**
	 * Returns the conversation id part of the composite flow execution key.
	 * @param key the composite key
	 * @return the conversationId key part
	 */
	protected ConversationId getConversationId(FlowExecutionKey key) {
		return ((CompositeFlowExecutionKey)key).getConversationId();
	}

	/**
	 * Returns the continuation id part of the composite flow execution key.
	 * @param key the composite key
	 * @return the continuation id key part
	 */
	protected Serializable getContinuationId(FlowExecutionKey key) {
		return ((CompositeFlowExecutionKey)key).getContinuationId();
	}

	/**
	 * Returns the conversation governing the execution of the
	 * {@link FlowExecution} with the provided key.
	 * @param key the flow execution key
	 * @return the governing conversation
	 */
	protected Conversation getConversation(FlowExecutionKey key) {
		try {
			return getConversationManager().getConversation(getConversationId(key));
		}
		catch (NoSuchConversationException e) {
			throw new NoSuchFlowExecutionException(key, e);
		}
	}

	/**
	 * Returns the conversation scope attribute for the flow execution with the
	 * key provided.
	 * @param key the flow execution key
	 * @return the execution's conversation scope
	 */
	protected MutableAttributeMap getConversationScope(FlowExecutionKey key) {
		return (MutableAttributeMap)getConversation(key).getAttribute(SCOPE_ATTRIBUTE);
	}

	/**
	 * Sets the conversation scope attribute for the flow execution with the key
	 * provided.
	 * @param key the flow execution key
	 * @param scope the execution's conversation scope
	 */
	protected void putConversationScope(FlowExecutionKey key, MutableAttributeMap scope) {
		getConversation(key).putAttribute(SCOPE_ATTRIBUTE, scope);
	}

	// abstract template methods

	/**
	 * Template method used to generate a new continuation id for this flow
	 * execution. Subclasses must override.
	 * @param flowExecution the flow execution
	 * @return the continuation id
	 */
	protected abstract Serializable generateContinuationId(FlowExecution flowExecution);

	/**
	 * Template method to parse the continuation id from the encoded string.
	 * @param encodedId the string identifier
	 * @return the parsed continuation id
	 */
	protected abstract Serializable parseContinuationId(String encodedId) throws FlowExecutionRepositoryException;

}