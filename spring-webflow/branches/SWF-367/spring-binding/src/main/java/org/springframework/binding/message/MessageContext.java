package org.springframework.binding.message;

/**
 * A context for accessing and recording messages for display.
 */
public interface MessageContext {

	/**
	 * Get all messages in this context. The messages returned should be suitable for display as-is.
	 * @return the messages
	 */
	public Message[] getMessages();

	/**
	 * Get messages by source.
	 * @param source the source that recorded the message
	 * @return messages
	 */
	public Message[] getMessages(Object source);

	/**
	 * Add a new message to this context.
	 * @param messageResolver the resolver that will resolve the message to be added
	 */
	public void addMessage(MessageResolver messageResolver);
}
