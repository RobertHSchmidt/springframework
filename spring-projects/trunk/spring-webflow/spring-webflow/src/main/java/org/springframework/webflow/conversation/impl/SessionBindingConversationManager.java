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
package org.springframework.webflow.conversation.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.springframework.webflow.context.SharedAttributeMap;
import org.springframework.webflow.context.support.ExternalContextHolder;
import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationException;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.ConversationParameters;
import org.springframework.webflow.conversation.NoSuchConversationException;

public class SessionBindingConversationManager extends AbstractConversationManager {

	private static final String CONVERSATIONS = "conversations";

	private static final String CONVERSATION_IDS = "conversationIds";

	/**
	 * The maximum number of active conversations allowed in a session.
	 */
	private int maxConversations;

	/**
	 * Creates a new session binding conversation service.
	 * @param maxConversations the maximum number of conversations that can be
	 * active at once within this session.
	 */
	public SessionBindingConversationManager(int maxConversations) {
		this.maxConversations = maxConversations;
	}

	public Conversation beginConversation(ConversationParameters conversationParameters) throws ConversationException {
		ConversationId conversationId = new SimpleConversationId(getConversationIdGenerator().generateUid());
		getConversations().put(conversationId, createConversation(conversationParameters, conversationId));
		getConversationIds().add(conversationId);
		// end the oldest conversation if them maximium number of
		// conversations has been exceeded
		if (maxExceeded()) {
			endOldestConversation();
		}
		return getConversation(conversationId);
	}

	public Conversation getConversation(ConversationId id) throws ConversationException {
		if (!getConversations().containsKey(id)) {
			throw new NoSuchConversationException(id);
		}
		return new ConversationProxy(id);
	}

	protected LinkedList getConversationIds() {
		SharedAttributeMap session = ExternalContextHolder.getExternalContext().getSessionMap();
		synchronized (session.getMutex()) {
			LinkedList conversationIds = (LinkedList)session.get(CONVERSATION_IDS);
			if (conversationIds == null) {
				conversationIds = createConversationIds();
			}
			return conversationIds;
		}
	}

	private LinkedList createConversationIds() {
		LinkedList conversationIds = new LinkedList();
		ExternalContextHolder.getExternalContext().getSessionMap().put(CONVERSATION_IDS, conversationIds);
		return conversationIds;
	}

	protected Map getConversations() {
		SharedAttributeMap session = ExternalContextHolder.getExternalContext().getSessionMap();
		synchronized (session.getMutex()) {
			Map conversations = (Map)session.get(CONVERSATIONS);
			if (conversations == null) {
				conversations = createConversations();
			}
			return conversations;
		}
	}

	private Map createConversations() {
		Map conversations = new HashMap();
		ExternalContextHolder.getExternalContext().getSessionMap().put(CONVERSATIONS, conversations);
		return conversations;
	}

	private void endOldestConversation() {
		ConversationId conversationId = (ConversationId)getConversationIds().getFirst();
		Conversation oldest = getConversation(conversationId);
		oldest.lock();
		oldest.end();
	}

	private boolean maxExceeded() {
		return maxConversations > 0 && getConversations().size() > maxConversations;
	}

}
