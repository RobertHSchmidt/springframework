/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * <li>A conversationId, which identifies a logical <i>conversation</i> or
 * <i>application transaction</i> within the Spring Web Flow system. This key
 * is used as an index into a single <i>logical</i> executing flow, identifying
 * a user interaction that is currently in process and has not yet completed.
 * <li>A continuationId, which serves as a identifier to a specific, restorable
 * snapshot or state of a logical conversation at a point in time relative to
 * the client. This key is used as a index into a specific instance of a
 * <code>FlowExecution</code> created at a point in time that can be restored.
 * </ol>
 * 
 * @author Keith Donald
 */
public class FlowExecutionKey implements Serializable {

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
	public FlowExecutionKey(Serializable conversationId, Serializable continuationId) {
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