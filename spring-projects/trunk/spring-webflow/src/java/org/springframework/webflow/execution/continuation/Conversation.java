package org.springframework.webflow.execution.continuation;

import java.io.Serializable;
import java.util.ListIterator;
import java.util.Stack;

public class Conversation implements Serializable {
	
	private static final long serialVersionUID = 3532014613434083365L;

	private Stack continuations = new Stack();

	public FlowExecutionContinuation getContinuation(Serializable id) {
		ListIterator it = continuations.listIterator(continuations.size());
		while (it.hasPrevious()) {
			FlowExecutionContinuation continuation = (FlowExecutionContinuation)it.previous();
			if (continuation.getId().equals(id)) {
				return continuation;
			}
		}
		return null;
	}

	public void addContinuation(FlowExecutionContinuation continuation) {
		continuations.push(continuation);
	}

	public int getContinuationCount() {
		return continuations.size();
	}
}