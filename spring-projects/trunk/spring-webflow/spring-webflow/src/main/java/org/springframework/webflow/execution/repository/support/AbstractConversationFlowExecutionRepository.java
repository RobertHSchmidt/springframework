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
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.ConversationParameters;
import org.springframework.webflow.conversation.ConversationManager;
import org.springframework.webflow.conversation.ConversationException;
import org.springframework.webflow.conversation.NoSuchConversationException;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.factory.FlowExecutionFactory;
import org.springframework.webflow.execution.repository.BadlyFormattedFlowExecutionKeyException;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.FlowExecutionLock;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryException;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;

/**
 * A convenient base for flow execution repository implementations that delegate
 * to a conversation service for managing conversations that govern the
 * persistent state of paused flow executions.
 * 
 * @author Keith Donald
 */
public abstract class AbstractConversationFlowExecutionRepository extends AbstractFlowExecutionRepository implements
		Serializable {

	/**
	 * The conversation "scope" attribute.
	 */
	private static final String SCOPE_ATTRIBUTE = "scope";

	/**
	 * The conversation service to delegate to for managing conversations
	 * initiated by this repository.
	 */
	private ConversationManager conversationService;

	/**
	 * No-arg constructor to satisfy use with subclass implementations are that
	 * serializable.
	 */
	protected AbstractConversationFlowExecutionRepository() {

	}

	/**
	 * Creates a new flow execution repository
	 * @param repositoryServices the common services needed by this repository
	 * to function.
	 */
	public AbstractConversationFlowExecutionRepository(FlowExecutionFactory executionFactory,
			ConversationManager conversationService) {
		setFlowExecutionFactory(executionFactory);
		setConversationService(conversationService);
	}

	/**
	 * Returns the configured conversation service.
	 */
	protected ConversationManager getConversationService() {
		return conversationService;
	}

	/**
	 * Sets the conversationService reference.
	 * @param conversationService the conversation service, may not be null.
	 */
	public void setConversationService(ConversationManager conversationService) {
		Assert.notNull(conversationService, "The conversation service is required");
		this.conversationService = conversationService;
	}

	public FlowExecutionKey generateKey(FlowExecution flowExecution) {
		Conversation conversation = conversationService.beginConversation(createNewConversation(flowExecution));
		onBegin(conversation);
		return new CompositeFlowExecutionKey(conversation.getId(), generateContinuationId(flowExecution));
	}

	public FlowExecutionLock getLock(FlowExecutionKey key) throws FlowExecutionRepositoryException {
		return new ConversationBackedFlowExecutionLock(getConversation(key));
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
		try {
			ConversationId conversationId = conversationService.parseConversationId(keyParts[0]);
			return new CompositeFlowExecutionKey(conversationId, parseContinuationId(keyParts[1]));
		}
		catch (ConversationException e) {
			throw new BadlyFormattedFlowExecutionKeyException(encodedKey, CompositeFlowExecutionKey.getFormat(), e);
		}
		catch (FlowExecutionRepositoryException e) {
			throw new BadlyFormattedFlowExecutionKeyException(encodedKey, CompositeFlowExecutionKey.getFormat(), e);
		}
	}

	protected ConversationParameters createNewConversation(FlowExecution flowExecution) {
		FlowDefinition flow = flowExecution.getFlowDefinition();
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
			return getConversationService().getConversation(getConversationId(key));
		}
		catch (NoSuchConversationException e) {
			throw new NoSuchFlowExecutionException(key, e);
		}
	}

	protected MutableAttributeMap getConversationScope(FlowExecutionKey key) {
		return (MutableAttributeMap)getConversation(key).getAttribute(SCOPE_ATTRIBUTE);
	}

	protected void putConversationScope(FlowExecutionKey key, MutableAttributeMap scope) {
		getConversation(key).putAttribute(SCOPE_ATTRIBUTE, scope);
	}

	protected FlowExecution restoreState(FlowExecution flowExecution, FlowExecutionKey key) {
		return getFlowExecutionFactory().restoreState(flowExecution, getConversationScope(key));
	}

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