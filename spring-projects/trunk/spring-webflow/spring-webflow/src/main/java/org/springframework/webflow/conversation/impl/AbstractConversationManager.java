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

import java.util.LinkedList;
import java.util.Map;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.ConversationManager;
import org.springframework.webflow.conversation.ConversationParameters;
import org.springframework.webflow.conversation.NoSuchConversationException;
import org.springframework.webflow.util.RandomGuidUidGenerator;
import org.springframework.webflow.util.UidGenerator;

public abstract class AbstractConversationManager implements ConversationManager {

	/**
	 * The uid generation strategy to use.
	 */
	private UidGenerator conversationIdGenerator = new RandomGuidUidGenerator();

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

	/**
	 * Returns the Map of conversations used by this conversation manager.
	 */
	protected abstract Map getConversations();

	/**
	 * Returns the list of conversationIds managed by this conversation manager.
	 */
	protected abstract LinkedList getConversationIds();

	public ConversationId parseConversationId(String conversationId) {
		return new SimpleConversationId(getConversationIdGenerator().parseUid(conversationId));
	}

	private ConversationLock getLock(ConversationId conversationId) {
		if (!getConversations().containsKey(conversationId)) {
			throw new NoSuchConversationException(conversationId);
		}
		return getConversationEntry(conversationId).getLock();
	}

	private Object getAttribute(ConversationId conversationId, Object name) {
		if (!getConversations().containsKey(conversationId)) {
			throw new NoSuchConversationException(conversationId);
		}
		return getConversationEntry(conversationId).getAttributes().get(name);
	}

	private Object putAttribute(ConversationId conversationId, Object name, Object value) {
		if (!getConversations().containsKey(conversationId)) {
			throw new NoSuchConversationException(conversationId);
		}
		return getConversationEntry(conversationId).getAttributes().put(name, value);
	}

	private Object removeAttribute(ConversationId conversationId, Object name) {
		if (!getConversations().containsKey(conversationId)) {
			throw new NoSuchConversationException(conversationId);
		}
		return getConversationEntry(conversationId).getAttributes().remove(name);
	}

	protected void end(ConversationId conversationId) {
		if (!getConversations().containsKey(conversationId)) {
			throw new NoSuchConversationException(conversationId);
		}
		ConversationLock lock = getConversationEntry(conversationId).getLock();
		try {
			lock.unlock();
		}
		catch (Exception e) {

		}
		getConversations().remove(conversationId);
		getConversationIds().remove(conversationId);
	}

	protected ConversationEntry createConversation(ConversationParameters newConversation, ConversationId conversationId) {
		return new ConversationEntry(conversationId, newConversation.getName(), newConversation.getCaption(),
				newConversation.getDescription());
	}

	private ConversationEntry getConversationEntry(ConversationId conversationId) {
		return ((ConversationEntry)getConversations().get(conversationId));
	}

	/**
	 * A proxy to a keyed entry in the conversation map.
	 * 
	 * @author Keith Donald
	 */
	protected class ConversationProxy implements Conversation {

		private ConversationId conversationId;

		public ConversationProxy(ConversationId id) {
			this.conversationId = id;
		}

		public ConversationId getId() {
			return conversationId;
		}

		public void lock() {
			AbstractConversationManager.this.getLock(conversationId).lock();
		}

		public void end() {
			AbstractConversationManager.this.end(conversationId);
		}

		public Object getAttribute(Object name) {
			return AbstractConversationManager.this.getAttribute(conversationId, name);
		}

		public void putAttribute(Object name, Object value) {
			AbstractConversationManager.this.putAttribute(conversationId, name, value);
		}

		public void removeAttribute(Object name) {
			AbstractConversationManager.this.removeAttribute(conversationId, name);
		}

		public void unlock() {
			try {
				AbstractConversationManager.this.getLock(conversationId).unlock();
			}
			catch (NoSuchConversationException e) {
				// ignore
			}
		}

		public String toString() {
			return new ToStringCreator(this).append("id", conversationId).toString();
		}

		public boolean equals(Object o) {
			if (!(o instanceof ConversationProxy)) {
				return false;
			}
			return conversationId.equals(((ConversationProxy)o).conversationId);
		}

		public int hashCode() {
			return conversationId.hashCode();
		}

	}
}
