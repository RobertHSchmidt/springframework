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

import junit.framework.TestCase;

import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationParameters;
import org.springframework.webflow.core.collection.AttributeMapBindingEvent;
import org.springframework.webflow.test.MockExternalContext;

/**
 * Unit tests for {@link LocalConversationManager}.
 */
public class LocalConversationManagerTests extends TestCase {

	private static final String USER_CONVERSATION_CONTEXT = "webflow.conversation.userContext";

	private ExternalContext context;

	protected void setUp() throws Exception {
		context = new MockExternalContext();
		ExternalContextHolder.setExternalContext(context);
	}

	public void testBeginConversation() {
		LocalConversationManager conversationManager = new LocalConversationManager();
		Conversation conversation = conversationManager.beginConversation(new ConversationParameters(
				"testBeginConversation", "testBeginConversation", "testBeginConversation"));
		Map conversationMap = conversationManager.getConversationMap();
		assertEquals("There should be one conversation in the map", 1, conversationMap.size());
		assertTrue("The conversation should exist in the map", conversationMap.containsKey(conversation.getId()));
	}

	public void testBeginConversationExceedMax() {
		LocalConversationManager conversationManager = new LocalConversationManager(1);
		Conversation conversation1 = conversationManager.beginConversation(new ConversationParameters(
				"testBeginConversationExceedMax", "testBeginConversationExceedMax", "testBeginConversationExceedMax"));
		Conversation conversation2 = conversationManager.beginConversation(new ConversationParameters(
				"testBeginConversationExceedMax", "testBeginConversationExceedMax", "testBeginConversationExceedMax"));
		Map conversationMap = conversationManager.getConversationMap();
		assertEquals("There should be one conversation in the map", 1, conversationMap.size());
		assertFalse("The conversation should exist in the map", conversationMap.containsKey(conversation1.getId()));
		assertTrue("The conversation should exist in the map", conversationMap.containsKey(conversation2.getId()));

	}

	public void testConversationProxyEnd() {
		LocalConversationManager conversationManager = new LocalConversationManager();
		Conversation conversation = conversationManager.beginConversation(new ConversationParameters(
				"testConversationProxyEnd", "testConversationProxyEnd", "testConversationProxyEnd"));
		conversation.end();
		Map conversationMap = conversationManager.getConversationMap();
		assertEquals("There should be no conversations in the map", 0, conversationMap.size());
		assertFalse("The conversation should not exist in the map", conversationMap.containsKey(conversation.getId()));
	}

	public void testSessionExpiration() {
		LocalConversationManager conversationManager = new LocalConversationManager();
		Conversation conversation1 = conversationManager.beginConversation(new ConversationParameters(
				"testBeginConversationExceedMax", "testBeginConversationExceedMax", "testBeginConversationExceedMax"));
		Conversation conversation2 = conversationManager.beginConversation(new ConversationParameters(
				"testBeginConversationExceedMax", "testBeginConversationExceedMax", "testBeginConversationExceedMax"));
		UserConversationContext userConversationContext = (UserConversationContext)context.getSessionMap().get(
				USER_CONVERSATION_CONTEXT);
		userConversationContext.valueUnbound(new AttributeMapBindingEvent(context.getSessionMap(),
				USER_CONVERSATION_CONTEXT, userConversationContext));
		Map conversationMap = conversationManager.getConversationMap();
		assertEquals("There should be no conversations in the map", 0, conversationMap.size());
		assertFalse("The conversation should not exist in the map", conversationMap.containsKey(conversation1.getId()));
		assertFalse("The conversation should not exist in the map", conversationMap.containsKey(conversation2.getId()));
	}
}
