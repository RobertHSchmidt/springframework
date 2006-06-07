/**
 * 
 */
package org.springframework.webflow.execution.repository.support;

import org.springframework.webflow.execution.repository.FlowExecutionLock;
import org.springframework.webflow.execution.repository.conversation.Conversation;
import org.springframework.webflow.execution.repository.conversation.NoSuchConversationException;

class ConversationBackedFlowExecutionLock implements FlowExecutionLock {
	private Conversation conversation;

	public ConversationBackedFlowExecutionLock(Conversation conversation) {
		this.conversation = conversation;
	}

	public void lock() {
		conversation.lock();
	}

	public void unlock() {
		try {
			conversation.unlock();
		} catch (NoSuchConversationException e) {
			
		}
	}
}