package org.springframework.webflow.execution.continuation;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.ListIterator;

import org.springframework.util.Assert;
import org.springframework.webflow.Scope;
import org.springframework.webflow.ScopeType;

/**
 * Represents an active, ongoing conversation between a user and the Spring Web
 * Flow system.
 * @author Keith Donald
 */
public class Conversation implements Serializable {

	private static final long serialVersionUID = 3532014613434083365L;

	/**
	 * A map for data stored in 'conversational scope'.
	 */
	private Scope scope = new Scope(ScopeType.FLOW);

	/**
	 * A stack of conversation continuations. Each continuation represents a
	 * snapshot of the conversation at a point in time relative to the user.
	 */
	private LinkedList continuations = new LinkedList();

	/**
	 * The maximum number of continuations that can be created for this
	 * conversation.
	 */
	private int maxContinuations;

	public Conversation(int maxContinuations) {
		Assert.isTrue(maxContinuations > 0, "'maxContinuations' must be greater than 0");
		this.maxContinuations = maxContinuations;
	}

	public Scope getScope() {
		return scope;
	}

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
		continuations.add(continuation);
		if (continuations.size() > maxContinuations) {
			continuations.removeFirst();
		}
	}

	public int getContinuationCount() {
		return continuations.size();
	}
}