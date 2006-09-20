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
package org.springframework.webflow.conversation;

/**
 * Thrown when a user tries to access a conversation without having the
 * permissions to do so.
 * 
 * @author Ben Hale
 */
public class ConversationAccessException extends ConversationException {

	/**
	 * The unique conversation identifier.
	 */
	private ConversationId conversationId;

	/**
	 * Creates a new conversation access exception.
	 * @param conversationId the unique conversation identifier
	 */
	public ConversationAccessException(ConversationId conversationId) {
		super("You do not have access to conversation with id '" + conversationId + "'");
		this.conversationId = conversationId;
	}

	/**
	 * Returns the conversation id of the conversation that could not
	 * be accessed.
	 */
	public ConversationId getConversationId() {
		return conversationId;
	}
}