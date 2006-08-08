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

import org.springframework.util.Assert;
import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.ConversationParameters;
import org.springframework.webflow.conversation.ConversationService;
import org.springframework.webflow.conversation.NoSuchConversationException;
import org.springframework.webflow.util.RandomGuidUidGenerator;
import org.springframework.webflow.util.UidGenerator;

/**
 * The default implementation of the {@link ConversationService}. This
 * implementation maintains an internal state of all conversations that have
 * been begun.
 * 
 * @author Ben Hale
 * @author Keith Donald
 */
public class LocalConversationService implements ConversationService, Serializable {

	/**
	 * The local conversation data store.
	 */
	private Map conversations = new HashMap();

	/**
	 * An ordered list of conversation ids. Each conversation id represents an
	 * pointer to an actiove conversation in the map.  The first element is the oldest 
	 * conversation and the last is the youngest. 
	 */
	private LinkedList conversationIds = new LinkedList();

	/**
	 * The maximum number of active conversations allowed.
	 */
	private int maxConversations;
	
	/**
	 * The uid generation strategy to use.
	 */
	private UidGenerator conversationIdGenerator = new RandomGuidUidGenerator();

	/**
	 * Creates a new local conversation service.
	 * @param maxConversations the maximum number of conversations that can be 
	 * active at once within this service.
	 */
	public LocalConversationService(int maxConversations) {
		this.maxConversations = maxConversations;
	}
	
	/**
	 * Returns the configured generator for simple conversation ids.
	 */
	protected UidGenerator getConversationIdGenerator() {
		return conversationIdGenerator;
	}

	/**
	 * Sets the configured generator simple conversation ids.
	 */
	public void setConversationIdGenerator(UidGenerator uidGenerator) {
		this.conversationIdGenerator = uidGenerator;
	}

	public Conversation beginConversation(ConversationParameters conversationParameters) {
		Assert.notNull(conversationParameters, "New conversation parameters must not be null");
		ConversationId conversationId = new SimpleConversationId(conversationIdGenerator.generateUid());
		conversations.put(conversationId, createConversation(conversationParameters, conversationId));
		conversationIds.add(conversationId);
		// end the oldest conversation if them maximium number of
		// conversations has been exceeded
		if (maxExceeded()) {
			endOldestConversation();
		}		
		return getConversation(conversationId);
	}

	public Conversation getConversation(ConversationId id) throws NoSuchConversationException {
		if (!conversations.containsKey(id)) {
			throw new NoSuchConversationException(id);
		}
		return new ConversationProxy(id);
	}

	public ConversationId parseConversationId(String conversationId) {
		return new SimpleConversationId(conversationIdGenerator.parseUid(conversationId));
	}

	private ConversationEntry createConversation(ConversationParameters newConversation, ConversationId conversationId) {
		return new ConversationEntry(conversationId, newConversation.getName(), newConversation.getCaption(),
				newConversation.getDescription());
	}

	private boolean maxExceeded() {
		return maxConversations > 0 && conversationIds.size() > maxConversations;
	}

	private void endOldestConversation() {
		Conversation oldest = getConversation((ConversationId)conversationIds.getFirst());
		oldest.lock();
		oldest.end();
	}
	
	private ConversationLock getLock(ConversationId conversationId) {
		Assert.notNull(conversationId, "conversationId must not be null");
		if (!conversations.containsKey(conversationId)) {
			throw new NoSuchConversationException(conversationId);
		}
		return getConversationEntry(conversationId).getLock();
	}

	private Object getAttribute(ConversationId conversationId, Object name) {
		Assert.notNull(conversationId, "conversationId must not be null");
		Assert.notNull(name, "name must not be null");
		if (!conversations.containsKey(conversationId)) {
			throw new NoSuchConversationException(conversationId);
		}
		return getConversationEntry(conversationId).getAttributes().get(name);
	}

	private Object putAttribute(ConversationId conversationId, Object name, Object value) {
		Assert.notNull(conversationId, "conversationId must not be null");
		Assert.notNull(name, "name must not be null");
		if (!conversations.containsKey(conversationId)) {
			throw new NoSuchConversationException(conversationId);
		}
		return getConversationEntry(conversationId).getAttributes().put(name, value);
	}

	private Object removeAttribute(ConversationId conversationId, Object name) {
		Assert.notNull(conversationId, "conversationId must not be null");
		Assert.notNull(name, "name must not be null");
		if (!conversations.containsKey(conversationId)) {
			throw new NoSuchConversationException(conversationId);
		}
		return getConversationEntry(conversationId).getAttributes().remove(name);
	}

	private void end(ConversationId conversationId) {
		Assert.notNull(conversationId, "conversationId must not be null");
		if (!conversations.containsKey(conversationId)) {
			throw new NoSuchConversationException(conversationId);
		}
		getConversationEntry(conversationId).getLock().unlock();
		conversations.remove(conversationId);
		conversationIds.remove(conversationId);
	}

	private ConversationEntry getConversationEntry(ConversationId conversationId) {
		return ((ConversationEntry)conversations.get(conversationId));
	}

	/**
	 * A proxy to a keyed entry in the conversation map.
	 * 
	 * @author Keith Donald
	 */
	private class ConversationProxy implements Conversation {

		private ConversationId conversationId;

		public ConversationProxy(ConversationId id) {
			this.conversationId = id;
		}

		public ConversationId getId() {
			return conversationId;
		}

		public void lock() {
			LocalConversationService.this.getLock(conversationId).lock();
		}

		public void end() {
			LocalConversationService.this.end(conversationId);
		}

		public Object getAttribute(Object name) {
			return LocalConversationService.this.getAttribute(conversationId, name);
		}

		public void putAttribute(Object name, Object value) {
			LocalConversationService.this.putAttribute(conversationId, name, value);
		}

		public void removeAttribute(Object name) {
			LocalConversationService.this.removeAttribute(conversationId, name);
		}

		public void unlock() {
			try {
				LocalConversationService.this.getLock(conversationId).unlock();
			} catch (NoSuchConversationException e) {
				// ignore
			}
		}
	}
}