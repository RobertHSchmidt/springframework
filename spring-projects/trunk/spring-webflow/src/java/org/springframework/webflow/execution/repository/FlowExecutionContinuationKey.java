package org.springframework.webflow.execution.repository;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * A key that uniquely identifies a flow execution in a managed
 * {@link FlowExecutionRepository}.
 * <p>
 * This key consists of two parts:
 * <ol>
 * <li>A conversationId, which identifies a logical "conversation" or
 * "application transaction" within the Spring Web Flow system. This key is used
 * as an index into a single <i>logical</i> executing flow, identifying a user
 * interaction that is currently in process and has not yet completed.
 * <li>A continuationId, which serves as a identifier to a specific snapshot or
 * state of the logical conversation at a point in time relative to the client.
 * This key is used as a index into a specific instance of a
 * <code>FlowExecution</code> created at a point in time that can be restored.
 * </ol>
 * 
 * @author Keith Donald
 */
public class FlowExecutionContinuationKey implements Serializable {

	/**
	 * The serialization version id.
	 */
	private static final long serialVersionUID = 780746376513564069L;

	/**
	 * The conversation key part.
	 */
	private Serializable conversationId;

	/**
	 * The continuation key part.
	 */
	private Serializable continuationId;

	/**
	 * Creates a new, immutable flow execution key.
	 * @param conversationId the conversation key part
	 * @param continuationId the continuation key part
	 */
	public FlowExecutionContinuationKey(Serializable conversationId, Serializable continuationId) {
		Assert.notNull(conversationId, "The conversationId is required");
		Assert.notNull(continuationId, "The conversationId is required");
		this.conversationId = conversationId;
		this.continuationId = continuationId;
	}

	/**
	 * Returns the conversation id key part.
	 */
	public Serializable getConversationId() {
		return conversationId;
	}

	/**
	 * Returns the continuation id key part.
	 */
	public Serializable getContinuationId() {
		return continuationId;
	}

	public boolean equals(Object o) {
		if (!(o instanceof FlowExecutionContinuationKey)) {
			return false;
		}
		FlowExecutionContinuationKey other = (FlowExecutionContinuationKey)o;
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