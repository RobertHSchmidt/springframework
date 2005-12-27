package org.springframework.webflow.execution.continuation;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.FlowExecution;

public abstract class AbstractFlowExecutionContinuation implements FlowExecutionContinuation {
	private Serializable id;

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