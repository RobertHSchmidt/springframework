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
package org.springframework.webflow.execution.repository.conversation.impl;

import org.springframework.webflow.execution.repository.conversation.Conversation;
import org.springframework.webflow.execution.repository.conversation.ConversationId;
import org.springframework.webflow.execution.repository.conversation.ConversationParameters;
import org.springframework.webflow.execution.repository.conversation.ConversationService;
import org.springframework.webflow.execution.repository.conversation.ConversationServiceException;
import org.springframework.webflow.execution.repository.conversation.NoSuchConversationException;

public class StatelessConversationService implements ConversationService {

	public Conversation begin(ConversationParameters newConversation) throws ConversationServiceException {
		return null;
	}

	public Conversation getConversation(ConversationId id) throws NoSuchConversationException {
		return null;
	}

	public ConversationId parseConversationId(String encodedId) throws ConversationServiceException {
		return null;
	}

}
