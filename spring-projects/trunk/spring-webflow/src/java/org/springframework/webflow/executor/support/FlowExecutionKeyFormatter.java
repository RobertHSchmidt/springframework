package org.springframework.webflow.executor.support;

import org.springframework.binding.format.Formatter;
import org.springframework.binding.format.InvalidFormatException;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.repository.FlowExecutionKey;

/**
 * A formatter that converts from a <code>java.lang.String</code>
 * representation of a flow execution key to a {@link FlowExecutionKey} object.
 * <p>
 * Implements the <code>Formatter</code> interface to allow pluggability of
 * FlowExecutionKey formatting and parsing logic.
 * <p>
 * This implementation expects parseable flow execution keys to be in the
 * following string format: <code>
 * {@link #CONVERSATION_ID_PREFIX}&lt;conversationId&gt;{@link #CONTINUATION_ID_PREFIX}&lt;continuationId&gt;
 * </code>
 * @author Keith Donald
 */
public class FlowExecutionKeyFormatter implements Formatter {

	/**
	 * The default conversation id prefix delimiter ("_c");
	 */
	private static final String CONVERSATION_ID_PREFIX = "_c";

	/**
	 * The default continuation id prefix delimiter ("_k");
	 */
	private static final String CONTINUATION_ID_PREFIX = "_k";

	/**
	 * Returns the conversation id prefix delimiter.
	 */
	public String getConversationIdPrefix() {
		return CONVERSATION_ID_PREFIX;
	}

	/**
	 * Returns the continuation id prefix delimiter.
	 */
	public String getContinuationIdPrefix() {
		return CONTINUATION_ID_PREFIX;
	}

	public String formatValue(Object flowExecutionKey) throws IllegalArgumentException {
		Assert.notNull(flowExecutionKey, "The flow execution key is required");
		Assert.isInstanceOf(FlowExecutionKey.class, flowExecutionKey, "Not of expected type: ");
		FlowExecutionKey key = (FlowExecutionKey)flowExecutionKey;
		return getConversationIdPrefix() + key.getConversationId() + getContinuationIdPrefix()
				+ key.getContinuationId();
	}

	public Object parseValue(String encodedKey, Class targetClass) throws InvalidFormatException {
		Assert.hasText(encodedKey, "The string encoded flow execution key is required");
		if (!encodedKey.startsWith(getConversationIdPrefix())) {
			throw new InvalidFormatException(encodedKey, getFormat());
		}
		int continuationStart = encodedKey.indexOf(getContinuationIdPrefix(), getConversationIdPrefix().length());
		if (continuationStart == -1) {
			throw new InvalidFormatException(encodedKey, getFormat());
		}
		String conversationId = encodedKey.substring(getConversationIdPrefix().length(), continuationStart);
		String continuationId = encodedKey.substring(continuationStart + getContinuationIdPrefix().length());
		return new FlowExecutionKey(conversationId, continuationId);
	}

	protected String getFormat() {
		return getConversationIdPrefix() + "<conversationId>" + getContinuationIdPrefix() + "<continuationId>";
	}

	public boolean isFormatted(String encodedKey) {
		return encodedKey.startsWith(getConversationIdPrefix())
				&& encodedKey.indexOf(getContinuationIdPrefix(), getConversationIdPrefix().length()) != -1;
	}
}