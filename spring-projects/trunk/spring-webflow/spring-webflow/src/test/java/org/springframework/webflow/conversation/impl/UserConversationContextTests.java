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