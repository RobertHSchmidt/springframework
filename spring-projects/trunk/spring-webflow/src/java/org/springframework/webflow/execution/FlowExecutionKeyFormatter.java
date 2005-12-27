package org.springframework.webflow.execution;

import org.springframework.binding.format.Formatter;
import org.springframework.binding.format.InvalidFormatException;
import org.springframework.util.Assert;

public class FlowExecutionKeyFormatter implements Formatter {

	private static final String CONVERSATION_ID_PREFIX = "_s";

	private static final String CONTINUATION_ID_PREFIX = "_c";

	public String formatValue(Object continuationKey) throws IllegalArgumentException {
		FlowExecutionKey key = (FlowExecutionKey)continuationKey;
		return CONVERSATION_ID_PREFIX + key.getConversationId() + CONTINUATION_ID_PREFIX + key.getContinuationId();
	}

	public Object parseValue(String continuationKeyString, Class targetClass) throws InvalidFormatException {
		Assert.hasText(continuationKeyString, "The encoded continuation key must have text");
		Assert.isTrue(continuationKeyString.startsWith(CONVERSATION_ID_PREFIX), "Invalid encoded flow execution key '" + continuationKeyString + "'");
		int continuationStart = continuationKeyString.indexOf(CONTINUATION_ID_PREFIX, CONVERSATION_ID_PREFIX.length());
		String conversationId = continuationKeyString.substring(CONVERSATION_ID_PREFIX.length(), continuationStart);
		String continuationId = continuationKeyString.substring(continuationStart + CONTINUATION_ID_PREFIX.length());
		return new FlowExecutionKey(conversationId, continuationId);
	}
}