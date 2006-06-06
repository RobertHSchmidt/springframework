package org.springframework.webflow.execution.repository.support;

import java.io.Serializable;

import org.springframework.binding.format.InvalidFormatException;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.conversation.ConversationId;

class CompositeFlowExecutionKey extends FlowExecutionKey {
	
	private ConversationId conversationId;

	private Serializable continuationId;

	/**
	 * The default conversation id prefix delimiter ("_c");
	 */
	private static final String CONVERSATION_ID_PREFIX = "_c";

	/**
	 * The default continuation id prefix delimiter ("_k");
	 */
	private static final String CONTINUATION_ID_PREFIX = "_k";

	public CompositeFlowExecutionKey(ConversationId conversationId, Serializable continuationId) {
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
	
	private static String getFormat() {
		return CONVERSATION_ID_PREFIX  + "<conversationId>" + CONTINUATION_ID_PREFIX + "<continuationId>";
	}
	
	public String toString() {
		return CONVERSATION_ID_PREFIX + getConversationId() + CONTINUATION_ID_PREFIX + getContinuationId();
	}
}