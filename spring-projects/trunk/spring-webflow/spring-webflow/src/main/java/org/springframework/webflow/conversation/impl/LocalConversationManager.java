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

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.springframework.webflow.context.SharedAttributeMap;
import org.springframework.webflow.context.support.ExternalContextHolder;
import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationAccessException;
import org.springframework.webflow.conversation.ConversationException;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.ConversationManager;
import org.springframework.webflow.conversation.ConversationParameters;
import org.springframework.webflow.conversation.NoSuchConversationException;

/**
 * The default implementation of the {@link ConversationManager}. This
 * implementation maintains an internal state of all conversations that have
 * been begun.
 * 
 * @author Ben Hale
 * @author Keith Donald
 */
public class LocalConversationManager extends AbstractConversationManager implements Serializable {

	private static final String USER_CONVERSATION_CONTEXT = "userConversationContext";

	/**
	 * The local conversation data store.
	 */
	private Map conversations = new HashMap();

	/**
	 * An ordered list of conversation ids. Each conversation id represents an
	 * pointer to an actiove conversation in the map. The first element is the
	 * oldest conversation and the last is the youngest.
	 */
	private LinkedList conversationIds = new LinkedList();

	/**
	 * The maximum number of active conversations allowed in a session.
	 */
	private int maxConversations;

	/**
	 * Creates a new local conversation service.
	 * @param maxConversations the maximum number of conversations that can be
	 * active at once within this session.
	 */
	public LocalConversationManager(int maxConversations) {
		this.maxConversations = maxConversations;
	}

	protected Map getConversations() {
		return conversations;
	}

	protected LinkedList getConversationIds() {
		return conversationIds;
	}

	public Conversation beginConversation(ConversationParameters conversationParameters) {
		ConversationId conversationId = new SimpleConversationId(getConversationIdGenerator().generateUid());
		getConversations().put(conversationId, createConversation(conversationParameters, conversationId));
		getConversationIds().add(conversationId);
		getUserContext().add(conversationId);
		// end the oldest conversation if them maximium number of
		// conversations has been exceeded
		if (maxExceeded()) {
			endOldestConversation();
		}
		return getConversation(conversationId);
	}

	public Conversation getConversation(ConversationId id) throws NoSuchConversationException {
		if (!getConversations().containsKey(id)) {
			throw new NoSuchConversationException(id);
		}
		if (!getUserContext().contains(id)) {
			throw new ConversationAccessException(id);
		}
		return new LocalConversationProxy(id);
	}

	void expireConversation(ConversationId id) throws ConversationException {
		if (!getConversations().containsKey(id)) {
			throw new NoSuchConversationException(id);
		}
		end(id);
	}

	private UserConversationContext getUserContext() {
		SharedAttributeMap session = ExternalContextHolder.getExternalContext().getSessionMap();
		synchronized (session.getMutex()) {
			UserConversationContext context = (UserConversationContext)session.get(USER_CONVERSATION_CONTEXT);
			if (context == null) {
				context = createUserContext();
			}
			return context;
		}
	}

	private UserConversationContext createUserContext() {
		UserConversationContext context = new UserConversationContext(this);
		ExternalContextHolder.getExternalContext().getSessionMap().put(USER_CONVERSATION_CONTEXT, context);
		return context;
	}

	private boolean maxExceeded() {
		return maxConversations > 0 && getUserContext().size() > maxConversations;
	}

	private void endOldestConversation() {
		ConversationId conversationId = getUserContext().getFirst();
		Conversation oldest = getConversation(conversationId);
		oldest.lock();
		oldest.end();
	}

	private class LocalConversationProxy extends ConversationProxy {

		public LocalConversationProxy(ConversationId id) {
			super(id);
		}

		public void end() {
			super.end();
			getUserContext().remove(getId());
		}

	}
}