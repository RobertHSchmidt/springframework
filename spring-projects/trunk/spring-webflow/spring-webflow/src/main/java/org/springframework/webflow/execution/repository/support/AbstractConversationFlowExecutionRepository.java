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
import org.springframework.webflow.Flow;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.FlowExecutionLock;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryException;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.execution.repository.conversation.Conversation;
import org.springframework.webflow.execution.repository.conversation.ConversationId;
import org.springframework.webflow.execution.repository.conversation.ConversationParameters;
import org.springframework.webflow.execution.repository.conversation.ConversationService;
import org.springframework.webflow.execution.repository.conversation.NoSuchConversationException;

/**
 * A convenient base for flow execution repository implementations.
 * <p>
 * Exposes a configuration interface for setting the set of services common to
 * most repository implementations. Also provides some basic implementation
 * assistance.
 * 
 * @author Keith Donald
 */
public abstract class AbstractConversationFlowExecutionRepository extends AbstractFlowExecutionRepository implements
		Serializable {

	/**
	 * The conversation service to delegate to for managing conversations
	 * initiated by this repository.
	 */
	private ConversationService conversationService;

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
	public AbstractConversationFlowExecutionRepository(FlowExecutionRepositoryServices repositoryServices,
			ConversationService conversationService) {
		setRepositoryServices(repositoryServices);
		setConversationService(conversationService);
	}

	protected ConversationService getConversationService() {
		return conversationService;
	}

	protected void setConversationService(ConversationService conversationService) {
		Assert.notNull(conversationService, "The conversation service is required");
		this.conversationService = conversationService;
	}

	public FlowExecutionKey generateKey(FlowExecution flowExecution) {
		Conversation conversation = conversationService.begin(createNewConversation(flowExecution));
		onBegin(conversation);
		return new CompositeFlowExecutionKey(conversation.getId(), newContinuationId(flowExecution));
	}

	public FlowExecutionLock getLock(FlowExecutionKey key) throws FlowExecutionRepositoryException {
		return new ConversationBackedFlowExecutionLock(getConversation(key));
	}

	public FlowExecutionKey getNextKey(FlowExecution flowExecution, FlowExecutionKey previousKey) {
		if (isAlwaysGenerateNewNextKey()) {
			CompositeFlowExecutionKey key = (CompositeFlowExecutionKey)previousKey;
			return new CompositeFlowExecutionKey(key.getConversationId(), newContinuationId(flowExecution));
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

	public FlowExecutionKey parseFlowExecutionKey(String encodedKey) {
		Assert.hasText(encodedKey, "The string encoded flow execution key is required");
		String[] keyParts = CompositeFlowExecutionKey.keyParts(encodedKey);
		ConversationId conversationId = conversationService.parseConversationId(keyParts[0]);
		return new CompositeFlowExecutionKey(conversationId, parseContinuationId(keyParts[1]));
	}

	protected ConversationParameters createNewConversation(FlowExecution flowExecution) {
		Flow flow = flowExecution.getFlow();
		return new ConversationParameters(flow.getId(), flow.getCaption(), flow.getDescription());
	}

	protected void onBegin(Conversation conversation) {
	}

	protected ConversationId getConversationId(FlowExecutionKey key) {
		return ((CompositeFlowExecutionKey)key).getConversationId();
	}

	protected Serializable getContinuationId(FlowExecutionKey key) {
		return ((CompositeFlowExecutionKey)key).getContinuationId();
	}

	protected Conversation getConversation(FlowExecutionKey key) {
		try {
			return getConversationService().getConversation(getConversationId(key));
		}
		catch (NoSuchConversationException e) {
			throw new NoSuchFlowExecutionException(key, e);
		}
	}

	protected abstract Serializable newContinuationId(FlowExecution flowExecution);

	protected abstract Serializable parseContinuationId(String encodedId);
}