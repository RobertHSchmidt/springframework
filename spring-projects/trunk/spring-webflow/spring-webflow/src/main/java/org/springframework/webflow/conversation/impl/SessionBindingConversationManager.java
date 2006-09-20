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
import java.util.List;
import java.util.Map;

import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.core.collection.SharedAttributeMap;

/**
 * Stateless conversation manager that puts all conversational state in the
 * {@link ExternalContext#getSessionMap() session map}.  Conversations 
 * are indexed in this map by their identifiers.
 * <p>
 * Supports rebinding conversation entries to the session on an unlock operation
 * to facilitate replication in a traditionally clustered environment.
 * 
 * @author Keith Donald
 */
public class SessionBindingConversationManager extends AbstractConversationManager {

	private static final String CONVERSATION_ID_LIST = "webflow.conversation.idList";

	/**
	 * The maximum number of active conversations allowed in a session.
	 */
	private int maxConversations;

	/**
	 * Creates a new session binding conversation manager.
	 */
	public SessionBindingConversationManager() {
		this(-1);
	}

	/**
	 * Creates a new session binding conversation manager.
	 * @param maxConversations the maximum number of conversations that can be
	 * active at once within a, or -1 if unlimited
	 */
	public SessionBindingConversationManager(int maxConversations) {
		this.maxConversations = maxConversations;
	}
	
	// overridden hooks

	protected Map getConversationMap() {
		return ExternalContextHolder.getExternalContext().getSessionMap().asMap();
	}
	
	protected void onBegin(ConversationId conversationId) {
		getConversationIdList().add(conversationId);
		// end the oldest conversation if them maximum number of
		// conversations has been exceeded
		if (maxExceeded()) {
			endOldestConversation();
		}
	}

	protected void onEnd(ConversationId id) {
		getConversationIdList().remove(id);
	}

	protected void onUnlock(ConversationId conversationId) {
		// do rebinding to force session attribute replication if necessary
		rebind(conversationId);
	}

	// internal helpers
	
	private List getConversationIdList() {
		SharedAttributeMap session = ExternalContextHolder.getExternalContext().getSessionMap();
		synchronized (session.getMutex()) {
			List conversationIds = (List)session.get(CONVERSATION_ID_LIST);
			if (conversationIds == null) {
				conversationIds = new LinkedList();
				ExternalContextHolder.getExternalContext().getSessionMap().put(CONVERSATION_ID_LIST, conversationIds);
			}
			return conversationIds;
		}
	}

	/**
	 * End the oldest conversation stored in the session.
	 */
	private void endOldestConversation() {
		ConversationId id = (ConversationId)getConversationIdList().get(0);
		getConversationMap().remove(id);
		getConversationIdList().remove(id);
	}
	
	/**
	 * Rebind identified conversation in the session to ensure proper
	 * replication in a clustered environment.
	 */
	private void rebind(ConversationId conversationId) {
		ConversationEntry entry = getConversationEntry(conversationId);
		getConversationMap().put(conversationId, entry);
	}

	/**
	 * Has the maximum number of allowed concurrent conversations in the session
	 * been exceeded?
	 */
	private boolean maxExceeded() {
		return maxConversations > 0 && getConversationIdList().size() > maxConversations;
	}
}