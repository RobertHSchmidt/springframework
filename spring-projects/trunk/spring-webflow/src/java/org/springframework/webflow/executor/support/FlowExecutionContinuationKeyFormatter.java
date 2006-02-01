package org.springframework.webflow.executor.support;

import org.springframework.binding.format.Formatter;
import org.springframework.binding.format.InvalidFormatException;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKey;

/**
 * A formatter that converts from a <code>java.lang.String</code>
 * representation of a flow execution key to a {@link FlowExecutionContinuationKey} object.
 * <p>
 * Implements the <code>Formatter</code> interface to allow pluggability of
 * FlowExecutionKey formatting and parsing logic.
 * <p>
 * This implementation expects parseable flow execution keys to be in the
 * following string format:
 * <code>
 * {@link #CONVERSATION_ID_PREFIX}&lt;conversationId&gt;{@link #CONTINUATION_ID_PREFIX}&lt;continuationId&gt;
 * </code>
 * @author Keith Donald
 */
public class FlowExecutionContinuationKeyFormatter implements Formatter {

	/**
	 * The conversation id prefix delimiter ("_s");
	 */
	private static final String CONVERSATION_ID_PREFIX = "_s";

	/**
	 * The continuation id prefix delimiter ("_c");
	 */
	private static final String CONTINUATION_ID_PREFIX = "_c";

	public String formatValue(Object continuationKey) throws IllegalArgumentException {
		Assert.notNull(continuationKey, "The continuation key is required");
		FlowExecutionContinuationKey key = (FlowExecutionContinuationKey)continuationKey;
		return CONVERSATION_ID_PREFIX + key.getConversationId() + CONTINUATION_ID_PREFIX + key.getContinuationId();
	}

	public Object parseValue(String continuationKeyString, Class targetClass) throws InvalidFormatException {
		Assert.hasText(continuationKeyString, "The encoded continuation key must have text");
		Assert.isTrue(continuationKeyString.startsWith(CONVERSATION_ID_PREFIX), "Invalid encoded flow execution key '"
				+ continuationKeyString + "'");
		int continuationStart = continuationKeyString.indexOf(CONTINUATION_ID_PREFIX, CONVERSATION_ID_PREFIX.length());
		String conversationId = continuationKeyString.substring(CONVERSATION_ID_PREFIX.length(), continuationStart);
		String continuationId = continuationKeyString.substring(continuationStart + CONTINUATION_ID_PREFIX.length());
		return new FlowExecutionContinuationKey(conversationId, continuationId);
	}
}