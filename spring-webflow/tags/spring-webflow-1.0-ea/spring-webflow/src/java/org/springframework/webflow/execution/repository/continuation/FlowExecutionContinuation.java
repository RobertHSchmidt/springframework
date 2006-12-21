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
package org.springframework.webflow.execution.repository.continuation;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.FlowExecution;

/**
 * Convenient base for flow execution continuation implementations. Simply
 * stores a serializable identifier property uniquely identifying this
 * continuation in the context of exactly one conversation.
 * 
 * @author Keith Donald
 */
public abstract class FlowExecutionContinuation implements Serializable {

	/**
	 * The continuation id.
	 */
	private Serializable id;

	/**
	 * Creates a new continuation with the id provided.
	 * @param id the continuation id.
	 */
	public FlowExecutionContinuation(Serializable id) {
		Assert.notNull(id, "The 'id' property is required");
		this.id = id;
	}

	public Serializable getId() {
		return id;
	}

	public abstract FlowExecution getFlowExecution();

	public abstract byte[] toByteArray();

	public boolean equals(Object o) {
		if (!(o instanceof FlowExecutionContinuation)) {
			return false;
		}
		return id.equals(((FlowExecutionContinuation)o).id);
	}

	public int hashCode() {
		return id.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("id", id).toString();
	}
}