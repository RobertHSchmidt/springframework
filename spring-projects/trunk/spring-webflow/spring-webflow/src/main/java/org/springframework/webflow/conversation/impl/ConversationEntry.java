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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.conversation.ConversationId;

/**
 * A container that holds the state of a conversation. The primary identifier is
 * a {@link ConversationId}, which identifies a logical <i>conversation</i> or
 * <i>application transaction</i>. This key is used as an index into a single
 * <i>logical</i> executing conversation, identifying a user interaction that
 * is currently in process and has not yet completed.
 * <p>
 * This is an internal helper class of the {@link AbstractConversationManager}.
 * 
 * @see AbstractConversationManager
 * 
 * @author Ben Hale
 */
class ConversationEntry implements Serializable {

	private ConversationId id;

	private long beginTime = new Date().getTime();
	
	private String name;
	
	private String caption;

	private String description;

	private transient ConversationLock lock = ConversationLockFactory.createLock();

	private Map attributes = new HashMap();

	/**
	 * Create a new conversation entry.
	 * @param id the id to assign
	 * @param name the name of the conversation
	 * @param caption a short description
	 * @param description a long description
	 */
	public ConversationEntry(ConversationId id, String name, String caption, String description) {
		this.id = id;
		this.name = name;
		this.caption = caption;
		this.description = description;
	}

	/**
	 * Returns the unique id of the conversation.
	 */
	public ConversationId getId() {
		return id;
	}

	/**
	 * Returns the timestamp when the conversation began, represented as the number of
	 * milliseconds since January 1, 1970, 00:00:00 GMT.
	 */
	public long getBeginTime() {
		return beginTime;
	}
	
	/**
	 * Returns the name of the conversation.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the short description of the conversation.
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * Returns the long description of the conversation.
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Returns a map of attributes associated with the conversation.
	 */
	public Map getAttributes() {
		return attributes;
	}

	/**
	 * Returns a lock for the conversation.
	 */
	public ConversationLock getLock() {
		return lock;
	}

	public boolean equals(Object o) {
		if (!(o instanceof ConversationEntry)) {
			return false;
		}
		return id.equals(((ConversationEntry)o).id);
	}

	public int hashCode() {
		return id.hashCode();
	}

	//custom serialization

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		lock = ConversationLockFactory.createLock();
	}
	
	public String toString() {
		return new ToStringCreator(this).append("id", id).append("caption", caption).append("lock", lock).toString();
	}
}