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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.core.JdkVersion;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.conversation.ConversationAccessException;
import org.springframework.webflow.conversation.ConversationException;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.ConversationManager;
import org.springframework.webflow.core.collection.SharedAttributeMap;

/**
 * The default implementation of the {@link ConversationManager}. This
 * implementation maintains an internal state of all conversations that have
 * been begun.
 * 
 * @author Ben Hale
 * @author Keith Donald
 */
public class LocalConversationManager extends AbstractConversationManager implements Serializable {

	/**
	 * Session attribute that stores the UserConversationContext.
	 */
	private static final String USER_CONVERSATION_CONTEXT = "webflow.conversation.userContext";

	/**
	 * The local conversation data store.
	 */
	private Map conversations;

	/**
	 * The maximum number of active conversations allowed by this manager.
	 */
	private int maxConversations;

	private static boolean utilConcurrentPresent;
	static {
		try {
			Class.forName("EDU.oswego.cs.dl.util.concurrent.ReentrantLock");
			utilConcurrentPresent = true;
		}
		catch (ClassNotFoundException ex) {
			utilConcurrentPresent = false;
		}
	}

	/**
	 * Creates a new local conversation manager.
	 */
	public LocalConversationManager() {
		this(-1);
	}

	/**
	 * Creates a new local conversation manager.
	 * @param maxConversations the maximum number of conversations that can be
	 * active at once, or -1 if not limited
	 */
	public LocalConversationManager(int maxConversations) {
		this.maxConversations = maxConversations;
		this.conversations = createConversations();
	}

	protected Map getConversationMap() {
		return conversations;
	}

	// overridden hooks

	protected void assertValid(ConversationId conversationId) {
		super.assertValid(conversationId);
		if (!getUserContext().contains(conversationId)) {
			throw new ConversationAccessException(conversationId);
		}
	}

	protected void onBegin(ConversationId conversationId) {
		getUserContext().add(conversationId);
		// end the oldest conversation if the maximum number of
		// conversations has been exceeded
		if (maxExceeded()) {
			removeOldestConversation();
		}
	}

	protected void onEnd(ConversationId conversationId) {
		getUserContext().remove(conversationId);
	}

	// called by an async monitor thread
	void expire(Collection conversationIds) throws ConversationException {
		for (Iterator it = conversationIds.iterator(); it.hasNext();) {
			ConversationId id = (ConversationId)it.next();
			getConversationMap().remove(id);
		}
	}

	// helpers

	/**
	 * Create the <tt>conversations</tt> map.
	 */
	private Map createConversations() {
		if (JdkVersion.getMajorJavaVersion() >= JdkVersion.JAVA_15) {
			return new java.util.concurrent.ConcurrentHashMap();
		}
		else if (utilConcurrentPresent) {
			return new EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap();
		}
		else {
			return Collections.synchronizedMap(new HashMap());
		}
	}

	/**
	 * Returns the UserConversationContext storing the conversations
	 * managed by this manager. Sets up a new UserConversationContext object if
	 * non can be found in the session.
	 */
	private UserConversationContext getUserContext() {
		SharedAttributeMap session = ExternalContextHolder.getExternalContext().getSessionMap();
		synchronized (session.getMutex()) {
			UserConversationContext context = (UserConversationContext)session.get(USER_CONVERSATION_CONTEXT);
			if (context == null) {
				context = new UserConversationContext(this);
				ExternalContextHolder.getExternalContext().getSessionMap().put(USER_CONVERSATION_CONTEXT, context);
			}
			return context;
		}
	}

	/**
	 * Has the maximum number of allowed concurrent conversations been exceeded?
	 */
	private boolean maxExceeded() {
		return maxConversations > 0 && getUserContext().size() > maxConversations;
	}

	/**
	 * Remove the oldest conversation from the map of managed conversations.
	 */
	private void removeOldestConversation() {
		ConversationId conversationId = getUserContext().getFirst();
		getConversationMap().remove(conversationId);
		getUserContext().remove(conversationId);
	}
}