package org.springframework.binding.message;

public interface MessageContext {
	public Message[] getMessages(String objectId);

	public void addMessage(String objectId, MessageResolver messageResolver);
}
