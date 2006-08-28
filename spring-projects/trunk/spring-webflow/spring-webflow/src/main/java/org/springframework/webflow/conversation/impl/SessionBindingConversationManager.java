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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.webflow.context.SharedAttributeMap;
import org.springframework.webflow.context.support.ExternalContextHolder;
import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationException;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.ConversationParameters;

public class SessionBindingConversationManager extends AbstractConversationManager {

	private static final String CONVERSATION_MAP = "webflow.conversation.map";

	private static final String CONVERSATION_ID_LIST = "webflow.conversation.idList";

	/**
	 * The maximum number of active conversations allowed in a session.
	 */
	private int maxConversations;

	/**
	 * Creates a new session binding conversation service.
	 */
	public SessionBindingConversationManager() {
		this(-1);
	}

	/**
	 * Creates a new session binding conversation service.
	 * @param maxConversations the maximum number of conversations that can be
	 * active at once within this session.
	 */
	public SessionBindingConversationManager(int maxConversations) {
		this.maxConversations = maxConversations;
	}

	public Conversation beginConversation(ConversationParameters conversationParameters) throws ConversationException {
		ConversationId conversationId = new SimpleConversationId(getConversationIdGenerator().generateUid());
		getConversationMap().put(conversationId, createConversation(conversationParameters, conversationId));
		getConversationIdList().add(conversationId);
		// end the oldest conversation if them maximium number of
		// conversations has been exceeded
		if (maxExceeded()) {
			endOldestConversation();
		}
		return getConversation(conversationId);
	}

	protected Map getConversationMap() {
		SharedAttributeMap session = ExternalContextHolder.getExternalContext().getSessionMap();
		synchronized (session.getMutex()) {
			Map map = (Map)session.get(CONVERSATION_MAP);
			if (map == null) {
				map = new HashMap();
				ExternalContextHolder.getExternalContext().getSessionMap().put(CONVERSATION_MAP, map);
			}
			return map;
		}
	}

	protected List getConversationIdList() {
		SharedAttributeMap session = ExternalContextHolder.getExternalContext().getSessionMap();
		synchronized (session.getMutex()) {
			LinkedList conversationIds = (LinkedList)session.get(CONVERSATION_ID_LIST);
			if (conversationIds == null) {
				conversationIds = new LinkedList();
				ExternalContextHolder.getExternalContext().getSessionMap().put(CONVERSATION_ID_LIST, conversationIds);
			}
			return conversationIds;
		}
	}

	private void endOldestConversation() {
		ConversationId id = (ConversationId)getConversationIdList().get(0);
		getConversationMap().remove(id);
		getConversationIdList().remove(id);
	}
	
	protected void onEnd(ConversationId id) {
		getConversationIdList().remove(id);
	}

	private boolean maxExceeded() {
		return maxConversations > 0 && getConversationMap().size() > maxConversations;
	}
}