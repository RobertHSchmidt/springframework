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

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationParameters;
import org.springframework.webflow.test.MockExternalContext;

public class SessionBindingConversationManagerTests extends TestCase {

	private static final String CONVERSATION_ID_LIST = "webflow.conversation.idList";

	private ExternalContext context;

	protected void setUp() throws Exception {
		context = new MockExternalContext();
		ExternalContextHolder.setExternalContext(context);
	}

	public void testBeginConversation() {
		SessionBindingConversationManager conversationManager = new SessionBindingConversationManager();
		Conversation conversation = conversationManager.beginConversation(new ConversationParameters(
				"testBeginConversation", "testBeginConversation", "testBeginConversation"));
		List conversationIdList = (List)context.getSessionMap().get(CONVERSATION_ID_LIST);
		assertEquals("There should be one conversation id in the list", 1, conversationIdList.size());
		assertTrue("The conversation id should exist in the list", conversationIdList.contains(conversation.getId()));
		Map sessionMap = context.getSessionMap().asMap();
		assertTrue("The conversation should be in the session", sessionMap.containsKey(conversation.getId()));
	}

	public void testBeginConversationExceedMax() {
		SessionBindingConversationManager conversationManager = new SessionBindingConversationManager(1);
		Conversation conversation1 = conversationManager.beginConversation(new ConversationParameters(
				"testBeginConversationExceedMax", "testBeginConversationExceedMax", "testBeginConversationExceedMax"));
		Conversation conversation2 = conversationManager.beginConversation(new ConversationParameters(
				"testBeginConversationExceedMax", "testBeginConversationExceedMax", "testBeginConversationExceedMax"));
		List conversationIdList = (List)context.getSessionMap().get(CONVERSATION_ID_LIST);
		assertEquals("There should be one conversation id in the list", 1, conversationIdList.size());
		assertFalse("The conversation id should not exist in the list", conversationIdList.contains(conversation1
				.getId()));
		assertTrue("The conversation id should exist in the list", conversationIdList.contains(conversation2.getId()));
		Map sessionMap = context.getSessionMap().asMap();
		assertFalse("The conversation should not be in the session", sessionMap.containsKey(conversation1.getId()));
		assertTrue("The conversation should be in the session", sessionMap.containsKey(conversation2.getId()));
	}

	public void testConversationProxyEnd() {
		SessionBindingConversationManager conversationManager = new SessionBindingConversationManager();
		Conversation conversation = conversationManager.beginConversation(new ConversationParameters(
				"testConversationProxyEnd", "testConversationProxyEnd", "testConversationProxyEnd"));
		conversation.end();
		Map sessionMap = context.getSessionMap().asMap();
		List conversationIdList = (List)sessionMap.get(CONVERSATION_ID_LIST);
		assertFalse("The conversationId should not exist in the list", conversationIdList
				.contains(conversation.getId()));
		assertFalse("The conversation should not be in the session", sessionMap.containsKey(conversation.getId()));
	}
}
