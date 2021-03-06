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
package org.springframework.webflow.execution.repository.support;

import java.io.Serializable;

import org.springframework.binding.format.InvalidFormatException;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.continuation.FlowExecutionContinuation;
import org.springframework.webflow.execution.repository.conversation.ConversationId;
import org.springframework.webflow.execution.repository.conversation.ConversationService;

/**
 * A flow execution key consisting of two parts:
 * <ol>
 * <li>A conversationId, identifying an active conversation managed by a
 * {@link ConversationService}.
 * <li>A continuationId, identifying a restorable
 * {@link FlowExecutionContinuation} within a continuation group governed by
 * that conversation.
 * 
 * This key is used to restore a FlowExecution from a conversation-service
 * backed store.
 * 
 * @author Keith Donald
 */
class CompositeFlowExecutionKey extends FlowExecutionKey {

	/**
	 * The default conversation id prefix delimiter ("_c");
	 */
	private static final String CONVERSATION_ID_PREFIX = "_c";

	/**
	 * The default continuation id prefix delimiter ("_k");
	 */
	private static final String CONTINUATION_ID_PREFIX = "_k";

	/**
	 * The conversation id.
	 */
	private ConversationId conversationId;

	/**
	 * The continuation id.
	 */
	private Serializable continuationId;

	public CompositeFlowExecutionKey(ConversationId conversationId, Serializable continuationId) {
		Assert.notNull(conversationId, "The conversation id is required");
		Assert.notNull(continuationId, "The continuation id is required");
		this.conversationId = conversationId;
		this.continuationId = continuationId;
	}

	public ConversationId getConversationId() {
		return conversationId;
	}

	public Serializable getContinuationId() {
		return continuationId;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof CompositeFlowExecutionKey)) {
			return false;
		}
		CompositeFlowExecutionKey other = (CompositeFlowExecutionKey)obj;
		return conversationId.equals(other.conversationId) && continuationId.equals(other.continuationId);
	}

	public int hashCode() {
		return conversationId.hashCode() + continuationId.hashCode();
	}

	/**
	 * Helper that splits the string-form of an instance of this class into its
	 * "parts" so the parts can be easily parsed.
	 * @param encodedKey the string-encoded composite flow execution key
	 * @return the composite key parts as a String array (conversationId = 0,
	 * continuationId = 1)
	 */
	public static String[] keyParts(String encodedKey) {
		if (!encodedKey.startsWith(CONVERSATION_ID_PREFIX)) {
			throw new InvalidFormatException(encodedKey, CompositeFlowExecutionKey.getFormat());
		}
		int continuationStart = encodedKey.indexOf(CONTINUATION_ID_PREFIX, CONVERSATION_ID_PREFIX.length());
		if (continuationStart == -1) {
			throw new InvalidFormatException(encodedKey, getFormat());
		}
		String conversationId = encodedKey.substring(CONVERSATION_ID_PREFIX.length(), continuationStart);
		String continuationId = encodedKey.substring(continuationStart + CONTINUATION_ID_PREFIX.length());
		return new String[] { conversationId, continuationId };
	}

	/**
	 * Returns the format of the default string-encoded form.
	 * @return the format
	 */
	private static String getFormat() {
		return CONVERSATION_ID_PREFIX + "<conversationId>" + CONTINUATION_ID_PREFIX + "<continuationId>";
	}

	public String toString() {
		return CONVERSATION_ID_PREFIX + getConversationId() + CONTINUATION_ID_PREFIX + getContinuationId();
	}
}