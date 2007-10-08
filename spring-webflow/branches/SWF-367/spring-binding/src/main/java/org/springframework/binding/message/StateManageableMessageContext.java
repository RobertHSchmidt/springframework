package org.springframework.binding.message;

import java.io.Serializable;

public interface StateManageableMessageContext extends MessageContext {
	public Serializable createMessagesMemento();

	public void restoreMessages(Serializable messagesMemento);
}
