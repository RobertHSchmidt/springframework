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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationException;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.ConversationManager;
import org.springframework.webflow.conversation.ConversationParameters;
import org.springframework.webflow.conversation.NoSuchConversationException;
import org.springframework.webflow.util.RandomGuidUidGenerator;
import org.springframework.webflow.util.UidGenerator;

/**
 * Simple implementation of a conversation manager that stores conversations in
 * the session attribute map.
 * 
 * @author Erwin Vervaet
 */
public class SessionBindingConversationManager implements ConversationManager {

	/**
	 * Key of the session attribute holding the conversation container.
	 */
	private static final String CONVERSATION_CONTAINER_KEY = "webflow.conversation.container";

	/**
	 * The conversation uid generation strategy to use.
	 */
	private UidGenerator conversationIdGenerator = new RandomGuidUidGenerator();

	/**
	 * The maximum number of active conversations allowed in a session.
	 */
	private int maxConversations = -1;

	/**
	 * Sets the configured generator for conversation ids.
	 */
	public void setConversationIdGenerator(UidGenerator uidGenerator) {
		this.conversationIdGenerator = uidGenerator;
	}

	/**
	 * Set the maximum number of allowed concurrent conversations. Set to -1 for
	 * no limit.
	 */
	public void setMaxConversations(int maxConversations) {
		this.maxConversations = maxConversations;
	}

	public Conversation beginConversation(ConversationParameters conversationParameters) throws ConversationException {
		ConversationId conversationId = new SimpleConversationId(conversationIdGenerator.generateUid());
		return getConversationContainer().createAndAddConversation(conversationId, conversationParameters);
	}

	public Conversation getConversation(ConversationId id) throws ConversationException {
		return getConversationContainer().getConversation(id);
	}

	public ConversationId parseConversationId(String encodedId) throws ConversationException {
		return new SimpleConversationId(conversationIdGenerator.parseUid(encodedId));
	}

	// internal helpers

	/**
	 * Obtain the conversation container from the session. Create a new empty
	 * container and add it to the session if no existing container can be
	 * found.
	 */
	private ConversationContainer getConversationContainer() {
		Map sessionMap = ExternalContextHolder.getExternalContext().getSessionMap().asMap();
		ConversationContainer container = (ConversationContainer)sessionMap.get(CONVERSATION_CONTAINER_KEY);
		if (container == null) {
			container = new ConversationContainer(maxConversations);
			sessionMap.put(CONVERSATION_CONTAINER_KEY, container);
		}
		return container;
	}

	/**
	 * Container for conversations that is stored in the session. When the
	 * session expires this container will go with it, implicitly expiring all
	 * contained conversations.
	 */
	private static class ConversationContainer implements Serializable {

		private int maxConversations;

		private List conversations;

		/**
		 * Create a new conversation container.
		 * @param maxConversations the maximum number of allowed concurrent
		 * conversations, -1 for unlimited
		 */
		public ConversationContainer(int maxConversations) {
			this.maxConversations = maxConversations;
			this.conversations = new ArrayList();
		}

		/**
		 * Create a new conversation based on given parameters and add it to the
		 * container.
		 * @param id the unique id of the conversation
		 * @param parameters descriptive parameters
		 * @return the created conversation
		 */
		public synchronized Conversation createAndAddConversation(ConversationId id, ConversationParameters parameters) {
			// conversation parameters are not used
			ContainedConversation conversation = new ContainedConversation(this, id);
			conversations.add(conversation);
			if (maxExceeded()) {
				// remove oldest conversation
				conversations.remove(0);
			}
			return conversation;
		}

		/**
		 * Return the identified conversation.
		 * @param id the id to lookup
		 * @return the conversation
		 * @throws NoSuchConversationException if the conversation cannot be
		 * found
		 */
		public synchronized Conversation getConversation(ConversationId id) throws NoSuchConversationException {
			for (Iterator it = conversations.iterator(); it.hasNext();) {
				ContainedConversation conversation = (ContainedConversation)it.next();
				if (conversation.getId().equals(id)) {
					return conversation;
				}
			}
			throw new NoSuchConversationException(id);
		}

		/**
		 * Remove identified conversation from this container.
		 */
		public synchronized void removeConversation(ConversationId id) {
			for (Iterator it = conversations.iterator(); it.hasNext();) {
				ContainedConversation conversation = (ContainedConversation)it.next();
				if (conversation.getId().equals(id)) {
					it.remove();
					break;
				}
			}
		}

		/**
		 * Has the maximum number of allowed concurrent conversations in the
		 * session been exceeded?
		 */
		private boolean maxExceeded() {
			return maxConversations > 0 && conversations.size() > maxConversations;
		}
	}

	/**
	 * Internal {@link Conversation} implementation used by the conversation
	 * container.
	 */
	private static class ContainedConversation implements Conversation, Serializable {

		private ConversationContainer container;

		private ConversationId id;

		private transient ConversationLock lock;

		private Map attributes;

		/**
		 * Create a new contained conversation.
		 * @param container the container containing the conversation
		 * @param id the unique id assigned to the conversation
		 */
		public ContainedConversation(ConversationContainer container, ConversationId id) {
			this.container = container;
			this.id = id;
			this.lock = ConversationLockFactory.createLock();
			this.attributes = new HashMap();
		}

		public ConversationId getId() {
			return id;
		}

		public void lock() {
			lock.lock();
		}

		public Object getAttribute(Object name) {
			return attributes.get(name);
		}

		public void putAttribute(Object name, Object value) {
			attributes.put(name, value);
		}

		public void removeAttribute(Object name) {
			attributes.remove(name);
		}

		public void end() {
			container.removeConversation(getId());
		}

		public void unlock() {
			lock.unlock();
		}

		public String toString() {
			return getId().toString();
		}

		// id based equality

		public boolean equals(Object obj) {
			if (!(obj instanceof ContainedConversation)) {
				return false;
			}
			return id.equals(((ContainedConversation)obj).id);
		}

		public int hashCode() {
			return id.hashCode();
		}

		// custom serialisation

		private void writeObject(ObjectOutputStream out) throws IOException {
			out.defaultWriteObject();
		}

		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
			lock = ConversationLockFactory.createLock();
		}
	}
}