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

import junit.framework.TestCase;

import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationAccessException;
import org.springframework.webflow.conversation.ConversationParameters;
import org.springframework.webflow.conversation.NoSuchConversationException;
import org.springframework.webflow.core.collection.AttributeMapBindingEvent;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.test.MockExternalContext;

/**
 * Unit tests for {@link UserConversationContext}.
 */
public class UserConversationContextTests extends TestCase {

	private static final String USER_CONVERSATION_CONTEXT = "webflow.conversation.userContext";

	private UserConversationContext userContext;

	private LocalConversationManager conversationManager;

	protected void setUp() throws Exception {
		conversationManager = new LocalConversationManager();
		userContext = new UserConversationContext(conversationManager);
		buildExternalContext(userContext);
	}

	private void buildExternalContext(UserConversationContext userContext) {
		ExternalContext externalContext = new MockExternalContext();
		externalContext.getSessionMap().put(USER_CONVERSATION_CONTEXT, userContext);
		ExternalContextHolder.setExternalContext(externalContext);
	}

	public void testConversationCreation() {
		Conversation conversation = conversationManager.beginConversation(new ConversationParameters("name", "caption",
				"description"));
		assertTrue("userContext should have a reference", userContext.contains(conversation.getId()));
	}

	public void testConversationCreationEnd() {
		Conversation conversation1 = conversationManager.beginConversation(new ConversationParameters("name",
				"caption", "description"));
		conversationManager.beginConversation(new ConversationParameters("name", "caption", "description"));
		assertTrue("userContext should have a reference", userContext.contains(conversation1.getId()));
		conversation1.end();
		assertFalse("userContext should not have a reference", userContext.contains(conversation1.getId()));
	}

	public void testConversationRetrieval() {
		Conversation conversation = conversationManager.beginConversation(new ConversationParameters("name", "caption",
				"description"));
		assertEquals("Should return conversation successfully", conversation, conversationManager
				.getConversation(conversation.getId()));
	}

	public void testConversationRetrievalIllegalConversation() {
		Conversation conversation = conversationManager.beginConversation(new ConversationParameters("name", "caption",
				"description"));
		userContext.remove(conversation.getId());
		try {
			conversationManager.getConversation(conversation.getId());
			fail("Should not have retreived a conversation that was not in the UserContext");
		}
		catch (ConversationAccessException e) {
		}
	}

	public void testSessionExpiration() {
		Conversation conversation = conversationManager.beginConversation(new ConversationParameters("name", "caption",
				"description"));
		userContext.valueUnbound(new AttributeMapBindingEvent(new LocalAttributeMap(), USER_CONVERSATION_CONTEXT,
				userContext));
		try {
			conversationManager.getConversation(conversation.getId());
		}
		catch (NoSuchConversationException e) {
		}
	}
}