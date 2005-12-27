package org.springframework.webflow.execution;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

public class FlowExecutionKey implements Serializable {
	private Serializable conversationId;

	private Serializable continuationId;

	public FlowExecutionKey(Serializable conversationId, Serializable continuationId) {
		Assert.notNull(conversationId, "The conversationId is required");
		Assert.notNull(continuationId, "The conversationId is required");
		this.conversationId = conversationId;
		this.continuationId = continuationId;
	}

	public Serializable getConversationId() {
		return conversationId;
	}

	public Serializable getContinuationId() {
		return continuationId;
	}

	public boolean equals(Object o) {
		if (!(o instanceof FlowExecutionKey)) {
			return false;
		}
		FlowExecutionKey other = (FlowExecutionKey)o;
		return conversationId.equals(other.conversationId) && continuationId.equals(other.continuationId);
	}

	public int hashCode() {
		return conversationId.hashCode() + continuationId.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("conversationId", conversationId).append("continuationId",
				continuationId).toString();
	}
}