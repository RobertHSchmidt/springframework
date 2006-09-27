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
import java.util.LinkedList;

import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.core.collection.AttributeMapBindingEvent;
import org.springframework.webflow.core.collection.AttributeMapBindingListener;

/**
 * Container for the contextual information for a given user. This context can
 * be bound as a binding listener in an <code>ExternalContext</code>.
 * 
 * @see ExternalContext
 * @see AttributeMapBindingListener
 * 
 * @author Ben Hale
 */
class UserConversationContext implements AttributeMapBindingListener, Serializable {

	private LinkedList conversationIds = new LinkedList();

	private transient LocalConversationManager conversationManager;

	/**
	 * Create a new user conversation context for given conversation manager.
	 */
	public UserConversationContext(LocalConversationManager conversationManager) {
		this.conversationManager = conversationManager;
	}

	/**
	 * Register a new conversation in this context.
	 */
	public synchronized boolean add(ConversationId conversationId) {
		return conversationIds.add(conversationId);
	}

	/**
	 * Does this context contain identified conversation?
	 */
	public synchronized boolean contains(ConversationId conversationId) {
		return conversationIds.contains(conversationId);
	}

	/**
	 * Returns the id of the first conversation that was registered
	 * with this context.
	 * @see #add(ConversationId)
	 */
	public synchronized ConversationId getFirst() {
		return (ConversationId)conversationIds.getFirst();
	}

	/**
	 * Unregister identified conversation from this context.
	 * @param conversationId the id of the conversation to remove
	 * @return true if the conversation was known in this context, false otherwise
	 */
	public synchronized boolean remove(ConversationId conversationId) {
		return conversationIds.remove(conversationId);
	}

	/**
	 * Returns the number of conversations registered in this context.
	 */
	public synchronized int size() {
		return conversationIds.size();
	}

	public void valueBound(AttributeMapBindingEvent event) {
		// ignore
	}

	public synchronized void valueUnbound(AttributeMapBindingEvent event) {
		// all conversations have expired
		conversationManager.expire(conversationIds);
		conversationIds.clear();
	}
	
	public String toString() {
		return conversationIds.toString();
	}
}