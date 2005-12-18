package org.springframework.webflow.execution;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;

public class ContinuationKey implements Serializable {
	private Serializable conversationId;

	private Serializable continuationId;

	public ContinuationKey(Serializable conversationId, Serializable continuationId) {
		this.conversationId = conversationId;
		this.continuationId = continuationId;
	}

	public Serializable getConversationId() {
		return conversationId;
	}

	public Serializable getContinuationId() {
		return continuationId;
	}

	public String toString() {
		return new ToStringCreator(this).append("conversationId", conversationId).append("continuationId",
				continuationId).toString();
	}
}
