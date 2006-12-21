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
import java.util.HashMap;
import java.util.Map;

import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationId;

/**
 * Internal {@link Conversation} implementation used by the conversation
 * container.
 * <p>
 * This is an internal helper class of the {@link SessionBindingConversationManager}.
 * 
 * @author Erwin Vervaet
 */
class ContainedConversation implements Conversation, Serializable {

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