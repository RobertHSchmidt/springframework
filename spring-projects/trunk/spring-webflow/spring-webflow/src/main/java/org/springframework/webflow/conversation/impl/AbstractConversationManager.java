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

import java.util.Map;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationException;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.ConversationManager;
import org.springframework.webflow.conversation.ConversationParameters;
import org.springframework.webflow.conversation.NoSuchConversationException;
import org.springframework.webflow.util.RandomGuidUidGenerator;
import org.springframework.webflow.util.UidGenerator;

/**
 * Convenient base class for conversation manager implementations. Subclasses
 * must override {@link #getConversationMap()} to return the data structure to
 * store conversation entries.
 * 
 * @author Keith Donald
 * @author Ben Hale
 */
public abstract class AbstractConversationManager implements ConversationManager {

	/**
	 * The conversation uid generation strategy to use.
	 */
	private UidGenerator conversationIdGenerator = new RandomGuidUidGenerator();

	/**
	 * Returns the configured generator for conversation ids.
	 */
	protected UidGenerator getConversationIdGenerator() {
		return conversationIdGenerator;
	}

	/**
	 * Sets the configured generator for conversation ids.
	 */
	public void setConversationIdGenerator(UidGenerator uidGenerator) {
		this.conversationIdGenerator = uidGenerator;
	}

	public Conversation beginConversation(ConversationParameters conversationParameters) throws ConversationException {
		ConversationId conversationId = new SimpleConversationId(getConversationIdGenerator().generateUid());
		getConversationMap().put(conversationId, createConversationEntry(conversationParameters, conversationId));
		onBegin(conversationId);
		return getConversation(conversationId);
	}

	public Conversation getConversation(ConversationId id) throws NoSuchConversationException {
		assertValid(id);
		// return a conversation proxy that delegates all calls on the Conversation
		// back to this manager so we can take appropriate actions
		return new ConversationProxy(id);
	}

	public ConversationId parseConversationId(String conversationId) {
		return new SimpleConversationId(getConversationIdGenerator().parseUid(conversationId));
	}

	// internal hooks called by ConversationProxy inner class to manage conversations
	
	private ConversationLock getLock(ConversationId conversationId) throws NoSuchConversationException {
		assertValid(conversationId);
		return getConversationEntry(conversationId).getLock();
	}

	private Object getAttribute(ConversationId conversationId, Object name) throws NoSuchConversationException {
		assertValid(conversationId);
		return getConversationEntry(conversationId).getAttributes().get(name);
	}

	private Object putAttribute(ConversationId conversationId, Object name, Object value)
			throws NoSuchConversationException {
		assertValid(conversationId);
		return getConversationEntry(conversationId).getAttributes().put(name, value);
	}

	private Object removeAttribute(ConversationId conversationId, Object name) throws NoSuchConversationException {
		assertValid(conversationId);
		return getConversationEntry(conversationId).getAttributes().remove(name);
	}

	private void end(ConversationId conversationId) throws NoSuchConversationException {
		assertValid(conversationId);
		getConversationMap().remove(conversationId);
		onEnd(conversationId);
	}
	
	// internal helpers

	/**
	 * Create a new conversation entry, to be stored in the conversation map
	 * managed by this manager.
	 * @param parameters descriptive parameters
	 * @param id the id to assign
	 * @return the conversation entry
	 */
	protected ConversationEntry createConversationEntry(ConversationParameters parameters, ConversationId id) {
		return new ConversationEntry(id, parameters.getName(), parameters.getCaption(), parameters.getDescription());
	}

	/**
	 * Make sure given conversation id is valid, i.e. known in the conversation map.
	 */
	protected void assertValid(ConversationId conversationId) throws NoSuchConversationException {
		if (!getConversationMap().containsKey(conversationId)) {
			throw new NoSuchConversationException(conversationId);
		}
	}

	/**
	 * Returns the identified conversation entry, or null if not found.
	 * @param conversationId the id to lookup
	 * @return the conversation entry
	 */
	protected ConversationEntry getConversationEntry(ConversationId conversationId) {
		return (ConversationEntry)getConversationMap().get(conversationId);
	}

	// hook methods for subclasses

	/**
	 * Hook method that is called each time a new conversation begins.
	 * The default implementation is empty.
	 * @param the newly genrated id for the conversation
	 * @see #beginConversation(ConversationParameters)
	 */
	protected void onBegin(ConversationId conversationId) {
	}

	/**
	 * Hook method that is called each time a conversation ends.
	 * The default implementation is empty.
	 * @param conversationId the id of the conversation that ended
	 */
	protected void onEnd(ConversationId conversationId) {
	}
	
	/**
	 * Hook method that is called each time a conversation is locked.
	 * The default implemenation is empty.
	 * @param conversationId the id of the conversation that was locked
	 */
	protected void onLock(ConversationId conversationId) {
	}

	/**
	 * Hook method that is called each time a conversation is unlocked.
	 * The default implemenation is empty.
	 * @param conversationId the id of the conversation that was unlocked
	 */
	protected void onUnlock(ConversationId conversationId) {
	}

	/**
	 * Returns the Map of conversations used by this conversation manager.
	 * The map will be used to store {@link ConversationEntry} objects.
	 */
	protected abstract Map getConversationMap();

	/**
	 * A proxy to a keyed {@link ConversationEntry entry} in the conversation map.
	 * All calls on the proxy will be delegated to this conversation manager.
	 * 
	 * @author Keith Donald
	 */
	private class ConversationProxy implements Conversation {

		private ConversationId conversationId;

		/**
		 * Create a conversation proxy for a conversation with given id.
		 */
		public ConversationProxy(ConversationId id) {
			this.conversationId = id;
		}

		public ConversationId getId() {
			return conversationId;
		}

		public void lock() {
			AbstractConversationManager.this.getLock(conversationId).lock();
			AbstractConversationManager.this.onLock(conversationId);
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
				// only called on valid unlock
				AbstractConversationManager.this.onUnlock(conversationId);
			}
			catch (NoSuchConversationException e) {
				// ignore
			}
		}

		public void end() {
			try {
				// release lock if necessary from this thread
				AbstractConversationManager.this.getLock(conversationId).unlock();
			}
			catch (Exception e) {
				// ignore
			}
			AbstractConversationManager.this.end(conversationId);
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