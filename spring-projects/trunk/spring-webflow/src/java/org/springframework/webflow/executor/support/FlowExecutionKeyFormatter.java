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
	 * The conversation id prefix delimiter ("_c");
	 */
	private static final String CONVERSATION_ID_PREFIX = "_c";

	/**
	 * The continuation id prefix delimiter ("_k");
	 */
	private static final String CONTINUATION_ID_PREFIX = "_k";

	public String formatValue(Object flowExecutionKey) throws IllegalArgumentException {
		Assert.notNull(flowExecutionKey, "The flow execution key is required");
		Assert.isInstanceOf(FlowExecutionKey.class, flowExecutionKey, "Not of expected type: ");
		FlowExecutionKey key = (FlowExecutionKey)flowExecutionKey;
		return CONVERSATION_ID_PREFIX + key.getConversationId() + CONTINUATION_ID_PREFIX + key.getContinuationId();
	}

	public Object parseValue(String encodedFlowExecutionKey, Class targetClass) throws InvalidFormatException {
		Assert.hasText(encodedFlowExecutionKey, "The string encoded flow execution key is required");
		if (!encodedFlowExecutionKey.startsWith(CONVERSATION_ID_PREFIX)) {
			throw new InvalidFormatException(encodedFlowExecutionKey, getFormat());
		}
		int continuationStart = encodedFlowExecutionKey.indexOf(CONTINUATION_ID_PREFIX, CONVERSATION_ID_PREFIX.length());
		if (continuationStart == -1) {
			throw new InvalidFormatException(encodedFlowExecutionKey, getFormat());
		}
		String conversationId = encodedFlowExecutionKey.substring(CONVERSATION_ID_PREFIX.length(), continuationStart);
		String continuationId = encodedFlowExecutionKey.substring(continuationStart + CONTINUATION_ID_PREFIX.length());
		return new FlowExecutionKey(conversationId, continuationId);
	}
	
	protected String getFormat() {
		return CONVERSATION_ID_PREFIX + "<conversationId>" + CONTINUATION_ID_PREFIX + "<continuationId>";
	}
}