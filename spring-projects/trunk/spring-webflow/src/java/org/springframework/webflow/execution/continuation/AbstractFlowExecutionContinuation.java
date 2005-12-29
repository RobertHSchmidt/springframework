package org.springframework.webflow.execution.continuation;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.FlowExecution;

/**
 * Convenient abstract base class for flow execution continuation
 * implementations. Simply stores a serializable identifier property uniquely
 * identifying this continuation in the context of a conversation.
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowExecutionContinuation implements FlowExecutionContinuation {

	/**
	 * The continuation id.
	 */
	private Serializable id;

	/**
	 * Creates a new continuation with the id provided.
	 * @param id the continuation id.
	 */
	public AbstractFlowExecutionContinuation(Serializable id) {
		Assert.notNull(id, "The 'id' property is required");
		this.id = id;
	}

	public Serializable getId() {
		return id;
	}

	public abstract FlowExecution getFlowExecution();

	public boolean equals(Object o) {
		if (!(o instanceof AbstractFlowExecutionContinuation)) {
			return false;
		}
		return id.equals(((AbstractFlowExecutionContinuation)o).id);
	}

	public int hashCode() {
		return id.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("id", id).toString();
	}
}